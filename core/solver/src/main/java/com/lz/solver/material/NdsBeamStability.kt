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
 * NDS 3.3.3 Beam Stability Factor CL — single source of truth.
 *
 * Shared by [NdsWoodCapacityCalculator] (applies CL to F'b for the actual
 * bending capacity check) and [NdsClCalculator] (reports CL as the generic
 * [com.lz.solver.bracing.StabilityFactorCalculator] segment factor, i.e.
 * [com.lz.model.structural.StationDemand.cb]). Both must agree on the same
 * number for the same inputs — computing this in two places previously let
 * them silently drift (see NdsClCalculator, which stubbed CL = 1.0).
 *
 * CL accounts for lateral-torsional buckling of beams. Requires Emin for
 * stability calculations — uses E/1.76 as approximation when Emin is not
 * separately tracked (conservative, per NDS commentary).
 *
 * @param lb Unbraced length in inches (0 or negative → no LTB check, CL = 1.0).
 */
internal fun computeNdsCL(
    lb: Double,
    profile: SectionProfile,
    material: MaterialGrade.Wood,
    adjustmentFactors: NdsAdjustmentFactors,
    isGlulam: Boolean
): Double {
    if (lb <= 0.0) return 1.0

    val designMaterial = resolveDesignMaterial(material, profile)
    val d = profile.depth.inInches
    val b = if (profile is WoodProfile) profile.dressedWidth.inInches
    else profile.propertiesWeakAxis.s.inIn3 / profile.propertiesWeakAxis.i.inIn4 * 2.0

    if (b <= 0.0) return 1.0

    // Effective span length le (NDS Table 3.3.3 — approximate for uniformly loaded)
    val le = 1.63 * lb + 3.0 * d

    val rbSquared = le * d / b.pow(2)
    if (rbSquared <= 0.0) return 1.0

    // Critical buckling stress FbE (NDS 3.3.3)
    val eMin = woodEmin(designMaterial)
    val fbe = 1.20 * eMin / rbSquared

    val fbStar = designMaterial.referenceBending.inPsi *
            adjustmentFactors.cd * adjustmentFactors.cm *
            adjustmentFactors.ct *
            resolveSawnCf(adjustmentFactors, designMaterial, profile, NdsCfProperty.BENDING) *
            adjustmentFactors.ci * adjustmentFactors.cr

    if (fbStar <= 0.0) return 1.0

    val ratio = fbe / fbStar
    val c = if (isGlulam) 0.90 else 0.85 // NDS 3.3.3: c=0.90 glulam, 0.85 sawn

    // NDS Eq. 3.3-6
    val term = (1.0 + ratio) / (2.0 * c)
    return term - sqrt(term.pow(2) - ratio / c)
}
