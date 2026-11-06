package com.lz.solver.material

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.structural.*
import com.lz.model.units.*
import com.lz.solver.analysis.AnalysisConfig
import com.lz.solver.analysis.EffectiveLengthClassifier
import com.lz.solver.analysis.MemberAnalysisSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * NDS Table 3.3.3 effective length by the actual loading (rows as printed in the 2015, 2018 and 2024
 * editions, which are identical). Hand calcs, 2x10 (d = 9.25 in, b = 1.5 in), lu = 60 in (lu/d = 6.49) or
 * 100 in (10.81), E'min = 580,000 psi, F*b = 875 x 1.1 = 962.5 psi:
 *   fn. 1 / UDL row  le = 2.06 x 60 = 123.6      CL = 0.916942
 *   center point     le = 1.80 x 60 = 108.0      CL = 0.936617
 *   cantilever UDL   le = 1.33 x 60 =  79.8      CL = 0.962676
 *   cantilever tip   le = 1.87 x 60 = 112.2      CL = 0.931743
 */
class NdsEffectiveLengthCaseTest {

    private val d = 9.25

    private fun le(lu: Double, c: EffectiveLengthCase) = ndsBeamEffectiveLength(lu, d, c)

    @Test
    fun `specific Table 3_3_3 rows give the printed coefficients below and above lu over d of 7`() {
        assertEquals(108.0, le(60.0, EffectiveLengthCase.SINGLE_SPAN_CENTER_POINT).le, 1e-9)
        assertEquals(164.75, le(100.0, EffectiveLengthCase.SINGLE_SPAN_CENTER_POINT).le, 1e-9)      // 1.37 x 100 + 3 x 9.25
        assertEquals(79.8, le(60.0, EffectiveLengthCase.CANTILEVER_UNIFORM).le, 1e-9)
        assertEquals(117.75, le(100.0, EffectiveLengthCase.CANTILEVER_UNIFORM).le, 1e-9)            // 0.90 x 100 + 27.75
        assertEquals(112.2, le(60.0, EffectiveLengthCase.CANTILEVER_TIP_POINT).le, 1e-9)
        assertEquals(171.75, le(100.0, EffectiveLengthCase.CANTILEVER_TIP_POINT).le, 1e-9)          // 1.44 x 100 + 27.75
        assertEquals(123.6, le(60.0, EffectiveLengthCase.SINGLE_SPAN_UNIFORM).le, 1e-9)
        assertEquals(190.75, le(100.0, EffectiveLengthCase.SINGLE_SPAN_UNIFORM).le, 1e-9)           // 1.63 x 100 + 27.75
        // lu/d = 7 exactly takes the second expression: 0.90 x 64.75 + 27.75
        assertEquals(86.025, le(64.75, EffectiveLengthCase.CANTILEVER_UNIFORM).le, 1e-9)
    }

    @Test
    fun `unspecified and multi-span use footnote 1 and say it was assumed`() {
        val u = le(60.0, EffectiveLengthCase.UNSPECIFIED)
        assertEquals(123.6, u.le, 1e-9)
        assertTrue(u.tableReference, u.tableReference.contains("ASSUMED") && u.tableReference.contains("not one of the supported"))
        val m = le(60.0, EffectiveLengthCase.MULTI_SPAN)
        assertEquals(123.6, m.le, 1e-9)
        assertTrue(m.tableReference, m.tableReference.contains("ASSUMED") && m.tableReference.contains("multi-span"))
        // above lu/d = 14.3 footnote 1 keeps 1.84 lu (the printed UDL row has no such cap)
        assertEquals(368.0, le(200.0, EffectiveLengthCase.UNSPECIFIED).le, 1e-9)
        assertEquals(353.75, le(200.0, EffectiveLengthCase.SINGLE_SPAN_UNIFORM).le, 1e-9)
    }

    // ---------------- classifier ----------------

    private fun member(vararg bcs: NodeBoundaryCondition, lengths: List<Double> = listOf(100.0)): StructuralMember {
        val nodes = bcs.map { StructuralNode(id = UUID.randomUUID(), boundaryCondition = it) }
        val spans = lengths.mapIndexed { i, l ->
            SpanGeometry(id = UUID.randomUUID(), length = l.inches, startNodeId = nodes[i].id, endNodeId = nodes[i + 1].id)
        }
        return StructuralMember(nodes = nodes, spans = spans)
    }

    private fun udl(m: StructuralMember, from: Double = 0.0, to: Double = 100.0) =
        Load.UniformDistributedLoad(ForcePerLength(10.0), m.spans[0].id, from.inches, to.inches)

    private fun pt(m: StructuralMember, x: Double, dir: LoadDirection = LoadDirection.VERTICAL_DOWN) =
        Load.PointLoad(Force(1000.0), m.spans[0].id, x.inches, direction = dir)

    private fun cls(m: StructuralMember, loads: List<Load>, braces: List<NormalizedBraceState> = emptyList()) =
        EffectiveLengthClassifier.classify(m, loads, braces)[m.spans[0].id]

    private val simple get() = member(NodeBoundaryCondition.pinned(), NodeBoundaryCondition.roller())
    private val cantilever get() = member(NodeBoundaryCondition.fixed(), NodeBoundaryCondition.free())

    @Test
    fun `simple span patterns`() {
        val m = simple
        assertEquals(EffectiveLengthCase.SINGLE_SPAN_UNIFORM, cls(m, listOf(udl(m))))
        assertEquals(EffectiveLengthCase.SINGLE_SPAN_UNIFORM, cls(m, listOf(udl(m), udl(m))))
        assertEquals(EffectiveLengthCase.SINGLE_SPAN_CENTER_POINT, cls(m, listOf(pt(m, 50.0))))
        // axial loads do not change the transverse pattern
        assertEquals(EffectiveLengthCase.SINGLE_SPAN_CENTER_POINT, cls(m, listOf(pt(m, 50.0), pt(m, 50.0, LoadDirection.AXIAL_COMPRESSION))))
        // mixed or off-pattern loading is not matched
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(m, listOf(udl(m), pt(m, 50.0))))
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(m, listOf(pt(m, 40.0))))
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(m, listOf(pt(m, 50.0), pt(m, 50.0))))
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(m, listOf(udl(m, 0.0, 60.0))))
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(m, listOf(pt(m, 50.0, LoadDirection.VERTICAL_UP))))
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(m, listOf(Load.PointMoment(Moment(1000.0), m.spans[0].id, 50.0.inches))))
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(m, emptyList()))
    }

    @Test
    fun `cantilever patterns`() {
        val c = cantilever
        assertEquals(EffectiveLengthCase.CANTILEVER_UNIFORM, cls(c, listOf(udl(c))))
        assertEquals(EffectiveLengthCase.CANTILEVER_TIP_POINT, cls(c, listOf(pt(c, 100.0))))
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(c, listOf(pt(c, 50.0))))
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(c, listOf(udl(c), pt(c, 100.0))))
        // fixed root at the span end, free tip at the start
        val r = member(NodeBoundaryCondition.free(), NodeBoundaryCondition.fixed())
        assertEquals(EffectiveLengthCase.CANTILEVER_TIP_POINT, cls(r, listOf(pt(r, 0.0))))
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(r, listOf(pt(r, 100.0))))
    }

    @Test
    fun `bracing, supports and span count`() {
        val m = simple
        // an intermediate brace point or a short unbraced segment removes the "no intermediate lateral support" condition
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(m, listOf(udl(m)), listOf(NormalizedBraceState(50.0.inches))))
        assertEquals(EffectiveLengthCase.SINGLE_SPAN_UNIFORM, cls(m, listOf(udl(m)), listOf(NormalizedBraceState(0.0.inches), NormalizedBraceState(100.0.inches))))
        val braced = StructuralMember(m.nodes, listOf(m.spans[0].copy(unbracedSegments = listOf(
            UnbracedSegment(0.0.inches, 100.0.inches, lbTop = 48.0.inches, lbBottom = 100.0.inches)))))
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(braced, listOf(udl(braced))))
        // fixed-fixed is neither a simple span nor a cantilever
        val ff = member(NodeBoundaryCondition.fixed(), NodeBoundaryCondition.fixed())
        assertEquals(EffectiveLengthCase.UNSPECIFIED, cls(ff, listOf(udl(ff))))
        // more than one span
        val two = member(NodeBoundaryCondition.pinned(), NodeBoundaryCondition.roller(), NodeBoundaryCondition.roller(), lengths = listOf(100.0, 100.0))
        val r = EffectiveLengthClassifier.classify(two, listOf(udl(two)), emptyList())
        assertEquals(setOf(EffectiveLengthCase.MULTI_SPAN), r.values.toSet())
        assertEquals(2, r.size)
    }

    // ---------------- per combination, through the solver ----------------

    @Test
    fun `the case follows the loads that act in each combination`() {
        val m = simple
        val dead = udl(m).copy(category = LoadCategory.DEAD)
        val live = pt(m, 50.0).copy(category = LoadCategory.LIVE)
        fun combo(name: String, vararg f: Pair<LoadCategory, Double>) = LoadCombination(
            id = name, name = name, methodology = DesignMethodology.ASD, type = CombinationType.STRENGTH,
            equationText = name, factors = mapOf(*f), codeReference = "test"
        )
        val r = MemberAnalysisSolver.solve(
            AnalysisConfig(
                member = m,
                loadCases = listOf(LoadCase("D", "Dead", listOf(dead)), LoadCase("L", "Live", listOf(live))),
                combinations = listOf(
                    combo("D only", LoadCategory.DEAD to 1.0),
                    combo("L only", LoadCategory.LIVE to 1.0),
                    combo("D+L", LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 1.0)
                ),
                modulusOfElasticity = ElasticModulus(1_600_000.0),
                momentOfInertiaX = MomentOfInertia(100.0),
                crossSectionalArea = Area(10.0)
            )
        )
        fun caseOf(name: String) = r.combinationResults[name]!!.spanResults.flatMap { it.stationDemands }.map { it.effectiveLengthCase }.toSet()
        assertEquals(setOf(EffectiveLengthCase.SINGLE_SPAN_UNIFORM), caseOf("D only"))
        assertEquals(setOf(EffectiveLengthCase.SINGLE_SPAN_CENTER_POINT), caseOf("L only"))
        assertEquals(setOf(EffectiveLengthCase.UNSPECIFIED), caseOf("D+L"))
    }

    // ---------------- calculator: CL and the flagged le row ----------------

    private val profile = WoodProfile(
        id = "2x10", designation = "2x10", nominalWidth = 2.0.inches, nominalDepth = 10.0.inches,
        dressedWidth = 1.5.inches, dressedDepth = 9.25.inches
    )
    private val material = MaterialGrade.Wood(
        id = "df-l-no2", name = "DF-L No.2", species = WoodSpecies.DF_L, grade = WoodGrade.NO_2,
        referenceBending = 875.0.psi, referenceShear = 180.0.psi, referenceCompressionParallel = 1350.0.psi,
        referenceCompressionPerp = 625.0.psi, referenceTensionParallel = 575.0.psi,
        modulusOfElasticity = 1600000.0.psi, shearModulus = 100000.0.psi, densityPcf = 35.0
    )

    private fun result(c: EffectiveLengthCase, strongOrientation: Boolean = true) =
        NdsWoodCapacityCalculator(profile, material, adjustmentFactors = NdsAdjustmentFactors(), memberIsStrongAxis = strongOrientation)
            .evaluateDetailed(
                StationDemand(
                    spanId = UUID.randomUUID(), x = 50.0.inches, moment = Moment(-5000.0), shear = Force(0.0),
                    momentY = Moment(0.0), axial = Force(0.0), lbTop = 60.0.inches, lbBottom = 60.0.inches,
                    compressionFlange = Flange.TOP, effectiveLengthCase = c
                ), DesignMethodology.ASD
            )

    private fun row(r: StrengthDesignResult, symbol: String) =
        r.selectedProperties.flatMap { it.rows }.first { it.symbol == symbol }

    @Test
    fun `CL and the reported le follow the matched row`() {
        assertEquals("0.917", row(result(EffectiveLengthCase.UNSPECIFIED), "CL (strong axis)").value)
        assertEquals("0.937", row(result(EffectiveLengthCase.SINGLE_SPAN_CENTER_POINT), "CL (strong axis)").value)
        assertEquals("0.963", row(result(EffectiveLengthCase.CANTILEVER_UNIFORM), "CL (strong axis)").value)
        assertEquals("0.932", row(result(EffectiveLengthCase.CANTILEVER_TIP_POINT), "CL (strong axis)").value)
        val le = row(result(EffectiveLengthCase.SINGLE_SPAN_CENTER_POINT), "le (strong-axis bending)")
        assertTrue(le.value, le.value.startsWith("108.0 in"))
        assertTrue(le.description, le.description.contains("concentrated load at center") && !le.description.contains("ASSUMED"))
        val fn1 = row(result(EffectiveLengthCase.UNSPECIFIED), "le (strong-axis bending)")
        assertTrue(fn1.value, fn1.value.startsWith("123.6 in"))
        assertTrue(fn1.description, fn1.description.contains("ASSUMED"))
        assertTrue(row(result(EffectiveLengthCase.MULTI_SPAN), "le (strong-axis bending)").description.contains("multi-span"))
    }

    @Test
    fun `a member oriented about its weak axis keeps footnote 1`() {
        // the loading row describes the primary plane; here the strong axis is the secondary plane
        val r = result(EffectiveLengthCase.SINGLE_SPAN_CENTER_POINT, strongOrientation = false)
        assertFalse(row(r, "le (strong-axis bending)").description.contains("concentrated load at center"))
    }
}
