package com.lz.model.regulatory.loads

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.structural.DesignMethodology
import kotlin.math.abs

/**
 * Optional seismic and live-load inputs applied to the catalogued ASCE 7 combinations at run time.
 *
 *  - [sds]: design spectral response acceleration at short periods. When entered, the vertical seismic
 *    effect Ev = 0.2 SDS D (ASCE 7-10 §12.4.2.2 / 7-16, 7-22 §12.4.2.2) is added to the dead-load factor
 *    of each seismic combination (e.g. LRFD 1.2D becomes (1.2 + 0.2 SDS) D; 0.9D becomes (0.9 - 0.2 SDS) D).
 *  - [rho]: redundancy factor (§12.3.4), 1.0 or 1.3. The entered seismic load is QE (the horizontal effect
 *    before rho); Eh = rho QE.
 *  - [halfLive]: the user claims the exception that permits 0.5L (Lo <= 100 psf, not a garage or place of
 *    public assembly) in the combinations where the code allows it (strength design only).
 *
 * With none of them the seismic load is used exactly as entered (Ev = 0, rho = 1.0) and L at its full factor,
 * and the notes say so.
 */
data class SeismicCombinationInputs(
    val sds: Double? = null,
    val rho: Double? = null,
    val halfLive: Boolean = false
) {
    fun blockReason(): String? = when {
        sds != null && !(sds.isFinite() && sds >= 0.0) -> "SDS must be zero or more (entered $sds)."
        rho != null && rho != 1.0 && rho != 1.3 -> "The redundancy factor rho is 1.0 or 1.3 (ASCE 7 §12.3.4); entered $rho."
        else -> null
    }
}

object SeismicCombinationAdjuster {

    data class Result(
        val combinations: List<LoadCombination>,
        /** Every assumption and source remark that applies; the output must carry these. */
        val notes: List<String>
    )

    private fun parse(setId: String): Pair<AsceEdition, DesignMethodology>? {
        for (e in AsceEdition.values()) for (m in DesignMethodology.values())
            if (setId == AsceLoadCombinationCatalog.setId(e, m)) return e to m
        return null
    }

    private fun clean(v: Double) = Math.round(v * 1e6) / 1e6

    private fun fmt(v: Double): String {
        val s = java.math.BigDecimal.valueOf(clean(v)).stripTrailingZeros().toPlainString()
        return s
    }

    private val order = listOf(
        LoadCategory.DEAD, LoadCategory.LIVE, LoadCategory.ROOF_LIVE, LoadCategory.SNOW,
        LoadCategory.RAIN, LoadCategory.WIND, LoadCategory.SEISMIC
    )

    private fun text(f: Map<LoadCategory, Double>) = order.filter { it in f }.joinToString(" + ") {
        val v = f.getValue(it)
        (if (v == 1.0) "" else fmt(v)) + it.shortLabel
    }

    fun adjust(setId: String, combinations: List<LoadCombination>, inputs: SeismicCombinationInputs): Result {
        val (edition, method) = parse(setId) ?: return Result(combinations, emptyList())
        inputs.blockReason()?.let { throw IllegalArgumentException(it) }
        val catalog = AsceLoadCombinationCatalog.combinations(edition, method).associateBy { it.name }
        val rho = inputs.rho ?: 1.0
        val applied = mutableListOf<String>()
        val halfNames = mutableListOf<String>()
        var anySeismic = false

        val out = combinations.map { c ->
            val entry = catalog[c.name] ?: return@map c
            val f = c.factors.toMutableMap()
            val seismic = LoadCategory.SEISMIC in f
            if (seismic) anySeismic = true
            if (seismic && inputs.sds != null && entry.seismicDeadCoefficient != 0.0) {
                f[LoadCategory.DEAD] = clean(f.getValue(LoadCategory.DEAD) + entry.seismicDeadCoefficient * inputs.sds)
            }
            if (seismic && rho != 1.0) f[LoadCategory.SEISMIC] = clean(f.getValue(LoadCategory.SEISMIC) * rho)
            if (inputs.halfLive && entry.halfLiveAllowed && LoadCategory.LIVE in f) {
                f[LoadCategory.LIVE] = 0.5; halfNames += c.name
            }
            if (f == c.factors) c
            else c.copy(factors = f, equationText = text(f) + " [" + listOfNotNull(
                inputs.sds?.takeIf { seismic && entry.seismicDeadCoefficient != 0.0 }?.let { "SDS = ${fmt(it)}" },
                inputs.rho?.takeIf { seismic }?.let { "\u03C1 = ${fmt(it)}" },
                "0.5L exception".takeIf { c.name in halfNames }
            ).joinToString(", ") + "]")
        }

        if (anySeismic) {
            applied += if (inputs.sds != null)
                "Vertical seismic effect included: Ev = 0.2 x SDS x D with SDS = ${fmt(inputs.sds)} in the seismic combinations."
            else
                "ASSUMED: SDS not entered, so the vertical seismic effect Ev = 0.2 SDS D is not included (Ev = 0); " +
                    "enter SDS, or enter E as the total effect including Ev."
            applied += if (inputs.rho != null)
                "Redundancy factor \u03C1 = ${fmt(inputs.rho)} applied to the seismic load (the seismic load entered is QE)."
            else
                "ASSUMED: \u03C1 not entered, so \u03C1 = 1.0 and the seismic load is used as entered " +
                    "(\u03C1 is 1.3 for Seismic Design Categories D to F unless ASCE 7 \u00a712.3.4.2 is met)."
        }
        if (method == DesignMethodology.LRFD) {
            applied += if (inputs.halfLive)
                "0.5L exception claimed (Lo <= 100 psf, not a garage or place of public assembly): L at 0.5 in ${halfNames.size} " +
                    "combinations where the code allows it."
            else "L is at its full factor in every combination (the 0.5L exception is not claimed)."
        }
        combinations.mapNotNull { catalog[it.name]?.note }.distinct().forEach { applied += it }
        if (edition == AsceEdition.ASCE_7_22)
            applied += "ASCE 7-22 tornado load WT is not offered; the combinations use W only."
        return Result(out, applied)
    }
}
