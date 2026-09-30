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
import com.lz.model.structural.WoodGrade
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

    /**
     * [material] with any member-width-dependent values (Southern Pine,
     * NDS Table 4B) resolved for this profile. All capacity math below
     * reads reference values from here, not from [material] directly.
     */
    private val designMaterial: MaterialGrade.Wood = resolveDesignMaterial(material, profile)

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
        val fb = designMaterial.referenceBending.inPsi
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
            val cfEffective = computeSawnCF(NdsCfProperty.BENDING)
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
        computeNdsCL(lb, profile, designMaterial, adjustmentFactors, isGlulam)

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

        val x = if (material.species == WoodSpecies.GLULAM_SP_SP) 20.0 else 10.0

        val cv = (21.0 / l).pow(1.0 / x) *
                (12.0 / d).pow(1.0 / x) *
                (5.125 / b).pow(1.0 / x)

        return min(cv, 1.0)
    }

    /**
     * Size factor CF for sawn dimension lumber, looked up by nominal width,
     * thickness and grade — NOT the continuous `(12/d)^(1/9)` formula, which
     * is Table 4D's timber (5"x5" and larger) formula. Table 4A gives Fb,
     * Ft and Fc *separate* schedules (Fc's is much smaller than Fb's, and
     * Fb differs again for 4"-thick members), so each capacity path asks for
     * its own [property]. Southern Pine (Table 4B) has size built into its
     * per-width tabulated values instead. Delegates to [resolveSawnCf].
     */
    private fun computeSawnCF(property: NdsCfProperty): Double =
        resolveSawnCf(adjustmentFactors, designMaterial, profile, property)

    companion object {
        /**
         * NDS 2018 Supplement Table 4A "Size Factors, CF" for dimension
         * lumber 2"-4" thick (all species except Southern Pine, whose
         * Table 4B builds size into the tabulated values).
         *
         * The table has separate columns: Fb for 2"-3" thick, Fb for 4"
         * thick, Ft, and Fc. Ft matches Fb(2"-3"); Fc has its own, smaller
         * schedule. [nominalThicknessIn] only matters for Fb (>= 4 selects
         * the 4"-thick column).
         *
         * Grade groups per the table: Select Structural / No.1 & Btr /
         * No.1 / No.2 / No.3 share one schedule; Stud has its own (and
         * defers to the No.3 schedule at 8" and wider); Construction and
         * Standard are 1.0; Utility is 1.0 at 4" wide and 0.4 (Fb, Ft) /
         * 0.6 (Fc) at 2"-3".
         */
        fun ndsTable4ASizeFactor(
            grade: WoodGrade,
            nominalWidthIn: Double,
            property: NdsCfProperty = NdsCfProperty.BENDING,
            nominalThicknessIn: Double = 2.0
        ): Double {
            fun selectStructuralGroupCf(width: Double): Double = when (property) {
                NdsCfProperty.COMPRESSION -> when {
                    width <= 4.0 -> 1.15
                    width <= 6.0 -> 1.1
                    width <= 8.0 -> 1.05
                    width <= 12.0 -> 1.0
                    else -> 0.9
                }
                NdsCfProperty.BENDING ->
                    if (nominalThicknessIn >= 4.0) when {
                        width <= 4.0 -> 1.5
                        width <= 5.0 -> 1.4
                        width <= 6.0 -> 1.3
                        width <= 8.0 -> 1.3
                        width <= 10.0 -> 1.2
                        width <= 12.0 -> 1.1
                        else -> 1.0
                    } else when {
                        width <= 4.0 -> 1.5
                        width <= 5.0 -> 1.4
                        width <= 6.0 -> 1.3
                        width <= 8.0 -> 1.2
                        width <= 10.0 -> 1.1
                        width <= 12.0 -> 1.0
                        else -> 0.9
                    }
                NdsCfProperty.TENSION -> when {
                    width <= 4.0 -> 1.5
                    width <= 5.0 -> 1.4
                    width <= 6.0 -> 1.3
                    width <= 8.0 -> 1.2
                    width <= 10.0 -> 1.1
                    width <= 12.0 -> 1.0
                    else -> 0.9
                }
            }
            return when (grade) {
                WoodGrade.SELECT_STRUCTURAL, WoodGrade.NO_1, WoodGrade.NO_2, WoodGrade.NO_3 ->
                    selectStructuralGroupCf(nominalWidthIn)
                WoodGrade.STUD -> when {
                    nominalWidthIn <= 4.0 ->
                        if (property == NdsCfProperty.COMPRESSION) 1.05 else 1.1
                    nominalWidthIn <= 6.0 -> 1.0
                    // "8\" & wider: Use No.3 Grade tabulated design values
                    // and size factors" — NDS Table 4A.
                    else -> selectStructuralGroupCf(nominalWidthIn)
                }
                WoodGrade.CONSTRUCTION, WoodGrade.STANDARD -> 1.0
                WoodGrade.UTILITY ->
                    if (nominalWidthIn <= 3.0) {
                        if (property == NdsCfProperty.COMPRESSION) 0.6 else 0.4
                    } else 1.0
                // Glulam grades (both the legacy MOE-style names and the
                // newer NDS Table 5A combination symbols) never reach this
                // path — glulam has no CF — but return 1.0 rather than
                // throw if ever called with one.
                else -> 1.0
            }
        }
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
        val fvAdj = adjustmentFactors.adjustedShear(designMaterial.referenceShear.inPsi)
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
        val fc   = designMaterial.referenceCompressionParallel.inPsi
        val area = profile.area.inIn2

        val cp = computeCP(lu)

        val fcAdj = fc * adjustmentFactors.cd * adjustmentFactors.cm *
                adjustmentFactors.ct * computeSawnCF(NdsCfProperty.COMPRESSION) *
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
        val eMin      = woodEmin(designMaterial)
        val fce       = 0.822 * eMin / slenderness.pow(2)

        // F*c = Fc with all adjustments except CP
        val fcStar    = designMaterial.referenceCompressionParallel.inPsi *
                adjustmentFactors.cd * adjustmentFactors.cm *
                adjustmentFactors.ct * computeSawnCF(NdsCfProperty.COMPRESSION) * adjustmentFactors.ci

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
     * check and not applied here. Computed inline (not via
     * [NdsAdjustmentFactors.adjustedTension]) because that helper uses
     * the raw, un-resolved CF field — see [resolveSawnCf].
     */
    private fun calculateTension(): Pair<Double, String> {
        val ft = designMaterial.referenceTensionParallel.inPsi
        val ftAdj = ft * adjustmentFactors.cd * adjustmentFactors.cm *
                adjustmentFactors.ct * computeSawnCF(NdsCfProperty.TENSION) * adjustmentFactors.ci
        val area = profile.area.inIn2
        return (ftAdj * area) to "Tension (NDS 3.8)"
    }
}