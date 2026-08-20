package com.lz.solver.capacity

import com.lz.model.regulatory.aisc.DesignFactor
import com.lz.model.structural.DesignMethodology

/**
 * Per-limit-state design factors for one methodology (LRFD or ASD), supplied
 * by each material's [CapacityCalculator] so [CapacityEngine] can factor
 * nominal capacities correctly without embedding material-specific numbers
 * itself.
 *
 * [DesignFactor] (value + code citation) is reused here even though it lives
 * in the `com.lz.model.regulatory.aisc` package — the (value, citation) shape
 * is material-agnostic. A future cleanup could relocate it to a neutral
 * package; not done here to keep this fix's blast radius small.
 *
 * Interpretation of `value` mirrors [com.lz.model.regulatory.aisc.AiscDesignFactors.applyToNominal]:
 *  - LRFD: design = nominal * value   (value is phi, or an NDS-style
 *          lambda*phi format-converted phi for wood)
 *  - ASD:  design = nominal / value   (value is omega; for wood ASD this is
 *          1.0, since NDS's "nominal" already IS the fully code-adjusted
 *          ASD allowable capacity — dividing by anything other than 1.0
 *          here would double-count NDS's adjustment factors)
 *
 * Axial tension and compression are kept separate because they can have
 * different factors (NDS: phi=0.80 tension vs 0.90 compression; AISC:
 * currently equal, but modeled separately for correctness/future editions).
 */
data class DesignFactorSet(
    val methodology: DesignMethodology,
    val flexure: DesignFactor,
    val shear: DesignFactor,
    val axialTension: DesignFactor,
    val axialCompression: DesignFactor,
    val torsion: DesignFactor
) {
    fun apply(nominal: Double, factor: DesignFactor): Double =
        when (methodology) {
            DesignMethodology.LRFD -> nominal * factor.value
            DesignMethodology.ASD  -> nominal / factor.value
        }
}
