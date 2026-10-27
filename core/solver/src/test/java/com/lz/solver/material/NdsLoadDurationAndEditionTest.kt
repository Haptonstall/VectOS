package com.lz.solver.material

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.regulatory.nds.NdsVersionRegistry
import com.lz.model.regulatory.nds.TemperatureRange
import com.lz.model.structural.*
import com.lz.model.units.*
import com.lz.solver.analysis.MemberAnalysisSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Load duration (ASD CD, LRFD lambda), LRFD format conversion (KF, phi) and
 * edition awareness. Expected numbers come from `nds_hand_calcs_261015.py`
 * (T18, T20, T21), worked from NDS 2018 Tables 2.3.2 / 2.3.5 / 2.3.6 and the
 * adjustment chain — not read back from the code under test.
 * Fixture: 2x10 DF-L No.2 as in NdsWoodCombinedChecksTest, strong axis.
 */
class NdsLoadDurationAndEditionTest {

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

    private val D = LoadCategory.DEAD
    private val L = LoadCategory.LIVE
    private val S = LoadCategory.SNOW
    private val W = LoadCategory.WIND

    private fun demand(
        loads: Map<LoadCategory, Double>, method: DesignMethodology? = DesignMethodology.ASD,
        moment: Double = 5000.0, shear: Double = 100.0, axial: Double = 0.0, lb: Double = 120.0,
        name: String? = "combo"
    ) = StationDemand(
        spanId = spanId, x = 60.0.inches, moment = Moment(moment), shear = Force(shear), axial = Force(axial),
        lbTop = lb.inches, lbBottom = lb.inches, compressionFlange = Flange.TOP,
        combinationName = name, combinationLoadFactors = loads, combinationMethodology = method
    )

    private fun calc(
        f: NdsAdjustmentFactors = NdsAdjustmentFactors(), edition: NdsEdition = NdsEdition.NDS_2018,
        default: DesignMethodology = DesignMethodology.ASD
    ) = NdsWoodCapacityCalculator(profile, material, adjustmentFactors = f, edition = edition, defaultMethodology = default)

    private fun near(expected: Double, actual: Double, what: String) =
        assertEquals(what, expected, actual, kotlin.math.abs(expected) * 1e-6 + 1e-9)

    // the combinations used below, with their real ASCE 7 factors
    private val dOnly = mapOf(D to 1.0)                           // D
    private val dPlusL = mapOf(D to 1.0, L to 1.0)                // D + L
    private val dPlusS = mapOf(D to 1.0, S to 1.0)                // D + S
    private val dPlusW = mapOf(D to 1.0, W to 0.6)                // D + 0.6W

    // ------------------------------------------------------------------ CD (ASD)

    @Test
    fun `CD follows the shortest-duration load acting in the combination`() {
        fun cd(vararg c: LoadCategory) =
            NdsLoadDuration.resolve(c.associateWith { 1.0 }, DesignMethodology.ASD, NdsEdition.NDS_2018)!!
        assertEquals(0.9, cd(D).factor, 0.0)            // permanent
        assertEquals(1.0, cd(D, L).factor, 0.0)         // ten years
        assertEquals(1.15, cd(D, S).factor, 0.0)        // two months
        assertEquals(1.25, cd(D, LoadCategory.ROOF_LIVE).factor, 0.0)
        assertEquals(1.6, cd(D, W).factor, 0.0)         // ten minutes
        assertEquals(1.6, cd(D, L, S, W).factor, 0.0)   // wind is the shortest
        assertEquals(1.6, cd(D, LoadCategory.SEISMIC).factor, 0.0)
        assertEquals(LoadCategory.WIND, cd(D, L, W).governingCategory)
        assertNull(NdsLoadDuration.resolve(emptyMap(), DesignMethodology.ASD, NdsEdition.NDS_2018))
    }

    @Test
    fun `ASD capacity uses the CD of each combination, including its effect on CL`() {
        val c = calc()
        // CD also multiplies F*b, which changes CL, so capacity is not simply proportional to CD.
        near(14028.108797140261, c.evaluateDetailed(demand(dOnly), DesignMethodology.ASD).momentCheck.capacity.lbIn, "D only, CD 0.9")
        near(14491.636560084275, c.evaluateDetailed(demand(dPlusL), DesignMethodology.ASD).momentCheck.capacity.lbIn, "D + L, CD 1.0")
        near(14937.35645948547, c.evaluateDetailed(demand(dPlusS), DesignMethodology.ASD).momentCheck.capacity.lbIn, "D + S, CD 1.15")
        near(15522.305391143278, c.evaluateDetailed(demand(dPlusW), DesignMethodology.ASD).momentCheck.capacity.lbIn, "D + 0.6W, CD 1.6")
        near(15522.305391143278, c.evaluate(demand(dPlusW)).nominalFlexureX, "evaluate() D + 0.6W")
        near(1498.5, c.evaluateDetailed(demand(dOnly), DesignMethodology.ASD).shearCheck.capacity.pounds, "shear, CD 0.9")
        near(2664.0, c.evaluateDetailed(demand(dPlusW), DesignMethodology.ASD).shearCheck.capacity.pounds, "shear, CD 1.6")
    }

    @Test
    fun `a hand-entered CD, or auto turned off, overrides the combination`() {
        near(14028.108797140261, calc(NdsAdjustmentFactors(cd = 0.9)).evaluateDetailed(demand(dPlusW), DesignMethodology.ASD)
            .momentCheck.capacity.lbIn, "manual 0.9 under wind")
        near(14491.636560084275, calc(NdsAdjustmentFactors(loadDurationFromCombination = false))
            .evaluateDetailed(demand(dOnly), DesignMethodology.ASD).momentCheck.capacity.lbIn, "auto off -> CD 1.0 under dead load")
        assertFalse(NdsAdjustmentFactors(cd = 0.9).usesAutoLoadDuration)
        assertTrue(NdsAdjustmentFactors().usesAutoLoadDuration)
    }

    @Test
    fun `a demand not tied to a combination falls back to the manual CD and says so`() {
        val r = calc().evaluateDetailed(demand(emptyMap(), method = null, name = null), DesignMethodology.ASD)
        near(14491.636560084275, r.momentCheck.capacity.lbIn, "CD 1.0 fallback")
        assertTrue(r.codeDataNotices.any { it.dataset.contains("Load duration") && it.message.contains("isn't tied") })
    }

    @Test
    fun `only loads that are both in the combination and applied count, with their factors`() {
        val combo = LoadCombination(
            id = "x", name = "x", methodology = DesignMethodology.ASD, type = CombinationType.STRENGTH,
            equationText = "x", factors = mapOf(D to 1.0, S to 0.75, W to 0.0), codeReference = "test"
        )
        // Snow is in the combination but no snow load was entered; wind has a zero factor.
        assertEquals(mapOf(D to 1.0), MemberAnalysisSolver.loadFactorsActing(combo, setOf(D, W)))
        assertEquals(mapOf(D to 1.0, S to 0.75), MemberAnalysisSolver.loadFactorsActing(combo, setOf(D, S, W)))
    }

    @Test
    fun `selected properties name the CD, where it came from, and the loads acting`() {
        val r = calc().evaluateDetailed(demand(dPlusW, name = "D + 0.6W"), DesignMethodology.ASD)
        val cdRow = r.selectedProperties.first { it.title == "Adjustment factors" }.rows.first { it.symbol == "CD" }
        assertEquals("1.60", cdRow.value)
        assertTrue(cdRow.description.contains("wind"))
        val code = r.selectedProperties.first { it.title == "Code and standard" }
        assertEquals("D + 0.6W", code.rows.first { it.symbol == "Combination" }.value)
        assertEquals("DL, W", code.rows.first { it.symbol == "Loads acting" }.value)
    }

    // ------------------------------------------------------------------ lambda (LRFD), Table N3

    @Test
    fun `lambda follows Table N3 by the combination's factors`() {
        fun lam(vararg f: Pair<LoadCategory, Double>) =
            NdsLoadDuration.resolve(mapOf(*f), DesignMethodology.LRFD, NdsEdition.NDS_2018)!!.factor
        assertEquals(0.6, lam(D to 1.4), 0.0)                                       // 1.4D
        assertEquals(0.8, lam(D to 1.2, L to 1.6), 0.0)                              // 1.2D + 1.6L (+ 0.5 roof)
        assertEquals(0.8, lam(D to 1.2, L to 1.6, S to 0.5), 0.0)
        assertEquals(0.8, lam(D to 1.2, S to 1.6, L to 1.0), 0.0)                    // 1.2D + 1.6S + L
        // 1.2D + 1.6S + 0.5W: wind is present but at 0.5, so Table N3 gives 0.8 (not 1.0)
        assertEquals(0.8, lam(D to 1.2, S to 1.6, W to 0.5), 0.0)
        assertEquals(1.0, lam(D to 1.2, W to 1.0, L to 1.0, S to 0.5), 0.0)          // 1.2D + 1.0W + L + 0.5S
        assertEquals(1.0, lam(D to 1.2, LoadCategory.SEISMIC to 1.0, L to 1.0, S to 0.2), 0.0)
        assertEquals(1.0, lam(D to 0.9, W to 1.0), 0.0)                              // 0.9D + 1.0W
        assertEquals(1.0, lam(D to 0.9, LoadCategory.SEISMIC to 1.0), 0.0)
    }

    @Test
    fun `LRFD puts KF x phi x lambda inside the design values, before CL and CP`() {
        val c = calc()
        fun r(loads: Map<LoadCategory, Double>, moment: Double = 5000.0, axial: Double = 0.0, lb: Double = 120.0) =
            c.evaluateDetailed(demand(loads, DesignMethodology.LRFD, moment = moment, axial = axial, lb = lb), DesignMethodology.LRFD)
        val d14 = mapOf(D to 1.4)                       // 1.4D            lambda 0.6
        val l16 = mapOf(D to 1.2, L to 1.6)             // 1.2D + 1.6L     lambda 0.8
        val w10 = mapOf(D to 0.9, W to 1.0)             // 0.9D + 1.0W     lambda 1.0
        // Bending: F*b = Fb x KF(2.54) x phi(0.85) x lambda x CF ; E'min = Emin x 1.76 x 0.85 ; then CL ; Mn = F*b CL Sx
        near(20683.286125837058, r(d14).momentCheck.capacity.lbIn, "Mn, 1.4D")
        near(22361.700708587065, r(l16).momentCheck.capacity.lbIn, "Mn, 1.2D + 1.6L")
        near(23019.681453128185, r(w10).momentCheck.capacity.lbIn, "Mn, 0.9D + 1.0W")
        // Shear: Vn = 2/3 Fv KF(2.88) phi(0.75) lambda A
        near(2157.84, r(d14, moment = 0.0).shearCheck.capacity.pounds, "Vn, 1.4D")
        near(3596.4000000000005, r(w10, moment = 0.0).shearCheck.capacity.pounds, "Vn, 0.9D + 1.0W")
        // Compression: F*c = Fc KF(2.40) phi(0.90) lambda ; FcE uses the LRFD E'min ; CP ; Pn = F*c CP A  (lb = 60)
        near(5818.254784916252, r(d14, moment = 0.0, axial = -500.0, lb = 60.0).axialCheck.capacity.pounds, "Pn, 1.4D")
        near(5920.038100017735, r(l16, moment = 0.0, axial = -500.0, lb = 60.0).axialCheck.capacity.pounds, "Pn, 1.2D + 1.6L")
        near(5977.802765150828, r(w10, moment = 0.0, axial = -500.0, lb = 60.0).axialCheck.capacity.pounds, "Pn, 0.9D + 1.0W")
        // Tension: F't = Ft KF(2.70) phi(0.80) lambda CF A
        near(11373.615000000002, r(d14, moment = 0.0, axial = 500.0).axialCheck.capacity.pounds, "Tn, 1.4D")
        near(18956.025, r(w10, moment = 0.0, axial = 500.0).axialCheck.capacity.pounds, "Tn, 0.9D + 1.0W")
    }

    @Test
    fun `LRFD nominal capacities from evaluate() match the detailed path`() {
        val c = calc(default = DesignMethodology.LRFD)
        val w10 = mapOf(D to 0.9, W to 1.0)
        near(23019.681453128185, c.evaluate(demand(w10, DesignMethodology.LRFD)).nominalFlexureX, "evaluate() LRFD")
        // A demand with no combination takes lambda = 0.8 and the calculator's default methodology.
        near(22361.700708587065, c.evaluate(demand(emptyMap(), method = null, name = null)).nominalFlexureX, "evaluate() default lambda 0.8")
        // ASD default: the same legacy demand is NOT given KF x phi.
        near(14491.636560084275, calc().evaluate(demand(emptyMap(), method = null, name = null)).nominalFlexureX, "evaluate() ASD default")
    }

    @Test
    fun `LRFD combined bending and tension uses the factored values directly`() {
        val w = mapOf(D to 1.2, W to 1.0, L to 1.0, S to 0.5)    // 1.2D + 1.0W + L + 0.5S, lambda 1.0
        val r = calc().evaluateDetailed(
            demand(w, DesignMethodology.LRFD, moment = 12000.0, axial = 1000.0), DesignMethodology.LRFD
        )
        val c = r.combinedChecks.single()
        near(0.32271676653033665, c.equations.first { it.equation == "Eq. 3.9-1" }.value, "Eq 3.9-1 (LRFD)")
        near(0.45432137515144166, c.equations.first { it.equation.startsWith("Eq. 3.9-2") }.value, "Eq 3.9-2 (LRFD)")
        assertTrue(c.notes.any { it.contains("KF x phi x lambda") })
    }

    @Test
    fun `design factors are 1 for both methodologies because the nominals are already factored`() {
        val c = calc()
        for (m in DesignMethodology.values()) {
            val f = c.designFactors(m)
            assertEquals(1.0, f.flexure.value, 0.0); assertEquals(1.0, f.shear.value, 0.0)
            assertEquals(1.0, f.axialTension.value, 0.0); assertEquals(1.0, f.axialCompression.value, 0.0)
        }
        // The point-path engine therefore reports the nominal as the design capacity.
        val w10 = mapOf(D to 0.9, W to 1.0)
        val res = com.lz.solver.capacity.CapacityEngine.evaluate(
            demands = listOf(demand(w10, DesignMethodology.LRFD)), section = profile,
            methodology = DesignMethodology.LRFD, factors = c.designFactors(DesignMethodology.LRFD)
        ) { c.evaluate(it) }
        near(23019.681453128185, res.single().flexureCheckX.capacity, "engine design capacity")
    }

    @Test
    fun `the LRFD result states the factors that were applied`() {
        val r = calc().evaluateDetailed(
            demand(mapOf(D to 1.2, L to 1.6), DesignMethodology.LRFD), DesignMethodology.LRFD
        )
        val row = r.selectedProperties.first { it.title == "Adjustment factors" }.rows.first { it.symbol == "\u03bb" }
        assertEquals("0.80", row.value)
        assertTrue(r.momentCheck.traces.any { it.symbolicEquation.contains("KF\u03c6\u03bb") })
    }

    // ------------------------------------------------------------------ edition awareness

    @Test
    fun `a sawn-lumber check against NDS 2018 or 2015 has no missing-data notice, 2024 does`() {
        val asd = DesignMethodology.ASD
        fun notices(e: NdsEdition) = calc(edition = e).evaluateDetailed(demand(dPlusL), asd).codeDataNotices
        assertTrue(notices(NdsEdition.NDS_2018).none { it.status == CodeDataStatus.NOT_SOURCED })
        assertTrue(notices(NdsEdition.NDS_2015).none { it.status == CodeDataStatus.NOT_SOURCED })
        for (e in listOf(NdsEdition.NDS_2024)) {
            val n = notices(e).first { it.status == CodeDataStatus.NOT_SOURCED }
            assertTrue(n.dataset.contains("Reference design values"))
            assertTrue(n.message.contains("NDS ${e.year}"))
            assertTrue(n.message.contains("blocked"))
        }
        assertEquals("NDS 2024", calc(edition = NdsEdition.NDS_2024).evaluateDetailed(demand(dOnly), asd).standardLabel)
        assertEquals("NDS 2015", calc(edition = NdsEdition.NDS_2015).evaluateDetailed(demand(dOnly), asd)
            .selectedProperties.first { it.title == "Code and standard" }.rows.first { it.symbol == "Standard" }.value)
    }

    @Test
    fun `Chapter 2 factors are checked for NDS 2018 and 2015 but not 2024`() {
        fun asd(e: NdsEdition) = NdsCodeData.noticesFor(e, DesignMethodology.ASD, isGlulam = false)
        fun lrfd(e: NdsEdition) = NdsCodeData.noticesFor(e, DesignMethodology.LRFD, isGlulam = false)
        assertTrue(asd(NdsEdition.NDS_2018).none { it.dataset.startsWith("Load duration factor CD") })
        assertTrue(asd(NdsEdition.NDS_2024).any { it.dataset.startsWith("Load duration factor CD") })
        assertTrue(lrfd(NdsEdition.NDS_2018).none { it.dataset.startsWith("Format conversion") })
        assertTrue(asd(NdsEdition.NDS_2015).none { it.dataset.startsWith("Load duration factor CD") })
        assertTrue(lrfd(NdsEdition.NDS_2015).none { it.dataset.startsWith("Format conversion") })
        assertTrue(lrfd(NdsEdition.NDS_2024).any { it.dataset.startsWith("Format conversion") })
        // Lambda (Appendix N) is unchecked for every edition; the duration mapping assumptions are always disclosed.
        for (e in NdsEdition.values()) {
            assertTrue(lrfd(e).any { it.dataset.contains("\u03bb") && it.status == CodeDataStatus.UNVERIFIED })
            assertTrue(asd(e).any { it.dataset.startsWith("Load type to duration") })
            assertTrue((asd(e) + lrfd(e)).none { it.status == CodeDataStatus.NOT_IMPLEMENTED })
        }
    }

    @Test
    fun `Chapter 3 stability provisions are checked for NDS 2015 only`() {
        fun has(e: NdsEdition) = NdsCodeData.noticesFor(e, DesignMethodology.ASD, isGlulam = false)
            .any { it.dataset.startsWith("Stability and combined-stress") }
        assertTrue(!has(NdsEdition.NDS_2015))
        assertTrue(has(NdsEdition.NDS_2018))
        assertTrue(has(NdsEdition.NDS_2024))
    }

    @Test
    fun `glulam is flagged as mixed provenance even for 2018`() {
        val n = NdsCodeData.noticesFor(NdsEdition.NDS_2018, DesignMethodology.ASD, isGlulam = true)
        assertTrue(n.any { it.status == CodeDataStatus.UNVERIFIED && it.dataset.contains("glulam") })
        assertTrue(NdsCodeData.noticesFor(NdsEdition.NDS_2024, DesignMethodology.ASD, isGlulam = true)
            .any { it.status == CodeDataStatus.NOT_SOURCED && it.dataset.contains("glulam") })
    }

    // ------------------------------------------------------------------ registry values, NDS 2018 text

    @Test
    fun `registry KF and phi match NDS 2018 Tables 2_3_5 and 2_3_6`() {
        val c = NdsVersionRegistry.getConstantsFor(NdsEdition.NDS_2018)
        assertEquals(2.54, c.kfBending, 0.0); assertEquals(2.70, c.kfTension, 0.0); assertEquals(2.88, c.kfShear, 0.0)
        assertEquals(2.40, c.kfCompParallel, 0.0); assertEquals(1.67, c.kfCompPerp, 0.0); assertEquals(1.76, c.kfModulusMin, 0.0)
        assertEquals(0.85, c.phiBending, 0.0); assertEquals(0.80, c.phiTension, 0.0); assertEquals(0.75, c.phiShear, 0.0)
        assertEquals(0.90, c.phiCompression, 0.0); assertEquals(0.85, c.phiStability, 0.0)
        // KF x phi is 2.16 for the strength properties (a useful consistency check on the table).
        assertEquals(2.16, c.kfBending * c.phiBending, 0.005); assertEquals(2.16, c.kfTension * c.phiTension, 0.005)
        assertEquals(2.16, c.kfShear * c.phiShear, 0.005); assertEquals(2.16, c.kfCompParallel * c.phiCompression, 0.005)
    }

    @Test
    fun `registry temperature factors match NDS 2018 Table 2_3_3`() {
        fun ct(r: TemperatureRange, wet: Boolean) = NdsVersionRegistry.getTemperatureFactors(r, wet)
        for (wet in listOf(false, true)) {
            val a = ct(TemperatureRange.T_100_OR_LESS, wet)
            assertEquals(listOf(1.0, 1.0, 1.0, 1.0), listOf(a.ctTension, a.ctBending, a.ctCompPerp, a.ctModulus))
            // Ft, E, Emin: 0.9 and 0.9 whether wet or dry
            assertEquals(0.9, ct(TemperatureRange.T_101_TO_125, wet).ctTension, 0.0)
            assertEquals(0.9, ct(TemperatureRange.T_126_TO_150, wet).ctModulus, 0.0)
        }
        // Fb, Fv, Fc, Fc-perp: dry 0.8 / 0.7, wet 0.7 / 0.5
        assertEquals(0.8, ct(TemperatureRange.T_101_TO_125, false).ctBending, 0.0)
        assertEquals(0.8, ct(TemperatureRange.T_101_TO_125, false).ctCompPerp, 0.0)
        assertEquals(0.7, ct(TemperatureRange.T_126_TO_150, false).ctBending, 0.0)
        assertEquals(0.7, ct(TemperatureRange.T_101_TO_125, true).ctBending, 0.0)
        assertEquals(0.5, ct(TemperatureRange.T_126_TO_150, true).ctCompPerp, 0.0)
    }
}
