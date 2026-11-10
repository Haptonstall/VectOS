package com.lz.solver.material

import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.structural.*
import com.lz.model.units.*
import com.lz.solver.bracing.BracingLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Strong-plane column length default (item 18 / 20): with no strong-plane input the span
 * length (Ke = 1.0) is assumed, because lateral bracing does not restrain buckling in the
 * plane of bending, and the assumption is flagged. Fixture as NdsWoodCombinedChecksTest:
 * 2x10 DF-L No.2, 1.5 x 9.25 in, A = 13.875 in^2, Fc = 1350 psi, E'min = 580,000 psi, c = 0.8.
 *
 * Expected values were computed separately (Python, NDS 3.7.1 Eq. 3.7-1 and 3.7-2), not read
 * from the code under test:
 *   le1 = 120, le2 = 10:  le/d = max(120/9.25, 10/1.5) = 12.973 -> Pn = 16387.62970937548 lb
 *   le1 = 10,  le2 = 10:  le/d = max(10/9.25, 10/1.5)  = 6.667  -> Pn = 18222.781729014285 lb
 */
class NdsWoodColumnLengthDefaultTest {

    private val profile = WoodProfile(
        id = "2x10", designation = "2x10",
        nominalWidth = 2.0.inches, nominalDepth = 10.0.inches,
        dressedWidth = 1.5.inches, dressedDepth = 9.25.inches
    )
    private val material = MaterialGrade.Wood(
        id = "df-l-no2", name = "DF-L No.2",
        species = WoodSpecies.DF_L, grade = WoodGrade.NO_2,
        referenceBending = 875.0.psi, referenceShear = 180.0.psi,
        referenceCompressionParallel = 1350.0.psi, referenceCompressionPerp = 625.0.psi,
        referenceTensionParallel = 575.0.psi,
        modulusOfElasticity = 1600000.0.psi, shearModulus = 100000.0.psi, densityPcf = 35.0
    )
    private val spanId: UUID = UUID.randomUUID()

    private fun demand(axial: Double, moment: Double = 0.0, lb: Double = 10.0) = StationDemand(
        spanId = spanId, x = 60.0.inches, moment = Moment(moment), shear = Force(0.0),
        momentY = Moment(0.0), axial = Force(axial),
        lbTop = lb.inches, lbBottom = lb.inches, compressionFlange = Flange.TOP
    )

    private fun detailed(d: StationDemand) =
        NdsWoodCapacityCalculator(profile, material, adjustmentFactors = NdsAdjustmentFactors(), memberIsStrongAxis = true)
            .evaluateDetailed(d, DesignMethodology.ASD)

    private fun near(expected: Double, actual: Double, what: String) =
        assertEquals(what, expected, actual, kotlin.math.abs(expected) * 1e-6 + 1e-9)

    private fun planeTrace(r: StrengthDesignResult) =
        r.axialCheck.traces.first { it.symbolicEquation.startsWith("le/d = max") }.substitutedEquation

    @Test
    fun `span length is the strong-plane column length when none is entered and the assumption is flagged`() {
        val r = detailed(demand(axial = -500.0).copy(spanLength = 120.0.inches))
        near(16387.62970937548, r.axialCheck.capacity.pounds, "Pn with le1 = span length 120")
        val t = planeTrace(r)
        assertTrue(t, t.contains("ASSUMED"))
        assertTrue(t, t.contains("span length 120.0 in"))
        assertTrue(t, t.contains("Ke = 1.0"))
    }

    @Test
    fun `without a span length the previous bracing envelope fallback is used and also flagged`() {
        val r = detailed(demand(axial = -500.0))
        near(18222.781729014285, r.axialCheck.capacity.pounds, "Pn with le1 = envelope 10")
        val t = planeTrace(r)
        assertTrue(t, t.contains("ASSUMED") && t.contains("larger of top/bottom bracing"))
        assertFalse(t, t.contains("= span length"))
    }

    @Test
    fun `an entered strong-plane length overrides the span length and is not flagged as assumed`() {
        val r = detailed(demand(axial = -500.0).copy(spanLength = 120.0.inches, luStrongAxis = 30.0.inches))
        // le1/d1 = 30/9.25 = 3.24 < le2/d2 = 6.67 -> same as the 10/10 case
        near(18222.781729014285, r.axialCheck.capacity.pounds, "Pn, input le1 = 30")
        val t = planeTrace(r)
        assertFalse(t, t.contains("le1 (strong plane) assumed"))
        assertTrue(t, t.contains("le2 (weak plane) assumed"))
    }

    @Test
    fun `both lengths entered leaves no assumption note`() {
        val r = detailed(demand(axial = -500.0).copy(spanLength = 120.0.inches, luStrongAxis = 120.0.inches, luWeakAxis = 10.0.inches))
        near(16387.62970937548, r.axialCheck.capacity.pounds, "Pn, both entered")
        assertFalse(planeTrace(r).contains("ASSUMED"))
    }

    @Test
    fun `the combined check lists the assumed column lengths and the longer strong length raises the ratio`() {
        val withSpan = detailed(demand(axial = -500.0, moment = 5000.0).copy(spanLength = 120.0.inches)).combinedChecks.single()
        val without = detailed(demand(axial = -500.0, moment = 5000.0)).combinedChecks.single()
        assertTrue(withSpan.notes.any { it.contains("ASSUMED column lengths") && it.contains("span length 120.0 in") })
        assertTrue(without.notes.any { it.contains("ASSUMED column lengths") && it.contains("larger of top/bottom bracing") })
        assertTrue(withSpan.utilization > without.utilization)
    }

    @Test
    fun `BracingLogic stamps each station with the length of its own span`() {
        val n = List(3) { StructuralNode(id = UUID.randomUUID(), boundaryCondition = NodeBoundaryCondition()) }
        val s1 = SpanGeometry(id = UUID.randomUUID(), length = 100.0.inches, startNodeId = n[0].id, endNodeId = n[1].id)
        val s2 = SpanGeometry(id = UUID.randomUUID(), length = 40.0.inches, startNodeId = n[1].id, endNodeId = n[2].id)
        val member = StructuralMember(nodes = n, spans = listOf(s1, s2))
        fun st(span: SpanGeometry, x: Double) = StationDemand(
            spanId = span.id, x = x.inches, moment = Moment(0.0), shear = Force(0.0), momentY = Moment(0.0), axial = Force(0.0)
        )
        val out = BracingLogic.calculateDiscreteLb(listOf(st(s1, 50.0), st(s2, 120.0)), emptyList(), 140.0, member)
        assertEquals(100.0, out.first { it.spanId == s1.id }.spanLength!!.inches, 1e-9)
        assertEquals(40.0, out.first { it.spanId == s2.id }.spanLength!!.inches, 1e-9)
        // no member: the total length
        val solo = BracingLogic.calculateDiscreteLb(listOf(st(s1, 50.0)), emptyList(), 96.0)
        assertEquals(96.0, solo.single().spanLength!!.inches, 1e-9)
    }
}
