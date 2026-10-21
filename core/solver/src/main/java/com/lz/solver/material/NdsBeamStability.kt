package com.lz.solver.material

import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodProfile
import com.lz.model.structural.WoodSpecies
import com.lz.model.units.inIn3
import com.lz.model.units.inIn4
import com.lz.model.units.inInches
import com.lz.model.units.inPsi
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Which tabulated property a size factor is being resolved for. NDS Table
 * 4A gives Fb, Ft and Fc *different* size-factor schedules (Fc's is much
 * smaller — e.g. 1.15 vs 1.5 for a 2x4 — and Fb also differs for 4"-thick
 * members), so a single shared CF is wrong for at least one of them.
 */
enum class NdsCfProperty { BENDING, TENSION, COMPRESSION }

/**
 * Resolves the effective size factor CF for [property]: the caller's
 * explicit override if they set one (non-default — an override applies to
 * every property), otherwise the tabulated value.
 *
 *  - Sawn lumber other than Southern Pine: NDS Table 4A (by nominal width,
 *    thickness and grade — see [NdsWoodCapacityCalculator.ndsTable4ASizeFactor]).
 *  - Southern Pine: NDS Table 4B already builds size into the tabulated
 *    values for each width (see [resolveDesignMaterial]), so applying the
 *    Table 4A factor on top would double-count. Only the >12" cases in the
 *    Table 4B adjustment-factor text remain. The optional CF = 1.1 for
 *    4"-thick, 8"-and-wider Fb is "permitted", not required, so it isn't
 *    applied automatically (use the CF override to take it).
 *  - Glulam: no CF (bending uses CV/CL; axial Table 5 values are already
 *    full-size), so 1.0.
 */
internal fun resolveSawnCf(
    adjustmentFactors: NdsAdjustmentFactors,
    material: MaterialGrade.Wood,
    profile: SectionProfile,
    property: NdsCfProperty
): Double {
    if (adjustmentFactors.cf != 1.0) return adjustmentFactors.cf
    if (material.species.isGlulam) return 1.0

    val nominalWidthIn =
        if (profile is WoodProfile) profile.nominalDepth.inInches else profile.depth.inInches
    val thicknessIn =
        if (profile is WoodProfile) profile.nominalWidth.inInches else 2.0

    if (material.species == WoodSpecies.SOUTHERN_PINE) {
        if (nominalWidthIn <= 12.0) return 1.0
        val dense = material.grade == WoodGrade.DENSE_STRUCTURAL_86 ||
            material.grade == WoodGrade.DENSE_STRUCTURAL_72 ||
            material.grade == WoodGrade.DENSE_STRUCTURAL_65
        return when {
            // Dense Structural, d > 12": Fb only, CF = (12/d)^(1/9).
            dense -> if (property == NdsCfProperty.BENDING) (12.0 / nominalWidthIn).pow(1.0 / 9.0) else 1.0
            // Other grades wider than 12": 0.9 on Fb, Ft and Fc.
            else -> 0.9
        }
    }
    return NdsWoodCapacityCalculator.ndsTable4ASizeFactor(
        material.grade, nominalWidthIn, property, thicknessIn
    )
}

/**
 * The material with any width-dependent tabulated values resolved for this
 * member. Only Southern Pine needs it (NDS Table 4B lists different Fb, Ft
 * and Fc per width); everything else is returned unchanged. Resolves from
 * the table by species + grade + width rather than scaling the material's
 * own values, so it is idempotent and safe to call from more than one place.
 */
internal fun resolveDesignMaterial(
    material: MaterialGrade.Wood,
    profile: SectionProfile
): MaterialGrade.Wood {
    if (material.species != WoodSpecies.SOUTHERN_PINE || profile !is WoodProfile) return material
    val p = WoodPropertyService.getSouthernPineDimensionValues(
        material.grade, profile.nominalDepth.inInches
    ) ?: return material
    return material.copy(
        referenceBending = p.bending,
        referenceShear = p.shear,
        referenceCompressionParallel = p.compressionParallel,
        referenceCompressionPerp = p.compressionPerp,
        referenceTensionParallel = p.tensionParallel
    )
}

/**
 * Emin (psi) for the stability factors CL and CP. Uses the tabulated NDS
 * Emin where the tables have it; otherwise falls back to E / 1.76. That
 * fallback is only a rough stand-in — tabulated sawn-lumber Emin is about
 * 0.37 E, so the fallback overstates it (unconservative for CL / CP), which
 * is why solid-sawn and hardwood-glulam entries carry the real value.
 * Prefers the table's E over the material's own so a stale saved material
 * can't feed a wrong E into the stability check.
 */
internal fun woodEmin(material: MaterialGrade.Wood): Double {
    val table = WoodPropertyService.getReferenceProperties(material.species, material.grade)
    if (table != null) {
        return table.eMin?.inPsi ?: (table.modulusOfElasticity.inPsi / 1.76)
    }
    return material.modulusOfElasticity.inPsi / 1.76
}

/**
 * Everything NDS 3.3.3 derives on the way to CL, so the Design tab's trace
 * can show the development (RB, FbE, F*b) rather than just the answer.
 *
 * @property rb slenderness ratio RB = sqrt(le*d / b^2) (NDS Eq. 3.3-5).
 *   NDS 3.3.3.7 limits RB to 50 — see [rbExceedsLimit].
 * @property eMinAdj E'min = Emin * CM * Ct * Ci (NDS Table 4.3.1).
 * @property fbE critical buckling design value 1.20 E'min / RB^2 (NDS 3.3.3.8).
 * @property fStarB F*b for the CL calculation: Fb times every applicable
 *   adjustment factor except Cfu, CV and CL (NDS 3.3.3.8).
 */
internal data class NdsBeamStabilityDetail(
    val lb: Double,
    val le: Double,
    val d: Double,
    val b: Double,
    val rb: Double,
    val eMinAdj: Double,
    val fbE: Double,
    val fStarB: Double,
    val cl: Double,
    /** Which Table 3.3.3 expression produced [le], e.g. "le = 1.63 lu + 3d". */
    val effectiveLength: NdsEffectiveLength,
    /** True for bending about the strong (x-x) axis, false for flatwise (y-y). */
    val aboutStrongAxis: Boolean = true,
    /** NDS 3.3.3.1: d <= b -> no lateral-torsional buckling, CL = 1.0 exactly. */
    val exemptByShape: Boolean = false
) {
    val rbExceedsLimit: Boolean get() = !exemptByShape && rb > 50.0
}

/**
 * Effective length le of a bending member's compression edge — NDS Table
 * 3.3.3, and the factor that was used so the Design tab can show it.
 *
 * The calculator does not know the load pattern (uniform vs. concentrated,
 * cantilever vs. single span), so it uses the table's footnote 1 — the
 * expressions for "loading conditions not specified in Table 3.3.3":
 *
 *   lu/d <  7          le = 2.06 lu
 *   7 <= lu/d <= 14.3  le = 1.63 lu + 3d
 *   lu/d > 14.3        le = 1.84 lu
 *
 * Those are at or above every specific row of the table for the same lu/d
 * (the largest specific coefficients are 2.06 / 1.63 + 3d / 1.84), so using
 * them is the conservative reading; a known load pattern could give a
 * smaller le (e.g. 1.37 lu + 3d for a single center point load).
 *
 * @param lu unbraced length of the compression edge, inches.
 * @param d  depth of the member in the plane of bending (for flatwise
 *           bending, the narrow dimension), inches.
 */
internal data class NdsEffectiveLength(
    val le: Double,
    /** The multiplier on lu (2.06, 1.63 or 1.84). */
    val coefficient: Double,
    /** Added constant in units of d (3.0 for the 1.63 row, else 0). */
    val dTerm: Double,
    val luOverD: Double,
    val tableReference: String
) {
    /** e.g. "le = 1.63 lu + 3d". */
    val formula: String
        get() = if (dTerm > 0.0) "le = ${coefficient} lu + ${dTerm.toInt()}d" else "le = ${coefficient} lu"
}

internal fun ndsBeamEffectiveLength(lu: Double, d: Double): NdsEffectiveLength {
    val ratio = if (d > 0.0) lu / d else 0.0
    return when {
        ratio < 7.0 -> NdsEffectiveLength(
            2.06 * lu, 2.06, 0.0, ratio, "NDS Table 3.3.3 fn. 1 (lu/d < 7)"
        )
        ratio <= 14.3 -> NdsEffectiveLength(
            1.63 * lu + 3.0 * d, 1.63, 3.0, ratio, "NDS Table 3.3.3 fn. 1 (7 <= lu/d <= 14.3)"
        )
        else -> NdsEffectiveLength(
            1.84 * lu, 1.84, 0.0, ratio, "NDS Table 3.3.3 fn. 1 (lu/d > 14.3)"
        )
    }
}

/**
 * Full NDS 3.3.3 beam-stability development, or null when there's no
 * lateral-torsional buckling check to make (unbraced length not
 * positive, degenerate section, or no usable F*b) — callers treat null
 * as CL = 1.0.
 */
@Suppress("UNUSED_PARAMETER")
internal fun computeNdsBeamStability(
    lb: Double,
    profile: SectionProfile,
    material: MaterialGrade.Wood,
    adjustmentFactors: NdsAdjustmentFactors,
    isGlulam: Boolean,
    aboutStrongAxis: Boolean = true
): NdsBeamStabilityDetail? {
    if (lb <= 0.0) return null

    val designMaterial = resolveDesignMaterial(material, profile)
    val depth = profile.depth.inInches
    val width = if (profile is WoodProfile) profile.dressedWidth.inInches
    else profile.propertiesWeakAxis.s.inIn3 / profile.propertiesWeakAxis.i.inIn4 * 2.0

    if (width <= 0.0) return null

    // "d" is the dimension in the plane of bending and "b" the one across it:
    // edgewise bending about x-x has d = depth, b = width; flatwise bending
    // about y-y swaps them. That swap is what makes CL differ by axis.
    val d = if (aboutStrongAxis) depth else width
    val b = if (aboutStrongAxis) width else depth

    // Effective length le (NDS Table 3.3.3) — see ndsBeamEffectiveLength.
    val effLen = ndsBeamEffectiveLength(lb, d)
    val le = effLen.le

    val rbSquared = le * d / b.pow(2)
    if (rbSquared <= 0.0) return null

    // Critical buckling stress FbE (NDS 3.3.3.8), from the *adjusted* Emin.
    val eMinAdj = woodAdjustedEmin(designMaterial, adjustmentFactors)
    val fbE = 1.20 * eMinAdj / rbSquared

    val fStarB = designMaterial.referenceBending.inPsi *
            adjustmentFactors.cd * adjustmentFactors.cm *
            adjustmentFactors.ct *
            resolveSawnCf(adjustmentFactors, designMaterial, profile, NdsCfProperty.BENDING) *
            adjustmentFactors.ci * adjustmentFactors.cr

    if (fStarB <= 0.0) return null

    val ratio = fbE / fStarB

    // NDS Eq. 3.3-6. The 1.9 and 0.95 are the same for every product —
    // sawn, glulam and structural composite lumber alike (the product-
    // dependent constant c lives in the *column* stability factor CP,
    // NDS 3.7.1, not here). This used to use c = 0.85 (sawn) / 0.90
    // (glulam), which isn't in 3.3-6 at all; it was conservative, but it
    // wasn't the code.
    val term = (1.0 + ratio) / 1.9
    val clRaw = term - sqrt(term.pow(2) - ratio / 0.95)

    // NDS 3.3.3.1: when d <= b the member cannot buckle laterally and
    // CL = 1.0. (Flatwise bending of any rectangular member lands here.)
    val exempt = d <= b

    return NdsBeamStabilityDetail(
        lb = lb, le = le, d = d, b = b, rb = sqrt(rbSquared),
        eMinAdj = eMinAdj, fbE = fbE, fStarB = fStarB,
        cl = if (exempt) 1.0 else clRaw,
        effectiveLength = effLen, aboutStrongAxis = aboutStrongAxis, exemptByShape = exempt
    )
}

/**
 * NDS 3.3.3 Beam Stability Factor CL — single source of truth, shared by
 * [NdsWoodCapacityCalculator] (applies CL to F'b for the bending capacity
 * check) and [NdsClCalculator] (reports CL as the generic
 * [com.lz.solver.bracing.StabilityFactorCalculator] segment factor). Both
 * must agree on the same number for the same inputs. See
 * [computeNdsBeamStability] for the full development.
 *
 * @param lb Unbraced length in inches (0 or negative -> no LTB check, CL = 1.0).
 */
internal fun computeNdsCL(
    lb: Double,
    profile: SectionProfile,
    material: MaterialGrade.Wood,
    adjustmentFactors: NdsAdjustmentFactors,
    isGlulam: Boolean,
    aboutStrongAxis: Boolean = true
): Double = computeNdsBeamStability(lb, profile, material, adjustmentFactors, isGlulam, aboutStrongAxis)?.cl ?: 1.0

/**
 * E'min = Emin x CM x Ct x Ci (NDS Table 4.3.1) — the adjusted stability
 * modulus used in FbE (CL), FcE and CP. Until now CL and CP used the bare
 * tabulated Emin, so a user-entered wet-service / temperature / incising
 * factor never reached the stability checks even though the traces labelled
 * the value E'min.
 */
internal fun woodAdjustedEmin(
    material: MaterialGrade.Wood,
    adjustmentFactors: NdsAdjustmentFactors
): Double = woodEmin(material) * adjustmentFactors.cm * adjustmentFactors.ct * adjustmentFactors.ci

/**
 * Flat use factor Cfu for bending about the member's weak axis (load on
 * the wide face). The caller's explicit [NdsAdjustmentFactors.cfu]
 * override wins when set (non-default); otherwise the tabulated value:
 *
 *  - Sawn lumber (Table 4A, and Southern Pine Table 4B — identical): by
 *    nominal width (the larger dimension) and thickness (2"-3" or 4").
 *  - Glulam (Table 5A / 5C): by the member dimension parallel to the wide
 *    faces — the member width — applying only below 12". Between tabulated
 *    sizes the next larger tabulated dimension's (smaller, conservative)
 *    factor is used.
 */
internal fun resolveFlatUseCfu(
    adjustmentFactors: NdsAdjustmentFactors,
    material: MaterialGrade.Wood,
    profile: SectionProfile
): Double {
    if (adjustmentFactors.cfu != 1.0) return adjustmentFactors.cfu

    if (material.species.isGlulam) {
        val width = if (profile is WoodProfile) profile.dressedWidth.inInches
        else return 1.0
        return when {
            width <= 2.5   -> 1.19
            width <= 3.125 -> 1.16
            width <= 5.125 -> 1.10
            width <= 6.75  -> 1.07
            width <= 8.75  -> 1.04
            width <= 10.75 -> 1.01
            else           -> 1.0
        }
    }

    if (profile !is WoodProfile) return 1.0
    val widthIn = profile.nominalDepth.inInches
    val thick4 = profile.nominalWidth.inInches >= 4.0
    return when {
        widthIn <= 3.0  -> 1.0
        widthIn <= 4.0  -> if (thick4) 1.0 else 1.1
        widthIn <= 5.0  -> if (thick4) 1.05 else 1.1
        widthIn <= 6.0  -> if (thick4) 1.05 else 1.15
        widthIn <= 8.0  -> if (thick4) 1.05 else 1.15
        else            -> if (thick4) 1.1 else 1.2
    }
}
