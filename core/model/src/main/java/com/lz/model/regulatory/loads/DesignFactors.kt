package com.lz.model.regulatory.loads

import com.lz.model.structural.DesignMethodology
import kotlinx.serialization.Serializable

/**
 * Resistance and safety factors for a specific standard edition and methodology.
 * LRFD uses phi (φ) reduction factors applied to nominal capacity.
 * ASD uses omega (Ω) safety factors dividing nominal capacity.
 */
@Serializable
data class DesignFactors(
    val methodology: DesignMethodology,

    // Flexure
    val phiMoment: Double? = null,       // LRFD: φb, typically 0.90 (AISC 360 F1)
    val omegaMoment: Double? = null,     // ASD:  Ωb, typically 1.67

    // Shear
    val phiShear: Double? = null,        // LRFD: φv, typically 0.90 (AISC 360 G2)
    val omegaShear: Double? = null,      // ASD:  Ωv, typically 1.67

    // Axial - Tension
    val phiTensionYield: Double? = null, // LRFD: φt yielding, 0.90 (AISC 360 D2)
    val phiTensionRupture: Double? = null,// LRFD: φt rupture, 0.75
    val omegaTension: Double? = null,    // ASD:  Ωt, 1.67 / 2.00

    // Axial - Compression
    val phiCompression: Double? = null,  // LRFD: φc, 0.90 (AISC 360 E1)
    val omegaCompression: Double? = null // ASD:  Ωc, 1.67
)