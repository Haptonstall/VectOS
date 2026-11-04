package com.lz.solver.material

import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.regulatory.nds.NdsNetSection
import com.lz.model.structural.*
import com.lz.model.units.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Net-section inputs (NDS 3.1.2.1, 3.8.1, 3.9.1) for tension and tension + bending.
 * Fixture: 2x10 DF-L No.2, 1.5 x 9.25 in, A = 13.875 in^2, Sx = 21.390625 in^3, Ft = 575 psi,
 * Fb = 875 psi, CF = 1.1 (bending and tension), CD = CM = Ct = 1.0, no lateral-torsional
 * reduction (lb = 0, CL = 1.0). Expected values were computed separately (Python):
 *   F't = 575 x 1.1 = 632.5 psi,  F*b = F**b = 875 x 1.1 = 962.5 psi
 *   Pn = F't x An:      An/Ag = 1.0 -> 8775.9375 lb;   0.8 -> 7020.75 lb
 *   T = 2000 lb, M = 5000 lb-in:
 *     gross:         ft = 144.144, fb = 233.747, Eq. 3.9-1 = 0.470750177, Eq. 3.9-2 = 0.093094147
 *     An/Ag 0.8, Sn/S 0.6: ft = 180.180, fb = 389.579, 3.9-1 = 0.689627012, 3.9-2 = 0.217556974
 *     An/Ag 0.8 only:      3.9-1 = 0.527724147
 *     Sn/S 0.6 only:       3.9-1 = 0.632653042
 */
class NdsNetSectionTest {

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

    private fun demand(axial: Double, moment: Double = 0.0) = StationDemand(
        spanId = spanId, x = 60.0.inches, moment = Moment(moment), shear = Force(0.0),
        momentY = Moment(0.0), axial = Force(axial),
        lbTop = 0.0.inches, lbBottom = 0.0.inches, compressionFlange = Flange.TOP
    )

    private fun detailed(d: StationDemand, a: Double = 1.0, s: Double = 1.0) =
        NdsWoodCapacityCalculator(
            profile, material,
            adjustmentFactors = NdsAdjustmentFactors(netSection = NdsNetSection(a, s)),
            memberIsStrongAxis = true
        ).evaluateDetailed(d, DesignMethodology.ASD)

    private fun near(expected: Double, actual: Double, what: String) =
        assertEquals(what, expected, actual, kotlin.math.abs(expected) * 1e-6 + 1e-9)

    private fun eq(r: StrengthDesignResult, name: String) =
        r.combinedChecks.single().equations.first { it.equation == name }.value

    private fun prop(r: StrengthDesignResult, group: String, symbol: String) =
        r.selectedProperties.first { it.title == group }.rows.first { it.symbol == symbol }

    // ---------------- tension capacity (NDS 3.8.1) ----------------

    @Test
    fun `tension capacity uses the net area`() {
        near(8775.9375, detailed(demand(axial = 2000.0)).axialCheck.capacity.pounds, "gross")
        near(7020.75, detailed(demand(axial = 2000.0), a = 0.8).axialCheck.capacity.pounds, "An/Ag = 0.8")
        // Sn/S alone does not change tension capacity
        near(8775.9375, detailed(demand(axial = 2000.0), s = 0.6).axialCheck.capacity.pounds, "Sn/S only")
    }

    // ---------------- combined tension + bending (NDS 3.9.1) ----------------

    @Test
    fun `gross section reproduces the unreduced Eq 3_9-1 and 3_9-2`() {
        val r = detailed(demand(axial = 2000.0, moment = 5000.0))
        near(0.47075017697814403, eq(r, "Eq. 3.9-1"), "Eq. 3.9-1")
        near(0.09309414714820119, eq(r, "Eq. 3.9-2 (strong axis)"), "Eq. 3.9-2")
    }

    @Test
    fun `net area and net modulus both enter the combined check`() {
        val r = detailed(demand(axial = 2000.0, moment = 5000.0), a = 0.8, s = 0.6)
        near(0.6896270116011596, eq(r, "Eq. 3.9-1"), "Eq. 3.9-1")
        near(0.21755697431373108, eq(r, "Eq. 3.9-2 (strong axis)"), "Eq. 3.9-2")
        near(0.5277241469955923, eq(detailed(demand(axial = 2000.0, moment = 5000.0), a = 0.8), "Eq. 3.9-1"), "An only")
        near(0.6326530415837113, eq(detailed(demand(axial = 2000.0, moment = 5000.0), s = 0.6), "Eq. 3.9-1"), "Sn only")
    }

    // ---------------- per-station value used by the beam plot ----------------

    @Test
    fun `the per-station interaction value behind the beam plot includes the net section`() {
        fun ratio(a: Double, s: Double) = NdsWoodCapacityCalculator(
            profile, material,
            adjustmentFactors = NdsAdjustmentFactors(netSection = NdsNetSection(a, s)),
            memberIsStrongAxis = true
        ).pointInteractionCheck(demand(axial = 2000.0, moment = 5000.0), DesignMethodology.ASD).ratio
        near(0.47075017697814403, ratio(1.0, 1.0), "gross")
        near(0.6896270116011596, ratio(0.8, 0.6), "An/Ag 0.8, Sn/S 0.6")
    }

    // ---------------- scope: not applied elsewhere ----------------

    @Test
    fun `compression and bending-only capacities are unchanged by net-section ratios`() {
        val c0 = detailed(demand(axial = -500.0))
        val c1 = detailed(demand(axial = -500.0), a = 0.5, s = 0.5)
        near(c0.axialCheck.capacity.pounds, c1.axialCheck.capacity.pounds, "compression Pn")
        val b0 = detailed(demand(axial = 0.0, moment = 5000.0))
        val b1 = detailed(demand(axial = 0.0, moment = 5000.0), a = 0.5, s = 0.5)
        near(b0.momentCheck.capacity.lbIn, b1.momentCheck.capacity.lbIn, "bending Mn")
    }

    // ---------------- flags ----------------

    @Test
    fun `the gross-section assumption and the entered net section are both stated`() {
        val gross = detailed(demand(axial = 2000.0, moment = 5000.0)).combinedChecks.single()
        assertTrue(gross.notes.any { it.contains("ASSUMED: gross section") })
        val net = detailed(demand(axial = 2000.0, moment = 5000.0), a = 0.8, s = 0.6).combinedChecks.single()
        assertTrue(net.notes.any { it.contains("Net section (user input)") && it.contains("0.800") && it.contains("0.600") })
        assertTrue(net.notes.any { it.contains("Eccentricity") })
        assertFalse(net.notes.any { it.contains("ASSUMED: gross section") })

        val g = detailed(demand(axial = 2000.0))
        assertTrue(prop(g, "Section", "An/Ag").description.contains("ASSUMED default"))
        val n = detailed(demand(axial = 2000.0), a = 0.8, s = 0.6)
        assertFalse(prop(n, "Section", "An/Ag").description.contains("ASSUMED"))
        assertEquals("0.800", prop(n, "Section", "An/Ag").value)
        assertEquals("0.600", prop(n, "Section", "Sn/S").value)
        val t = n.axialCheck.traces.first { it.symbolicEquation.startsWith("Pn = F't x An") }.substitutedEquation
        assertTrue(t, t.contains("An/Ag = 0.800") && !t.contains("ASSUMED"))
        assertTrue(g.axialCheck.traces.first { it.symbolicEquation.startsWith("Pn = F't x An") }
            .substitutedEquation.contains("ASSUMED"))
    }

    // ---------------- blocks ----------------

    @Test
    fun `ratios outside 0 to 1 are blocked, defaults are not`() {
        fun reason(a: Double, s: Double) =
            NdsCodeData.blockReason(NdsEdition.NDS_2018, material, NdsAdjustmentFactors(netSection = NdsNetSection(a, s)))
        assertNull(reason(1.0, 1.0))
        assertNull(reason(0.5, 0.25))
        listOf(1.2 to 1.0, 1.0 to 1.0001, 0.0 to 1.0, 1.0 to -0.1, Double.NaN to 1.0, 1.0 to Double.POSITIVE_INFINITY)
            .forEach { (a, s) -> assertNotNull("a=$a s=$s", reason(a, s)) }
        // the calculator itself refuses an invalid ratio
        var blocked = false
        try {
            NdsWoodCapacityCalculator(profile, material, adjustmentFactors = NdsAdjustmentFactors(netSection = NdsNetSection(1.5, 1.0)))
                .evaluateDetailed(demand(axial = 2000.0), DesignMethodology.ASD)
        } catch (e: CodeDataBlockedException) { blocked = true }
        assertTrue(blocked)
    }
}
