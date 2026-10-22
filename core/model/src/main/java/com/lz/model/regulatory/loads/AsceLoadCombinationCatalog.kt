package com.lz.model.regulatory.loads

import com.lz.model.regulatory.LoadCategory
import com.lz.model.structural.DesignMethodology

/** One load combination as defined by the catalog (before it is stored). */
data class CatalogCombination(
    /** Display name, e.g. "1.2D + 1.6S + 0.5W". */
    val name: String,
    /** Stable id fragment derived from the factors, so a changed factor is a new id. */
    val idSuffix: String,
    val factors: Map<LoadCategory, Double>,
    /** The ASCE 7 equation this expands, e.g. "Eq. 3 (L or 0.5W)". */
    val reference: String
)

/** ASCE 7 editions whose basic combinations are catalogued. */
enum class AsceEdition(val sourceId: String, val sourceName: String, val lrfdSection: String, val asdSection: String) {
    ASCE_7_10("ASCE_7_10", "ASCE 7-10", "ASCE 7-10 \u00a72.3.2", "ASCE 7-10 \u00a72.4.1"),
    ASCE_7_16("ASCE_7_16", "ASCE 7-16", "ASCE 7-16 \u00a72.3.1", "ASCE 7-16 \u00a72.4.1"),
    ASCE_7_22("ASCE_7_22", "ASCE 7-22", "ASCE 7-22 \u00a72.3.1", "ASCE 7-22 \u00a72.4.1")
}

/**
 * The ASCE 7 basic load combinations for strength design (LRFD) and
 * allowable stress design (ASD), with EVERY "or" alternative written out as
 * its own combination.
 *
 * The code writes several combinations with alternatives, e.g.
 *   1.2D + 1.6(Lr or S or R) + (L or 0.5W)
 * which is six separate combinations: each of Lr, S, R with each of L, 0.5W.
 * A member carrying snow must be checked against the snow combinations —
 * folding the alternatives into one combination that carries only the first
 * (roof live load) silently drops snow and rain from the design.
 *
 * LRFD (ASCE 7-16 / 7-22 \u00a72.3.1, ASCE 7-10 \u00a72.3.2), 16 combinations:
 *  1. 1.4D
 *  2. 1.2D + 1.6L + 0.5(Lr or S or R)                 x3
 *  3. 1.2D + 1.6(Lr or S or R) + (L or 0.5W)          x6
 *  4. 1.2D + 1.0W + L + 0.5(Lr or S or R)             x3
 *  5. 1.2D + 1.0E + L + 0.2S
 *  6. 0.9D + 1.0W
 *  7. 0.9D + 1.0E
 *
 * ASD (\u00a72.4.1), 16 combinations:
 *  1. D            2. D + L
 *  3. D + (Lr or S or R)                               x3
 *  4. D + 0.75L + 0.75(Lr or S or R)                   x3
 *  5. D + (0.6W or 0.7E)                               x2
 *  6. D + 0.75L + 0.75(0.6W) + 0.75(Lr or S or R)      x3
 *  7. D + 0.75L + 0.75(0.7E) + 0.75S
 *  8. 0.6D + 0.6W  9. 0.6D + 0.7E
 * (the 0.75 x 0.6 and 0.75 x 0.7 products are stored as 0.45 and 0.525).
 *
 * L is taken at its full factor in every combination that contains it —
 * conservative where the code allows 0.5L (e.g. L <= 100 psf, not garages).
 *
 * STATUS: transcribed from the published ASCE 7 combinations. The basic
 * combinations have the same structure in 7-10, 7-16 and 7-22 as far as is
 * known, so all three editions currently share one definition and differ
 * only in their section reference; they have NOT yet been checked against
 * each edition's own pages — branch on [AsceEdition] in [combinations] where
 * an edition is found to differ.
 */
object AsceLoadCombinationCatalog {

    val editions: List<AsceEdition> = AsceEdition.values().toList()

    /** Number of combinations in each of the LRFD and ASD sets. */
    const val LRFD_COUNT = 16
    const val ASD_COUNT = 16

    private val D = LoadCategory.DEAD
    private val L = LoadCategory.LIVE
    private val W = LoadCategory.WIND
    private val E = LoadCategory.SEISMIC
    private val S = LoadCategory.SNOW

    /** The "(Lr or S or R)" alternatives, in a fixed order. */
    private val roofAlternatives = listOf(LoadCategory.ROOF_LIVE, LoadCategory.SNOW, LoadCategory.RAIN)

    fun setId(edition: AsceEdition, methodology: DesignMethodology) = "${edition.sourceId}_${methodology.name}"

    fun combinationId(edition: AsceEdition, methodology: DesignMethodology, combo: CatalogCombination) =
        "${setId(edition, methodology)}_${combo.idSuffix}"

    fun combinations(edition: AsceEdition, methodology: DesignMethodology): List<CatalogCombination> =
        if (methodology == DesignMethodology.LRFD) lrfd(edition) else asd(edition)

    // ------------------------------------------------------------------

    private fun lrfd(edition: AsceEdition): List<CatalogCombination> {
        val ref = edition.lrfdSection
        val out = mutableListOf<CatalogCombination>()
        out += combo(mapOf(D to 1.4), "$ref Eq. 1")
        for (alt in roofAlternatives)
            out += combo(mapOf(D to 1.2, L to 1.6, alt to 0.5), "$ref Eq. 2 (${label(alt)})")
        for (alt in roofAlternatives) {
            out += combo(mapOf(D to 1.2, alt to 1.6, L to 1.0), "$ref Eq. 3 (${label(alt)}, L)")
            out += combo(mapOf(D to 1.2, alt to 1.6, W to 0.5), "$ref Eq. 3 (${label(alt)}, 0.5W)")
        }
        for (alt in roofAlternatives)
            out += combo(mapOf(D to 1.2, W to 1.0, L to 1.0, alt to 0.5), "$ref Eq. 4 (${label(alt)})")
        out += combo(mapOf(D to 1.2, E to 1.0, L to 1.0, S to 0.2), "$ref Eq. 5")
        out += combo(mapOf(D to 0.9, W to 1.0), "$ref Eq. 6")
        out += combo(mapOf(D to 0.9, E to 1.0), "$ref Eq. 7")
        return out
    }

    private fun asd(edition: AsceEdition): List<CatalogCombination> {
        val ref = edition.asdSection
        val out = mutableListOf<CatalogCombination>()
        out += combo(mapOf(D to 1.0), "$ref Eq. 1")
        out += combo(mapOf(D to 1.0, L to 1.0), "$ref Eq. 2")
        for (alt in roofAlternatives)
            out += combo(mapOf(D to 1.0, alt to 1.0), "$ref Eq. 3 (${label(alt)})")
        for (alt in roofAlternatives)
            out += combo(mapOf(D to 1.0, L to 0.75, alt to 0.75), "$ref Eq. 4 (${label(alt)})")
        out += combo(mapOf(D to 1.0, W to 0.6), "$ref Eq. 5 (0.6W)")
        out += combo(mapOf(D to 1.0, E to 0.7), "$ref Eq. 5 (0.7E)")
        for (alt in roofAlternatives)
            out += combo(mapOf(D to 1.0, L to 0.75, W to 0.45, alt to 0.75), "$ref Eq. 6 (${label(alt)})")
        out += combo(mapOf(D to 1.0, L to 0.75, E to 0.525, S to 0.75), "$ref Eq. 7")
        out += combo(mapOf(D to 0.6, W to 0.6), "$ref Eq. 8")
        out += combo(mapOf(D to 0.6, E to 0.7), "$ref Eq. 9")
        return out
    }

    // ------------------------------------------------------------------

    private fun label(c: LoadCategory) = when (c) {
        LoadCategory.DEAD -> "D"
        LoadCategory.LIVE -> "L"
        LoadCategory.ROOF_LIVE -> "Lr"
        LoadCategory.SNOW -> "S"
        LoadCategory.WIND -> "W"
        LoadCategory.SEISMIC -> "E"
        LoadCategory.RAIN -> "R"
    }

    private fun num(v: Double): String = if (v == 1.0) "" else v.toString().removeSuffix(".0")

    /** Dead first, then the rest in a fixed order, so names read like the code's equations. */
    private val nameOrder = listOf(
        LoadCategory.DEAD, LoadCategory.LIVE, LoadCategory.ROOF_LIVE, LoadCategory.SNOW,
        LoadCategory.RAIN, LoadCategory.WIND, LoadCategory.SEISMIC
    )

    private fun combo(factors: Map<LoadCategory, Double>, reference: String): CatalogCombination {
        val terms = nameOrder.filter { it in factors }.map { num(factors.getValue(it)) + label(it) }
        val name = terms.joinToString(" + ")
        return CatalogCombination(
            name = name,
            idSuffix = terms.joinToString("_"),
            factors = factors,
            reference = reference
        )
    }
}
