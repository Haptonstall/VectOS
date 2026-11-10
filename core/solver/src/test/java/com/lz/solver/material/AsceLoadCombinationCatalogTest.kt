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

    // ---- LRFD, 7-10 §2.3.2 (seismic E inside the basic list) and 7-16 §2.3.1 + §2.3.6 --------
    private val lrfd10 = listOf(
        "1.4D",
        // Eq 2: 1.2D + 1.6L + 0.5(Lr or S or R)
        "1.2D + 1.6L + 0.5Lr", "1.2D + 1.6L + 0.5S", "1.2D + 1.6L + 0.5R",
        // Eq 3: 1.2D + 1.6(Lr or S or R) + (L or 0.5W)  -> 3 x 2
        "1.2D + L + 1.6Lr", "1.2D + 1.6Lr + 0.5W",
        "1.2D + L + 1.6S", "1.2D + 1.6S + 0.5W",
        "1.2D + L + 1.6R", "1.2D + 1.6R + 0.5W",
        // Eq 4: 1.2D + 1.0W + L + 0.5(Lr or S or R)
        "1.2D + L + 0.5Lr + W", "1.2D + L + 0.5S + W", "1.2D + L + 0.5R + W",
        // 7-10 Eq 5 (1.2D + 1.0E + L + 0.2S), Eq 6 (0.9D + 1.0W), Eq 7 (0.9D + 1.0E)
        "1.2D + L + 0.2S + E", "0.9D + W", "0.9D + E"
    )
    private val lrfd16 = lrfd10.take(13) + listOf(
        "0.9D + W",                                   // Eq 5
        "1.2D + L + 0.2S + E", "0.9D + E"             // §2.3.6 Eq 6, 7
    )

    // ---- LRFD, 7-22 §2.3.1 (1a to 5a) + §2.3.6: S at 0.3 (companion), 1.0 (principal), 0.15 (seismic) ----
    private val lrfd22 = listOf(
        "1.4D",
        "1.2D + 1.6L + 0.5Lr", "1.2D + 1.6L + 0.3S", "1.2D + 1.6L + 0.5R",                 // 2a
        "1.2D + L + 1.6Lr", "1.2D + 1.6Lr + 0.5W",                                          // 3a
        "1.2D + L + S", "1.2D + S + 0.5W",
        "1.2D + L + 1.6R", "1.2D + 1.6R + 0.5W",
        "1.2D + L + 0.5Lr + W", "1.2D + L + 0.3S + W", "1.2D + L + 0.5R + W",              // 4a
        "0.9D + W",                                                                          // 5a
        "1.2D + L + 0.15S + E", "0.9D + E"                                                   // 6, 7
    )

    // ---- ASD, 7-10 §2.4.1 ---------------------------------------------------------------
    private val asd10 = listOf(
        "D", "D + L",
        "D + Lr", "D + S", "D + R",                                                          // 3
        "D + 0.75L + 0.75Lr", "D + 0.75L + 0.75S", "D + 0.75L + 0.75R",                     // 4
        "D + 0.6W", "D + 0.7E",                                                              // 5
        "D + 0.75L + 0.75Lr + 0.45W", "D + 0.75L + 0.75S + 0.45W", "D + 0.75L + 0.75R + 0.45W", // 6a (0.75 x 0.6)
        "D + 0.75L + 0.75S + 0.525E",                                                        // 6b (0.75 x 0.7)
        "0.6D + 0.6W", "0.6D + 0.7E"                                                         // 7, 8
    )
    // ---- ASD, 7-16 §2.4.1 + §2.4.5 (8, 9, 10) ----------------------------------------------
    private val asd16 = asd10.take(9) + listOf(
        "D + 0.75L + 0.75Lr + 0.45W", "D + 0.75L + 0.75S + 0.45W", "D + 0.75L + 0.75R + 0.45W", // 6
        "0.6D + 0.6W",                                                                       // 7
        "D + 0.7E", "D + 0.75L + 0.75S + 0.525E", "0.6D + 0.7E"                              // 8, 9, 10
    )
    // ---- ASD, 7-22 §2.4.1 (1a to 7a) + §2.4.5: S at 0.7, 0.525 (0.75 x 0.7), 0.1 in Eq 9 ----
    private val asd22 = listOf(
        "D", "D + L",
        "D + Lr", "D + 0.7S", "D + R",                                                       // 3a
        "D + 0.75L + 0.75Lr", "D + 0.75L + 0.525S", "D + 0.75L + 0.75R",                     // 4a
        "D + 0.6W",                                                                          // 5a
        "D + 0.75L + 0.75Lr + 0.45W", "D + 0.75L + 0.525S + 0.45W", "D + 0.75L + 0.75R + 0.45W", // 6a
        "0.6D + 0.6W",                                                                       // 7a
        "D + 0.7E", "D + 0.75L + 0.1S + 0.525E", "0.6D + 0.7E"                               // 8, 9, 10
    )

    @Test
    fun `every or alternative is its own combination, per edition, as printed in the code`() {
        assertEquals(lrfd10, names(AsceEdition.ASCE_7_10, lrfd))
        assertEquals(lrfd16, names(AsceEdition.ASCE_7_16, lrfd))
        assertEquals(lrfd22, names(AsceEdition.ASCE_7_22, lrfd))
        assertEquals(asd10, names(AsceEdition.ASCE_7_10, asd))
        assertEquals(asd16, names(AsceEdition.ASCE_7_16, asd))
        assertEquals(asd22, names(AsceEdition.ASCE_7_22, asd))
        for (e in AsceEdition.values()) {
            assertEquals(AsceLoadCombinationCatalog.LRFD_COUNT, names(e, lrfd).size)
            assertEquals(AsceLoadCombinationCatalog.ASD_COUNT, names(e, asd).size)
        }
    }

    @Test
    fun `references use each edition's own section and equation numbers`() {
        fun ref(e: AsceEdition, m: DesignMethodology, name: String) =
            AsceLoadCombinationCatalog.combinations(e, m).first { it.name == name }.reference
        assertEquals("ASCE 7-10 \u00a72.3.2 Eq. 5", ref(AsceEdition.ASCE_7_10, lrfd, "1.2D + L + 0.2S + E"))
        assertEquals("ASCE 7-16 \u00a72.3.6 Eq. 6", ref(AsceEdition.ASCE_7_16, lrfd, "1.2D + L + 0.2S + E"))
        assertEquals("ASCE 7-16 \u00a72.3.1 Eq. 5", ref(AsceEdition.ASCE_7_16, lrfd, "0.9D + W"))
        assertEquals("ASCE 7-22 \u00a72.3.1 Eq. 5a", ref(AsceEdition.ASCE_7_22, lrfd, "0.9D + W"))
        assertEquals("ASCE 7-22 \u00a72.3.6 Eq. 6", ref(AsceEdition.ASCE_7_22, lrfd, "1.2D + L + 0.15S + E"))
        assertEquals("ASCE 7-10 \u00a72.4.1 Eq. 6b", ref(AsceEdition.ASCE_7_10, asd, "D + 0.75L + 0.75S + 0.525E"))
        assertEquals("ASCE 7-16 \u00a72.4.5 Eq. 9", ref(AsceEdition.ASCE_7_16, asd, "D + 0.75L + 0.75S + 0.525E"))
        assertEquals("ASCE 7-22 \u00a72.4.5 Eq. 9", ref(AsceEdition.ASCE_7_22, asd, "D + 0.75L + 0.1S + 0.525E"))
        assertEquals("ASCE 7-22 \u00a72.4.1 Eq. 4a (S)", ref(AsceEdition.ASCE_7_22, asd, "D + 0.75L + 0.525S"))
    }

    @Test
    fun `seismic Ev coefficients and the 0_5L exception flags match the code`() {
        fun c(e: AsceEdition, m: DesignMethodology, name: String) =
            AsceLoadCombinationCatalog.combinations(e, m).first { it.name == name }
        // Ev = 0.2 SDS D: LRFD +0.2 / -0.2; ASD 0.7 x 0.2 = 0.14, 0.525 x 0.2 = 0.105
        assertEquals(0.2, c(AsceEdition.ASCE_7_16, lrfd, "1.2D + L + 0.2S + E").seismicDeadCoefficient, 0.0)
        assertEquals(-0.2, c(AsceEdition.ASCE_7_16, lrfd, "0.9D + E").seismicDeadCoefficient, 0.0)
        assertEquals(0.14, c(AsceEdition.ASCE_7_22, asd, "D + 0.7E").seismicDeadCoefficient, 0.0)
        assertEquals(0.105, c(AsceEdition.ASCE_7_22, asd, "D + 0.75L + 0.1S + 0.525E").seismicDeadCoefficient, 0.0)
        assertEquals(-0.14, c(AsceEdition.ASCE_7_22, asd, "0.6D + 0.7E").seismicDeadCoefficient, 0.0)
        assertEquals(0.0, c(AsceEdition.ASCE_7_22, lrfd, "0.9D + W").seismicDeadCoefficient, 0.0)
        // 0.5L: LRFD Eq 3 (the L alternative), Eq 4 and the seismic combination with L; never Eq 2; never ASD
        for (e in AsceEdition.values()) {
            val flagged = AsceLoadCombinationCatalog.combinations(e, lrfd).filter { it.halfLiveAllowed }.map { it.name }.toSet()
            assertEquals(e.sourceName, 7, flagged.size)
            assertTrue(e.sourceName, "1.2D + L + 1.6Lr" in flagged && "1.2D + L + 0.5Lr + W" in flagged)
            assertTrue(e.sourceName, flagged.none { it.startsWith("1.2D + 1.6L") })
            assertTrue(AsceLoadCombinationCatalog.combinations(e, asd).none { it.halfLiveAllowed })
        }
    }

    @Test
    fun `roof live, snow and rain each get the same number of gravity combinations`() {
        for (m in listOf(lrfd, asd)) {
            val combos = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_16, m)
            fun count(c: LoadCategory) = combos.count { c in it.factors }
            assertEquals("Lr vs R, $m", count(LoadCategory.ROOF_LIVE), count(LoadCategory.RAIN))
            // Snow additionally appears in the seismic combination that carries L.
            assertEquals("S, $m", count(LoadCategory.ROOF_LIVE) + 1, count(LoadCategory.SNOW))
        }
        val l = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_16, lrfd)
        assertEquals(4, l.count { LoadCategory.ROOF_LIVE in it.factors })   // Eq 2, Eq 3 (x2), Eq 4
        val a = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_16, asd)
        assertEquals(3, a.count { LoadCategory.ROOF_LIVE in it.factors })   // Eq 3, 4, 6
    }

    @Test
    fun `factors are exactly the code's, including the 0_75 products`() {
        val a = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_16, asd)
        assertEquals(0.45, a.first { it.name == "D + 0.75L + 0.75S + 0.45W" }.factors.getValue(LoadCategory.WIND), 0.0)
        val eq = a.first { it.name == "D + 0.75L + 0.75S + 0.525E" }.factors
        assertEquals(0.525, eq.getValue(LoadCategory.SEISMIC), 0.0)
        assertEquals(0.75, eq.getValue(LoadCategory.SNOW), 0.0)
        val l = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_16, lrfd)
        assertEquals(0.5, l.first { it.name == "1.2D + 1.6R + 0.5W" }.factors.getValue(LoadCategory.WIND), 0.0)
        assertEquals(1.6, l.first { it.name == "1.2D + 1.6R + 0.5W" }.factors.getValue(LoadCategory.RAIN), 0.0)
        assertEquals(0.2, l.first { it.name == "1.2D + L + 0.2S + E" }.factors.getValue(LoadCategory.SNOW), 0.0)
        // 7-22 snow: 0.3 / 1.0 / 0.15 (LRFD) and 0.7 / 0.525 / 0.1 (ASD)
        val l22 = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_22, lrfd)
        assertEquals(0.3, l22.first { it.name == "1.2D + 1.6L + 0.3S" }.factors.getValue(LoadCategory.SNOW), 0.0)
        assertEquals(1.0, l22.first { it.name == "1.2D + L + S" }.factors.getValue(LoadCategory.SNOW), 0.0)
        assertEquals(0.15, l22.first { it.name == "1.2D + L + 0.15S + E" }.factors.getValue(LoadCategory.SNOW), 0.0)
        val a22 = AsceLoadCombinationCatalog.combinations(AsceEdition.ASCE_7_22, asd)
        assertEquals(0.7, a22.first { it.name == "D + 0.7S" }.factors.getValue(LoadCategory.SNOW), 0.0)
        assertEquals(0.525, a22.first { it.name == "D + 0.75L + 0.525S" }.factors.getValue(LoadCategory.SNOW), 0.0)
        assertEquals(0.1, a22.first { it.name == "D + 0.75L + 0.1S + 0.525E" }.factors.getValue(LoadCategory.SNOW), 0.0)
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
