package com.lz.solver.material

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.loads.AsceEdition
import com.lz.model.regulatory.loads.AsceLoadCombinationCatalog
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.regulatory.loads.SeismicCombinationAdjuster
import com.lz.model.regulatory.loads.SeismicCombinationInputs
import com.lz.model.structural.DesignMethodology
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Optional SDS, rho and 0.5L inputs applied to the catalogued ASCE combinations. Expected factors are hand
 * calculations from the code's expressions:
 *   Ev = 0.2 SDS D, Eh = rho QE
 *   LRFD seismic:  (1.2 + 0.2 SDS) D + rho QE + L + 0.2S   and   (0.9 - 0.2 SDS) D + rho QE
 *   ASD (7-16/22): (1.0 + 0.14 SDS) D + 0.7 rho QE;  (1.0 + 0.105 SDS) D + 0.525 rho QE + 0.75L + S-term;
 *                  (0.6 - 0.14 SDS) D + 0.7 rho QE
 */
class SeismicCombinationAdjusterTest {

    private fun combos(e: AsceEdition, m: DesignMethodology) =
        AsceLoadCombinationCatalog.combinations(e, m).map {
            LoadCombination(
                id = AsceLoadCombinationCatalog.combinationId(e, m, it), name = it.name, methodology = m,
                type = CombinationType.STRENGTH, equationText = "", factors = it.factors, codeReference = it.reference
            )
        }

    private fun adjust(e: AsceEdition, m: DesignMethodology, i: SeismicCombinationInputs) =
        SeismicCombinationAdjuster.adjust(AsceLoadCombinationCatalog.setId(e, m), combos(e, m), i)

    private fun f(r: SeismicCombinationAdjuster.Result, name: String) = r.combinations.first { it.name == name }.factors

    private fun near(expected: Double, actual: Double?, what: String) = assertEquals(what, expected, actual!!, 1e-9)

    @Test
    fun `LRFD SDS and rho scale the dead and seismic factors, nothing else`() {
        val r = adjust(AsceEdition.ASCE_7_16, DesignMethodology.LRFD, SeismicCombinationInputs(sds = 0.4, rho = 1.3))
        val a = f(r, "1.2D + L + 0.2S + E")
        near(1.28, a[LoadCategory.DEAD], "D"); near(1.3, a[LoadCategory.SEISMIC], "E")
        near(1.0, a[LoadCategory.LIVE], "L"); near(0.2, a[LoadCategory.SNOW], "S")
        val b = f(r, "0.9D + E")
        near(0.82, b[LoadCategory.DEAD], "0.9 - 0.2 x 0.4"); near(1.3, b[LoadCategory.SEISMIC], "E")
        // a non-seismic combination is untouched, and keeps the catalog's text
        val nonSeismic = r.combinations.first { it.name == "1.2D + 1.6L + 0.5S" }
        near(1.2, nonSeismic.factors[LoadCategory.DEAD], "D unchanged")
        assertEquals("", nonSeismic.equationText)
        assertTrue(r.combinations.first { it.name == "0.9D + E" }.equationText, r.combinations.first { it.name == "0.9D + E" }.equationText.startsWith("0.82DL + 1.3E"))
    }

    @Test
    fun `ASD 7-22 coefficients 0_14 and 0_105 and rho on 0_7 and 0_525`() {
        val r = adjust(AsceEdition.ASCE_7_22, DesignMethodology.ASD, SeismicCombinationInputs(sds = 0.5, rho = 1.3))
        val e8 = f(r, "D + 0.7E")
        near(1.07, e8[LoadCategory.DEAD], "1 + 0.14 x 0.5"); near(0.91, e8[LoadCategory.SEISMIC], "0.7 x 1.3")
        val e9 = f(r, "D + 0.75L + 0.1S + 0.525E")
        near(1.0525, e9[LoadCategory.DEAD], "1 + 0.105 x 0.5"); near(0.6825, e9[LoadCategory.SEISMIC], "0.525 x 1.3")
        near(0.75, e9[LoadCategory.LIVE], "L"); near(0.1, e9[LoadCategory.SNOW], "S")
        val e10 = f(r, "0.6D + 0.7E")
        near(0.53, e10[LoadCategory.DEAD], "0.6 - 0.14 x 0.5"); near(0.91, e10[LoadCategory.SEISMIC], "E")
    }

    @Test
    fun `7-10 puts the Ev coefficient in its own seismic combinations`() {
        val r = adjust(AsceEdition.ASCE_7_10, DesignMethodology.ASD, SeismicCombinationInputs(sds = 0.4))
        near(1.056, f(r, "D + 0.7E")[LoadCategory.DEAD], "1 + 0.14 x 0.4")
        near(1.042, f(r, "D + 0.75L + 0.75S + 0.525E")[LoadCategory.DEAD], "1 + 0.105 x 0.4")
        near(0.544, f(r, "0.6D + 0.7E")[LoadCategory.DEAD], "0.6 - 0.14 x 0.4")
        near(0.7, f(r, "D + 0.7E")[LoadCategory.SEISMIC], "rho = 1 leaves E alone")
    }

    @Test
    fun `the 0_5L exception applies only where the code allows it and only to strength design`() {
        val r = adjust(AsceEdition.ASCE_7_16, DesignMethodology.LRFD, SeismicCombinationInputs(halfLive = true))
        val halved = r.combinations.filter { it.factors[LoadCategory.LIVE] == 0.5 }.map { it.name }.toSet()
        assertEquals(
            setOf("1.2D + L + 1.6Lr", "1.2D + L + 1.6S", "1.2D + L + 1.6R", "1.2D + L + 0.5Lr + W",
                "1.2D + L + 0.5S + W", "1.2D + L + 0.5R + W", "1.2D + L + 0.2S + E"), halved
        )
        near(1.6, f(r, "1.2D + 1.6L + 0.5S")[LoadCategory.LIVE], "Eq. 2 keeps 1.6L")
        val asd = adjust(AsceEdition.ASCE_7_16, DesignMethodology.ASD, SeismicCombinationInputs(halfLive = true))
        assertTrue(asd.combinations.none { it.factors[LoadCategory.LIVE] == 0.5 })
        assertTrue(asd.notes.none { it.contains("0.5L") })
    }

    @Test
    fun `with no inputs the combinations are unchanged and every assumption is stated`() {
        val e = AsceEdition.ASCE_7_16
        val r = adjust(e, DesignMethodology.LRFD, SeismicCombinationInputs())
        assertEquals(combos(e, DesignMethodology.LRFD), r.combinations)
        assertTrue(r.notes.toString(), r.notes.any { it.startsWith("ASSUMED: SDS not entered") })
        assertTrue(r.notes.toString(), r.notes.any { it.startsWith("ASSUMED: \u03C1 not entered") })
        assertTrue(r.notes.toString(), r.notes.any { it.contains("0.5L exception is not claimed") })
        val entered = adjust(e, DesignMethodology.LRFD, SeismicCombinationInputs(sds = 0.4, rho = 1.0, halfLive = true))
        assertTrue(entered.notes.toString(), entered.notes.none { it.startsWith("ASSUMED") })
        assertTrue(entered.notes.toString(), entered.notes.any { it.contains("0.5L exception claimed") && it.contains("7 combinations") })
    }

    @Test
    fun `source remarks and edition notes are carried`() {
        val r10 = adjust(AsceEdition.ASCE_7_10, DesignMethodology.ASD, SeismicCombinationInputs())
        assertTrue(r10.notes.toString(), r10.notes.any { it.contains("0.10 SDS") && it.contains("0.105") })
        val r22 = adjust(AsceEdition.ASCE_7_22, DesignMethodology.LRFD, SeismicCombinationInputs())
        assertTrue(r22.notes.toString(), r22.notes.any { it.contains("tornado") })
        assertTrue(adjust(AsceEdition.ASCE_7_16, DesignMethodology.LRFD, SeismicCombinationInputs()).notes.none { it.contains("tornado") })
    }

    @Test
    fun `invalid inputs are rejected and unknown sets are left alone`() {
        assertNotNull(SeismicCombinationInputs(rho = 1.2).blockReason())
        assertNotNull(SeismicCombinationInputs(sds = -0.1).blockReason())
        assertNotNull(SeismicCombinationInputs(sds = Double.NaN).blockReason())
        assertNull(SeismicCombinationInputs(sds = 0.0, rho = 1.3).blockReason())
        var threw = false
        try { adjust(AsceEdition.ASCE_7_16, DesignMethodology.LRFD, SeismicCombinationInputs(rho = 1.2)) }
        catch (e: IllegalArgumentException) { threw = true }
        assertTrue(threw)
        val other = SeismicCombinationAdjuster.adjust("SOME_OTHER_SET", combos(AsceEdition.ASCE_7_16, DesignMethodology.LRFD),
            SeismicCombinationInputs(sds = 0.4, rho = 1.3, halfLive = true))
        assertEquals(combos(AsceEdition.ASCE_7_16, DesignMethodology.LRFD), other.combinations)
        assertTrue(other.notes.isEmpty())
    }
}
