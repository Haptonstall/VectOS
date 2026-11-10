package com.lz.solver.analysis

import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.regulatory.nds.NdsShearNearSupport
import com.lz.model.structural.*
import com.lz.model.units.*
import com.lz.solver.material.NdsCodeData
import com.lz.solver.material.NdsWoodCapacityCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * NDS 3.4.3.1(a) near-support shear: uniform loads within d of a support face are ignored,
 * concentrated loads there are multiplied by x/d; only the design shear at stations within d changes.
 *
 * Hand calcs. Simple span L = 100 in, d = 10 in, no bearing length (x from the support centerline):
 *   UDL w = 10 lb/in: unreduced V(5) = 500 - 50 = 450; reduced = R' = 10 x 80 / 2 = 400
 *   P = 1000 at x = 6 (factor 6/10 = 0.6, P' = 600): R'left = 600 x 94/100 = 564, R'right = 36
 *     reduced V(3) = 564 (unreduced 940), V(8) = 564 - 600 = -36 (unreduced -60), V(95) = -36 (unreduced -60)
 *   with a 4 in bearing (face 2 in from the centerline): x = 6 - 2 = 4, factor 0.4, P' = 400:
 *     R'left = 376, R'right = 24
 *   P at x = 1 over the bearing (x < 0): ignored entirely, reduced V(3) = 0
 * Two equal spans of 100 in, w = 10 lb/in, loads within d of the three supports ignored (slope-deflection,
 * done separately): R'A = 282, R'B = 1036, R'C = 282, so V(4) = 282, V(96) = -518, V(104) = 518, V(196) = -282
 * (unreduced: V(4) = 375 - 40 = 335, ...).
 */
class ShearNearSupportTest {

    private fun beam(lengths: List<Double>, bcs: List<NodeBoundaryCondition>): StructuralMember {
        val nodes = bcs.map { StructuralNode(id = UUID.randomUUID(), boundaryCondition = it) }
        val spans = lengths.mapIndexed { i, l ->
            SpanGeometry(id = UUID.randomUUID(), length = l.inches, startNodeId = nodes[i].id, endNodeId = nodes[i + 1].id)
        }
        return StructuralMember(nodes = nodes, spans = spans)
    }

    private fun simple() = beam(listOf(100.0), listOf(NodeBoundaryCondition.pinned(), NodeBoundaryCondition.roller()))
    private fun twoSpan() = beam(listOf(100.0, 100.0),
        listOf(NodeBoundaryCondition.pinned(), NodeBoundaryCondition.roller(), NodeBoundaryCondition.roller()))

    private fun solve(m: StructuralMember, loads: List<Load>, reduction: ShearReductionInput?) =
        MemberAnalysisSolver.solve(
            AnalysisConfig(
                member = m,
                loadCases = listOf(LoadCase("DL", "Dead", loads)),
                modulusOfElasticity = ElasticModulus(1_600_000.0),
                momentOfInertiaX = MomentOfInertia(100.0),
                crossSectionalArea = Area(10.0),
                shearReduction = reduction
            )
        )

    private fun st(r: AnalysisResult, x: Double) =
        r.spanResults.flatMap { it.stationDemands }.first { Math.abs(it.x.inches - x) < 1e-6 }

    private fun udl(m: StructuralMember) = Load.UniformDistributedLoad(ForcePerLength(10.0), m.spans[0].id, 0.0.inches, 100.0.inches)
    private fun pt(m: StructuralMember, x: Double) = Load.PointLoad(Force(1000.0), m.spans[0].id, x.inches)
    private val d10 = ShearReductionInput(Length(10.0))

    private fun near(expected: Double, actual: Double, what: String) = assertEquals(what, expected, actual, 1e-6)

    @Test
    fun `off leaves the shear unchanged and unflagged`() {
        val m = simple()
        val s = st(solve(m, listOf(udl(m)), null), 5.0)
        near(450.0, Math.abs(s.shear.pounds), "V(5)")
        assertNull(s.shearReductionNote); assertNull(s.unreducedShear)
    }

    @Test
    fun `uniform load within d is ignored and only stations within d change`() {
        val m = simple()
        val r = solve(m, listOf(udl(m)), d10)
        val a = st(r, 5.0)
        near(400.0, Math.abs(a.shear.pounds), "V(5) reduced")
        near(450.0, Math.abs(a.unreducedShear!!.pounds), "V(5) full")
        assertNotNull(a.shearReductionNote)
        near(400.0, Math.abs(st(r, 10.0).shear.pounds), "V(10) at the edge of the zone")
        val out = st(r, 11.0)
        near(390.0, Math.abs(out.shear.pounds), "V(11) outside the zone")
        assertNull(out.shearReductionNote)
        near(400.0, Math.abs(st(r, 95.0).shear.pounds), "V(95) at the far support")
        near(0.0, Math.abs(st(r, 50.0).shear.pounds - 0.0) - Math.abs(st(r, 50.0).shear.pounds), "midspan untouched")
    }

    @Test
    fun `concentrated load within d is multiplied by x over d, measured from the centerline without a bearing length`() {
        val m = simple()
        val r = solve(m, listOf(pt(m, 6.0)), d10)
        near(564.0, Math.abs(st(r, 3.0).shear.pounds), "V(3)")
        near(940.0, Math.abs(st(r, 3.0).unreducedShear!!.pounds), "V(3) full")
        near(36.0, Math.abs(st(r, 8.0).shear.pounds), "V(8)")
        near(36.0, Math.abs(st(r, 95.0).shear.pounds), "V(95)")
        near(60.0, Math.abs(st(r, 50.0).shear.pounds), "V(50) outside every zone")
        assertNull(st(r, 50.0).shearReductionNote)
    }

    @Test
    fun `a bearing length moves the support face and a load over the bearing is ignored`() {
        val m = simple()
        val b4 = ShearReductionInput(Length(10.0), Length(4.0))
        val r = solve(m, listOf(pt(m, 6.0)), b4)
        near(376.0, Math.abs(st(r, 3.0).shear.pounds), "V(3), x from the face = 4")
        near(24.0, Math.abs(st(r, 95.0).shear.pounds), "V(95)")
        assertFalse(st(r, 3.0).shearReductionNote!!.contains("ASSUMED"))
        val over = solve(m, listOf(pt(m, 1.0)), b4)
        near(0.0, Math.abs(st(over, 3.0).shear.pounds), "load over the bearing is ignored")
        assertTrue(st(solve(m, listOf(pt(m, 6.0)), d10), 3.0).shearReductionNote!!.contains("ASSUMED"))
    }

    @Test
    fun `continuous spans re-solve with the near-support loads removed`() {
        val m = twoSpan()
        val load = Load.UniformDistributedLoad(ForcePerLength(10.0), m.spans[0].id, 0.0.inches, 100.0.inches)
        val load2 = Load.UniformDistributedLoad(ForcePerLength(10.0), m.spans[1].id, 0.0.inches, 100.0.inches)
        val r = solve(m, listOf(load, load2), d10)
        near(282.0, Math.abs(st(r, 4.0).shear.pounds), "V(4)")
        near(335.0, Math.abs(st(r, 4.0).unreducedShear!!.pounds), "V(4) full")
        near(518.0, Math.abs(st(r, 96.0).shear.pounds), "V(96)")
        near(518.0, Math.abs(st(r, 104.0).shear.pounds), "V(104)")
        near(282.0, Math.abs(st(r, 196.0).shear.pounds), "V(196)")
        near(125.0, Math.abs(st(r, 100.0 - 50.0).shear.pounds), "V(50) outside every zone is the full value")
    }

    @Test
    fun `the diagram keeps the true shear`() {
        val m = simple()
        val r = solve(m, listOf(udl(m)), d10)
        val d = r.spanResults.flatMap { it.shearDiagram }.first { Math.abs(it.x.inches - 5.0) < 1e-6 }
        near(450.0, Math.abs(d.value), "diagram V(5) is the actual shear")
    }

    // ---------------- wood calculator output ----------------

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

    private fun detailed(note: String?, f: NdsAdjustmentFactors) = NdsWoodCapacityCalculator(profile, material, adjustmentFactors = f)
        .evaluateDetailed(
            StationDemand(
                spanId = UUID.randomUUID(), x = 5.0.inches, moment = Moment(0.0), shear = Force(400.0),
                momentY = Moment(0.0), axial = Force(0.0),
                unreducedShear = note?.let { Force(450.0) }, shearReductionNote = note
            ), DesignMethodology.ASD
        )

    @Test
    fun `the shear check and the properties card say when the reduction was used`() {
        val on = NdsAdjustmentFactors(shearNearSupport = NdsShearNearSupport(true, null))
        val r = detailed("NOTE TEXT", on)
        val t = r.shearCheck.traces.first { it.codeReference == "NDS 3.4.3.1(a)" && it.symbolicEquation.startsWith("V design") }
        assertTrue(t.substitutedEquation, t.substitutedEquation.contains("450.0") && t.substitutedEquation.contains("400.0"))
        assertTrue(r.shearCheck.traces.any { it.substitutedEquation == "NOTE TEXT" })
        val g = r.selectedProperties.first { it.title.startsWith("Shear near supports") }
        assertTrue(g.rows.first { it.symbol == "Option" }.value.startsWith("On"))
        assertTrue(g.rows.first { it.symbol == "Bearing" }.value.contains("ASSUMED"))
        val off = detailed(null, NdsAdjustmentFactors())
        assertTrue(off.selectedProperties.first { it.title.startsWith("Shear near supports") }
            .rows.first { it.symbol == "Option" }.value.startsWith("Off"))
        assertFalse(off.shearCheck.traces.any { it.codeReference == "NDS 3.4.3.1(a)" })
    }

    @Test
    fun `a negative bearing length is blocked`() {
        fun reason(b: Double?) = NdsCodeData.blockReason(
            NdsEdition.NDS_2018, material, NdsAdjustmentFactors(shearNearSupport = NdsShearNearSupport(true, b))
        )
        assertNull(reason(null)); assertNull(reason(0.0)); assertNull(reason(4.0))
        assertNotNull(reason(-1.0)); assertNotNull(reason(Double.NaN))
    }
}
