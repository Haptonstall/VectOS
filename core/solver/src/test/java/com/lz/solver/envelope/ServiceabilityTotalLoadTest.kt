package com.lz.solver.envelope

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.regulatory.codes.ServiceabilityCriterion
import com.lz.model.regulatory.codes.ServiceabilityLimitType
import com.lz.model.regulatory.loads.AsceEdition
import com.lz.model.regulatory.loads.AsceLoadCombinationCatalog
import com.lz.model.regulatory.loads.ServiceabilityCombinations
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.Load
import com.lz.model.structural.LoadCase
import com.lz.model.structural.NodeBoundaryCondition
import com.lz.model.structural.SpanGeometry
import com.lz.model.structural.StructuralMember
import com.lz.model.structural.StructuralNode
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.units.Area
import com.lz.model.units.ElasticModulus
import com.lz.model.units.ForcePerLength
import com.lz.model.units.MomentOfInertia
import com.lz.model.units.inInches
import com.lz.model.units.inches
import com.lz.solver.analysis.AnalysisConfig
import com.lz.solver.analysis.MemberAnalysisSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * "Total Load" deflection uses the unfactored service combinations (not the factored strength combination).
 * Simple span L = 100 in, E = 1,000,000 psi, I = 100 in^4, uniform loads in lb/in: delta = 5 w L^4 / (384 E I)
 * = 0.0130208333 in per lb/in. Hand calcs: D = 10 -> 0.130208333; L = 20 -> 0.260416667; S = 15 -> 0.1953125.
 *   D + L                = 0.390625
 *   D + 0.75L (+ 0.75S)  = smaller than D + L
 *   strength 1.2D + 1.6L = 0.572916667 (what the old check reported)
 *   snow only: 7-16 D + S = 0.325520833; 7-22 D + 0.7S = 0.130208333 + 0.13671875 = 0.266927083
 */
class ServiceabilityTotalLoadTest {

    private val perLbIn = 5.0 * 1e8 / (384.0 * 1_000_000.0 * 100.0)   // 0.0130208333

    private fun member(): StructuralMember {
        val nodes = listOf(NodeBoundaryCondition.pinned(), NodeBoundaryCondition.roller())
            .map { StructuralNode(id = UUID.randomUUID(), boundaryCondition = it) }
        val span = SpanGeometry(id = UUID.randomUUID(), length = 100.0.inches, startNodeId = nodes[0].id, endNodeId = nodes[1].id)
        return StructuralMember(nodes = nodes, spans = listOf(span))
    }

    private fun solve(m: StructuralMember, loads: Map<LoadCategory, Double>): com.lz.solver.analysis.AnalysisResult {
        val cases = loads.map { (cat, w) ->
            LoadCase(cat.name, cat.label, listOf<Load>(
                Load.UniformDistributedLoad(ForcePerLength(w), m.spans[0].id, 0.0.inches, 100.0.inches, category = cat)))
        }
        // one strength combination so the analysis has combination results and category results
        val strength = LoadCombination(
            id = "s", name = "1.2D + 1.6L", methodology = DesignMethodology.LRFD, type = CombinationType.STRENGTH,
            equationText = "", factors = mapOf(LoadCategory.DEAD to 1.2, LoadCategory.LIVE to 1.6), codeReference = "test"
        )
        return MemberAnalysisSolver.solve(
            AnalysisConfig(
                member = m, loadCases = cases, combinations = listOf(strength),
                modulusOfElasticity = ElasticModulus(1_000_000.0), momentOfInertiaX = MomentOfInertia(100.0),
                crossSectionalArea = Area(10.0)
            )
        )
    }

    private val total = ServiceabilityCriterion(
        limitType = ServiceabilityLimitType.TOTAL_LOAD_DEFLECTION, name = "Total", loadCategory = null,
        spanDenominator = 240.0, description = "Total load"
    )
    private val live = ServiceabilityCriterion(
        limitType = ServiceabilityLimitType.LIVE_LOAD_DEFLECTION, name = "Live", loadCategory = LoadCategory.LIVE,
        spanDenominator = 360.0, description = "Live load"
    )
    private val code = BuildingCode(id = "t", shortName = "t", longName = "t", serviceabilityCriteria = listOf(total, live))

    private fun evaluate(m: StructuralMember, r: com.lz.solver.analysis.AnalysisResult, setId: String?) =
        ServiceabilityEvaluationService.evaluate(m, r, code, serviceCombinations = ServiceabilityCombinations.forCombinationSet(setId))

    private fun near(expected: Double, actual: Double, what: String) = assertEquals(what, expected, actual, 1e-6)

    @Test
    fun `total deflection is D plus L unfactored, not the factored strength combination`() {
        val m = member()
        val r = solve(m, mapOf(LoadCategory.DEAD to 10.0, LoadCategory.LIVE to 20.0))
        val res = evaluate(m, r, AsceLoadCombinationCatalog.setId(AsceEdition.ASCE_7_16, DesignMethodology.LRFD))
        val t = res.first { it.criterion.name == "Total" }
        near(0.390625, Math.abs(t.actualDeflection.inInches), "D + L")
        assertTrue(Math.abs(t.actualDeflection.inInches) < 0.572916667 - 0.1)
        assertTrue(t.basis!!, t.basis!!.contains("Governing service combination: D + L") && t.basis!!.contains("ASCE 7-16"))
        assertTrue(t.basis!!, t.basis!!.contains("creep not applied"))
        // the live-only check stays the unfactored live deflection and carries no basis
        val l = res.first { it.criterion.name == "Live" }
        near(0.260416667, Math.abs(l.actualDeflection.inInches), "L alone")
        assertNull(l.basis)
    }

    @Test
    fun `the service combinations follow the edition - snow is 1_0S in 7-16 and 0_7S in 7-22`() {
        val m = member()
        val r = solve(m, mapOf(LoadCategory.DEAD to 10.0, LoadCategory.SNOW to 15.0))
        near(0.325520833, Math.abs(evaluate(m, r, "ASCE_7_16_ASD").first { it.criterion.name == "Total" }.actualDeflection.inInches), "7-16 D + S")
        val t22 = evaluate(m, r, "ASCE_7_22_LRFD").first { it.criterion.name == "Total" }
        near(0.266927083, Math.abs(t22.actualDeflection.inInches), "7-22 D + 0.7S")
        assertTrue(t22.basis!!, t22.basis!!.contains("D + 0.7S"))
    }

    @Test
    fun `the 0_75 combination governs when it is larger and wind or seismic are never in the total`() {
        val m = member()
        // L and S together: D + 0.75L + 0.75S = 0.130208 + 0.75 x 0.260417 + 0.75 x 0.1953125 = 0.471680; D + L = 0.390625
        val r = solve(m, mapOf(LoadCategory.DEAD to 10.0, LoadCategory.LIVE to 20.0, LoadCategory.SNOW to 15.0, LoadCategory.WIND to 100.0))
        val t = evaluate(m, r, "ASCE_7_16_LRFD").first { it.criterion.name == "Total" }
        near(0.130208333 + 0.75 * 0.260416667 + 0.75 * 0.1953125, Math.abs(t.actualDeflection.inInches), "D + 0.75L + 0.75S")
        assertTrue(t.basis!!, t.basis!!.contains("D + 0.75L + 0.75S"))
        val combos = ServiceabilityCombinations.forCombinationSet("ASCE_7_16_LRFD").combinations
        assertEquals(8, combos.size)
        assertTrue(combos.none { LoadCategory.WIND in it.factors || LoadCategory.SEISMIC in it.factors })
    }

    @Test
    fun `without a recognised ASCE set a generic unfactored list is used and flagged`() {
        val g = ServiceabilityCombinations.forCombinationSet("SOME_OTHER_SET")
        assertEquals(listOf("D", "D + LL", "D + RLL", "D + S", "D + R").size, g.combinations.size)
        assertTrue(g.basis, g.basis.startsWith("ASSUMED"))
        val m = member()
        val r = solve(m, mapOf(LoadCategory.DEAD to 10.0, LoadCategory.LIVE to 20.0))
        near(0.390625, Math.abs(evaluate(m, r, null).first { it.criterion.name == "Total" }.actualDeflection.inInches), "generic D + L")
    }
}
