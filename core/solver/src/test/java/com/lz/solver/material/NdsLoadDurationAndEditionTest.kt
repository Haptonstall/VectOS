package com.lz.solver.material

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.structural.*
import com.lz.model.units.*
import com.lz.solver.analysis.MemberAnalysisSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Load duration (CD / lambda) per load combination, and edition awareness.
 * Expected numbers come from `nds_hand_calcs_261015.py` (T18, T19), worked from
 * NDS 2.3.2 / Appendix N and the adjustment chain — not read back from the code.
 * Fixture: 2x10 DF-L No.2 as in NdsWoodCombinedChecksTest, lb = 120 in, strong axis.
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

    private fun demand(
        cats: Set<LoadCategory>, method: DesignMethodology? = DesignMethodology.ASD,
        moment: Double = 5000.0, shear: Double = 100.0, name: String? = "combo"
    ) = StationDemand(
        spanId = spanId, x = 60.0.inches, moment = Moment(moment), shear = Force(shear),
        lbTop = 120.0.inches, lbBottom = 120.0.inches, compressionFlange = Flange.TOP,
        combinationName = name, combinationLoadCategories = cats, combinationMethodology = method
    )

    private fun calc(
        f: NdsAdjustmentFactors = NdsAdjustmentFactors(), edition: NdsEdition = NdsEdition.NDS_2018
    ) = NdsWoodCapacityCalculator(profile, material, adjustmentFactors = f, edition = edition)

    private fun near(expected: Double, actual: Double, what: String) =
        assertEquals(what, expected, actual, kotlin.math.abs(expected) * 1e-6 + 1e-9)

    private val D = LoadCategory.DEAD
    private val L = LoadCategory.LIVE
    private val S = LoadCategory.SNOW
    private val W = LoadCategory.WIND

    // ------------------------------------------------------------------ CD (ASD)

    @Test
    fun `CD follows the shortest-duration load acting in the combination`() {
        fun cd(vararg c: LoadCategory) = NdsLoadDuration.resolve(c.toSet(), DesignMethodology.ASD, NdsEdition.NDS_2018)!!
        assertEquals(0.9, cd(D).factor, 0.0)            // permanent
        assertEquals(1.0, cd(D, L).factor, 0.0)         // ten years
        assertEquals(1.15, cd(D, S).factor, 0.0)        // two months
        assertEquals(1.25, cd(D, LoadCategory.ROOF_LIVE).factor, 0.0)
        assertEquals(1.6, cd(D, W).factor, 0.0)         // ten minutes
        assertEquals(1.6, cd(D, L, S, W).factor, 0.0)   // wind is the shortest
        assertEquals(1.6, cd(D, LoadCategory.SEISMIC).factor, 0.0)
        assertEquals(LoadCategory.WIND, cd(D, L, W).governingCategory)
        assertNull(NdsLoadDuration.resolve(emptySet(), DesignMethodology.ASD, NdsEdition.NDS_2018))
    }

    @Test
    fun `ASD capacity uses the CD of each combination, including its effect on CL`() {
        val c = calc()
        // CD also multiplies F*b, which changes CL, so capacity is not simply proportional to CD.
        near(14028.108797140261, c.evaluateDetailed(demand(setOf(D)), DesignMethodology.ASD).momentCheck.capacity.lbIn, "D only, CD 0.9")
        near(14491.636560084275, c.evaluateDetailed(demand(setOf(D, L)), DesignMethodology.ASD).momentCheck.capacity.lbIn, "D + L, CD 1.0")
        near(14937.35645948547, c.evaluateDetailed(demand(setOf(D, S)), DesignMethodology.ASD).momentCheck.capacity.lbIn, "D + S, CD 1.15")
        near(15522.305391143278, c.evaluateDetailed(demand(setOf(D, W)), DesignMethodology.ASD).momentCheck.capacity.lbIn, "D + W, CD 1.6")
        // The point path (evaluate) agrees with the detailed path.
        near(15522.305391143278, c.evaluate(demand(setOf(D, W))).nominalFlexureX, "evaluate() D + W")
        // Shear: Vn = (2/3) Fv CD A
        near(1498.5, c.evaluateDetailed(demand(setOf(D)), DesignMethodology.ASD).shearCheck.capacity.pounds, "shear, CD 0.9")
        near(2664.0, c.evaluateDetailed(demand(setOf(D, W)), DesignMethodology.ASD).shearCheck.capacity.pounds, "shear, CD 1.6")
    }

    @Test
    fun `a hand-entered CD, or auto turned off, overrides the combination`() {
        val manual = calc(NdsAdjustmentFactors(cd = 0.9))
        near(14028.108797140261, manual.evaluateDetailed(demand(setOf(D, W)), DesignMethodology.ASD).momentCheck.capacity.lbIn, "manual 0.9 under wind")
        val off = calc(NdsAdjustmentFactors(loadDurationFromCombination = false))
        near(14491.636560084275, off.evaluateDetailed(demand(setOf(D)), DesignMethodology.ASD).momentCheck.capacity.lbIn, "auto off -> CD 1.0 under dead load")
        assertFalse(NdsAdjustmentFactors(cd = 0.9).usesAutoLoadDuration)
        assertTrue(NdsAdjustmentFactors().usesAutoLoadDuration)
    }

    @Test
    fun `a demand not tied to a combination falls back to the manual CD and says so`() {
        val r = calc().evaluateDetailed(demand(emptySet(), method = null, name = null), DesignMethodology.ASD)
        near(14491.636560084275, r.momentCheck.capacity.lbIn, "CD 1.0 fallback")
        assertTrue(r.codeDataNotices.any { it.dataset.contains("Load duration") && it.message.contains("isn't tied") })
    }

    @Test
    fun `only loads that are both in the combination and applied count`() {
        fun combo(vararg f: Pair<LoadCategory, Double>) = LoadCombination(
            id = "x", name = "x", methodology = DesignMethodology.ASD, type = CombinationType.STRENGTH,
            equationText = "x", factors = mapOf(*f), codeReference = "test"
        )
        val c = combo(D to 1.0, S to 0.75, W to 0.0)
        // Snow is in the combination but no snow load was entered; wind has a zero factor.
        assertEquals(setOf(D), MemberAnalysisSolver.loadCategoriesActing(c, setOf(D, W)))
        assertEquals(setOf(D, S), MemberAnalysisSolver.loadCategoriesActing(c, setOf(D, S, W)))
    }

    @Test
    fun `selected properties name the CD, where it came from, and the loads acting`() {
        val r = calc().evaluateDetailed(demand(setOf(D, W), name = "D + 0.6W"), DesignMethodology.ASD)
        val cdRow = r.selectedProperties.first { it.title == "Adjustment factors" }.rows.first { it.symbol == "CD" }
        assertEquals("1.60", cdRow.value)
        assertTrue(cdRow.description.contains("wind"))
        val code = r.selectedProperties.first { it.title == "Code and standard" }
        assertEquals("D + 0.6W", code.rows.first { it.symbol == "Combination" }.value)
        assertEquals("DL, W", code.rows.first { it.symbol == "Loads acting" }.value)
    }

    // ------------------------------------------------------------------ lambda (LRFD)

    @Test
    fun `lambda follows the combination under LRFD, and CD does not apply`() {
        fun lam(vararg c: LoadCategory) = NdsLoadDuration.resolve(c.toSet(), DesignMethodology.LRFD, NdsEdition.NDS_2018)!!.factor
        assertEquals(0.6, lam(D), 0.0)               // 1.4D
        assertEquals(0.8, lam(D, L), 0.0)            // 1.2D + 1.6L
        assertEquals(0.8, lam(D, S), 0.0)            // 1.2D + 1.6S
        assertEquals(1.0, lam(D, L, W), 0.0)         // 1.2D + 1.0W + L
        assertEquals(1.0, lam(D, LoadCategory.SEISMIC), 0.0)

        val c = calc()
        fun mn(vararg cats: LoadCategory) =
            c.evaluateDetailed(demand(cats.toSet(), DesignMethodology.LRFD), DesignMethodology.LRFD).momentCheck.capacity.lbIn
        near(7390.7346456429805, mn(D), "LRFD D only: 0.6 x 0.85 x Mn(CD=1)")
        near(9854.312860857308, mn(D, L), "LRFD D + L: 0.8 x 0.85")
        near(12317.891076071634, mn(D, W), "LRFD wind: 1.0 x 0.85")
    }

    @Test
    fun `design factors per demand carry lambda x phi for that combination`() {
        val c = calc()
        near(0.6 * 0.85, c.designFactorsFor(demand(setOf(D), DesignMethodology.LRFD), DesignMethodology.LRFD).flexure.value, "D only")
        near(0.8 * 0.75, c.designFactorsFor(demand(setOf(D, L), DesignMethodology.LRFD), DesignMethodology.LRFD).shear.value, "D + L shear")
        near(1.0 * 0.90, c.designFactorsFor(demand(setOf(D, W), DesignMethodology.LRFD), DesignMethodology.LRFD).axialCompression.value, "wind compression")
        // Unknown combination -> the 0.8 default; ASD is untouched.
        near(0.8 * 0.85, c.designFactorsFor(demand(emptySet(), null, name = null), DesignMethodology.LRFD).flexure.value, "default")
        near(1.0, c.designFactorsFor(demand(setOf(D)), DesignMethodology.ASD).flexure.value, "ASD")

        // The point-path engine picks them up per station.
        val results = com.lz.solver.capacity.CapacityEngine.evaluate(
            demands = listOf(demand(setOf(D), DesignMethodology.LRFD), demand(setOf(D, W), DesignMethodology.LRFD)),
            section = profile, methodology = DesignMethodology.LRFD,
            factors = c.designFactors(DesignMethodology.LRFD),
            factorsProvider = { c.designFactorsFor(it, DesignMethodology.LRFD) }
        ) { c.evaluate(it) }
        near(0.6 * 0.85 * 14491.636560084275, results[0].flexureCheckX.capacity, "station 1 capacity")
        near(1.0 * 0.85 * 14491.636560084275, results[1].flexureCheckX.capacity, "station 2 capacity")
    }

    // ------------------------------------------------------------------ edition awareness

    @Test
    fun `a sawn-lumber check against NDS 2018 has no missing-data notice, 2015 and 2024 do`() {
        val asd = DesignMethodology.ASD
        fun notices(e: NdsEdition) = calc(edition = e).evaluateDetailed(demand(setOf(D, L)), asd).codeDataNotices
        assertTrue(notices(NdsEdition.NDS_2018).none { it.status == CodeDataStatus.NOT_SOURCED })
        for (e in listOf(NdsEdition.NDS_2015, NdsEdition.NDS_2024)) {
            val n = notices(e).first { it.status == CodeDataStatus.NOT_SOURCED }
            assertTrue(n.dataset.contains("Reference design values"))
            assertTrue(n.message.contains("NDS ${e.year}"))
            assertTrue(n.message.contains("NDS 2018"))
        }
        assertEquals("NDS 2024", calc(edition = NdsEdition.NDS_2024).evaluateDetailed(demand(setOf(D)), asd).standardLabel)
        assertEquals("NDS 2015", calc(edition = NdsEdition.NDS_2015).evaluateDetailed(demand(setOf(D)), asd)
            .selectedProperties.first { it.title == "Code and standard" }.rows.first { it.symbol == "Standard" }.value)
    }

    @Test
    fun `glulam is flagged as mixed provenance even for 2018`() {
        val n = NdsCodeData.noticesFor(NdsEdition.NDS_2018, DesignMethodology.ASD, isGlulam = true)
        assertTrue(n.any { it.status == CodeDataStatus.UNVERIFIED && it.dataset.contains("glulam") })
        assertTrue(NdsCodeData.noticesFor(NdsEdition.NDS_2024, DesignMethodology.ASD, isGlulam = true)
            .any { it.status == CodeDataStatus.NOT_SOURCED && it.dataset.contains("glulam") })
    }

    @Test
    fun `LRFD reports that KF is not applied, ASD does not`() {
        val lrfd = NdsCodeData.noticesFor(NdsEdition.NDS_2018, DesignMethodology.LRFD, isGlulam = false)
        assertTrue(lrfd.any { it.status == CodeDataStatus.NOT_IMPLEMENTED && it.dataset.contains("KF") })
        assertTrue(NdsCodeData.noticesFor(NdsEdition.NDS_2018, DesignMethodology.ASD, isGlulam = false)
            .none { it.status == CodeDataStatus.NOT_IMPLEMENTED })
        val r = calc().evaluateDetailed(demand(setOf(D, L), DesignMethodology.LRFD), DesignMethodology.LRFD)
        assertTrue(r.codeDataNotices.any { it.status == CodeDataStatus.NOT_IMPLEMENTED })
    }
}
