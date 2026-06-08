package com.lz.model.regulatory.nds

import kotlinx.serialization.Serializable

/**
 * NDS Table 4.3.1 adjustment factors applied to reference design values.
 * F' = F * CD * CM * Ct * CL * CF * Cfu * Ci * Cr
 * All factors default to 1.0 (no adjustment) until explicitly set.
 */
@Serializable
data class NdsAdjustmentFactors(
    val cd: Double = 1.0,   // Load duration factor — NDS Section 2.3.2
    val cm: Double = 1.0,   // Wet service factor — NDS Table 4A/4B
    val ct: Double = 1.0,   // Temperature factor — NDS Table 2.3.3
    val cl: Double = 1.0,   // Beam stability factor — NDS Section 3.3.3
    val cf: Double = 1.0,   // Size factor — NDS Table 4A (sawn lumber)
    val cfu: Double = 1.0,  // Flat use factor — NDS Table 4A
    val ci: Double = 1.0,   // Incising factor — NDS Section 4.3.8
    val cr: Double = 1.0,   // Repetitive member factor — NDS Section 4.3.9
    val cp: Double = 1.0,   // Column stability factor — NDS Section 3.7.1
    val cb: Double = 1.0    // Bearing area factor — NDS Section 3.10.4
) {
    /** Adjusted bending design value F'b */
    fun adjustedBending(fb: Double)              = fb * cd * cm * ct * cl * cf * cfu * ci * cr
    /** Adjusted shear design value F'v */
    fun adjustedShear(fv: Double)                = fv * cd * cm * ct * ci
    /** Adjusted compression parallel F'c */
    fun adjustedCompressionParallel(fc: Double)  = fc * cd * cm * ct * cf * ci * cp
    /** Adjusted compression perpendicular F'c⊥ */
    fun adjustedCompressionPerp(fcPerp: Double)  = fcPerp * cm * ct * ci * cb
    /** Adjusted tension parallel F't */
    fun adjustedTension(ft: Double)              = ft * cd * cm * ct * cf * ci
    /** Adjusted modulus of elasticity E' */
    fun adjustedModulus(e: Double)               = e * cm * ct * ci
}