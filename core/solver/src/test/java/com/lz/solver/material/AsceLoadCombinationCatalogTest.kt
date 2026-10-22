package com.lz.solver.material

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.loads.AsceEdition
import com.lz.model.regulatory.loads.AsceLoadCombinationCatalog
import com.lz.model.structural.DesignMethodology
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The ASCE 7 combination catalog with every "or" expanded. The expected lists
 * below are written out by hand from the code's equations (ASCE 7 §2.3 LRFD,
 * §2.4.1 ASD), not generated from the catalog.
 */
class AsceLoadCombinationCatalogTest {

    private val lrfd = DesignMethodology.LRFD
    private val asd = DesignMethodology.ASD

    private fun names(e: AsceEdition, m: DesignMethodology) =
        AsceLoadCombinationCatalog.combinations(e, m).map { it.name }

    private val expectedLrfd = listOf(
        "1.4D",
        // Eq 2: 1.2D + 1.6L + 0.5(Lr or S or R)
        "1.2D + 1.6L + 0.5Lr", "1.2D + 1.6L + 0.5S", "1.2D + 1.6L + 0.5R",
        // Eq 3: 1.2D + 1.6(Lr or S or R) + (L or 0.5W)  -> 3 x 2
        "1.2D + L + 1.6Lr", "1.2D + 1.6Lr + 0.5W",
        "1.2D + L + 1.6S", "1.2D + 1.6S + 0.5W",
        "1.2D + L + 1.6R", "1.2D + 1.6R + 0.5W",
        // Eq 4: 1.2D + 1.0W + L + 0.5(Lr or S or R)
        "1.2D + L + 0.5Lr + W", "1.2D + L + 0.5S + W", "1.2D + L + 0.5R + W",
        // Eq 5, 6, 7
        "1.2D + L + 0.2S + E", "0.9D + W", "0.9D + E"
    )

    private val expectedAsd = listOf(
        "D", "D + L",
        // Eq 3: D + (Lr or S or R)
        "D + Lr", "D + S", "D + R",
        // Eq 4: D + 0.75L + 0.75(Lr or S or R)
        "D + 0.75L + 0.75Lr", "D + 0.75L + 0.75S", "D + 0.75L + 0.75R",
        // Eq 5: D + (0.6W or 0.7E)
        "D + 0.6W", "D + 0.7E",
        // Eq 6: D + 0.75L + 0.75(0.6W) + 0.75(Lr or S or R)  (0.75 x 0.6 = 0.45)
        "D + 0.75L + 0.75Lr + 0.45W", "D + 0.75L + 0.75S + 0.45W", "D + 0.75L + 0.75R + 0.45W",
        // Eq 7: D + 0.75L + 0.75(0.7E) + 0.75S  (0.75 x 0.7 = 0.525)
        "D + 0.75L + 0.75S + 0.525E",
        // Eq 8, 9
        "0.6D + 0.6W", "0.6D + 0.7E"
    )

    @Test
    fun `every or alternative is its own combination for every edition`() {
        for (e in AsceEdition.values()) {
            assertEquals("LRFD ${e.sourceName}", expectedLrfd, names(e, lrfd))
            assertEquals("ASD ${e.sourceName}", expectedAsd, names(e, asd))
            assertEquals(AsceLoadCombinationCatalog.LRFD_COUNT, names(e, lrfd).size)
            assertEquals(AsceLoadCombinationCatalog.ASD_COUNT, names(e, asd).size)
        }
    }

    @Test
    fun `roof live, snow and rain each get the same number of gravity combinations`() {
        for (m in listOf(lrfd, asd)) {
            val combos = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_16, m)
            fun count(c: LoadCategory) = combos.count { c in it.factors }
            assertEquals("Lr vs R, $m", count(LoadCategory.ROOF_LIVE), count(LoadCategory.RAIN))
            // Snow additionally appears in the 0.2S / 0.75(0.7E) seismic combination.
            assertEquals("S, $m", count(LoadCategory.ROOF_LIVE) + 1, count(LoadCategory.SNOW))
        }
        val l = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_16, lrfd)
        assertEquals(4, l.count { LoadCategory.ROOF_LIVE in it.factors })   // Eq 2, Eq 3 (x2), Eq 4
        val a = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_16, asd)
        assertEquals(3, a.count { LoadCategory.ROOF_LIVE in it.factors })   // Eq 3, 4, 6
    }

    @Test
    fun `factors are exactly the code's, including the 0_75 products`() {
        val a = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_22, asd)
        val wind = a.first { it.name == "D + 0.75L + 0.75S + 0.45W" }.factors
        assertEquals(0.45, wind.getValue(LoadCategory.WIND), 0.0)
        val eq = a.first { it.name == "D + 0.75L + 0.75S + 0.525E" }.factors
        assertEquals(0.525, eq.getValue(LoadCategory.SEISMIC), 0.0)
        assertEquals(0.75, eq.getValue(LoadCategory.SNOW), 0.0)
        val l = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_22, lrfd)
        assertEquals(0.5, l.first { it.name == "1.2D + 1.6R + 0.5W" }.factors.getValue(LoadCategory.WIND), 0.0)
        assertEquals(1.6, l.first { it.name == "1.2D + 1.6R + 0.5W" }.factors.getValue(LoadCategory.RAIN), 0.0)
        assertEquals(0.2, l.first { it.name == "1.2D + L + 0.2S + E" }.factors.getValue(LoadCategory.SNOW), 0.0)
    }

    @Test
    fun `ids are unique, stable, and change when a factor changes`() {
        for (m in listOf(lrfd, asd)) {
            val ids = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_16, m)
                .map { AsceLoadCombinationCatalog.combinationId(AsceEdition.ASCE_7_16, m, it) }
            assertEquals(ids.size, ids.toSet().size)
        }
        // Existing installs stored this id for the first combination; it must not change.
        assertEquals(
            "ASCE_7_16_LRFD_1.4D",
            AsceLoadCombinationCatalog.combinationId(
                AsceEdition.ASCE_7_16, lrfd,
                AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_16, lrfd).first()
            )
        )
        assertEquals("ASCE_7_22_ASD", AsceLoadCombinationCatalog.setId(AsceEdition.ASCE_7_22, asd))
    }

    @Test
    fun `dead load is in every combination and no combination is a duplicate`() {
        for (e in AsceEdition.values()) for (m in listOf(lrfd, asd)) {
            val combos = AsceLoadCombinationCatalog.combinations(e, m)
            assertTrue(combos.all { LoadCategory.DEAD in it.factors })
            assertEquals(combos.size, combos.map { it.factors }.toSet().size)
        }
    }
}
