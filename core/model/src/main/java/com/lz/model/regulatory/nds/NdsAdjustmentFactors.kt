package com.lz.model.regulatory.nds

import kotlinx.serialization.Serializable

/** In-service moisture condition: decides the wet service factors CM (see [NdsAdjustmentFactors]). */
@Serializable
enum class MoistureCondition(val label: String) {
    DRY("Dry service"),
    WET("Wet service")
}

/**
 * What kind of live load L is, for the LRFD time effect factor (NDS Appendix
 * N, Table N3, row 1.2D + 1.6L + 0.5(Lr or S or R)): lambda = 0.7 for storage,
 * 0.8 for occupancy, 1.25 for impact.
 */
@Serializable
enum class LiveLoadKind(val lambda: Double, val label: String) {
    OCCUPANCY(0.8, "Occupancy"),
    STORAGE(0.7, "Storage"),
    IMPACT(1.25, "Impact")
}

/**
 * The project's assumptions about load duration — the places where NDS Table
 * 2.3.2 does not assign a duration to a load type, or where the designer may
 * know better. Defaults are the assumptions the calculator started with:
 * occupancy live 1.0 (ten years), roof live 1.25 (seven days), snow 1.15
 * (two months), rain 1.0 (the table has no rain entry). Dead (0.9) and wind /
 * earthquake (1.6) are fixed by Table 2.3.2 and are not editable here.
 * ASD uses the CD values; LRFD uses [liveKind].
 */
@Serializable
data class NdsLoadDurationAssumptions(
    val liveCd: Double = 1.0,
    val roofLiveCd: Double = 1.25,
    val snowCd: Double = 1.15,
    val rainCd: Double = 1.0,
    val liveKind: LiveLoadKind = LiveLoadKind.OCCUPANCY
) {
    val isValid: Boolean
        get() = listOf(liveCd, roofLiveCd, snowCd, rainCd).all { it.isFinite() && it > 0.0 && it <= 2.0 }
}

/**
 * NDS Table 4.3.1 adjustment factors applied to reference design values.
 * F' = F * CD * CM * Ct * CL * CF * Cfu * Ci * Cr
 * All factors default to 1.0 (no adjustment) until explicitly set.
 */
@Serializable
data class NdsAdjustmentFactors(
    val cd: Double = 1.0,   // Load duration factor — NDS Section 2.3.2 (manual value; see usesAutoLoadDuration)
    /** Moisture condition input: WET applies the wet service factors CM per design value. */
    val moisture: MoistureCondition = MoistureCondition.DRY,
    /**
     * LEGACY single wet-service value from before the moisture input existed (it was applied
     * to every property alike, which is wrong). Any value other than 1.0 is read as WET.
     */
    val cm: Double = 1.0,
    // Wet service factors per design value. Filled in by the calculator from [moisture], the
    // species and the member size (NDS Supplement CM tables) - not user inputs.
    val cmFb: Double = 1.0,
    val cmFt: Double = 1.0,
    val cmFv: Double = 1.0,
    val cmFcPerp: Double = 1.0,
    val cmFc: Double = 1.0,
    val cmE: Double = 1.0,
    val ct: Double = 1.0,   // Temperature factor — NDS Table 2.3.3
    val cl: Double = 1.0,   // Beam stability factor — NDS Section 3.3.3
    val cf: Double = 1.0,   // Size factor — NDS Table 4A (sawn lumber)
    val cfu: Double = 1.0,  // Flat use factor — NDS Table 4A
    val ci: Double = 1.0,   // Incising factor — NDS Section 4.3.8
    val cr: Double = 1.0,   // Repetitive member factor — NDS Section 4.3.9
    val cp: Double = 1.0,   // Column stability factor — NDS Section 3.7.1
    val cb: Double = 1.0,   // Bearing area factor — NDS Section 3.10.4
    /**
     * Take the load duration factor (ASD) / time effect factor (LRFD) from each
     * load combination instead of using [cd] — NDS 2.3.2. CD is a property of
     * the loads in a combination (the shortest-duration one governs), so a
     * single number is only right for a single combination.
     */
    val loadDurationFromCombination: Boolean = true,
    /** Duration assumptions behind the automatic CD / lambda (see [NdsLoadDurationAssumptions]). */
    val loadDurationAssumptions: NdsLoadDurationAssumptions = NdsLoadDurationAssumptions(),
    /**
     * Member pressure-treated with water-borne preservatives or fire-retardant
     * chemicals: CD above 1.6 and lambda above 1.0 do not apply (NDS Table
     * 2.3.2 note 2, 2.3.4, Table N3 note 1).
     */
    val treatedMember: Boolean = false,
    // --- LRFD only: set by the wood calculator, not by the user -------------
    // For LRFD the reference design values are multiplied by KF x phi x lambda
    // (Fb, Ft, Fv, Fc) and KF x phi-s (Emin) BEFORE the stability factors are
    // computed, because CL and CP are nonlinear in F*b / F*c and E'min. These
    // carry those products; they are 1.0 for ASD. (NDS 2.3.5 - 2.3.7, App. N.)
    val formatBending: Double = 1.0,
    val formatTension: Double = 1.0,
    val formatShear: Double = 1.0,
    val formatCompression: Double = 1.0,
    val formatEmin: Double = 1.0
) {
    /** The duration factor (ASD: CD) or its LRFD replacement (KF x phi x lambda), per property. */
    /** [cd] after the treated-member limit (CD <= 1.6, Table 2.3.2 note 2). */
    val effectiveCd: Double get() = if (treatedMember) minOf(cd, TREATED_MAX_CD) else cd
    /** True for wet service (the legacy single CM, when not 1.0, also means wet). */
    val isWet: Boolean get() = moisture == MoistureCondition.WET || cm != 1.0

    val cdBending: Double get() = effectiveCd * formatBending
    val cdTension: Double get() = effectiveCd * formatTension
    val cdShear: Double get() = effectiveCd * formatShear
    val cdCompression: Double get() = effectiveCd * formatCompression

    /**
     * Automatic CD / lambda is in force only while [cd] is still the untouched
     * 1.0 default. A CD the user typed in (anything else) is a deliberate
     * override and is respected — including for calculations saved before this
     * option existed, which keep the CD they were saved with. To force a manual
     * 1.0, turn [loadDurationFromCombination] off.
     */
    val usesAutoLoadDuration: Boolean get() = loadDurationFromCombination && cd == 1.0

    /**
     * Adjusted bending design value F'b.
     * CAUTION: `cf` here is the raw, un-resolved field — 1.0 unless the
     * caller explicitly overrode it. It does NOT apply the NDS Table 4A
     * tabulated lookup by nominal width/grade for sawn lumber. Wood
     * capacity code (`core:solver`'s `NdsWoodCapacityCalculator` /
     * `NdsBeamStability`) resolves the real CF via `resolveSawnCf` and
     * should not call these `adjusted*` helpers for any factor that
     * includes `cf` — see that resolver's doc for why.
     */
    fun adjustedBending(fb: Double)              = fb * cdBending * cmFb * ct * cl * cf * cfu * ci * cr
    /** Adjusted shear design value F'v */
    fun adjustedShear(fv: Double)                = fv * cdShear * cmFv * ct * ci
    /** Adjusted compression parallel F'c. CAUTION: raw `cf` — see [adjustedBending]. */
    fun adjustedCompressionParallel(fc: Double)  = fc * cdCompression * cmFc * ct * cf * ci * cp
    /** Adjusted compression perpendicular F'c⊥ */
    fun adjustedCompressionPerp(fcPerp: Double)  = fcPerp * cmFcPerp * ct * ci * cb
    /** Adjusted tension parallel F't. CAUTION: raw `cf` — see [adjustedBending]. */
    fun adjustedTension(ft: Double)              = ft * cdTension * cmFt * ct * cf * ci
    /** Adjusted modulus of elasticity E' */
    fun adjustedModulus(e: Double)               = e * cmE * ct * ci

    companion object {
        const val TREATED_MAX_CD = 1.6
    }
}
