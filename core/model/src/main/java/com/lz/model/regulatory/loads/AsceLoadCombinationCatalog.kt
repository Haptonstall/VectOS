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
    val reference: String,
    /**
     * Seismic combinations only: the vertical seismic effect Ev = 0.2 SDS D adds [seismicDeadCoefficient] x SDS
     * to the dead-load factor (negative where the code writes -Ev). 0 = not a seismic combination.
     */
    val seismicDeadCoefficient: Double = 0.0,
    /** True where the code's exception lets the factor on L be 0.5 (Lo <= 100 psf, not garages or public assembly). */
    val halfLiveAllowed: Boolean = false,
    /** A source remark that the output must carry when this combination is used. */
    val note: String? = null
)

/** ASCE 7 editions whose basic combinations are catalogued. */
enum class AsceEdition(val sourceId: String, val sourceName: String, val lrfdSection: String, val asdSection: String) {
    ASCE_7_10("ASCE_7_10", "ASCE 7-10", "ASCE 7-10 \u00a72.3.2", "ASCE 7-10 \u00a72.4.1"),
    ASCE_7_16("ASCE_7_16", "ASCE 7-16", "ASCE 7-16 \u00a72.3.1", "ASCE 7-16 \u00a72.4.1"),
    ASCE_7_22("ASCE_7_22", "ASCE 7-22", "ASCE 7-22 \u00a72.3.1", "ASCE 7-22 \u00a72.4.1")
}

/**
 * The ASCE 7 basic load combinations for strength design (LRFD) and allowable stress design (ASD),
 * with EVERY "or" alternative written out as its own combination, taken from each edition's own
 * Chapter 2 (checked against the published pages of ASCE 7-10, 7-16 and 7-22):
 *
 *  - ASCE 7-10 §2.3.2 / §2.4.1: seismic E is inside the basic list (LRFD Eq. 5 and 7; ASD Eq. 5, 6b and 8).
 *  - ASCE 7-16 and 7-22: seismic is in §2.3.6 (LRFD Eq. 6 and 7) and §2.4.5 (ASD Eq. 8, 9, 10), written with
 *    Ev and Eh; 0.9D + 1.0W is LRFD Eq. 5 (5a in 7-22).
 *  - Snow factors differ: 7-10 and 7-16 use 0.5S (companion) and 1.6S (principal) in LRFD, 0.2S / 0.75S in the
 *    seismic combination; 7-22 uses 0.3S, 1.0S and 0.15S in LRFD and 0.7S in ASD (so 0.525S where the code
 *    multiplies by 0.75), 0.1S in ASD Eq. 9.
 *
 * Seismic combinations carry E as the horizontal effect Eh = rho QE. With the optional SDS and rho inputs
 * [SeismicCombinationAdjuster] adds Ev = 0.2 SDS D and multiplies E by rho; with neither, E is used as
 * entered (Ev = 0, rho = 1.0), which the output states.
 *
 * L is at its full factor unless the user claims the 0.5L exception (see [CatalogCombination.halfLiveAllowed]).
 * Tornado load WT (ASCE 7-22) and the flood, ice, self-straining and notional-load combinations are not
 * catalogued.
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

    private const val NOTE_7_10_6B =
        "ASCE 7-10 prints 0.10 SDS in 12.4.2.3 and 0.105 SDS in 12.4.3.2 for this combination; 0.75 x 0.14 = 0.105 is used."

    // ------------------------------------------------------------------

    private fun lrfd(edition: AsceEdition): List<CatalogCombination> {
        val is22 = edition == AsceEdition.ASCE_7_22
        val is10 = edition == AsceEdition.ASCE_7_10
        val sCompanion = if (is22) 0.3 else 0.5       // Eq. 2 and 4
        val sPrincipal = if (is22) 1.0 else 1.6       // Eq. 3
        val sSeismic = if (is22) 0.15 else 0.2        // seismic combination with L
        val ref = edition.lrfdSection
        val seismicRef = if (is10) ref else "${edition.sourceName} \u00a72.3.6"
        val n = { k: Int -> if (is22) "${k}a" else "$k" }
        fun companion(c: LoadCategory) = if (c == S) sCompanion else 0.5
        fun principal(c: LoadCategory) = if (c == S) sPrincipal else 1.6

        val out = mutableListOf<CatalogCombination>()
        out += combo(mapOf(D to 1.4), "$ref Eq. ${n(1)}")
        for (alt in roofAlternatives)
            out += combo(mapOf(D to 1.2, L to 1.6, alt to companion(alt)), "$ref Eq. ${n(2)} (${label(alt)})")
        for (alt in roofAlternatives) {
            out += combo(mapOf(D to 1.2, alt to principal(alt), L to 1.0), "$ref Eq. ${n(3)} (${label(alt)}, L)", halfLive = true)
            out += combo(mapOf(D to 1.2, alt to principal(alt), W to 0.5), "$ref Eq. ${n(3)} (${label(alt)}, 0.5W)")
        }
        for (alt in roofAlternatives)
            out += combo(mapOf(D to 1.2, W to 1.0, L to 1.0, alt to companion(alt)), "$ref Eq. ${n(4)} (${label(alt)})", halfLive = true)
        if (is10) {
            out += combo(mapOf(D to 1.2, E to 1.0, L to 1.0, S to sSeismic), "$ref Eq. 5", ev = 0.2, halfLive = true)
            out += combo(mapOf(D to 0.9, W to 1.0), "$ref Eq. 6")
            out += combo(mapOf(D to 0.9, E to 1.0), "$ref Eq. 7", ev = -0.2)
        } else {
            out += combo(mapOf(D to 0.9, W to 1.0), "$ref Eq. ${n(5)}")
            out += combo(mapOf(D to 1.2, E to 1.0, L to 1.0, S to sSeismic), "$seismicRef Eq. 6", ev = 0.2, halfLive = true)
            out += combo(mapOf(D to 0.9, E to 1.0), "$seismicRef Eq. 7", ev = -0.2)
        }
        return out
    }

    private fun asd(edition: AsceEdition): List<CatalogCombination> {
        val is22 = edition == AsceEdition.ASCE_7_22
        val is10 = edition == AsceEdition.ASCE_7_10
        val sFull = if (is22) 0.7 else 1.0            // D + (Lr or S or R)
        val sPart = if (is22) 0.525 else 0.75         // 0.75 (Lr or S or R), 0.75 x 0.7 = 0.525 in 7-22
        val sSeismic = if (is22) 0.1 else 0.75        // seismic combination with L
        val ref = edition.asdSection
        val seismicRef = if (is10) ref else "${edition.sourceName} \u00a72.4.5"
        val n = { k: Int -> if (is22) "${k}a" else "$k" }
        fun full(c: LoadCategory) = if (c == S) sFull else 1.0
        fun part(c: LoadCategory) = if (c == S) sPart else 0.75

        val out = mutableListOf<CatalogCombination>()
        out += combo(mapOf(D to 1.0), "$ref Eq. ${n(1)}")
        out += combo(mapOf(D to 1.0, L to 1.0), "$ref Eq. ${n(2)}")
        for (alt in roofAlternatives)
            out += combo(mapOf(D to 1.0, alt to full(alt)), "$ref Eq. ${n(3)} (${label(alt)})")
        for (alt in roofAlternatives)
            out += combo(mapOf(D to 1.0, L to 0.75, alt to part(alt)), "$ref Eq. ${n(4)} (${label(alt)})")
        out += combo(mapOf(D to 1.0, W to 0.6), "$ref Eq. ${n(5)} (0.6W)")
        if (is10) {
            out += combo(mapOf(D to 1.0, E to 0.7), "$ref Eq. 5 (0.7E)", ev = 0.14)
            for (alt in roofAlternatives)
                out += combo(mapOf(D to 1.0, L to 0.75, W to 0.45, alt to part(alt)), "$ref Eq. 6a (${label(alt)})")
            out += combo(mapOf(D to 1.0, L to 0.75, E to 0.525, S to sSeismic), "$ref Eq. 6b", ev = 0.105, note = NOTE_7_10_6B)
            out += combo(mapOf(D to 0.6, W to 0.6), "$ref Eq. 7")
            out += combo(mapOf(D to 0.6, E to 0.7), "$ref Eq. 8", ev = -0.14)
        } else {
            for (alt in roofAlternatives)
                out += combo(mapOf(D to 1.0, L to 0.75, W to 0.45, alt to part(alt)), "$ref Eq. ${n(6)} (${label(alt)})")
            out += combo(mapOf(D to 0.6, W to 0.6), "$ref Eq. ${n(7)}")
            out += combo(mapOf(D to 1.0, E to 0.7), "$seismicRef Eq. 8", ev = 0.14)
            out += combo(mapOf(D to 1.0, L to 0.75, E to 0.525, S to sSeismic), "$seismicRef Eq. 9", ev = 0.105)
            out += combo(mapOf(D to 0.6, E to 0.7), "$seismicRef Eq. 10", ev = -0.14)
        }
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

    private fun combo(
        factors: Map<LoadCategory, Double>,
        reference: String,
        ev: Double = 0.0,
        halfLive: Boolean = false,
        note: String? = null
    ): CatalogCombination {
        val terms = nameOrder.filter { it in factors }.map { num(factors.getValue(it)) + label(it) }
        val name = terms.joinToString(" + ")
        return CatalogCombination(
            name = name,
            idSuffix = terms.joinToString("_"),
            factors = factors,
            reference = reference,
            seismicDeadCoefficient = ev,
            halfLiveAllowed = halfLive,
            note = note
        )
    }
}
