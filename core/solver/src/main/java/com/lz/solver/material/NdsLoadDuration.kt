package com.lz.solver.material

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.nds.LoadDurationClass
import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.regulatory.nds.NdsVersionRegistry
import com.lz.model.structural.DesignMethodology

/**
 * A duration-dependent factor resolved for one load combination, with enough
 * explanation to show why it has the value it has.
 *
 * @property symbol "CD" for ASD, "λ" for LRFD.
 * @property governingCategory the load that set the value (the shortest-duration
 *   one acting), when there is a single such load.
 */
data class ResolvedLoadDuration(
    val factor: Double,
    val symbol: String,
    val governingCategory: LoadCategory?,
    val durationLabel: String,
    val explanation: String
)

/**
 * NDS load duration factor CD (ASD, §2.3.2) and LRFD time effect factor λ
 * (Appendix N, Table N3), derived from the loads that are acting in a
 * combination.
 *
 * Both are properties of the *combination*, not of the member: CD follows the
 * shortest-duration load in the combination (so D alone is 0.9, D + L is 1.0,
 * D + S is 1.15, D + W is 1.6), and λ follows the combination's load mix. One
 * number is only right for one combination, which is why this runs per
 * combination rather than once per member.
 *
 * STATUS: the duration classes come from the model's [LoadDurationClass]
 * table (NDS Table 2.3.2) and the λ rules from Table N3 as commonly
 * published; neither has yet been checked against page images of the
 * edition being designed to — [NdsCodeData] reports that as UNVERIFIED.
 */
object NdsLoadDuration {

    /** The duration class a load category falls in, and any assumption made in choosing it. */
    fun durationClassFor(category: LoadCategory): Pair<LoadDurationClass, String?> = when (category) {
        LoadCategory.DEAD      -> LoadDurationClass.PERMANENT to null
        LoadCategory.LIVE      -> LoadDurationClass.TEN_YEARS to
            "occupancy live load taken as ten-year duration (storage loads and impact are not distinguished)"
        LoadCategory.ROOF_LIVE -> LoadDurationClass.SEVEN_DAYS to
            "roof live load taken as seven-day (construction / maintenance) duration"
        LoadCategory.SNOW      -> LoadDurationClass.TWO_MONTHS to
            "snow taken as two-month duration (a longer snow duration at the site calls for a lower CD)"
        LoadCategory.WIND      -> LoadDurationClass.TEN_MINUTES to null
        LoadCategory.SEISMIC   -> LoadDurationClass.TEN_MINUTES to null
        LoadCategory.RAIN      -> LoadDurationClass.TEN_YEARS to
            "NDS tabulates no duration for rain load; ten-year (CD = 1.0) is used as a conservative assumption"
    }

    /**
     * Resolves the factor for a combination acting with [categories] under
     * [methodology]. Null when no loads are known to act (a demand that isn't
     * tied to a combination) — the caller then uses its manual setting.
     */
    fun resolve(
        loads: Map<LoadCategory, Double>,
        methodology: DesignMethodology,
        @Suppress("UNUSED_PARAMETER") edition: NdsEdition
    ): ResolvedLoadDuration? {
        if (loads.isEmpty()) return null
        return if (methodology == DesignMethodology.LRFD) lrfdLambda(loads) else asdCd(loads.keys)
    }

    private fun asdCd(categories: Set<LoadCategory>): ResolvedLoadDuration {
        val classed = categories.map { it to durationClassFor(it) }
        // NDS 2.3.2: the shortest-duration load in the combination sets CD.
        val cd = NdsVersionRegistry.getGoverningLoadDurationFactor(classed.map { it.second.first })
        val governing = classed.filter { it.second.first.cd == cd }
        val gov = governing.first()
        val names = categories.joinToString(" + ") { it.shortLabel }
        val assumption = gov.second.second?.let { "; $it" } ?: ""
        return ResolvedLoadDuration(
            factor = cd,
            symbol = "CD",
            governingCategory = gov.first,
            durationLabel = gov.second.first.description,
            explanation = "CD = ${"%.2f".format(cd)}: shortest-duration load acting is " +
                "${gov.first.label.lowercase()} (${gov.second.first.description.lowercase()}), loads acting: $names$assumption"
        )
    }

    /**
     * Table N3 assigns lambda by combination, and it keys on how big the wind
     * or earthquake factor is, not just whether one is present:
     *   1.4D                                   0.6
     *   1.2D + 1.6L + 0.5(Lr or S or R)        0.8  (L = occupancy)
     *   1.2D + 1.6(Lr or S or R) + (L or 0.5W) 0.8  <- wind present, factor 0.5
     *   1.2D + 1.0W + L + 0.5(Lr or S or R)    1.0
     *   1.2D + 1.0E + L + 0.2S                 1.0
     *   0.9D + 1.0W,  0.9D + 1.0E              1.0
     * So wind / earthquake at a strength-level factor (>= 1.0) gives 1.0;
     * otherwise any gravity load besides dead gives 0.8; dead alone gives 0.6.
     */
    private fun lrfdLambda(loads: Map<LoadCategory, Double>): ResolvedLoadDuration {
        val names = loads.entries.joinToString(" + ") { (c, f) ->
            (if (f == 1.0) "" else f.toString().removeSuffix(".0")) + c.shortLabel
        }
        fun strongLateral(c: LoadCategory) = (loads[c] ?: 0.0) >= 1.0
        val (lambda, why) = when {
            strongLateral(LoadCategory.WIND) || strongLateral(LoadCategory.SEISMIC) ->
                1.0 to "wind or earthquake at a strength-level factor (Table N3: 1.2D + 1.0W + ..., 1.2D + 1.0E + ..., 0.9D + 1.0W/E)"
            loads.keys.any { it != LoadCategory.DEAD } ->
                0.8 to "gravity loads besides dead (Table N3: 1.2D + 1.6L + ..., 1.2D + 1.6(Lr or S or R) + (L or 0.5W)); " +
                    "storage live load (0.7) and impact (1.25) are not distinguished"
            else ->
                0.6 to "dead load only (Table N3: 1.4D)"
        }
        return ResolvedLoadDuration(
            factor = lambda,
            symbol = "\u03bb",
            governingCategory = null,
            durationLabel = "Time effect",
            explanation = "\u03bb = ${"%.2f".format(lambda)}: $why; loads acting: $names"
        )
    }
}
