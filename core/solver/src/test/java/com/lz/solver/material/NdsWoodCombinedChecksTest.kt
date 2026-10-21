package com.lz.solver.material

import com.lz.model.regulatory.nds.NdsAdjustmentFactors
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
 * Tests for the axis-aware bending, flat-use, E'min and NDS 3.9 combined-stress
 * work. Every expected number below was computed SEPARATELY from the NDS
 * equations (3.3-5, 3.3-6, 3.3.3.8, 3.7.1, 3.9-1 .. 3.9-4) in
 * `nds_hand_calcs_261015.py` (kept alongside the patches) — none of them were
 * read back out of the code under test.
 *
 * Fixture: 2x10 DF-L No.2, dressed 1.5 x 9.25 in, Fb = 875 / Ft = 575 /
 * Fc = 1350 / Fv = 180 psi (as NdsClCalculatorTest), Emin = 580,000 psi from
 * the table lookup. CF (Table 4A, 2x10) = 1.1 bending & tension, 1.0
 * compression; Cfu (2x10 flatwise) = 1.2. A = 13.875 in^2, Sx = 21.390625 in^3,
 * Sy = 3.46875 in^3. Effective length le = 1.63 lb + 3 d (lb/d >= 7), and the
 * column effective length is the unbraced length itself.
 */
class NdsWoodCombinedChecksTest {

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

    // Glulam 16F-V3 DF/DF, 5.125 x 12 in: Fby = 1450 psi (NDS Table 5A), Cfu(5.125 in) = 1.10.
    private val glulamProfile = WoodProfile(
        id = "gl", designation = "5.125x12",
        nominalWidth = 5.125.inches, nominalDepth = 12.0.inches,
        dressedWidth = 5.125.inches, dressedDepth = 12.0.inches
    )

    private fun glulam(grade: WoodGrade) = MaterialGrade.Wood(
        id = "gl-$grade", name = "Glulam $grade",
        species = WoodSpecies.GLULAM_DF_DF, grade = grade,
        referenceBending = 1600.0.psi, referenceShear = 265.0.psi,
        referenceCompressionParallel = 1500.0.psi, referenceCompressionPerp = 560.0.psi,
        referenceTensionParallel = 975.0.psi,
        modulusOfElasticity = 1600000.0.psi, shearModulus = 100000.0.psi, densityPcf = 31.2
    )

    private val spanId: UUID = UUID.randomUUID()

    private fun demand(
        moment: Double = 0.0, momentY: Double = 0.0, axial: Double = 0.0,
        shear: Double = 0.0, lb: Double = 0.0
    ) = StationDemand(
        spanId = spanId, x = 60.0.inches,
        moment = Moment(moment), shear = Force(shear), momentY = Moment(momentY),
        axial = Force(axial),
        lbTop = lb.inches, lbBottom = lb.inches, compressionFlange = Flange.TOP
    )

    private fun detailed(
        d: StationDemand,
        strong: Boolean = true,
        factors: NdsAdjustmentFactors = NdsAdjustmentFactors(),
        mat: MaterialGrade.Wood = material,
        prof: WoodProfile = profile,
        method: DesignMethodology = DesignMethodology.ASD
    ) = NdsWoodCapacityCalculator(prof, mat, adjustmentFactors = factors, memberIsStrongAxis = strong)
        .evaluateDetailed(d, method)

    private fun near(expected: Double, actual: Double, what: String) =
        assertEquals(what, expected, actual, kotlin.math.abs(expected) * 1e-6 + 1e-9)

    // ------------------------------------------------------------------
    // CL (NDS Eq. 3.3-6) and E'min
    // ------------------------------------------------------------------

    @Test
    fun `CL follows NDS 3_3-6 with the 1_9 and 0_95 constants`() {
        val s = computeNdsBeamStability(120.0, profile, material, NdsAdjustmentFactors(), false)
        assertNotNull(s)
        s!!
        near(223.35, s.le, "le = 1.63 lb + 3d")
        near(30.30209, s.rb, "RB")
        near(757.991033, s.fbE, "FbE")
        near(962.5, s.fStarB, "F*b = 875 x 1.1")
        near(0.703871, s.cl, "CL")
    }

    @Test
    fun `strong-axis Mn uses CL, CF and Sx`() {
        val r = detailed(demand(moment = 5000.0, lb = 120.0))
        // F'b = 875 x 1.1 x 0.703871 = 677.476 psi; Mn = F'b x Sx
        near(14491.63656, r.momentCheck.capacity.lbIn, "Mn strong axis")
        near(5000.0 / 14491.63656, r.momentCheck.utilization, "bending ratio")
    }

    @Test
    fun `E'min carries CM, Ct and Ci into FbE and FcE`() {
        val f = NdsAdjustmentFactors(cm = 0.8)
        val s = computeNdsBeamStability(120.0, profile, material, f, false)!!
        near(464000.0, s.eMinAdj, "E'min = 580000 x 0.8")
        near(606.392827, s.fbE, "FbE with adjusted E'min (bare Emin would give 757.99)")

        val r = detailed(demand(axial = -500.0, lb = 60.0), factors = f)
        val fce = r.axialCheck.traces.first { it.symbolicEquation.startsWith("FcE") }
        near(464000.0, fce.variables["E'min"]!!, "E'min in the FcE trace")
        // The trace string is rounded to whole psi; compare at that precision.
        assertEquals("FcE = 0.822 x 464000 / 40^2", 238.38, fce.result.toDouble(), 0.5)
    }

    // ------------------------------------------------------------------
    // Weak axis / flat use
    // ------------------------------------------------------------------

    @Test
    fun `weak-axis sawn bending is Fb x CF x Cfu x Sy with no CL`() {
        // F'b = 875 x 1.1 x 1.2 = 1155 psi; Mn = 1155 x 3.46875
        for (lb in listOf(0.0, 120.0)) {
            val r = detailed(demand(moment = 2000.0, lb = lb), strong = false)
            near(4006.40625, r.momentCheck.capacity.lbIn, "weak-axis Mn at lb=$lb")
        }
    }

    @Test
    fun `secondary-axis check reports the other axis capacity`() {
        val r = detailed(demand(moment = 5000.0, momentY = 1000.0, lb = 120.0), strong = true)
        val sec = r.secondaryMomentCheck
        assertNotNull(sec)
        near(4006.40625, sec!!.capacity.lbIn, "secondary (weak) capacity")
        near(1000.0, sec.demand.lbIn, "secondary demand")
        near(1000.0 / 4006.40625, sec.utilization, "secondary ratio")
        assertNull(detailed(demand(moment = 5000.0, lb = 120.0)).secondaryMomentCheck)
    }

    @Test
    fun `glulam weak axis with no sourced Fby reports zero capacity and the reason`() {
        // Precondition: if Fby for 20F-V3 is ever sourced, update this test to a combo that still lacks it.
        assertNull(
            WoodPropertyService.getReferenceProperties(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_20F_V3)!!.bendingWeakAxis
        )
        val r = detailed(
            demand(moment = 1000.0, lb = 0.0), strong = false,
            mat = glulam(WoodGrade.G_20F_V3), prof = glulamProfile
        )
        near(0.0, r.momentCheck.capacity.lbIn, "capacity")
        assertTrue(r.momentCheck.governingMode!!.contains("Fby"))
        assertEquals(Double.POSITIVE_INFINITY, r.momentCheck.utilization, 0.0)
    }

    @Test
    fun `glulam weak axis uses Fby and the width-based Cfu`() {
        val r = detailed(
            demand(moment = 1000.0, lb = 0.0), strong = false,
            mat = glulam(WoodGrade.G_16F_V3), prof = glulamProfile
        )
        // F'by = 1450 x 1.10 = 1595 psi; Sy = 12 x 5.125^2 / 6 = 52.53125
        near(83787.34375, r.momentCheck.capacity.lbIn, "glulam weak-axis Mn")
    }

    // ------------------------------------------------------------------
    // NDS 3.9.1 — bending + tension
    // ------------------------------------------------------------------

    @Test
    fun `combined bending and tension Eq 3_9-1 and 3_9-2`() {
        // T = 1000 lb, M = 12000 lb-in, lb = 120.
        // ft = 72.072, fb = 560.993; F't = 575 x 1.1 = 632.5; F*b = 962.5 (no CL); F**b = 677.476 (CL in)
        val r = detailed(demand(moment = 12000.0, axial = 1000.0, lb = 120.0))
        assertEquals(1, r.combinedChecks.size)
        val c = r.combinedChecks[0]
        assertEquals("NDS 3.9.1", c.clause)
        near(0.696798, c.equations.first { it.equation == "Eq. 3.9-1" }.value, "Eq 3.9-1")
        near(0.721681, c.equations.first { it.equation.startsWith("Eq. 3.9-2") }.value, "Eq 3.9-2")
        assertEquals("Eq. 3.9-2 (strong axis)", c.governingEquation)
        near(0.721681, c.utilization, "governing")
    }

    // ------------------------------------------------------------------
    // NDS 3.9.2 — bending + compression, biaxial bending
    // ------------------------------------------------------------------

    @Test
    fun `combined bending and compression Eq 3_9-3, uniaxial`() {
        // P = 500 lb comp, M = 10000 lb-in, lb = 60: fc = 36.036, fb1 = 467.495,
        // CP = 0.209605 (FcE = 297.975, F*c = 1350), F'c = 282.967,
        // FcE1 = 11331.33, CL = 0.916942 (le = 2.06 lu: lu/d = 6.49 < 7), F'b1 = 882.557
        val r = detailed(demand(moment = 10000.0, axial = -500.0, lb = 60.0))
        assertEquals(1, r.combinedChecks.size)
        val c = r.combinedChecks[0]
        assertEquals("NDS 3.9.2", c.clause)
        assertEquals("Bending + Axial Compression", c.label)
        near(0.547612886566562, c.equations.first { it.equation == "Eq. 3.9-3" }.value, "Eq 3.9-3")
        assertTrue("uniaxial has no Eq 3.9-4", c.equations.none { it.equation == "Eq. 3.9-4" })
    }

    @Test
    fun `combined biaxial bending with compression Eq 3_9-3 and 3_9-4`() {
        // P = 600, Mx = 15000, My = 800, lb = 60: fc = 43.243, fb1 = 701.242, fb2 = 230.631,
        // FcE2 = 297.975, FbE = 1348.445, F'b2 = 1155.
        // terms: 0.023354 + 0.800023 + 0.341662
        val r = detailed(demand(moment = 15000.0, momentY = 800.0, axial = -600.0, lb = 60.0))
        val c = r.combinedChecks.single()
        near(1.1578129568563427, c.equations.first { it.equation == "Eq. 3.9-3" }.value, "Eq 3.9-3")
        val e4 = c.equations.first { it.equation == "Eq. 3.9-4" }
        near(0.4072270126627252, e4.value, "Eq 3.9-4")
        assertTrue("Eq 3.9-4 is a strict (<) limit", e4.isStrictLimit)
        assertFalse("Eq 3.9-3 > 1 must not pass", c.equations.first { it.equation == "Eq. 3.9-3" }.passes)
        assertEquals("Eq. 3.9-3", c.governingEquation)
    }

    @Test
    fun `biaxial bending alone is checked under 3_9-2 with no compressive stress`() {
        val r = detailed(demand(moment = 15000.0, momentY = 800.0, lb = 60.0))
        val c = r.combinedChecks.single()
        assertEquals("Biaxial Bending", c.label)
        assertEquals("NDS 3.9.2", c.clause)
        near(1.0651643618932103, c.equations.first { it.equation == "Eq. 3.9-3" }.value, "Eq 3.9-3 with fc = 0")
        near(0.2621032833456911, c.equations.first { it.equation == "Eq. 3.9-4" }.value, "Eq 3.9-4 with fc = 0")
    }

    @Test
    fun `member unstable under compression gives an infinite 3_9-3 ratio, not a number`() {
        // P = 5000 lb -> fc = 360.36 psi >= FcE2 = 297.975 psi
        val r = detailed(demand(moment = 15000.0, momentY = 800.0, axial = -5000.0, lb = 60.0))
        val c = r.combinedChecks.single()
        assertEquals(Double.POSITIVE_INFINITY, c.equations.first { it.equation == "Eq. 3.9-3" }.value, 0.0)
        assertEquals(Double.POSITIVE_INFINITY, c.utilization, 0.0)
        assertTrue(c.notes.any { it.contains("FcE2") })
    }

    @Test
    fun `no combined check without bending, or with one-axis bending and no axial load`() {
        assertTrue(detailed(demand(axial = -500.0, lb = 60.0)).combinedChecks.isEmpty())
        assertTrue(detailed(demand(axial = 500.0, lb = 60.0)).combinedChecks.isEmpty())
        assertTrue(detailed(demand(moment = 5000.0, lb = 60.0)).combinedChecks.isEmpty())
    }

    @Test
    fun `LRFD combined checks state the lambda-phi convention`() {
        val r = detailed(
            demand(moment = 12000.0, axial = 1000.0, lb = 120.0),
            method = DesignMethodology.LRFD
        )
        assertTrue(r.combinedChecks.single().notes.any { it.contains("LRFD") && it.contains("KF") })
    }

    // ------------------------------------------------------------------
    // Zero capacity / slenderness limits
    // ------------------------------------------------------------------

    @Test
    fun `RB over 50 is a failure shown as an infinite ratio, not 0_00`() {
        // lb = 600: lu/d = 64.9 > 14.3 -> le = 1.84 lu = 1104, RB = 67.37 > 50 (NDS 3.3.3.7) -> capacity 0
        val s = computeNdsBeamStability(600.0, profile, material, NdsAdjustmentFactors(), false)!!
        near(67.3696271821855, s.rb, "RB")
        assertTrue(s.rbExceedsLimit)

        val r = detailed(demand(moment = 1000.0, lb = 600.0))
        near(0.0, r.momentCheck.capacity.lbIn, "capacity")
        assertEquals(Double.POSITIVE_INFINITY, r.momentCheck.utilization, 0.0)
        // ...but with no demand the ratio is 0, not NaN or infinity.
        assertEquals(0.0, detailed(demand(moment = 0.0, lb = 600.0)).momentCheck.utilization, 0.0)
    }

    @Test
    fun `shear capacity is unchanged by the axis work`() {
        // Vn = (2/3) x 180 x 13.875 = 1665 lb
        near(1665.0, detailed(demand(shear = 100.0, lb = 60.0)).shearCheck.capacity.pounds, "Vn")
    }

    // ------------------------------------------------------------------
    // Point-by-point (header / diagram) path
    // ------------------------------------------------------------------

    @Test
    fun `point path judges wood by NDS 3_9 not AISC H1-1`() {
        val calc = NdsWoodCapacityCalculator(profile, material)
        // Same demand as the 3.9.1 test: governing is Eq. 3.9-2 = 0.721681
        val d = demand(moment = 12000.0, axial = 1000.0, lb = 120.0)
        val chk = calc.pointInteractionCheck(d, DesignMethodology.ASD)
        near(0.721681, chk.ratio, "point-path combined ratio")
        assertTrue(chk.limitState.contains("3.9"))

        val results = com.lz.solver.capacity.CapacityEngine.evaluate(
            demands = listOf(d), section = profile, methodology = DesignMethodology.ASD,
            factors = calc.designFactors(DesignMethodology.ASD),
            interactionCheckProvider = { calc.pointInteractionCheck(it, DesignMethodology.ASD) }
        ) { calc.evaluate(it) }
        assertTrue(results.single().interactionCheck.limitState.contains("3.9"))
        assertFalse(results.single().interactionCheck.limitState.contains("H1-1"))
    }

    @Test
    fun `point path combined ratio is zero where no combined check applies and capped when N_G`() {
        val calc = NdsWoodCapacityCalculator(profile, material)
        assertEquals(0.0, calc.pointInteractionCheck(demand(moment = 5000.0, lb = 60.0), DesignMethodology.ASD).ratio, 0.0)
        assertEquals(0.0, calc.pointInteractionCheck(demand(axial = -500.0, lb = 60.0), DesignMethodology.ASD).ratio, 0.0)
        val unstable = demand(moment = 15000.0, momentY = 800.0, axial = -5000.0, lb = 60.0)
        assertEquals(99.0, calc.pointInteractionCheck(unstable, DesignMethodology.ASD).ratio, 0.0)
    }

    @Test
    fun `engine keeps AISC H1-1 when no provider is given`() {
        val calc = NdsWoodCapacityCalculator(profile, material)
        val r = com.lz.solver.capacity.CapacityEngine.evaluate(
            demands = listOf(demand(moment = 12000.0, axial = 1000.0, lb = 120.0)),
            section = profile, methodology = DesignMethodology.ASD,
            factors = calc.designFactors(DesignMethodology.ASD)
        ) { calc.evaluate(it) }
        assertEquals("Interaction Eq H1-1", r.single().interactionCheck.limitState)
    }

    // ------------------------------------------------------------------
    // NDS Table 3.3.3 effective length, and per-axis lengths
    // ------------------------------------------------------------------

    @Test
    fun `effective length picks the Table 3_3_3 fn 1 row from lu over d`() {
        // d = 9.25: lu/d < 7 -> 2.06 lu ; 7 <= lu/d <= 14.3 -> 1.63 lu + 3d ; > 14.3 -> 1.84 lu
        val low = ndsBeamEffectiveLength(60.0, 9.25)
        assertEquals(2.06, low.coefficient, 0.0); near(123.6, low.le, "le, lu/d < 7")
        val mid = ndsBeamEffectiveLength(120.0, 9.25)
        assertEquals(1.63, mid.coefficient, 0.0); near(223.35, mid.le, "le, 7 <= lu/d <= 14.3")
        val high = ndsBeamEffectiveLength(200.0, 9.25)
        assertEquals(1.84, high.coefficient, 0.0); near(368.0, high.le, "le, lu/d > 14.3")
        assertEquals("le = 1.63 lu + 3d", mid.formula)
        // Row boundaries: lu/d = 7 exactly is the 1.63 row; just past 14.3 is the 1.84 row.
        assertEquals(1.63, ndsBeamEffectiveLength(7.0 * 9.25, 9.25).coefficient, 0.0)
        near(133.2925, ndsBeamEffectiveLength(64.75, 9.25).le, "le at lu/d = 7")
        assertEquals(1.63, ndsBeamEffectiveLength(14.3 * 9.25, 9.25).coefficient, 0.0)
        assertEquals(1.84, ndsBeamEffectiveLength(14.31 * 9.25, 9.25).coefficient, 0.0)
    }

    @Test
    fun `CL in the lu over d greater than 14_3 row`() {
        val st = computeNdsBeamStability(200.0, profile, material, NdsAdjustmentFactors(), false)!!
        near(368.0, st.le, "le = 1.84 lu")
        near(38.89587238883952, st.rb, "RB")
        near(460.0470035252645, st.fbE, "FbE")
        near(0.4585533756531186, st.cl, "CL")
    }

    @Test
    fun `flatwise bending swaps d and b so CL is exempt, and the swap shows for a wide member`() {
        // 2x10 flatwise: d = 1.5 <= b = 9.25 -> CL = 1.0 exactly (NDS 3.3.3.1)
        val weak = computeNdsBeamStability(120.0, profile, material, NdsAdjustmentFactors(), false, aboutStrongAxis = false)!!
        near(1.5, weak.d, "d (flatwise)"); near(9.25, weak.b, "b (flatwise)")
        assertTrue(weak.exemptByShape); near(1.0, weak.cl, "CL")
        near(220.8, weak.le, "le = 1.84 lu since lu/d = 80")
        assertFalse(weak.rbExceedsLimit)
        // A member whose width exceeds its depth (9.25 wide x 1.5 deep): bending about its
        // y-y axis has d = 9.25, b = 1.5 -> the same RB as the 2x10 edgewise case, so not exempt.
        val wide = WoodProfile(
            id = "w", designation = "10x2", nominalWidth = 10.0.inches, nominalDepth = 2.0.inches,
            dressedWidth = 9.25.inches, dressedDepth = 1.5.inches
        )
        val w = computeNdsBeamStability(120.0, wide, material, NdsAdjustmentFactors(), false, aboutStrongAxis = false)!!
        near(9.25, w.d, "d"); near(1.5, w.b, "b")
        assertFalse(w.exemptByShape)
        near(223.35, w.le, "le"); near(30.30209, w.rb, "RB — same as the 2x10 edgewise case")
        assertTrue(computeNdsBeamStability(120.0, wide, material, NdsAdjustmentFactors(), false)!!.exemptByShape)
    }

    @Test
    fun `the bending traces name the Table 3_3_3 expression that was used`() {
        val r = detailed(demand(moment = 5000.0, lb = 120.0))
        val le = r.momentCheck.traces.first { it.symbolicEquation.startsWith("le = ") }
        assertEquals("le = 1.63 lu + 3d", le.symbolicEquation)
        assertTrue(le.codeReference.contains("Table 3.3.3"))
        near(223.35, le.result.toDouble(), "le")
        // Flatwise shows why CL = 1.0
        val w = detailed(demand(moment = 1000.0, lb = 120.0), strong = false)
        assertTrue(w.momentCheck.traces.any { it.symbolicEquation == "d <= b  ->  CL = 1.0" })
    }

    @Test
    fun `column buckling checks both planes with their own lengths and the larger slenderness governs`() {
        // Weak plane governs: le1/d1 = 120/9.25 = 12.97 vs le2/d2 = 30/1.5 = 20.0
        val a = detailed(demand(axial = -500.0, lb = 120.0).copy(luWeakAxis = 30.0.inches))
        near(12109.11343185832, a.axialCheck.capacity.pounds, "Pn, weak plane governs")
        assertTrue(a.axialCheck.traces.first { it.symbolicEquation.startsWith("le/d = max") }
            .substitutedEquation.contains("weak axis governs"))
        // Strong plane governs: 12.97 vs 10/1.5 = 6.67
        val b = detailed(demand(axial = -500.0, lb = 120.0).copy(luWeakAxis = 10.0.inches))
        near(16387.62970937548, b.axialCheck.capacity.pounds, "Pn, strong plane governs")
        assertTrue(b.axialCheck.traces.first { it.symbolicEquation.startsWith("le/d = max") }
            .substitutedEquation.contains("strong axis governs"))
    }

    @Test
    fun `with no per-axis input the larger edge bracing length is used for columns, the flange length for LTB`() {
        // lbTop = 30 (compression flange), lbBottom = 60: LTB uses 30; column planes use the larger, 60.
        val d = demand(moment = 5000.0, axial = -500.0).copy(lbTop = 30.0.inches, lbBottom = 60.0.inches)
        val r = detailed(d)
        near(20066.0001548043, r.momentCheck.capacity.lbIn, "Mn uses the compression-flange length (30)")
        near(3926.166737806891, r.axialCheck.capacity.pounds, "Pn uses the larger length (60)")
    }

    @Test
    fun `point path shows zero capacity against a real demand as infinite, and N-A checks stay 0`() {
        val calc = NdsWoodCapacityCalculator(profile, material)
        fun run(d: StationDemand) = com.lz.solver.capacity.CapacityEngine.evaluate(
            demands = listOf(d), section = profile, methodology = DesignMethodology.ASD,
            factors = calc.designFactors(DesignMethodology.ASD)
        ) { calc.evaluate(it) }.single()

        // lb = 600 -> RB > 50 -> bending capacity 0. Torque is "not codified for wood": N/A, not a failure.
        val r = run(demand(moment = 1000.0, lb = 600.0).copy(torque = Moment(500.0)))
        assertEquals(Double.POSITIVE_INFINITY, r.flexureCheckX.ratio, 0.0)
        assertEquals(0.0, r.torsionCheck.ratio, 0.0)
        assertEquals(Double.POSITIVE_INFINITY, r.utilizationRatio, 0.0)
        // The governing demand is carried explicitly (ratio x capacity would be NaN here).
        near(1000.0, r.governingDemand, "governing demand")
        assertFalse(r.governingDemand.isNaN())

        // No demand at zero capacity is still 0.
        assertEquals(0.0, run(demand(moment = 0.0, lb = 600.0)).flexureCheckX.ratio, 0.0)
        // Ordinary capacity: demand / capacity, unchanged.
        near(5000.0 / 14491.63656, run(demand(moment = 5000.0, lb = 120.0)).flexureCheckX.ratio, "normal ratio")
    }

    // ------------------------------------------------------------------
    // Selected properties section
    // ------------------------------------------------------------------

    private fun prop(r: StrengthDesignResult, group: String, symbol: String) =
        r.selectedProperties.first { it.title == group }.rows.first { it.symbol == symbol }.value

    @Test
    fun `selected properties report the inputs the checks used`() {
        val r = detailed(demand(moment = 5000.0, axial = -500.0, lb = 120.0))
        assertEquals("875 psi", prop(r, "Reference design values", "Fb"))
        assertEquals("580000 psi", prop(r, "Reference design values", "Emin"))
        assertEquals("1.500 in", prop(r, "Section", "b"))
        assertEquals("9.250 in", prop(r, "Section", "d"))
        assertEquals("13.875 in\u00b2", prop(r, "Section", "A"))
        assertEquals("21.391 in\u00b3", prop(r, "Section", "Sx"))
        assertEquals("3.469 in\u00b3", prop(r, "Section", "Sy"))
        assertEquals("1.100", prop(r, "Adjustment factors", "CF (bending)"))
        assertEquals("1.200", prop(r, "Adjustment factors", "Cfu"))
        // CL differs by axis: 0.703871 about x-x (d = 9.25, b = 1.5), exactly 1 about y-y (d = 1.5 <= b = 9.25)
        assertEquals("0.704", prop(r, "Adjustment factors", "CL (strong axis)"))
        assertEquals("1.000", prop(r, "Adjustment factors", "CL (weak axis)"))
        assertTrue(prop(r, "Unbraced and effective lengths", "le (strong-axis bending)").startsWith("223.4 in"))
        assertTrue(r.selectedProperties.first { it.title == "Unbraced and effective lengths" }.rows
            .first { it.symbol == "le (strong-axis bending)" }.description.contains("1.63 lu + 3d"))
        // No per-axis input -> the default is disclosed
        assertTrue(r.selectedProperties.first { it.title == "Unbraced and effective lengths" }.rows
            .first { it.symbol == "le2 (column, weak plane)" }.description.contains("default"))
        val withInput = detailed(demand(axial = -500.0, lb = 120.0).copy(luWeakAxis = 30.0.inches))
        assertFalse(withInput.selectedProperties.first { it.title == "Unbraced and effective lengths" }.rows
            .first { it.symbol == "le2 (column, weak plane)" }.description.contains("default"))
    }

    @Test
    fun `glulam properties show Fby, or say it is not sourced`() {
        val ok = detailed(demand(moment = 1000.0), mat = glulam(WoodGrade.G_16F_V3), prof = glulamProfile)
        assertEquals("1450 psi", prop(ok, "Reference design values", "Fby"))
        val none = detailed(demand(moment = 1000.0), mat = glulam(WoodGrade.G_20F_V3), prof = glulamProfile)
        assertEquals("not sourced for this combination", prop(none, "Reference design values", "Fby"))
    }
}
