package com.lz.solver.material

import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.WoodProfile
import com.lz.model.units.inIn3
import com.lz.model.units.inIn4
import com.lz.model.units.inInches
import com.lz.model.units.inPsi
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Resolves the effective NDS Table 4A Size Factor CF — the caller's
 * explicit override if they set one (non-default), otherwise the
 * tabulated lookup by nominal width and grade. Single source of truth so
 * every capacity path that includes CF — bending's F'b, beam stability's
 * F*b (CL, below), compression's F'c and CP's F*c, and tension's F't —
 * agrees on the same value. Previously only bending resolved CF this way;
 * every other path read the raw, un-resolved [NdsAdjustmentFactors.cf]
 * directly (1.0 unless the user manually overrode it), silently ignoring
 * the tabulated size factor and understating/overstating capacity for any
 * sawn member where the true CF isn't 1.0 — and, for CL specifically,
 * corrupting the beam-stability factor that bending's own F'b then
 * multiplies by, compounding the error.
 *
 * Glulam never uses CF (bending substitutes CV per NDS 5.3.6; axial
 * NDS Table 5A values already reflect full-size laminated members) —
 * glulam [com.lz.model.structural.WoodGrade] values don't match any case
 * in [NdsWoodCapacityCalculator.ndsTable4ASizeFactor]'s `when`, so they
 * fall through to its `else -> 1.0`. Safe to call unconditionally.
 */
internal fun resolveSawnCf(
    adjustmentFactors: NdsAdjustmentFactors,
    material: MaterialGrade.Wood,
    profile: SectionProfile
): Double {
    if (adjustmentFactors.cf != 1.0) return adjustmentFactors.cf
    val nominalWidthIn =
        if (profile is WoodProfile) profile.nominalDepth.inInches else profile.depth.inInches
    return NdsWoodCapacityCalculator.ndsTable4ASizeFactor(material.grade, nominalWidthIn)
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

    val d = profile.depth.inInches
    val b = if (profile is WoodProfile) profile.dressedWidth.inInches
    else profile.propertiesWeakAxis.s.inIn3 / profile.propertiesWeakAxis.i.inIn4 * 2.0

    if (b <= 0.0) return 1.0

    // Effective span length le (NDS Table 3.3.3 — approximate for uniformly loaded)
    val le = 1.63 * lb + 3.0 * d

    val rbSquared = le * d / b.pow(2)
    if (rbSquared <= 0.0) return 1.0

    // Critical buckling stress FbE (NDS 3.3.3)
    val eMin = material.modulusOfElasticity.inPsi / 1.76 // Approx Emin = E / 1.76
    val fbe = 1.20 * eMin / rbSquared

    val fbStar = material.referenceBending.inPsi *
            adjustmentFactors.cd * adjustmentFactors.cm *
            adjustmentFactors.ct * resolveSawnCf(adjustmentFactors, material, profile) *
            adjustmentFactors.ci * adjustmentFactors.cr

    if (fbStar <= 0.0) return 1.0

    val ratio = fbe / fbStar
    val c = if (isGlulam) 0.90 else 0.85 // NDS 3.3.3: c=0.90 glulam, 0.85 sawn

    // NDS Eq. 3.3-6
    val term = (1.0 + ratio) / (2.0 * c)
    return term - sqrt(term.pow(2) - ratio / c)
}
