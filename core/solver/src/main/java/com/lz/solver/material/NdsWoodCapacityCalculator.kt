package com.lz.solver.material

import com.lz.model.regulatory.aisc.DesignFactor
import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.Flange
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.ShapeType
import com.lz.model.structural.StationDemand
import com.lz.model.structural.StrengthCheckResult
import com.lz.model.structural.StrengthDesignResult
import com.lz.model.structural.WoodProfile
import com.lz.model.structural.WoodSpecies
import com.lz.model.units.Force
import com.lz.model.units.Moment
import com.lz.model.units.inIn2
import com.lz.model.units.inIn3
import com.lz.model.units.inIn4
import com.lz.model.units.inInches
import com.lz.model.units.inPsi
import com.lz.solver.capacity.CapacityCalculator
import com.lz.solver.capacity.DesignFactorSet
import com.lz.solver.capacity.RawCapacityResult
import java.util.Locale
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * NDS 2018 capacity calculator for wood structural members.
 *
 * Covers:
 *   Bending     — NDS 3.3: F'b = Fb * CD * CM * Ct * CL * CF * Cfu * Ci * Cr
 *                 Glulam:  CV replaces CF; lesser of CV and CL governs (NDS 5.3.6)
 *   Shear       — NDS 3.4: F'v = Fv * CD * CM * Ct * Ci
 *   Compression — NDS 3.7: F'c = Fc * CD * CM * Ct * CF * Ci * CP
 *                 CP computed from slenderness ratio (NDS 3.7.1)
 *   Tension     — NDS 3.8: F't = Ft * CD * CM * Ct * CF * Ci
 *   Deflection  — serviceability limit from demand
 *
 * phi/omega factors are NOT applied here for the [evaluate] path —
 * [CapacityEngine] owns that step. [evaluateDetailed] applies NDS
 * ASD factors directly for report output (NDS is inherently ASD;
 * LRFD format factor lambda is applied when methodology is LRFD).
 *
 * Member type is inferred from [WoodSpecies.isGlulam]:
 *   isGlulam = true  → glulam provisions (CV, NDS Chapter 5)
 *   isGlulam = false → sawn lumber / SCL provisions (CF, NDS Chapter 4)
 */
class NdsWoodCapacityCalculator(
    private val profile: SectionProfile,
    private val material: MaterialGrade.Wood,
    private val adjustmentFactors: NdsAdjustmentFactors = NdsAdjustmentFactors(),
    /**
     * Resolved NDS edition (from the active project's BuildingCode ->
     * Standard -> StandardEdition.Nds chain). Not currently used to vary
     * any factor here — NDS 2015/2018/2024 share the same values for
     * everything this calculator models (adjustment factors, phi, lambda).
     * Carried through purely for report citation traceability; if a future
     * edition genuinely changes one of these values, branch on [edition]
     * at that point rather than assuming it stays inert.
     */
    private val edition: NdsEdition = NdsEdition.NDS_2018
) : CapacityCalculator {

    private val isGlulam: Boolean = material.species.isGlulam

    // ------------------------------------------------------------------
    // CapacityCalculator contract
    // ------------------------------------------------------------------

    override fun evaluate(demand: StationDemand): RawCapacityResult {
        val lb = if (demand.compressionFlange == Flange.TOP)
            demand.lbTop.inInches else demand.lbBottom.inInches
        val luAxial = lb  // Unbraced length for column stability

        val isAxialTension = demand.axial.pounds >= 0.0

        val (nomMn, lsMn) = calculateBending(lb)
        val (nomVn, lsVn) = calculateShear()
        val (nomPn, lsPn) = if (isAxialTension)
            calculateTension()
        else
            calculateCompression(luAxial)

        return RawCapacityResult(
            nominalFlexureX      = nomMn,
            limitStateFlexureX   = lsMn,
            nominalFlexureY      = 0.0,
            limitStateFlexureY   = "N/A — Weak axis not checked for wood",
            nominalShearX        = nomVn,
            limitStateShearX     = lsVn,
            nominalShearY        = 0.0,
            limitStateShearY     = "N/A",
            nominalAxial         = nomPn,
            limitStateAxial      = lsPn,
            nominalTorsion       = 0.0,
            limitStateTorsion    = "N/A — Torsion not codified for wood members",
            allowableDeflection  = demand.allowableDeflection.inInches,
            limitStateDeflection = "Deflection Limit"
        )
    }

    override fun evaluateDetailed(
        demand: StationDemand,
        methodology: DesignMethodology
    ): StrengthDesignResult {
        val lb = if (demand.compressionFlange == Flange.TOP)
            demand.lbTop.inInches else demand.lbBottom.inInches
        val luAxial = lb
        val isAxialTension = demand.axial.pounds >= 0.0

        val (nomMn, lsMn) = calculateBending(lb)
        val (nomVn, lsVn) = calculateShear()
        val (nomPn, lsPn) = if (isAxialTension)
            calculateTension()
        else
            calculateCompression(luAxial)

        // NDS is inherently ASD. For LRFD, apply format conversion factor
        // lambda per NDS Appendix N (lambda = 0.8 for occupancy live, snow,
        // roof live; 1.0 for wind/seismic). Conservative default: 0.8.
        val lambda = if (methodology == DesignMethodology.LRFD) 0.8 else 1.0

        // ASD: design value = adjusted F' * S (already in nominals)
        // LRFD: design value = lambda * phi * F'n * S
        val phiBending     = 0.85
        val phiShear       = 0.75
        val phiCompression = 0.90
        val phiTension     = 0.80

        val designMn = if (methodology == DesignMethodology.LRFD)
            lambda * phiBending * nomMn else nomMn
        val designVn = if (methodology == DesignMethodology.LRFD)
            lambda * phiShear * nomVn else nomVn
        val designPn = if (methodology == DesignMethodology.LRFD)
            lambda * (if (isAxialTension) phiTension else phiCompression) * nomPn
        else nomPn

        val ratioMn = if (designMn > 0) demand.moment.lbIn / designMn else 0.0
        val ratioVn = if (designVn > 0) demand.shear.pounds / designVn else 0.0
        val ratioPn = if (designPn > 0) demand.axial.pounds / designPn else 0.0

        val cl = computeCL(lb)
        val cv = if (isGlulam) computeCV(lb) else 1.0

        return StrengthDesignResult(
            momentCheck = StrengthCheckResult(
                demand               = demand.moment,
                capacity             = Moment(designMn),
                utilization          = ratioMn,
                governingCombination = "Current",
                governingMode        = lsMn
            ),
            shearCheck = StrengthCheckResult(
                demand               = demand.shear,
                capacity             = Force(designVn),
                utilization          = ratioVn,
                governingCombination = "Current",
                governingMode        = lsVn
            ),
            axialCheck = StrengthCheckResult(
                demand               = demand.axial,
                capacity             = Force(designPn),
                utilization          = ratioPn,
                governingCombination = "Current",
                governingMode        = lsPn
            ),
            torsionCheck = StrengthCheckResult(
                demand               = demand.torque,
                capacity             = Moment(0.0),
                utilization          = 0.0,
                governingCombination = "N/A",
                governingMode        = "Torsion not codified for wood"
            ),
            methodology      = methodology,
            designParameters = buildMap {
                put("Member Type",  if (isGlulam) "Glulam" else "Sawn Lumber / SCL")
                put("Species",      material.species.name)
                put("Grade",        material.grade.name)
                put("Lu (bending)", String.format(Locale.US, "%.1f in", lb))
                put("CL",           String.format(Locale.US, "%.3f", cl))
                if (isGlulam) put("CV", String.format(Locale.US, "%.3f", cv))
                put("CD",           String.format(Locale.US, "%.2f", adjustmentFactors.cd))
                put("CM",           String.format(Locale.US, "%.2f", adjustmentFactors.cm))
                put("Ct",           String.format(Locale.US, "%.2f", adjustmentFactors.ct))
                put("Cr",           String.format(Locale.US, "%.2f", adjustmentFactors.cr))
                if (methodology == DesignMethodology.LRFD)
                    put("Lambda", String.format(Locale.US, "%.2f", lambda))
            }
        )
    }

    /**
     * NDS is inherently ASD: [evaluate]'s nominal values already ARE the
     * fully code-adjusted allowable capacity (F' * S, with CD/CM/Ct/CL/CF/
     * etc. baked in). So for ASD, `value = 1.0` here — CapacityEngine will
     * divide by 1.0, i.e. use the nominal as-is. Dividing by anything else
     * would double-count NDS's own adjustment factors.
     *
     * For LRFD, reproduces exactly the same lambda*phi logic already used in
     * [evaluateDetailed] (NDS Appendix N format conversion). Folding lambda
     * into the returned factor's `value` lets [DesignFactorSet.apply] use
     * the same `nominal * value` shape as the AISC LRFD case.
     */
    override fun designFactors(methodology: DesignMethodology): DesignFactorSet {
        if (methodology == DesignMethodology.ASD) {
            val one = DesignFactor(1.0, "NDS ASD — nominal is already the adjusted allowable capacity")
            return DesignFactorSet(
                methodology       = methodology,
                flexure           = one,
                shear             = one,
                axialTension      = one,
                axialCompression  = one,
                torsion           = one
            )
        }

        // LRFD — NDS Appendix N. lambda = 0.8 for occupancy live/snow/roof
        // live, 1.0 for wind/seismic; 0.8 used as the conservative default,
        // matching evaluateDetailed().
        val lambda = 0.8
        val phiBending     = 0.85
        val phiShear       = 0.75
        val phiCompression = 0.90
        val phiTension     = 0.80

        return DesignFactorSet(
            methodology       = methodology,
            flexure           = DesignFactor(lambda * phiBending,     "NDS Appendix N — Kf/phi (bending)"),
            shear             = DesignFactor(lambda * phiShear,       "NDS Appendix N — Kf/phi (shear)"),
            axialTension      = DesignFactor(lambda * phiTension,     "NDS Appendix N — Kf/phi (tension)"),
            axialCompression  = DesignFactor(lambda * phiCompression, "NDS Appendix N — Kf/phi (compression)"),
            torsion           = DesignFactor(1.0, "Torsion not codified for wood")
        )
    }

    // ------------------------------------------------------------------
    // NDS 3.3 — Bending
    // ------------------------------------------------------------------

    /**
     * Computes adjusted bending capacity F'b * S.
     *
     * Sawn lumber: F'b = Fb * CD * CM * Ct * CL * CF * Cfu * Ci * Cr
     * Glulam:      F'b = Fb * CD * CM * Ct * min(CV, CL) * Cfu * Ci
     *              NDS 5.3.6: CV and CL are not applied simultaneously.
     */
    private fun calculateBending(lb: Double): Pair<Double, String> {
        val fb = material.referenceBending.inPsi
        val sx = profile.propertiesStrongAxis.s.inIn3

        val cl = computeCL(lb)

        val fbAdj: Double
        val limitState: String

        if (isGlulam) {
            val cv = computeCV(lb)
            // NDS 5.3.6: use lesser of CV and CL
            val stabilityFactor = min(cl, cv)
            val f = adjustmentFactors
            fbAdj = fb * f.cd * f.cm * f.ct * stabilityFactor * f.cfu * f.ci
            limitState = if (cv <= cl)
                "Bending — Volume Factor CV (NDS 5.3.6)"
            else
                "Bending — Beam Stability CL (NDS 3.3.3)"
        } else {
            // Sawn lumber / SCL: CF from adjustmentFactors (caller must set it)
            val cfEffective = computeSawnCF()
            val f = adjustmentFactors
            fbAdj = fb * f.cd * f.cm * f.ct * cl * cfEffective * f.cfu * f.ci * f.cr
            limitState = if (cl < 1.0)
                "Bending — Beam Stability CL (NDS 3.3.3)"
            else
                "Bending — Size Factor CF (NDS Table 4A)"
        }

        return (fbAdj * sx) to limitState
    }

    /**
     * NDS 3.3.3 Beam Stability Factor CL.
     * Delegates to [computeNdsCL] — see that function for the formula and
     * why it's shared with [NdsClCalculator].
     */
    private fun computeCL(lb: Double): Double =
        computeNdsCL(lb, profile, material, adjustmentFactors, isGlulam)

    /**
     * NDS 5.3.6 Volume Factor CV for glulam.
     * CV = (21/L)^(1/x) * (12/d)^(1/x) * (5.125/b)^(1/x) <= 1.0
     * x = 10 for western species, 20 for southern pine glulam.
     */
    private fun computeCV(spanLength: Double): Double {
        if (!isGlulam || spanLength <= 0.0) return 1.0

        val d = profile.depth.inInches
        val b = if (profile is WoodProfile) profile.dressedWidth.inInches else 5.125
        val l = spanLength / 12.0  // Convert inches to feet for NDS formula

        if (l <= 0.0 || d <= 0.0 || b <= 0.0) return 1.0

        val x = if (material.species == WoodSpecies.GLULAM_SP) 20.0 else 10.0

        val cv = (21.0 / l).pow(1.0 / x) *
                (12.0 / d).pow(1.0 / x) *
                (5.125 / b).pow(1.0 / x)

        return min(cv, 1.0)
    }

    /**
     * NDS Table 4A Size Factor CF for sawn lumber.
     * CF applies when d > 12 inches for bending.
     * CF = (12/d)^(1/9)
     * Returns adjustmentFactors.cf if explicitly set (non-default),
     * otherwise computes from section depth.
     */
    private fun computeSawnCF(): Double {
        // If caller explicitly set CF, use it
        if (adjustmentFactors.cf != 1.0) return adjustmentFactors.cf

        val d = profile.depth.inInches
        if (d <= 12.0) return 1.0
        return (12.0 / d).pow(1.0 / 9.0)
    }

    // ------------------------------------------------------------------
    // NDS 3.4 — Shear
    // ------------------------------------------------------------------

    /**
     * NDS 3.4 horizontal shear capacity.
     * V'n = F'v * A * (2/3) for rectangular sections.
     * The 2/3 factor accounts for the parabolic shear distribution
     * in rectangular cross-sections (NDS 3.4.2).
     */
    private fun calculateShear(): Pair<Double, String> {
        val fvAdj = adjustmentFactors.adjustedShear(material.referenceShear.inPsi)
        val area  = profile.area.inIn2

        val vn = (2.0 / 3.0) * fvAdj * area
        return vn to "Shear (NDS 3.4)"
    }

    // ------------------------------------------------------------------
    // NDS 3.7 — Compression Parallel to Grain
    // ------------------------------------------------------------------

    /**
     * NDS 3.7 compression capacity including column stability factor CP.
     * F'c = Fc * CD * CM * Ct * CF * Ci * CP
     * CP computed from NDS 3.7.1 using slenderness ratio le/d.
     */
    private fun calculateCompression(lu: Double): Pair<Double, String> {
        val fc   = material.referenceCompressionParallel.inPsi
        val area = profile.area.inIn2

        val cp = computeCP(lu)

        val fcAdj = fc * adjustmentFactors.cd * adjustmentFactors.cm *
                adjustmentFactors.ct * adjustmentFactors.cf *
                adjustmentFactors.ci * cp

        return (fcAdj * area) to "Compression (NDS 3.7, CP=${String.format(Locale.US, "%.3f", cp)})"
    }

    /**
     * NDS 3.7.1 Column Stability Factor CP.
     * CP = (1 + ratio) / (2c) - sqrt(((1 + ratio) / (2c))^2 - ratio/c)
     * where ratio = FcE / F*c, c = 0.8 (sawn) or 0.9 (glulam/SCL)
     */
    private fun computeCP(lu: Double): Double {
        if (lu <= 0.0) return 1.0

        val d = profile.depth.inInches
        val b = if (profile is WoodProfile) profile.dressedWidth.inInches
        else sqrt(profile.area.inIn2)

        // NDS 3.7.1: slenderness ratio le/d, must not exceed 50
        val le    = lu  // Effective length — caller responsible for ke * lu if needed
        val slenderness = le / minOf(d, b)
        if (slenderness > 50.0) return 0.0  // Exceeds NDS maximum — member inadequate

        // Critical buckling stress FcE (NDS 3.7.1)
        val eMin      = material.modulusOfElasticity.inPsi / 1.76
        val fce       = 0.822 * eMin / slenderness.pow(2)

        // F*c = Fc with all adjustments except CP
        val fcStar    = material.referenceCompressionParallel.inPsi *
                adjustmentFactors.cd * adjustmentFactors.cm *
                adjustmentFactors.ct * adjustmentFactors.cf * adjustmentFactors.ci

        if (fcStar <= 0.0) return 1.0

        val ratio = fce / fcStar
        val c     = if (isGlulam) 0.90 else 0.80

        val term = (1.0 + ratio) / (2.0 * c)
        return term - sqrt(term.pow(2) - ratio / c)
    }

    // ------------------------------------------------------------------
    // NDS 3.8 — Tension Parallel to Grain
    // ------------------------------------------------------------------

    /**
     * NDS 3.8 tension capacity.
     * F't = Ft * CD * CM * Ct * CF * Ci
     * Net section area reduction for connections is a connection-level
     * check and not applied here.
     */
    private fun calculateTension(): Pair<Double, String> {
        val ftAdj = adjustmentFactors.adjustedTension(
            material.referenceTensionParallel.inPsi
        )
        val area = profile.area.inIn2
        return (ftAdj * area) to "Tension (NDS 3.8)"
    }
}