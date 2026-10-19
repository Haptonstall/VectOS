package com.lz.solver.material

import com.lz.model.regulatory.aisc.DesignFactor
import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.structural.CodeCheck
import com.lz.model.structural.CombinedStressCheck
import com.lz.model.structural.DesignEquationTrace
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.Flange
import com.lz.model.structural.InteractionEquationResult
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
import com.lz.model.units.inLbIn
import com.lz.model.units.inPoundsForce
import com.lz.model.units.inPsi
import com.lz.solver.capacity.CapacityCalculator
import com.lz.solver.capacity.DesignFactorSet
import com.lz.solver.capacity.RawCapacityResult
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
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
 *   Weak axis   — flatwise bending about y-y (Sy, Cfu; CL = 1.0; glulam Fby)
 *   Combined    — NDS 3.9.1 bending + axial tension (Eq. 3.9-1, 3.9-2);
 *                 NDS 3.9.2 bending + axial compression and biaxial
 *                 bending (Eq. 3.9-3, 3.9-4) — [evaluateDetailed] only
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
    private val edition: NdsEdition = NdsEdition.NDS_2018,
    /**
     * Which principal axis the member is oriented to bend about — the
     * Design tab's strong/weak toggle. The analysis already swaps the
     * stiffness (Ix <-> Iy) for the toggle, so [StationDemand.moment] is the
     * moment about *this* axis; capacity must come from the same axis.
     * Previously this calculator always used Sx, so a member flipped to
     * weak-axis bending was analysed with Iy but checked against the
     * strong-axis section modulus (the same gap the steel calculator had).
     * True = edgewise / strong axis (x-x, NDS fb1); false = flatwise / weak
     * axis (y-y, NDS fb2).
     */
    private val memberIsStrongAxis: Boolean = true
) : CapacityCalculator {

    private val isGlulam: Boolean = material.species.isGlulam

    /**
     * [material] with any member-width-dependent values (Southern Pine,
     * NDS Table 4B) resolved for this profile. All capacity math below
     * reads reference values from here, not from [material] directly.
     */
    private val designMaterial: MaterialGrade.Wood = resolveDesignMaterial(material, profile)

    /**
     * Mirrors [AiscSteelCapacityCalculator]'s own private result wrapper —
     * same shape, same reasoning: a nominal/limit-state pair for the
     * [evaluate] path (CapacityEngine's point-by-point diagram, which
     * doesn't need traces) that also carries optional [DesignEquationTrace]s
     * for [evaluateDetailed]'s Design tab card. Destructuring
     * (`val (nominal, limitState) = ...`) still works unchanged against
     * this three-component class, since Kotlin destructuring only binds as
     * many components as the left side asks for.
     */
    private data class LimitStateResult(
        val nominal: Double,
        val limitState: String,
        val traces: List<DesignEquationTrace> = emptyList()
    )

    private fun fmt(v: Double, d: Int = 2): String = String.format(Locale.US, "%.${d}f", v)
    private fun kipFt(lbIn: Double): String = fmt(lbIn / 12000.0, 2)
    private fun kips(lb: Double): String = fmt(lb / 1000.0, 2)

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

        // The capacity about the *other* principal axis, checked against
        // StationDemand.momentY. (It used to be a flat "N/A — weak axis not
        // checked for wood", so out-of-plane moment never reached any check.)
        val secondaryAxis = calculateBendingAbout(!memberIsStrongAxis, lb)

        return RawCapacityResult(
            nominalFlexureX      = nomMn,
            limitStateFlexureX   = lsMn,
            nominalFlexureY      = secondaryAxis.nominal,
            limitStateFlexureY   = secondaryAxis.limitState,
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

        val (nomMn, lsMn, tracesMn) = calculateBending(lb)
        val (nomVn, lsVn, tracesVn) = calculateShear()
        val (nomPn, lsPn, tracesPn) = if (isAxialTension)
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

        // Utilization is always demand vs. capacity as magnitudes — the
        // signed value only matters upstream (e.g. isAxialTension, or
        // which diagram side a moment plots on), never for the check
        // itself. This was the bug behind negative utilization ratios.
        //
        // Zero capacity with real demand is a FAILURE, not a pass: it is what
        // the NDS slenderness limits produce (RB > 50 in 3.3.3.7, le/d > 50 in
        // 3.7.1.4 both zero the capacity), and a plain `demand / 0` guard
        // returning 0.0 showed such a member as 0.00 utilized.
        val ratioMn = utilizationRatio(abs(demand.moment.lbIn), designMn)
        val ratioVn = utilizationRatio(abs(demand.shear.pounds), designVn)
        val ratioPn = utilizationRatio(abs(demand.axial.pounds), designPn)

        val kBend = if (methodology == DesignMethodology.LRFD) lambda * phiBending else 1.0
        val kTens = if (methodology == DesignMethodology.LRFD) lambda * phiTension else 1.0
        val kComp = if (methodology == DesignMethodology.LRFD) lambda * phiCompression else 1.0

        // Moment about the other principal axis (StationDemand.momentY), when present.
        val secondaryDemand = abs(demand.momentY.lbIn)
        val secondaryCheck = if (secondaryDemand > 0.0) {
            val sec = calculateBendingAbout(!memberIsStrongAxis, lb)
            val designMnSec = if (methodology == DesignMethodology.LRFD)
                lambda * phiBending * sec.nominal else sec.nominal
            StrengthCheckResult(
                demand               = Moment(secondaryDemand),
                capacity             = Moment(designMnSec),
                utilization          = utilizationRatio(secondaryDemand, designMnSec),
                governingCombination = "Current",
                governingMode        = sec.limitState,
                traces               = sec.traces
            )
        } else null

        val combinedChecks = buildCombinedChecks(
            demand = demand, lb = lb, luAxial = luAxial,
            kBend = kBend, kTens = kTens, kComp = kComp,
            isLrfd = methodology == DesignMethodology.LRFD
        )

        val cl = computeCL(lb)
        val cv = if (isGlulam) computeCV(lb) else 1.0

        return StrengthDesignResult(
            momentCheck = StrengthCheckResult(
                // Stored as a magnitude, not the signed envelope value — a
                // demand/capacity check always compares |demand| to
                // capacity, so a negative Demand on the Design tab reads as
                // wrong regardless of which diagram side it came from.
                demand               = Moment(abs(demand.moment.lbIn)),
                capacity             = Moment(designMn),
                utilization          = ratioMn,
                governingCombination = "Current",
                governingMode        = lsMn,
                traces               = tracesMn
            ),
            shearCheck = StrengthCheckResult(
                demand               = Force(abs(demand.shear.pounds)),
                capacity             = Force(designVn),
                utilization          = ratioVn,
                governingCombination = "Current",
                governingMode        = lsVn,
                traces               = tracesVn
            ),
            axialCheck = StrengthCheckResult(
                demand               = Force(abs(demand.axial.pounds)),
                capacity             = Force(designPn),
                utilization          = ratioPn,
                governingCombination = "Current",
                governingMode        = lsPn,
                traces               = tracesPn
            ),
            torsionCheck = StrengthCheckResult(
                demand               = Moment(abs(demand.torque.lbIn)),
                capacity             = Moment(0.0),
                utilization          = 0.0,
                governingCombination = "N/A",
                governingMode        = "Torsion not codified for wood"
            ),
            methodology      = methodology,
            secondaryMomentCheck = secondaryCheck,
            combinedChecks   = combinedChecks,
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
     * Everything one axis's bending check needs, built once so the capacity
     * check (F'b x S), the NDS 3.9.1 tension interaction (which needs F*b
     * and F**b) and the NDS 3.9.2 compression / biaxial interaction (which
     * needs F'b1 and F'b2) all read a single development of the adjustment
     * factors instead of each rebuilding it.
     *
     * @property s section modulus about this axis (in^3).
     * @property fbAdj F'b — every applicable factor (the lesser of CL and CV
     *   for glulam strong axis; CL = 1.0 about the weak axis).
     * @property fStar NDS 3.9.1's F*b: all factors except CL (so CV stays in).
     * @property fStarStar NDS 3.9.1's F**b: all factors except CV (so CL stays in).
     * @property unavailableReason non-null when this axis can't be checked
     *   (glulam weak axis with no sourced Fby) — capacity is then reported
     *   as zero with this reason as the limit state, never a guessed value.
     */
    private data class BendingAxis(
        val strong: Boolean,
        val s: Double,
        val fbAdj: Double,
        val fStar: Double,
        val fStarStar: Double,
        val cl: Double,
        val rbExceedsLimit: Boolean,
        val limitState: String,
        val traces: List<DesignEquationTrace>,
        val fStarTrace: DesignEquationTrace? = null,
        val fStarStarTrace: DesignEquationTrace? = null,
        val unavailableReason: String? = null
    ) {
        val available: Boolean get() = unavailableReason == null
    }

    private fun bendingAxis(strong: Boolean, lb: Double): BendingAxis =
        if (strong) strongBendingAxis(lb) else weakBendingAxis()

    /**
     * Strong-axis (edgewise, x-x) bending — NDS 3.3.
     *
     * Sawn lumber: F'b = Fb * CD * CM * Ct * CL * CF * Cfu * Ci * Cr
     * Glulam:      F'b = Fb * CD * CM * Ct * min(CV, CL) * Cfu * Ci
     *              NDS 5.3.6: CV and CL are not applied simultaneously.
     */
    private fun strongBendingAxis(lb: Double): BendingAxis {
        val fb = designMaterial.referenceBending.inPsi
        val sx = profile.propertiesStrongAxis.s.inIn3
        val f = adjustmentFactors

        val stab = computeNdsBeamStability(lb, profile, designMaterial, f, isGlulam)
        val cl = stab?.cl ?: 1.0

        val traces = mutableListOf<DesignEquationTrace>()
        if (stab != null) {
            val cfStab = computeSawnCF(NdsCfProperty.BENDING)
            val ratio = stab.fbE / stab.fStarB
            traces += DesignEquationTrace(
                symbolicEquation = "RB = sqrt(le x d / b^2)",
                substitutedEquation = "RB = sqrt(${fmt(stab.le)} x ${fmt(stab.d)} / ${fmt(stab.b)}^2)",
                result = fmt(stab.rb, 2) + if (stab.rbExceedsLimit) " (exceeds 50 — NDS 3.3.3.7)" else "",
                units = "",
                codeReference = "NDS 3.3.3.6, Eq. 3.3-5",
                variables = mapOf("le" to stab.le, "d" to stab.d, "b" to stab.b)
            )
            traces += DesignEquationTrace(
                symbolicEquation = "FbE = 1.20 E'min / RB^2",
                substitutedEquation = "FbE = 1.20 x ${fmt(stab.eMinAdj, 0)} / ${fmt(stab.rb, 2)}^2",
                result = fmt(stab.fbE, 0),
                units = "psi",
                codeReference = "NDS 3.3.3.8",
                variables = mapOf("E'min" to stab.eMinAdj, "RB" to stab.rb)
            )
            traces += DesignEquationTrace(
                symbolicEquation = "F*b = Fb x CD x CM x Ct x CF x Ci x Cr   (all factors except Cfu, CV, CL)",
                substitutedEquation = "F*b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x ${fmt(f.ct)} x " +
                    "${fmt(cfStab, 3)} x ${fmt(f.ci)} x ${fmt(f.cr)}",
                result = fmt(stab.fStarB, 0),
                units = "psi",
                codeReference = "NDS 3.3.3.8",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct,
                    "CF" to cfStab, "Ci" to f.ci, "Cr" to f.cr
                )
            )
            traces += DesignEquationTrace(
                symbolicEquation = "CL = (1+(FbE/F*b))/1.9 - sqrt([(1+(FbE/F*b))/1.9]^2 - (FbE/F*b)/0.95)",
                substitutedEquation = "CL = (1+${fmt(ratio, 3)})/1.9 - sqrt([(1+${fmt(ratio, 3)})/1.9]^2 - " +
                    "${fmt(ratio, 3)}/0.95)",
                result = fmt(stab.cl, 3),
                units = "",
                codeReference = "NDS 3.3.3.8, Eq. 3.3-6",
                variables = mapOf("FbE" to stab.fbE, "F*b" to stab.fStarB, "FbE/F*b" to ratio)
            )
        }

        val fbAdj: Double
        val fStar: Double
        val fStarStar: Double
        val limitState: String
        val adjTrace: DesignEquationTrace
        val fStarTrace: DesignEquationTrace
        val fStarStarTrace: DesignEquationTrace

        if (isGlulam) {
            val cv = computeCV(lb)
            // NDS 5.3.6: use lesser of CV and CL
            val stabilityFactor = min(cl, cv)
            fbAdj = fb * f.cd * f.cm * f.ct * stabilityFactor * f.cfu * f.ci
            fStar = fb * f.cd * f.cm * f.ct * cv * f.cfu * f.ci
            fStarStar = fb * f.cd * f.cm * f.ct * cl * f.cfu * f.ci
            limitState = if (cv <= cl)
                "Bending — Volume Factor CV (NDS 5.3.6)"
            else
                "Bending — Beam Stability CL (NDS 3.3.3)"
            adjTrace = DesignEquationTrace(
                symbolicEquation = "F'b = Fb x CD x CM x Ct x min(CL, CV) x Cfu x Ci",
                substitutedEquation = "F'b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x " +
                    "${fmt(f.ct)} x min(${fmt(cl, 3)}, ${fmt(cv, 3)}) x ${fmt(f.cfu)} x ${fmt(f.ci)}",
                result = fmt(fbAdj, 0),
                units = "psi",
                codeReference = "NDS 5.3.2, 5.3.6",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct,
                    "CL" to cl, "CV" to cv, "Cfu" to f.cfu, "Ci" to f.ci
                )
            )
            fStarTrace = DesignEquationTrace(
                symbolicEquation = "F*b = Fb x CD x CM x Ct x CV x Cfu x Ci   (all factors except CL)",
                substitutedEquation = "F*b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x ${fmt(f.ct)} x " +
                    "${fmt(cv, 3)} x ${fmt(f.cfu)} x ${fmt(f.ci)}",
                result = fmt(fStar, 0),
                units = "psi",
                codeReference = "NDS 3.9.1",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct,
                    "CV" to cv, "Cfu" to f.cfu, "Ci" to f.ci
                )
            )
            fStarStarTrace = DesignEquationTrace(
                symbolicEquation = "F**b = Fb x CD x CM x Ct x CL x Cfu x Ci   (all factors except CV)",
                substitutedEquation = "F**b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x ${fmt(f.ct)} x " +
                    "${fmt(cl, 3)} x ${fmt(f.cfu)} x ${fmt(f.ci)}",
                result = fmt(fStarStar, 0),
                units = "psi",
                codeReference = "NDS 3.9.1",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct,
                    "CL" to cl, "Cfu" to f.cfu, "Ci" to f.ci
                )
            )
        } else {
            // Sawn lumber / SCL: CF from adjustmentFactors (caller must set it)
            val cfEffective = computeSawnCF(NdsCfProperty.BENDING)
            fbAdj = fb * f.cd * f.cm * f.ct * cl * cfEffective * f.cfu * f.ci * f.cr
            fStar = fb * f.cd * f.cm * f.ct * cfEffective * f.cfu * f.ci * f.cr
            fStarStar = fbAdj
            limitState = if (cl < 1.0)
                "Bending — Beam Stability CL (NDS 3.3.3)"
            else
                "Bending — Size Factor CF (NDS Table 4A)"
            adjTrace = DesignEquationTrace(
                symbolicEquation = "F'b = Fb x CD x CM x Ct x CL x CF x Cfu x Ci x Cr",
                substitutedEquation = "F'b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x " +
                    "${fmt(f.ct)} x ${fmt(cl, 3)} x ${fmt(cfEffective, 3)} x ${fmt(f.cfu)} x " +
                    "${fmt(f.ci)} x ${fmt(f.cr)}",
                result = fmt(fbAdj, 0),
                units = "psi",
                codeReference = "NDS 3.3.2, 3.3.3, Table 4A",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct, "CL" to cl,
                    "CF" to cfEffective, "Cfu" to f.cfu, "Ci" to f.ci, "Cr" to f.cr
                )
            )
            fStarTrace = DesignEquationTrace(
                symbolicEquation = "F*b = Fb x CD x CM x Ct x CF x Cfu x Ci x Cr   (all factors except CL)",
                substitutedEquation = "F*b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x ${fmt(f.ct)} x " +
                    "${fmt(cfEffective, 3)} x ${fmt(f.cfu)} x ${fmt(f.ci)} x ${fmt(f.cr)}",
                result = fmt(fStar, 0),
                units = "psi",
                codeReference = "NDS 3.9.1",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct,
                    "CF" to cfEffective, "Cfu" to f.cfu, "Ci" to f.ci, "Cr" to f.cr
                )
            )
            // Sawn lumber has no CV, so "all factors except CV" is F'b itself.
            fStarStarTrace = adjTrace.copy(
                symbolicEquation = "F**b = F'b   (no CV for sawn lumber — all factors apply)",
                codeReference = "NDS 3.9.1"
            )
        }

        traces += adjTrace
        return BendingAxis(
            strong = true, s = sx, fbAdj = fbAdj, fStar = fStar, fStarStar = fStarStar,
            cl = cl, rbExceedsLimit = stab?.rbExceedsLimit == true,
            limitState = limitState, traces = traces,
            fStarTrace = fStarTrace, fStarStarTrace = fStarStarTrace
        )
    }

    /**
     * Weak-axis (flatwise, y-y) bending — the load is on the member's wide
     * face. CL = 1.0 because the member's depth (its narrow dimension) does
     * not exceed its breadth (NDS 3.3.3.1), so there is no lateral-torsional
     * buckling to check. The flat use factor Cfu applies (NDS 4.3.7; Table
     * 4A / 4B for sawn lumber, Table 5A / 5C for glulam).
     *
     * Sawn lumber has one tabulated Fb for both axes: F'b = Fb * CD * CM *
     * Ct * CF * Cfu * Ci * Cr (CF as tabulated by the member's nominal width,
     * with Cfu on top — Table 4A's note for dimension lumber used flatwise).
     *
     * Glulam's Fby is tabulated separately from Fbx, and isn't sourced for
     * every combination: where it's missing this reports the axis as
     * unavailable rather than substituting Fbx.
     */
    private fun weakBendingAxis(): BendingAxis {
        val f = adjustmentFactors
        val sy = profile.propertiesWeakAxis.s.inIn3
        val cfu = resolveFlatUseCfu(f, designMaterial, profile)

        val fbAdj: Double
        val adjTrace: DesignEquationTrace
        if (isGlulam) {
            val fby = WoodPropertyService
                .getReferenceProperties(designMaterial.species, designMaterial.grade)
                ?.bendingWeakAxis?.inPsi
            if (fby == null) {
                val reason = "Weak-axis bending — Fby is not sourced for this glulam combination"
                return BendingAxis(
                    strong = false, s = sy, fbAdj = 0.0, fStar = 0.0, fStarStar = 0.0, cl = 1.0,
                    rbExceedsLimit = false, limitState = reason, traces = emptyList(),
                    unavailableReason = reason
                )
            }
            fbAdj = fby * f.cd * f.cm * f.ct * cfu * f.ci
            adjTrace = DesignEquationTrace(
                symbolicEquation = "F'by = Fby x CD x CM x Ct x CL x Cfu x Ci   (CL = 1.0, flatwise)",
                substitutedEquation = "F'by = ${fmt(fby, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x ${fmt(f.ct)} x " +
                    "1.000 x ${fmt(cfu, 3)} x ${fmt(f.ci)}",
                result = fmt(fbAdj, 0),
                units = "psi",
                codeReference = "NDS 3.3.3.1, Table 5A (Fby, Cfu)",
                variables = mapOf(
                    "Fby" to fby, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct,
                    "CL" to 1.0, "Cfu" to cfu, "Ci" to f.ci
                )
            )
        } else {
            val fb = designMaterial.referenceBending.inPsi
            val cf = computeSawnCF(NdsCfProperty.BENDING)
            fbAdj = fb * f.cd * f.cm * f.ct * cf * cfu * f.ci * f.cr
            adjTrace = DesignEquationTrace(
                symbolicEquation = "F'b = Fb x CD x CM x Ct x CL x CF x Cfu x Ci x Cr   (CL = 1.0, flatwise)",
                substitutedEquation = "F'b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x ${fmt(f.ct)} x " +
                    "1.000 x ${fmt(cf, 3)} x ${fmt(cfu, 3)} x ${fmt(f.ci)} x ${fmt(f.cr)}",
                result = fmt(fbAdj, 0),
                units = "psi",
                codeReference = "NDS 3.3.3.1, 4.3.7, Table 4A (CF, Cfu)",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct, "CL" to 1.0,
                    "CF" to cf, "Cfu" to cfu, "Ci" to f.ci, "Cr" to f.cr
                )
            )
        }

        return BendingAxis(
            strong = false, s = sy, fbAdj = fbAdj, fStar = fbAdj, fStarStar = fbAdj, cl = 1.0,
            rbExceedsLimit = false,
            limitState = "Bending — Weak axis / flatwise, Cfu = ${fmt(cfu, 2)} (NDS 4.3.7)",
            traces = listOf(adjTrace),
            fStarTrace = adjTrace.copy(
                symbolicEquation = "F*b = F'b   (flatwise: CL = 1.0, no CV)", codeReference = "NDS 3.9.1"
            ),
            fStarStarTrace = adjTrace.copy(
                symbolicEquation = "F**b = F'b   (flatwise: CL = 1.0, no CV)", codeReference = "NDS 3.9.1"
            )
        )
    }

    /** Bending capacity about the given principal axis: F'b x S, with its traces. */
    private fun calculateBendingAbout(strong: Boolean, lb: Double): LimitStateResult {
        val ax = bendingAxis(strong, lb)
        if (!ax.available) return LimitStateResult(0.0, ax.limitState, ax.traces)
        if (ax.rbExceedsLimit) {
            return LimitStateResult(
                0.0, "Bending — RB exceeds 50, not permitted (NDS 3.3.3.7)", ax.traces
            )
        }

        val mn = ax.fbAdj * ax.s
        val sName = if (strong) "Sx" else "Sy"
        val mnTrace = DesignEquationTrace(
            symbolicEquation = "Mn = F'b x $sName",
            substitutedEquation = "Mn = ${fmt(ax.fbAdj, 0)} x ${fmt(ax.s, 2)}",
            result = "${fmt(mn, 0)} lb-in (${kipFt(mn)} k-ft)",
            units = "lb-in",
            codeReference = "NDS 3.3.2",
            variables = mapOf("F'b" to ax.fbAdj, sName to ax.s)
        )
        return LimitStateResult(mn, ax.limitState, ax.traces + mnTrace)
    }

    /** Bending capacity about the axis the member is oriented to bend about. */
    private fun calculateBending(lb: Double): LimitStateResult =
        calculateBendingAbout(memberIsStrongAxis, lb)

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
        /** Cap on a point-path combined ratio — see [pointInteractionCheck]. */
        const val POINT_RATIO_CAP = 99.0

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
    private fun calculateShear(): LimitStateResult {
        val fv = designMaterial.referenceShear.inPsi
        val f = adjustmentFactors
        val fvAdj = f.adjustedShear(fv)
        val area  = profile.area.inIn2

        val fvTrace = DesignEquationTrace(
            symbolicEquation = "F'v = Fv x CD x CM x Ct x Ci",
            substitutedEquation = "F'v = ${fmt(fv, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x " +
                "${fmt(f.ct)} x ${fmt(f.ci)}",
            result = fmt(fvAdj, 0),
            units = "psi",
            codeReference = "NDS 3.4.1",
            variables = mapOf("Fv" to fv, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct, "Ci" to f.ci)
        )

        val vn = (2.0 / 3.0) * fvAdj * area
        val vnTrace = DesignEquationTrace(
            symbolicEquation = "Vn = (2/3) x F'v x A",
            substitutedEquation = "Vn = (2/3) x ${fmt(fvAdj, 0)} x ${fmt(area, 2)}",
            result = "${fmt(vn, 0)} lb (${kips(vn)} kips)",
            units = "lb",
            codeReference = "NDS 3.4.2",
            variables = mapOf("F'v" to fvAdj, "A" to area)
        )

        return LimitStateResult(vn, "Shear (NDS 3.4)", listOf(fvTrace, vnTrace))
    }

    // ------------------------------------------------------------------
    // NDS 3.7 — Compression Parallel to Grain
    // ------------------------------------------------------------------

    /**
     * F*c, CP and F'c — the compression development shared by the axial
     * capacity check and the NDS 3.9.2 combined-bending interaction (whose
     * first term is (fc / F'c)^2 and so needs the same F'c).
     */
    private data class CompressionParts(
        val fStarC: Double,
        val cp: Double,
        val fcAdj: Double,
        val traces: List<DesignEquationTrace>
    )

    private fun compressionParts(lu: Double): CompressionParts {
        val fc = designMaterial.referenceCompressionParallel.inPsi
        val f = adjustmentFactors

        val cpResult = computeCPTraced(lu)
        val fStarC = fc * f.cd * f.cm * f.ct * computeSawnCF(NdsCfProperty.COMPRESSION) * f.ci
        val fcAdj = fStarC * cpResult.cp

        val fcAdjTrace = DesignEquationTrace(
            symbolicEquation = "F'c = F*c x CP",
            substitutedEquation = "F'c = ${fmt(fStarC, 0)} x ${fmt(cpResult.cp, 3)}",
            result = fmt(fcAdj, 0),
            units = "psi",
            codeReference = "NDS 3.7.1",
            variables = mapOf("F*c" to fStarC, "CP" to cpResult.cp)
        )
        return CompressionParts(fStarC, cpResult.cp, fcAdj, cpResult.traces + fcAdjTrace)
    }

    /**
     * NDS 3.7 compression capacity including column stability factor CP.
     * F'c = Fc * CD * CM * Ct * CF * Ci * CP
     * CP computed from NDS 3.7.1 using slenderness ratio le/d.
     */
    private fun calculateCompression(lu: Double): LimitStateResult {
        val area = profile.area.inIn2
        val parts = compressionParts(lu)

        val pn = parts.fcAdj * area
        val pnTrace = DesignEquationTrace(
            symbolicEquation = "Pn = F'c x A",
            substitutedEquation = "Pn = ${fmt(parts.fcAdj, 0)} x ${fmt(area, 2)}",
            result = "${fmt(pn, 0)} lb (${kips(pn)} kips)",
            units = "lb",
            codeReference = "NDS 3.7",
            variables = mapOf("F'c" to parts.fcAdj, "A" to area)
        )

        return LimitStateResult(
            pn,
            "Compression (NDS 3.7, CP=${fmt(parts.cp, 3)})",
            parts.traces + pnTrace
        )
    }

    /** NDS 3.7.1 Column Stability Factor CP, with its derivation traced. */
    private data class CpResult(val cp: Double, val traces: List<DesignEquationTrace>)

    /**
     * NDS 3.7.1 Column Stability Factor CP.
     * CP = (1 + ratio) / (2c) - sqrt(((1 + ratio) / (2c))^2 - ratio/c)
     * where ratio = FcE / F*c, c = 0.8 (sawn) or 0.9 (glulam/SCL)
     */
    private fun computeCPTraced(lu: Double): CpResult {
        if (lu <= 0.0) return CpResult(1.0, emptyList())

        val d = profile.depth.inInches
        val b = if (profile is WoodProfile) profile.dressedWidth.inInches
        else sqrt(profile.area.inIn2)

        // NDS 3.7.1: slenderness ratio le/d, must not exceed 50
        val le    = lu  // Effective length — caller responsible for ke * lu if needed
        val slenderness = le / minOf(d, b)
        if (slenderness > 50.0) return CpResult(0.0, emptyList())  // Exceeds NDS max — inadequate

        // Critical buckling stress FcE (NDS 3.7.1)
        val eMin      = woodAdjustedEmin(designMaterial, adjustmentFactors)
        val fce       = 0.822 * eMin / slenderness.pow(2)
        val fceTrace = DesignEquationTrace(
            symbolicEquation = "FcE = 0.822 x E'min / (le/d)^2",
            substitutedEquation = "FcE = 0.822 x ${fmt(eMin, 0)} / ${fmt(slenderness, 2)}^2",
            result = fmt(fce, 0),
            units = "psi",
            codeReference = "NDS 3.7.1",
            variables = mapOf("E'min" to eMin, "le/d" to slenderness)
        )

        // F*c = Fc with all adjustments except CP
        val f = adjustmentFactors
        val fcStar    = designMaterial.referenceCompressionParallel.inPsi *
                f.cd * f.cm * f.ct * computeSawnCF(NdsCfProperty.COMPRESSION) * f.ci

        if (fcStar <= 0.0) return CpResult(1.0, listOf(fceTrace))

        val ratio = fce / fcStar
        val c     = if (isGlulam) 0.90 else 0.80

        val term = (1.0 + ratio) / (2.0 * c)
        val cp = term - sqrt(term.pow(2) - ratio / c)
        val cpTrace = DesignEquationTrace(
            symbolicEquation = "CP = (1+(FcE/F*c))/2c - sqrt([(1+(FcE/F*c))/2c]^2 - (FcE/F*c)/c)",
            substitutedEquation = "CP = (1+${fmt(ratio, 3)})/${fmt(2 * c, 2)} - sqrt([(1+${fmt(ratio, 3)})/" +
                "${fmt(2 * c, 2)}]^2 - ${fmt(ratio, 3)}/${fmt(c, 2)})",
            result = fmt(cp, 3),
            units = "",
            codeReference = "NDS 3.7.1",
            variables = mapOf("FcE" to fce, "F*c" to fcStar, "FcE/F*c" to ratio, "c" to c)
        )

        return CpResult(cp, listOf(fceTrace, cpTrace))
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
    private fun calculateTension(): LimitStateResult {
        val ft = designMaterial.referenceTensionParallel.inPsi
        val f = adjustmentFactors
        val cf = computeSawnCF(NdsCfProperty.TENSION)
        val ftAdj = adjustedTensionStress()
        val area = profile.area.inIn2

        val ftTrace = DesignEquationTrace(
            symbolicEquation = "F't = Ft x CD x CM x Ct x CF x Ci",
            substitutedEquation = "F't = ${fmt(ft, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x " +
                "${fmt(f.ct)} x ${fmt(cf, 3)} x ${fmt(f.ci)}",
            result = fmt(ftAdj, 0),
            units = "psi",
            codeReference = "NDS 3.8.1, Table 4A",
            variables = mapOf(
                "Ft" to ft, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct, "CF" to cf, "Ci" to f.ci
            )
        )
        val pn = ftAdj * area
        val pnTrace = DesignEquationTrace(
            symbolicEquation = "Pn = F't x A",
            substitutedEquation = "Pn = ${fmt(ftAdj, 0)} x ${fmt(area, 2)}",
            result = "${fmt(pn, 0)} lb (${kips(pn)} kips)",
            units = "lb",
            codeReference = "NDS 3.8",
            variables = mapOf("F't" to ftAdj, "A" to area)
        )

        return LimitStateResult(pn, "Tension (NDS 3.8)", listOf(ftTrace, pnTrace))
    }

    // ------------------------------------------------------------------
    // NDS 3.9 — Combined bending and axial loading
    // ------------------------------------------------------------------

    /** Demand / capacity as magnitudes; zero capacity with real demand is a failure, not a pass. */
    private fun utilizationRatio(demandAbs: Double, capacity: Double): Double =
        if (capacity > 0.0) demandAbs / capacity
        else if (demandAbs > 0.0) Double.POSITIVE_INFINITY
        else 0.0

    /** F't = Ft x CD x CM x Ct x CF x Ci (psi) — shared by the tension capacity and NDS 3.9.1. */
    private fun adjustedTensionStress(): Double {
        val f = adjustmentFactors
        return designMaterial.referenceTensionParallel.inPsi * f.cd * f.cm * f.ct *
            computeSawnCF(NdsCfProperty.TENSION) * f.ci
    }

    private fun fmtOrNg(v: Double, d: Int = 3): String =
        if (v.isNaN() || v.isInfinite()) "N.G." else fmt(v, d)

    /**
     * Combined-stress checks for the actual stresses at this station:
     *
     *  - axial tension + bending -> NDS 3.9.1 (Eq. 3.9-1, 3.9-2)
     *  - axial compression + bending, or biaxial bending alone (NDS
     *    3.3.3.10 sends biaxial bending to 3.9.2) -> NDS 3.9.2 (Eq. 3.9-3, 3.9-4)
     *
     * fb1 is the edgewise (strong axis, x-x) bending stress and fb2 the
     * flatwise (weak axis, y-y) bending stress, taken from the member's
     * primary moment (per [memberIsStrongAxis]) and the moment about the
     * other axis ([StationDemand.momentY]). Stresses use the gross section.
     * Returns nothing when there's no bending, or no axial load and
     * bending about only one axis (the ordinary flexure check covers that).
     */
    private fun buildCombinedChecks(
        demand: StationDemand,
        lb: Double,
        luAxial: Double,
        kBend: Double,
        kTens: Double,
        kComp: Double,
        isLrfd: Boolean
    ): List<CombinedStressCheck> {
        val area = profile.area.inIn2
        if (area <= 0.0) return emptyList()

        val axial = demand.axial.pounds
        val primaryM = abs(demand.moment.lbIn)
        val otherM = abs(demand.momentY.lbIn)

        val strongAx = bendingAxis(true, lb)
        val weakAx = bendingAxis(false, lb)

        val mStrong = if (memberIsStrongAxis) primaryM else otherM
        val mWeak = if (memberIsStrongAxis) otherM else primaryM
        val fb1 = if (strongAx.s > 0.0) mStrong / strongAx.s else 0.0
        val fb2 = if (weakAx.s > 0.0) mWeak / weakAx.s else 0.0
        if (fb1 <= 0.0 && fb2 <= 0.0) return emptyList()

        val notes = mutableListOf<String>()
        if (isLrfd) {
            notes += "LRFD: adjusted design values are scaled by lambda x phi exactly as in the " +
                "individual member checks (lambda = 0.8; phi = 0.85 bending, 0.80 tension, " +
                "0.90 compression). The NDS Appendix N format conversion factor KF is not applied."
        }

        return when {
            axial > 0.0 -> listOf(
                combinedTension(axial, area, mStrong, mWeak, fb1, fb2, strongAx, weakAx, kBend, kTens, notes)
            )
            axial < 0.0 || (fb1 > 0.0 && fb2 > 0.0) -> listOf(
                combinedCompression(
                    axial, area, luAxial, lb, mStrong, mWeak, fb1, fb2, strongAx, weakAx, kBend, kComp, notes
                )
            )
            else -> emptyList()
        }
    }

    private fun stressTrace(name: String, moment: Double, axis: BendingAxis, stress: Double) =
        DesignEquationTrace(
            symbolicEquation = "$name = M / ${if (axis.strong) "Sx" else "Sy"}",
            substitutedEquation = "$name = ${fmt(moment, 0)} / ${fmt(axis.s, 2)}",
            result = fmt(stress, 1),
            units = "psi",
            codeReference = "NDS 3.9",
            variables = mapOf("M" to moment, if (axis.strong) "Sx" to axis.s else "Sy" to axis.s)
        )

    /** NDS 3.9.1 — bending and axial tension. */
    private fun combinedTension(
        axialLb: Double, area: Double, mStrong: Double, mWeak: Double,
        fb1: Double, fb2: Double, strongAx: BendingAxis, weakAx: BendingAxis,
        kBend: Double, kTens: Double, notes: List<String>
    ): CombinedStressCheck {
        val inf = Double.POSITIVE_INFINITY
        val ft = abs(axialLb) / area
        val ftAdj = adjustedTensionStress() * kTens
        val allNotes = notes.toMutableList()
        val traces = mutableListOf<DesignEquationTrace>()
        val equations = mutableListOf<InteractionEquationResult>()

        traces += DesignEquationTrace(
            symbolicEquation = "ft = T / A",
            substitutedEquation = "ft = ${fmt(abs(axialLb), 0)} / ${fmt(area, 2)}",
            result = fmt(ft, 1),
            units = "psi",
            codeReference = "NDS 3.9.1",
            variables = mapOf("T" to abs(axialLb), "A" to area)
        )
        calculateTension().traces.firstOrNull()?.let { traces += it }

        if (fb1 > 0.0) {
            traces += stressTrace("fb1", mStrong, strongAx, fb1)
            strongAx.fStarTrace?.let { traces += it }
            strongAx.fStarStarTrace?.let { traces += it }
        }
        if (fb2 > 0.0) {
            traces += stressTrace("fb2", mWeak, weakAx, fb2)
            if (!weakAx.available) allNotes += weakAx.limitState
            weakAx.fStarTrace?.let { traces += it }
        }

        // Eq. 3.9-1:  ft/F't + fb/F*b <= 1.0
        val fStar1 = strongAx.fStar * kBend
        val fStar2 = weakAx.fStar * kBend
        var v1 = if (ftAdj > 0.0) ft / ftAdj else if (ft > 0.0) inf else 0.0
        val symbolic = StringBuilder("ft/F't")
        val substituted = StringBuilder("${fmt(ft, 1)}/${fmt(ftAdj, 0)}")
        val vars = linkedMapOf("ft" to ft, "F't" to ftAdj)
        if (fb1 > 0.0) {
            v1 += if (fStar1 > 0.0) fb1 / fStar1 else inf
            symbolic.append(" + fb1/F*b1")
            substituted.append(" + ${fmt(fb1, 1)}/${fmt(fStar1, 0)}")
            vars["fb1"] = fb1; vars["F*b1"] = fStar1
        }
        if (fb2 > 0.0) {
            v1 += if (weakAx.available && fStar2 > 0.0) fb2 / fStar2 else inf
            symbolic.append(" + fb2/F*b2")
            substituted.append(" + ${fmt(fb2, 1)}/${fmt(fStar2, 0)}")
            vars["fb2"] = fb2; vars["F*b2"] = fStar2
        }
        equations += InteractionEquationResult("Eq. 3.9-1", v1)
        traces += DesignEquationTrace(
            symbolicEquation = "$symbolic <= 1.0",
            substitutedEquation = "$substituted = ${fmtOrNg(v1)}",
            result = fmtOrNg(v1),
            units = "",
            codeReference = "NDS 3.9.1, Eq. 3.9-1",
            variables = vars
        )

        // Eq. 3.9-2:  (fb - ft)/F**b <= 1.0   (net stress on the compression face)
        if (fb1 > 0.0) {
            val fss1 = strongAx.fStarStar * kBend
            val v = if (fss1 > 0.0) (fb1 - ft) / fss1 else inf
            equations += InteractionEquationResult("Eq. 3.9-2 (strong axis)", v)
            traces += DesignEquationTrace(
                symbolicEquation = "(fb1 - ft)/F**b1 <= 1.0",
                substitutedEquation = "(${fmt(fb1, 1)} - ${fmt(ft, 1)})/${fmt(fss1, 0)} = ${fmtOrNg(v)}",
                result = fmtOrNg(v),
                units = "",
                codeReference = "NDS 3.9.1, Eq. 3.9-2",
                variables = mapOf("fb1" to fb1, "ft" to ft, "F**b1" to fss1)
            )
        }
        if (fb2 > 0.0 && weakAx.available) {
            val fss2 = weakAx.fStarStar * kBend
            val v = if (fss2 > 0.0) (fb2 - ft) / fss2 else inf
            equations += InteractionEquationResult("Eq. 3.9-2 (weak axis)", v)
            traces += DesignEquationTrace(
                symbolicEquation = "(fb2 - ft)/F**b2 <= 1.0",
                substitutedEquation = "(${fmt(fb2, 1)} - ${fmt(ft, 1)})/${fmt(fss2, 0)} = ${fmtOrNg(v)}",
                result = fmtOrNg(v),
                units = "",
                codeReference = "NDS 3.9.1, Eq. 3.9-2",
                variables = mapOf("fb2" to fb2, "ft" to ft, "F**b2" to fss2)
            )
        }
        if (fb1 > 0.0 && fb2 > 0.0) {
            allNotes += "Bending about both axes with axial tension: NDS 3.9.1 is written for a single " +
                "bending stress fb. The weak-axis (fb2) term is added to Eq. 3.9-1 and checked in " +
                "Eq. 3.9-2 as a conservative extension."
        }

        val governing = equations.maxByOrNull { it.value } ?: equations.first()
        return CombinedStressCheck(
            label = "Bending + Axial Tension",
            clause = "NDS 3.9.1",
            utilization = governing.value.coerceAtLeast(0.0),
            governingEquation = governing.equation,
            equations = equations,
            traces = traces,
            notes = allNotes
        )
    }

    /** NDS 3.9.2 — bending and axial compression (and biaxial bending, per 3.3.3.10). */
    private fun combinedCompression(
        axialLb: Double, area: Double, luAxial: Double, lb: Double,
        mStrong: Double, mWeak: Double, fb1: Double, fb2: Double,
        strongAx: BendingAxis, weakAx: BendingAxis,
        kBend: Double, kComp: Double, notes: List<String>
    ): CombinedStressCheck {
        val inf = Double.POSITIVE_INFINITY
        val fc = if (axialLb < 0.0) abs(axialLb) / area else 0.0
        val allNotes = notes.toMutableList()
        val traces = mutableListOf<DesignEquationTrace>()
        val equations = mutableListOf<InteractionEquationResult>()
        val problems = mutableListOf<String>()

        val parts = compressionParts(luAxial)
        val fcAdj = parts.fcAdj * kComp

        // d1 = wide face dimension (edgewise bending), d2 = narrow face (flatwise) — NDS Fig. 3H.
        val d1 = profile.depth.inInches
        val d2 = if (profile is WoodProfile) profile.dressedWidth.inInches else sqrt(area)
        val le = luAxial
        val eMinAdj = woodAdjustedEmin(designMaterial, adjustmentFactors)
        val fcE1 = if (le > 0.0 && d1 > 0.0) 0.822 * eMinAdj / (le / d1).pow(2) else inf
        val fcE2 = if (le > 0.0 && d2 > 0.0) 0.822 * eMinAdj / (le / d2).pow(2) else inf

        val stab = computeNdsBeamStability(lb, profile, designMaterial, adjustmentFactors, isGlulam)
        val fbE = stab?.fbE ?: inf

        val fb1Adj = strongAx.fbAdj * kBend
        val fb2Adj = weakAx.fbAdj * kBend

        // ---- stresses and their development
        if (fc > 0.0) {
            traces += DesignEquationTrace(
                symbolicEquation = "fc = P / A",
                substitutedEquation = "fc = ${fmt(abs(axialLb), 0)} / ${fmt(area, 2)}",
                result = fmt(fc, 1),
                units = "psi",
                codeReference = "NDS 3.9.2",
                variables = mapOf("P" to abs(axialLb), "A" to area)
            )
            traces += parts.traces
        }
        if (fb1 > 0.0) {
            traces += stressTrace("fb1", mStrong, strongAx, fb1)
            strongAx.traces.lastOrNull()?.let { traces += it }
        }
        if (fb2 > 0.0) {
            traces += stressTrace("fb2", mWeak, weakAx, fb2)
            if (weakAx.available) weakAx.traces.lastOrNull()?.let { traces += it }
            else { allNotes += weakAx.limitState; problems += "weak-axis bending value (Fby) is not available" }
        }

        if (fcE1.isFinite() && (fb1 > 0.0 || fb2 > 0.0)) {
            traces += DesignEquationTrace(
                symbolicEquation = "FcE1 = 0.822 E'min / (le1/d1)^2",
                substitutedEquation = "FcE1 = 0.822 x ${fmt(eMinAdj, 0)} / (${fmt(le)}/${fmt(d1)})^2",
                result = fmt(fcE1, 0),
                units = "psi",
                codeReference = "NDS 3.9.2",
                variables = mapOf("E'min" to eMinAdj, "le1" to le, "d1" to d1)
            )
        }
        if (fcE2.isFinite() && (fb2 > 0.0)) {
            traces += DesignEquationTrace(
                symbolicEquation = "FcE2 = 0.822 E'min / (le2/d2)^2",
                substitutedEquation = "FcE2 = 0.822 x ${fmt(eMinAdj, 0)} / (${fmt(le)}/${fmt(d2)})^2",
                result = fmt(fcE2, 0),
                units = "psi",
                codeReference = "NDS 3.9.2",
                variables = mapOf("E'min" to eMinAdj, "le2" to le, "d2" to d2)
            )
        }
        if (fb1 > 0.0 && fb2 > 0.0 && stab != null) {
            traces += DesignEquationTrace(
                symbolicEquation = "FbE = 1.20 E'min / RB^2",
                substitutedEquation = "FbE = 1.20 x ${fmt(stab.eMinAdj, 0)} / ${fmt(stab.rb, 2)}^2",
                result = fmt(stab.fbE, 0),
                units = "psi",
                codeReference = "NDS 3.9.2, 3.3.3.8",
                variables = mapOf("E'min" to stab.eMinAdj, "RB" to stab.rb)
            )
        }

        // ---- applicability limits stated with Eq. 3.9-3 / 3.9-4
        if (fb1 > 0.0 && fc >= fcE1) problems += "fc >= FcE1 (member is unstable about the strong axis)"
        if (fb2 > 0.0 && fc >= fcE2) problems += "fc >= FcE2 (member is unstable about the weak axis)"
        if (fb1 > 0.0 && fb2 > 0.0 && fb1 >= fbE) problems += "fb1 >= FbE (biaxial bending limit)"
        if (fc > 0.0 && fcAdj <= 0.0) problems += "F'c is zero (slenderness exceeds the NDS limit of 50)"
        if (fb1 > 0.0 && fb1Adj <= 0.0) problems += "F'b1 is zero"
        if (fb2 > 0.0 && weakAx.available && fb2Adj <= 0.0) problems += "F'b2 is zero"
        if (strongAx.rbExceedsLimit && fb1 > 0.0) problems += "RB exceeds 50 (NDS 3.3.3.7)"

        // ---- Eq. 3.9-4:  fc/FcE2 + (fb1/FbE)^2 < 1.0   (biaxial bending)
        if (fb1 > 0.0 && fb2 > 0.0) {
            val v4 = fc / fcE2 + (fb1 / fbE).pow(2)
            equations += InteractionEquationResult("Eq. 3.9-4", v4, isStrictLimit = true)
            traces += DesignEquationTrace(
                symbolicEquation = "fc/FcE2 + (fb1/FbE)^2 < 1.0",
                substitutedEquation = "${fmt(fc, 1)}/${fmtOrNg(fcE2, 0)} + (${fmt(fb1, 1)}/${fmtOrNg(fbE, 0)})^2 = ${fmtOrNg(v4)}",
                result = fmtOrNg(v4),
                units = "",
                codeReference = "NDS 3.9.2, Eq. 3.9-4",
                variables = buildMap {
                    put("fc", fc); put("fb1", fb1)
                    if (fcE2.isFinite()) put("FcE2", fcE2)
                    if (fbE.isFinite()) put("FbE", fbE)
                }
            )
        }

        // ---- Eq. 3.9-3
        val v3: Double
        if (problems.isNotEmpty()) {
            v3 = inf
            allNotes += "Eq. 3.9-3 cannot be satisfied: " + problems.joinToString("; ") + "."
        } else {
            val t1 = if (fc > 0.0) (fc / fcAdj).pow(2) else 0.0
            val t2 = if (fb1 > 0.0) fb1 / (fb1Adj * (1.0 - fc / fcE1)) else 0.0
            val t3 = if (fb2 > 0.0) fb2 / (fb2Adj * (1.0 - fc / fcE2 - (fb1 / fbE).pow(2))) else 0.0
            v3 = t1 + t2 + t3

            val symbolic = StringBuilder()
            val substituted = StringBuilder()
            val vars = linkedMapOf<String, Double>()
            fun addTerm(sym: String, sub: String) {
                if (symbolic.isNotEmpty()) { symbolic.append(" + "); substituted.append(" + ") }
                symbolic.append(sym); substituted.append(sub)
            }
            if (fc > 0.0) {
                addTerm("[fc/F'c]^2", "[${fmt(fc, 1)}/${fmt(fcAdj, 0)}]^2")
                vars["fc"] = fc; vars["F'c"] = fcAdj
            }
            if (fb1 > 0.0) {
                addTerm("fb1/(F'b1[1-(fc/FcE1)])",
                    "${fmt(fb1, 1)}/(${fmt(fb1Adj, 0)}[1-(${fmt(fc, 1)}/${fmtOrNg(fcE1, 0)})])")
                vars["fb1"] = fb1; vars["F'b1"] = fb1Adj
                if (fcE1.isFinite()) vars["FcE1"] = fcE1
            }
            if (fb2 > 0.0) {
                addTerm("fb2/(F'b2[1-(fc/FcE2)-(fb1/FbE)^2])",
                    "${fmt(fb2, 1)}/(${fmt(fb2Adj, 0)}[1-(${fmt(fc, 1)}/${fmtOrNg(fcE2, 0)})-" +
                        "(${fmt(fb1, 1)}/${fmtOrNg(fbE, 0)})^2])")
                vars["fb2"] = fb2; vars["F'b2"] = fb2Adj
                if (fcE2.isFinite()) vars["FcE2"] = fcE2
                if (fbE.isFinite()) vars["FbE"] = fbE
            }
            traces += DesignEquationTrace(
                symbolicEquation = "$symbolic <= 1.0",
                substitutedEquation = "$substituted = ${fmtOrNg(v3)}",
                result = fmtOrNg(v3),
                units = "",
                codeReference = "NDS 3.9.2, Eq. 3.9-3",
                variables = vars
            )
        }
        equations.add(0, InteractionEquationResult("Eq. 3.9-3", v3))

        val governing = equations.maxByOrNull { it.value } ?: equations.first()
        return CombinedStressCheck(
            label = if (fc > 0.0) "Bending + Axial Compression" else "Biaxial Bending",
            clause = "NDS 3.9.2",
            utilization = governing.value,
            governingEquation = governing.equation,
            equations = equations,
            traces = traces,
            notes = allNotes
        )
    }
    /**
     * NDS 3.9 combined-stress ratio for the point-by-point capacity path
     * (the header / governing-utilization diagram), as a [CodeCheck] to plug
     * into [CapacityEngine.evaluate]'s `interactionCheckProvider`. Without it
     * that path judged wood by AISC Eq. H1-1.
     *
     * Combined checks only exist where a station has bending with axial load,
     * or bending about both axes; elsewhere the individual checks already
     * cover the member and this returns 0. Where one applies, the worst of
     * the 3.9 checks is returned. An infinite ratio (a precondition such as
     * fc < FcE violated) is capped at [POINT_RATIO_CAP] so diagrams stay
     * finite; the Design tab's detailed card still shows it as N.G.
     */
    fun pointInteractionCheck(demand: StationDemand, methodology: DesignMethodology): CodeCheck {
        val label = "NDS 3.9 Combined"
        val mx = abs(demand.moment.inLbIn)
        val my = abs(demand.momentY.inLbIn)
        val axial = abs(demand.axial.inPoundsForce)
        val applies = ((mx > 0.0 || my > 0.0) && axial > 0.0) || (mx > 0.0 && my > 0.0)
        if (!applies) return CodeCheck(label, 0.0, 1.0, 0.0)
        val worst = evaluateDetailed(demand, methodology).combinedChecks.maxByOrNull { it.utilization }
            ?: return CodeCheck(label, 0.0, 1.0, 0.0)
        val ratio = worst.utilization.coerceAtMost(POINT_RATIO_CAP)
        return CodeCheck("${worst.clause} ${worst.governingEquation}", ratio, 1.0, ratio)
    }
}
