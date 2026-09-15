package com.lz.solver.material

import com.lz.model.regulatory.AiscEdition
import com.lz.model.regulatory.aisc.AiscDesignFactors
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.Flange
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.ShapeType
import com.lz.model.structural.StationDemand
import com.lz.model.structural.SteelProfile
import com.lz.model.structural.StrengthCheckResult
import com.lz.model.structural.StrengthDesignResult
import com.lz.model.structural.DesignEquationTrace
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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * AISC 360-22 capacity calculator for all standard steel section types.
 *
 * Covers:
 *   Flexure X  — Chapter F (F2 compact W/C, F3 noncompact/slender flanges,
 *                           F4/F5 other I-shapes, F6 weak axis, F7 box/HSS,
 *                           F8 round HSS/pipe, F9 T/double-angle, F11 bars/plates)
 *   Flexure Y  — Chapter F Section F6
 *   Shear      — Chapter G (G2 W/C webs, G4 single angles, G5 T-shapes,
 *                           G6 HSS/pipe)
 *   Axial      — Chapter E (E3 flexural buckling, E4 torsional/flexural-
 *                           torsional for singly/doubly symmetric sections)
 *   Torsion    — Chapter H Section H3 (closed) / open section yielding
 *
 * phi/omega factors are NOT applied here — CapacityEngine owns that step.
 * This calculator returns nominal (unfactored) capacities only.
 */
@Suppress("PropertyName", "LocalVariableName")
class AiscSteelCapacityCalculator(
    private val profile: SectionProfile,
    private val material: MaterialGrade.Steel,
    /**
     * Resolved AISC edition (from the active project's BuildingCode ->
     * Standard -> StandardEdition.Aisc360 chain). Defaults to AISC 360-22
     * — the same default AiscDesignFactors.forMethodology() used before
     * this was wired through — so existing call sites that haven't been
     * updated to pass a resolved edition keep identical behavior.
     */
    private val edition: AiscEdition = AiscEdition.AISC_360_22,
    /**
     * Member orientation from the Geometry tab's Strong Axis / Weak Axis
     * toggle. True = the beam bends about its strong axis (default,
     * matches prior behavior for every caller that hasn't been updated to
     * pass this). False = the single moment/shear this 2D solver computes
     * is actually loading the section's WEAK axis — the whole pipeline
     * downstream (evaluate/evaluateDetailed/CapacityEngine/demand.moment)
     * is hardwired to treat "flexureX"/"shear(isStrongAxis=true)" as *the*
     * governing check against that single demand value, so rather than
     * threading a second orientation-aware demand value through the whole
     * solver, calculateFlexureX and the governing shear call dispatch to
     * the weak-axis (F6/G6) formulas internally when this is false. See
     * calculateFlexureX and the two `calculateShear(memberIsStrongAxis)`
     * call sites below.
     *
     * Known gap: this fixes the STRENGTH CHECK only. Deflection and (for
     * multi-span continuous beams) moment distribution still use the
     * strong-axis I unconditionally (BeamAnalysisConfig.momentOfInertiaX),
     * since MemberAnalysisSolver's stiffness matrix isn't wired to this
     * flag — a weak-axis-oriented beam will still show understated
     * deflection and (for continuous beams only) slightly-off moment
     * redistribution until that's addressed separately.
     */
    private val memberIsStrongAxis: Boolean = true
) : CapacityCalculator {

    private val E: Double = material.modulusOfElasticity.inPsi
    private val Fy: Double = material.yieldStrength.inPsi

    /**
     * Internal result shape carried by the private per-limit-state calculator
     * functions: nominal capacity, the governing limit-state name (unchanged
     * contract — existing 2-var destructuring `val (n, ls) = calculateX(...)`
     * still works against this), plus optional [DesignEquationTrace]s for the
     * Design tab's equation-trace UI. Empty when a shape/limit-state
     * combination is out of scope for trace generation (traces default to
     * empty and the UI already renders that gracefully — no trace section).
     */
    private data class LimitStateResult(
        val nominal: Double,
        val limitState: String,
        val traces: List<DesignEquationTrace> = emptyList()
    )

    private fun fmt(v: Double, d: Int = 2): String = String.format(Locale.US, "%.${d}f", v)
    private fun kipFt(lbIn: Double): String = fmt(lbIn / 12000.0, 1)
    private fun kips(lb: Double): String = fmt(lb / 1000.0, 1)

    // ------------------------------------------------------------------
    // CapacityCalculator contract — returns nominal (unfactored) values
    // ------------------------------------------------------------------

    override fun evaluate(demand: StationDemand): RawCapacityResult {
        val lb = if (demand.compressionFlange == Flange.TOP)
            demand.lbTop.inInches else demand.lbBottom.inInches
        val cb = demand.cb

        // Dispatch axial check based on sign of demand
        // Positive axial = tension (AXIAL_TENSION direction), negative = compression
        val (nomPn, lsPn) = if (demand.axial.pounds >= 0.0)
            calculateAxialTension()
        else
            calculateAxialCompression(lb)

        val (nomMnX,  lsMnX)  = calculateFlexureX(lb, cb)
        val (nomMnY,  lsMnY)  = calculateFlexureY()
        val (nomVnX,  lsVnX)  = calculateShear(isStrongAxis = memberIsStrongAxis)
        val (nomVnY,  lsVnY)  = calculateShear(isStrongAxis = !memberIsStrongAxis)
        val (nomTn,   lsTn)   = calculateTorsion()

        return RawCapacityResult(
            nominalFlexureX      = nomMnX,
            limitStateFlexureX   = lsMnX,
            nominalFlexureY      = nomMnY,
            limitStateFlexureY   = lsMnY,
            nominalShearX        = nomVnX,
            limitStateShearX     = lsVnX,
            nominalShearY        = nomVnY,
            limitStateShearY     = lsVnY,
            nominalAxial         = nomPn,
            limitStateAxial      = lsPn,
            nominalTorsion       = nomTn,
            limitStateTorsion    = lsTn,
            allowableDeflection  = demand.allowableDeflection.inInches,
            limitStateDeflection = "Deflection Limit"
        )
    }

    /**
     * Detailed evaluation applying methodology factors, for calculation reports.
     * phi/omega factors sourced from [AiscDesignFactors] registry.
     */
    override fun evaluateDetailed(
        demand: StationDemand,
        methodology: DesignMethodology
    ): StrengthDesignResult {
        val f  = AiscDesignFactors.forEditionAndMethodology(edition, methodology)
        val lb = if (demand.compressionFlange == Flange.TOP)
            demand.lbTop.inInches else demand.lbBottom.inInches
        val cb = demand.cb

        // --- Nominal capacities (full results, including traces) ---
        val flexureResult = calculateFlexureX(lb, cb)
        val shearResult   = calculateShear(isStrongAxis = memberIsStrongAxis)
        val torsionResult = calculateTorsion()
        val isAxialTension = demand.axial.pounds >= 0.0
        val axialResult = if (isAxialTension)
            calculateAxialTension()
        else
            calculateAxialCompression(lb)

        val nomMnX = flexureResult.nominal; val lsMnX = flexureResult.limitState
        val nomVnX = shearResult.nominal;   val lsVnX = shearResult.limitState
        val nomTn  = torsionResult.nominal; val lsTn  = torsionResult.limitState
        val nomPn  = axialResult.nominal;   val lsPn  = axialResult.limitState

        // --- Design capacities (factored) ---
        val designMn = f.applyToNominal(nomMnX, f.flexure)
        val designVn = f.applyToNominal(nomVnX, f.shear)
        val designTn = f.applyToNominal(nomTn, f.torsion)
        // Tension uses tensionYield factor, compression uses compression factor
        val designPn = if (isAxialTension)
            f.applyToNominal(nomPn, f.tensionYield)
        else
            f.applyToNominal(nomPn, f.compression)

        // --- Utilization ratios ---
        val ratioFlexure = if (designMn > 0) abs(demand.moment.lbIn) / designMn else 0.0
        val ratioShear   = if (designVn > 0) abs(demand.shear.pounds) / designVn else 0.0
        val ratioAxial   = if (designPn > 0) abs(demand.axial.pounds) / designPn else 0.0
        val ratioTorsion = if (designTn > 0) abs(demand.torque.lbIn) / designTn else 0.0

        return StrengthDesignResult(
            momentCheck = StrengthCheckResult(
                demand               = demand.moment,
                capacity             = Moment(designMn),
                utilization          = ratioFlexure,
                governingCombination = "Current",
                governingMode        = lsMnX,
                traces               = flexureResult.traces
            ),
            shearCheck = StrengthCheckResult(
                demand               = demand.shear,
                capacity             = Force(designVn),
                utilization          = ratioShear,
                governingCombination = "Current",
                governingMode        = lsVnX,
                traces               = shearResult.traces
            ),
            axialCheck = StrengthCheckResult(
                demand               = demand.axial,
                capacity             = Force(designPn),
                utilization          = ratioAxial,
                governingCombination = "Current",
                governingMode        = lsPn,
                traces               = axialResult.traces
            ),
            torsionCheck = StrengthCheckResult(
                demand               = demand.torque,
                capacity             = Moment(designTn),
                utilization          = ratioTorsion,
                governingCombination = "Current",
                governingMode        = lsTn,
                traces               = torsionResult.traces
            ),
            methodology      = methodology,
            designParameters = mapOf(
                "Lb" to String.format(Locale.US, "%.1f in", lb),
                "Cb" to String.format(Locale.US, "%.2f",    cb),
                "Axial" to if (isAxialTension) "Tension (D2a)" else "Compression (E3)",
                "Edition" to f.edition.name
            )
        )
    }

    /**
     * Real AISC 360 phi/omega factors per limit state, sourced from
     * [AiscDesignFactors] — used by [com.lz.solver.capacity.CapacityEngine]
     * in place of its previous hardcoded 0.90/1.67 placeholder.
     */
    override fun designFactors(methodology: DesignMethodology): DesignFactorSet {
        val f = AiscDesignFactors.forEditionAndMethodology(edition, methodology)
        return DesignFactorSet(
            methodology       = methodology,
            flexure           = f.flexure,
            shear             = f.shear,
            axialTension      = f.tensionYield,
            axialCompression  = f.compression,
            torsion           = f.torsion
        )
    }

    // ------------------------------------------------------------------
    // Chapter F — Flexure
    // ------------------------------------------------------------------

    private fun calculateFlexureX(lb: Double, cb: Double): LimitStateResult {
        if (!memberIsStrongAxis) {
            // Weak-axis orientation — the demand this calculator is asked
            // to check against "X" is actually loading the minor axis.
            // No LTB term for a doubly symmetric shape bent about its
            // minor axis, so none of this needs lb/cb.
            if (profile is SteelProfile) {
                val zy = profile.propertiesWeakAxis.z.inIn3
                val sy = profile.propertiesWeakAxis.s.inIn3
                val mpY = min(Fy * zy, 1.6 * Fy * sy)
                when (profile.shapeType) {
                    // F7 doesn't distinguish which HSS wall pair is being
                    // checked — reuse the same local-buckling logic with
                    // the flat dimensions swapped (weak-axis bending flips
                    // which pair of walls is "flange" vs "web").
                    ShapeType.RECTANGULAR_HSS -> return flexureRectangularHss(mpY, sy, swapped = true)
                    // Round HSS/pipe: D/t is orientation-independent (Sy =
                    // Sx by symmetry), so no swap needed.
                    ShapeType.ROUND_HSS, ShapeType.PIPE -> return flexureRoundHss(mpY, sy)
                    else -> { /* fall through to calculateFlexureY() below */ }
                }
            }
            val (mn, ls) = calculateFlexureY()
            return LimitStateResult(mn, ls)
        }

        val zx = profile.propertiesStrongAxis.z.inIn3
        val sx = profile.propertiesStrongAxis.s.inIn3
        val mp = Fy * zx

        return when (profile.shapeType) {

            ShapeType.WIDE_FLANGE,
            ShapeType.CHANNEL -> ltbAndFlbIShape(lb, cb, mp, sx)

            ShapeType.RECTANGULAR_HSS -> flexureRectangularHss(mp, sx)

            ShapeType.ROUND_HSS,
            ShapeType.PIPE -> flexureRoundHss(mp, sx)

            ShapeType.TEE,
            ShapeType.DOUBLE_ANGLE -> {
                // F9 — Tee and double-angle
                // Simplified: LTB not calculated here; yielding governs conservatively
                LimitStateResult(mp, "Tee/Double-Angle Yielding")
            }

            ShapeType.SINGLE_ANGLE -> {
                // F10 — Single angle
                // Conservative: use yielding only
                LimitStateResult(mp, "Single Angle Yielding")
            }

            else -> LimitStateResult(mp, "Yielding")
        }
    }

    /**
     * AISC F2/F3 — LTB and FLB for doubly symmetric I-shapes and channels.
     */
    private fun ltbAndFlbIShape(
        lb: Double,
        cb: Double,
        mp: Double,
        sx: Double
    ): LimitStateResult {
        if (profile !is SteelProfile) return LimitStateResult(mp, "Yielding")

        val iy  = profile.propertiesWeakAxis.i.inIn4
        val ry  = profile.propertiesWeakAxis.r.inInches
        val j   = profile.torsionalConstantJ
        val cw  = profile.warpingConstantCw
        val ho  = profile.depth.inInches - profile.flangeThickness.inInches
        val rts = sqrt(sqrt(iy * cw) / sx)
        val c   = if (profile.shapeType == ShapeType.CHANNEL)
            (ho / 2.0) * sqrt(iy / cw) else 1.0
        val fL  = 0.7 * Fy

        val lp  = 1.76 * ry * sqrt(E / Fy)
        val lrPart = (j * c) / (sx * ho)
        val lr  = 1.95 * rts * (E / fL) * sqrt(
            lrPart + sqrt(lrPart.pow(2) + 6.76 * (fL / E).pow(2))
        )

        // 1. Lateral-Torsional Buckling (AISC F2-1, F2-2, F2-3)
        val zx = profile.propertiesStrongAxis.z.inIn3

        val (mnLTB, lsLTB, ltbTrace) = when {
            lb <= lp -> Triple(
                mp, "Yielding",
                DesignEquationTrace(
                    symbolicEquation    = "Lb ≤ Lp → Mn = Mp (no LTB reduction)",
                    substitutedEquation = "${fmt(lb, 1)} in ≤ ${fmt(lp, 1)} in → Mn = Mp = ${fmt(Fy, 0)} × ${fmt(zx, 2)}",
                    result              = kipFt(mp),
                    units               = "kip-ft",
                    codeReference       = "AISC 360 F2-1",
                    variables           = mapOf("Lb" to lb, "Lp" to lp)
                )
            )
            lb <= lr -> {
                val mnRaw = cb * (mp - (mp - fL * sx) * ((lb - lp) / (lr - lp)))
                val mn = min(mnRaw, mp)
                Triple(
                    mn, "Inelastic LTB",
                    DesignEquationTrace(
                        symbolicEquation    = "Mn = Cb[Mp − (Mp − 0.7FySx)((Lb−Lp)/(Lr−Lp))] ≤ Mp",
                        substitutedEquation = "Mn = ${fmt(cb)}×[${fmt(mp, 0)} − (${fmt(mp, 0)} − 0.7×${fmt(Fy, 0)}×${fmt(sx, 2)})×((${fmt(lb, 1)}−${fmt(lp, 1)})/(${fmt(lr, 1)}−${fmt(lp, 1)}))]",
                        result              = kipFt(mn),
                        units               = "kip-ft",
                        codeReference       = "AISC 360 F2-2",
                        variables           = mapOf("Cb" to cb, "Lb" to lb, "Lp" to lp, "Lr" to lr, "Fy" to Fy, "Sx" to sx)
                    )
                )
            }
            else -> {
                val fcr = (cb * PI.pow(2) * E / (lb / rts).pow(2)) *
                        sqrt(1.0 + 0.078 * (j * c) / (sx * ho) * (lb / rts).pow(2))
                val mn = min(fcr * sx, mp)
                Triple(
                    mn, "Elastic LTB",
                    DesignEquationTrace(
                        symbolicEquation    = "Fcr = (Cbπ²E)/(Lb/rts)² · √(1+0.078(Jc/(Sxho))(Lb/rts)²);  Mn = Fcr·Sx ≤ Mp",
                        substitutedEquation = "Fcr = (${fmt(cb)}×π²×${fmt(E, 0)})/(${fmt(lb, 1)}/${fmt(rts, 3)})² × √(1+0.078×(${fmt(j, 3)}×${fmt(c, 3)})/(${fmt(sx, 2)}×${fmt(ho, 2)})×(${fmt(lb, 1)}/${fmt(rts, 3)})²) = ${fmt(fcr, 0)} psi",
                        result              = kipFt(mn),
                        units               = "kip-ft",
                        codeReference       = "AISC 360 F2-3, F2-4",
                        variables           = mapOf("Cb" to cb, "Lb" to lb, "rts" to rts, "J" to j, "c" to c, "Sx" to sx, "ho" to ho)
                    )
                )
            }
        }

        // 2. Flange Local Buckling (AISC F3-1, F3-2)
        val lambdaF  = profile.flangeWidth.inInches / (2 * profile.flangeThickness.inInches)
        val lambdaPf = 0.38 * sqrt(E / Fy)
        val lambdaRf = 1.0  * sqrt(E / Fy)

        val (mnFLB, lsFLB, flbTrace) = when {
            lambdaF <= lambdaPf -> Triple(
                mp, "Yielding",
                DesignEquationTrace(
                    symbolicEquation    = "λf ≤ λpf → compact flange, Mn = Mp (no FLB reduction)",
                    substitutedEquation = "λf = bf/2tf = ${fmt(lambdaF, 3)} ≤ λpf = 0.38√(E/Fy) = ${fmt(lambdaPf, 3)}",
                    result              = kipFt(mp),
                    units               = "kip-ft",
                    codeReference       = "AISC 360 B4.1 / F3",
                    variables           = mapOf("lambdaF" to lambdaF, "lambdaPf" to lambdaPf)
                )
            )
            lambdaF <= lambdaRf -> {
                val mn = mp - (mp - fL * sx) * ((lambdaF - lambdaPf) / (lambdaRf - lambdaPf))
                Triple(
                    mn, "Flange Local Buckling",
                    DesignEquationTrace(
                        symbolicEquation    = "Mn = Mp − (Mp − 0.7FySx)((λf−λpf)/(λrf−λpf))",
                        substitutedEquation = "Mn = ${fmt(mp, 0)} − (${fmt(mp, 0)} − 0.7×${fmt(Fy, 0)}×${fmt(sx, 2)})×((${fmt(lambdaF, 3)}−${fmt(lambdaPf, 3)})/(${fmt(lambdaRf, 3)}−${fmt(lambdaPf, 3)}))",
                        result              = kipFt(mn),
                        units               = "kip-ft",
                        codeReference       = "AISC 360 F3-1",
                        variables           = mapOf("lambdaF" to lambdaF, "lambdaPf" to lambdaPf, "lambdaRf" to lambdaRf, "Fy" to Fy, "Sx" to sx)
                    )
                )
            }
            else -> {
                val kc  = (4.0 / sqrt(profile.depth.inInches / profile.webThickness.inInches))
                    .coerceIn(0.35, 0.76)
                val fcr = 0.9 * E * kc / lambdaF.pow(2)
                val mn = fcr * sx
                Triple(
                    mn, "Slender Flange Buckling",
                    DesignEquationTrace(
                        symbolicEquation    = "Fcr = 0.9·E·kc/λf²;  Mn = Fcr·Sx",
                        substitutedEquation = "Fcr = 0.9×${fmt(E, 0)}×${fmt(kc, 3)}/${fmt(lambdaF, 3)}² = ${fmt(fcr, 0)} psi",
                        result              = kipFt(mn),
                        units               = "kip-ft",
                        codeReference       = "AISC 360 F3-2",
                        variables           = mapOf("kc" to kc, "lambdaF" to lambdaF, "Sx" to sx)
                    )
                )
            }
        }

        // Governing: lowest nominal capacity. Both traces are surfaced
        // together (whichever is lower governs) so the Design tab shows the
        // full LTB + FLB check, not just the winning branch.
        val traces = listOf(ltbTrace, flbTrace)
        return if (mnLTB <= mnFLB) LimitStateResult(mnLTB, lsLTB, traces)
        else LimitStateResult(mnFLB, lsFLB, traces)
    }

    /**
     * AISC F7.2/F7.3 — flange and web local buckling for square/rectangular
     * HSS and box sections, applied on top of F7.1 yielding (Mn = mp =
     * min(Fy·Zx, 1.6·Fy·Sx), already computed by the caller). Classic
     * (360-10/360-16) form of F7-2/F7-3 — AISC 360-22 rewrote these
     * algebraically to match the F3 I-shape style, but per AISC's own
     * edition-comparison notes that was a format change to the equation,
     * not a change to the underlying strength curve, and this calculator
     * doesn't otherwise branch its W-shape formulas (F2/F3/G2) by edition
     * either — same precedent followed here.
     *
     * b/t and h/t use the *flat* width/height ([SteelProfile.flatWidthB]/
     * [SteelProfile.flatHeightH]) per Table B4.1a, not the overall B/H
     * ([flangeWidth]/[depth]) — falls back to an approximation if a profile
     * predates those fields (e.g. a hand-entered custom HSS).
     */
    private fun flexureRectangularHss(mp: Double, sx: Double, swapped: Boolean = false): LimitStateResult {
        if (profile !is SteelProfile) return LimitStateResult(mp, "HSS Box Yielding")

        val t = profile.webThickness.inInches
        // b = width of the flange resisting the bending being checked, h =
        // height of the web in that same direction. For weak-axis bending
        // (swapped=true, called from calculateFlexureY) these are the
        // strong-axis's h and b respectively — the flat dimensions swap
        // roles with the bending axis.
        val bRaw = profile.flatWidthB ?: (profile.flangeWidth.inInches - 3 * t)
        val hRaw = profile.flatHeightH ?: (profile.depth.inInches - 3 * t)
        val b = if (swapped) hRaw else bRaw
        val h = if (swapped) bRaw else hRaw
        if (t <= 0.0 || b <= 0.0 || h <= 0.0) return LimitStateResult(mp, "HSS Box Yielding")

        // --- F7.2 Flange Local Buckling (compression flange, width b) ---
        val bt = b / t
        val lambdaPf = 1.12 * sqrt(E / Fy)
        val lambdaRf = 1.40 * sqrt(E / Fy)

        val (mnFlange, lsFlange, flangeTrace) = when {
            bt <= lambdaPf -> Triple(
                mp, "HSS Box Yielding",
                DesignEquationTrace(
                    symbolicEquation    = "b/t ≤ λpf → compact flange, Mn = Mp",
                    substitutedEquation = "b/t = ${fmt(bt, 2)} ≤ λpf = 1.12√(E/Fy) = ${fmt(lambdaPf, 2)}",
                    result              = kipFt(mp),
                    units               = "kip-ft",
                    codeReference       = "AISC 360 B4.1 / F7.1",
                    variables           = mapOf("b_t" to bt, "lambdaPf" to lambdaPf)
                )
            )
            bt <= lambdaRf -> {
                val mn = min(mp - (mp - Fy * sx) * (3.57 * bt * sqrt(Fy / E) - 4.0), mp)
                Triple(
                    mn, "HSS Flange Local Buckling",
                    DesignEquationTrace(
                        symbolicEquation    = "Mn = Mp − (Mp − FySx)[3.57(b/t)√(Fy/E) − 4.0] ≤ Mp",
                        substitutedEquation = "Mn = ${fmt(mp, 0)} − (${fmt(mp, 0)} − ${fmt(Fy, 0)}×${fmt(sx, 2)})×[3.57×${fmt(bt, 2)}×√(${fmt(Fy, 0)}/${fmt(E, 0)}) − 4.0]",
                        result              = kipFt(mn),
                        units               = "kip-ft",
                        codeReference       = "AISC 360 F7-2",
                        variables           = mapOf("b_t" to bt, "Fy" to Fy, "Sx" to sx)
                    )
                )
            }
            else -> {
                val be = min(1.92 * t * sqrt(E / Fy) * (1.0 - (0.38 / bt) * sqrt(E / Fy)), b)
                // Effective Sx approximated by deducting the lost flange
                // material (b - be, both flanges) at its real lever arm from
                // the tabulated (accurate, corner-radius-inclusive) Ix,
                // rather than a blunt Sx×(be/b) scaling.
                val ix = if (swapped) profile.propertiesWeakAxis.i.inIn4 else profile.propertiesStrongAxis.i.inIn4
                val c = (if (swapped) profile.flangeWidth.inInches else profile.depth.inInches) / 2.0
                val ixEff = max(ix - 2.0 * (b - be) * t * c.pow(2), 0.0)
                val se = if (c > 0.0) ixEff / c else sx
                val mn = Fy * se
                Triple(
                    mn, "HSS Slender Flange Buckling",
                    DesignEquationTrace(
                        symbolicEquation    = "be = 1.92t√(E/Fy)[1−(0.38/(b/t))√(E/Fy)] ≤ b;  Mn = Fy·Se",
                        substitutedEquation = "be = ${fmt(be, 3)} in (b=${fmt(b, 3)} in) → Ieff = Ix−2(b−be)t·c² = ${fmt(ixEff, 1)} in⁴ → Se = ${fmt(se, 2)} in³",
                        result              = kipFt(mn),
                        units               = "kip-ft",
                        codeReference       = "AISC 360 F7-3, F7-4",
                        variables           = mapOf("b_t" to bt, "be" to be, "Se" to se)
                    )
                )
            }
        }

        // --- F7.3 Web Local Buckling (web height h) ---
        val ht = h / t
        val lambdaPw = 2.42 * sqrt(E / Fy)
        val lambdaRw = 5.70 * sqrt(E / Fy)

        val (mnWeb, lsWeb, webTrace) = when {
            ht <= lambdaPw -> Triple(
                mp, "HSS Box Yielding",
                DesignEquationTrace(
                    symbolicEquation    = "h/t ≤ λpw → compact web, Mn = Mp",
                    substitutedEquation = "h/t = ${fmt(ht, 2)} ≤ λpw = 2.42√(E/Fy) = ${fmt(lambdaPw, 2)}",
                    result              = kipFt(mp),
                    units               = "kip-ft",
                    codeReference       = "AISC 360 B4.1 / F7.1",
                    variables           = mapOf("h_t" to ht, "lambdaPw" to lambdaPw)
                )
            )
            ht <= lambdaRw -> {
                val mn = min(mp - (mp - Fy * sx) * (0.305 * ht * sqrt(Fy / E) - 0.738), mp)
                Triple(
                    mn, "HSS Web Local Buckling",
                    DesignEquationTrace(
                        symbolicEquation    = "Mn = Mp − (Mp − FySx)[0.305(h/t)√(Fy/E) − 0.738] ≤ Mp",
                        substitutedEquation = "Mn = ${fmt(mp, 0)} − (${fmt(mp, 0)} − ${fmt(Fy, 0)}×${fmt(sx, 2)})×[0.305×${fmt(ht, 2)}×√(${fmt(Fy, 0)}/${fmt(E, 0)}) − 0.738]",
                        result              = kipFt(mn),
                        units               = "kip-ft",
                        codeReference       = "AISC 360 F7-6",
                        variables           = mapOf("h_t" to ht, "Fy" to Fy, "Sx" to sx)
                    )
                )
            }
            else -> {
                // Slender webs in flexure sit outside the standard AISC HSS
                // product range (no cataloged wall-thickness/depth
                // combination at typical Fy reaches h/t this high) —
                // extend the noncompact formula as a conservative fallback
                // rather than leaving this branch undefined.
                val mn = max(min(mp - (mp - Fy * sx) * (0.305 * ht * sqrt(Fy / E) - 0.738), mp), 0.0)
                Triple(
                    mn, "HSS Web Local Buckling (slender, extrapolated)",
                    DesignEquationTrace(
                        symbolicEquation    = "h/t > λrw — outside standard HSS product range; F7.3 noncompact formula extrapolated conservatively",
                        substitutedEquation = "h/t = ${fmt(ht, 2)} > λrw = 5.70√(E/Fy) = ${fmt(lambdaRw, 2)}",
                        result              = kipFt(mn),
                        units               = "kip-ft",
                        codeReference       = "AISC 360 F7.3 (extrapolated)",
                        variables           = mapOf("h_t" to ht, "lambdaRw" to lambdaRw)
                    )
                )
            }
        }

        val traces = listOf(flangeTrace, webTrace)
        return if (mnFlange <= mnWeb) LimitStateResult(mnFlange, lsFlange, traces)
        else LimitStateResult(mnWeb, lsWeb, traces)
    }

    /**
     * AISC F8.2/F8.3 — local buckling for round HSS and pipe, on top of
     * F8.1 yielding (Mn = Mp, already computed by the caller as `mp`).
     * Unchanged across 360-10/16/22 editions.
     */
    private fun flexureRoundHss(mp: Double, sx: Double): LimitStateResult {
        if (profile !is SteelProfile) return LimitStateResult(mp, "Round HSS Yielding")

        val t = profile.webThickness.inInches
        val d = profile.depth.inInches
        if (t <= 0.0 || d <= 0.0) return LimitStateResult(mp, "Round HSS Yielding")

        val dt = d / t
        val lambdaP = 0.07 * E / Fy
        val lambdaR = 0.31 * E / Fy

        return when {
            dt <= lambdaP -> LimitStateResult(
                mp, "Round HSS Yielding",
                listOf(DesignEquationTrace(
                    symbolicEquation    = "D/t ≤ λp → compact, Mn = Mp",
                    substitutedEquation = "D/t = ${fmt(dt, 1)} ≤ λp = 0.07E/Fy = ${fmt(lambdaP, 1)}",
                    result              = kipFt(mp),
                    units               = "kip-ft",
                    codeReference       = "AISC 360 F8.1, F8-1",
                    variables           = mapOf("D_t" to dt, "lambdaP" to lambdaP)
                ))
            )
            dt <= lambdaR -> {
                val mn = (0.021 * E / dt + Fy) * sx
                LimitStateResult(
                    mn, "Round HSS Local Buckling",
                    listOf(DesignEquationTrace(
                        symbolicEquation    = "Mn = [0.021E/(D/t) + Fy]·S",
                        substitutedEquation = "Mn = [0.021×${fmt(E, 0)}/${fmt(dt, 1)} + ${fmt(Fy, 0)}]×${fmt(sx, 2)}",
                        result              = kipFt(mn),
                        units               = "kip-ft",
                        codeReference       = "AISC 360 F8-2",
                        variables           = mapOf("D_t" to dt, "Fy" to Fy, "S" to sx)
                    ))
                )
            }
            else -> {
                val fcr = 0.33 * E / dt
                val mn = fcr * sx
                LimitStateResult(
                    mn, "Round HSS Slender Buckling",
                    listOf(DesignEquationTrace(
                        symbolicEquation    = "Fcr = 0.33E/(D/t);  Mn = Fcr·S",
                        substitutedEquation = "Fcr = 0.33×${fmt(E, 0)}/${fmt(dt, 1)} = ${fmt(fcr, 0)} psi",
                        result              = kipFt(mn),
                        units               = "kip-ft",
                        codeReference       = "AISC 360 F8-3",
                        variables           = mapOf("D_t" to dt, "Fcr" to fcr, "S" to sx)
                    ))
                )
            }
        }
    }

    private fun calculateFlexureY(): Pair<Double, String> {
        val zy = profile.propertiesWeakAxis.z.inIn3
        val sy = profile.propertiesWeakAxis.s.inIn3
        val mp = min(Fy * zy, 1.6 * Fy * sy)

        if (profile !is SteelProfile) return mp to "Weak-Axis Yielding"
        if (profile.shapeType != ShapeType.WIDE_FLANGE &&
            profile.shapeType != ShapeType.CHANNEL) return mp to "Weak-Axis Yielding"

        val lambdaF  = profile.flangeWidth.inInches / (2 * profile.flangeThickness.inInches)
        val lambdaPf = 0.38 * sqrt(E / Fy)
        val lambdaRf = 1.0  * sqrt(E / Fy)

        return when {
            lambdaF <= lambdaPf -> mp to "Weak-Axis Yielding"
            lambdaF <= lambdaRf -> {
                val mn = mp - (mp - 0.7 * Fy * sy) *
                        ((lambdaF - lambdaPf) / (lambdaRf - lambdaPf))
                min(mn, mp) to "Weak-Axis FLB"
            }
            else -> {
                val fcr = 0.69 * E / lambdaF.pow(2)
                min(fcr * sy, mp) to "Weak-Axis Slender FLB"
            }
        }
    }

    // ------------------------------------------------------------------
    // Chapter G — Shear
    // ------------------------------------------------------------------

    private fun calculateShear(isStrongAxis: Boolean): LimitStateResult {
        if (profile !is SteelProfile) {
            val aw = profile.area.inIn2 * 0.5
            return LimitStateResult(0.6 * Fy * aw, "Shear Yielding")
        }

        return when (profile.shapeType) {

            ShapeType.WIDE_FLANGE,
            ShapeType.CHANNEL -> {
                if (isStrongAxis) {
                    // G2 — Web shear
                    val aw    = profile.depth.inInches * profile.webThickness.inInches
                    val hw    = profile.depth.inInches - 2 * profile.flangeThickness.inInches
                    val hTw   = hw / profile.webThickness.inInches
                    val kv    = 5.34
                    val lim1  = 1.10 * sqrt(kv * E / Fy)
                    val lim2  = 1.37 * sqrt(kv * E / Fy)
                    val (cv1, cvEqRef, cvSymbolic) = when {
                        hTw <= lim1 -> Triple(1.0, "AISC 360 G2-3", "h/tw ≤ 1.10√(kvE/Fy) → Cv1 = 1.0")
                        hTw <= lim2 -> Triple(lim1 / hTw, "AISC 360 G2-4", "Cv1 = 1.10√(kvE/Fy) / (h/tw)")
                        else        -> Triple(1.51 * kv * E / (hTw.pow(2) * Fy), "AISC 360 G2-5", "Cv1 = 1.51·kv·E / ((h/tw)²·Fy)")
                    }
                    val vn    = 0.6 * Fy * aw * cv1
                    val ls    = if (cv1 == 1.0) "Web Shear Yielding" else "Web Shear Buckling"
                    val trace = DesignEquationTrace(
                        symbolicEquation    = "Vn = 0.6·Fy·Aw·Cv1  [$cvSymbolic]",
                        substitutedEquation = "Vn = 0.6×${fmt(Fy, 0)}×${fmt(aw, 3)}×${fmt(cv1, 3)}  (h/tw=${fmt(hTw, 1)})",
                        result              = kips(vn),
                        units               = "kips",
                        codeReference       = "AISC 360 G2-1, $cvEqRef",
                        variables           = mapOf("Fy" to Fy, "Aw" to aw, "Cv1" to cv1, "h_tw" to hTw)
                    )
                    LimitStateResult(vn, ls, listOf(trace))
                } else {
                    // G6 — Flange shear for weak axis
                    val aw = 2.0 * profile.flangeWidth.inInches * profile.flangeThickness.inInches
                    LimitStateResult(0.6 * Fy * aw, "Flange Shear Yielding")
                }
            }

            ShapeType.RECTANGULAR_HSS -> {
                // G4 — HSS/box: both walls resist shear, same Cv2 buckling-
                // reduction structure as G2's I-shape web (kv fixed at 5.0
                // instead of G2's own web-plate-buckling-coefficient value).
                val t = profile.webThickness.inInches
                val h = if (isStrongAxis) (profile.flatHeightH ?: profile.depth.inInches)
                        else (profile.flatWidthB ?: profile.flangeWidth.inInches)
                val aw = 2.0 * h * t
                val kv = 5.0
                val hT = h / t
                val lim1 = 1.10 * sqrt(kv * E / Fy)
                val lim2 = 1.37 * sqrt(kv * E / Fy)
                val (cv2, cvEqRef, cvSymbolic) = when {
                    hT <= lim1 -> Triple(1.0, "AISC 360 G4-2", "h/t ≤ 1.10√(kvE/Fy) → Cv2 = 1.0")
                    hT <= lim2 -> Triple(lim1 / hT, "AISC 360 G4-3", "Cv2 = 1.10√(kvE/Fy) / (h/t)")
                    else       -> Triple(1.51 * kv * E / (hT.pow(2) * Fy), "AISC 360 G4-4", "Cv2 = 1.51·kv·E / ((h/t)²·Fy)")
                }
                val vn = 0.6 * Fy * aw * cv2
                val ls = if (cv2 == 1.0) "HSS Shear Yielding" else "HSS Shear Buckling"
                val trace = DesignEquationTrace(
                    symbolicEquation    = "Vn = 0.6·Fy·Aw·Cv2  [$cvSymbolic]",
                    substitutedEquation = "Vn = 0.6×${fmt(Fy, 0)}×${fmt(aw, 3)}×${fmt(cv2, 3)}  (h/t=${fmt(hT, 1)})",
                    result              = kips(vn),
                    units               = "kips",
                    codeReference       = "AISC 360 G4-1, $cvEqRef",
                    variables           = mapOf("Fy" to Fy, "Aw" to aw, "Cv2" to cv2, "h_t" to hT)
                )
                LimitStateResult(vn, ls, listOf(trace))
            }

            ShapeType.ROUND_HSS,
            ShapeType.PIPE -> {
                // G5 — Round HSS and pipe: yielding only. Fcr-based shear
                // buckling per G5 needs Lv (clear distance to the point of
                // zero shear along the member) — not available at this
                // per-section capacity level (StationDemand carries no Lv),
                // so this is conservative-by-omission for very thin-wall,
                // long-unbraced round HSS, same simplification already
                // accepted elsewhere in this file for other shape/limit-
                // state combinations outside the calculator's current scope.
                val aw = profile.area.inIn2 / 2.0
                val vn = 0.6 * Fy * aw
                val trace = DesignEquationTrace(
                    symbolicEquation    = "Vn = 0.6·Fy·Ag/2  (yielding; Fcr shear-buckling per G5 needs Lv, not available here)",
                    substitutedEquation = "Vn = 0.6×${fmt(Fy, 0)}×${fmt(aw, 3)}",
                    result              = kips(vn),
                    units               = "kips",
                    codeReference       = "AISC 360 G5 (simplified)",
                    variables           = mapOf("Fy" to Fy, "Aw" to aw)
                )
                LimitStateResult(vn, "Round HSS Shear Yielding", listOf(trace))
            }

            ShapeType.TEE -> {
                // G5 — Tee stem shear
                val aw = profile.depth.inInches * profile.webThickness.inInches
                LimitStateResult(0.6 * Fy * aw, "Tee Stem Shear Yielding")
            }

            ShapeType.SINGLE_ANGLE -> {
                // G4 — Single angle: conservative
                val aw = profile.area.inIn2 * 0.5
                LimitStateResult(0.6 * Fy * aw, "Single Angle Shear Yielding")
            }

            else -> {
                val aw = profile.area.inIn2 * 0.5
                LimitStateResult(0.6 * Fy * aw, "Shear Yielding")
            }
        }
    }

    // ------------------------------------------------------------------
    // Chapter E — Axial Compression
    // ------------------------------------------------------------------

    private fun calculateAxialCompression(lb: Double): LimitStateResult {
        val ag = profile.area.inIn2
        val rx = profile.propertiesStrongAxis.r.inInches
        val ry = profile.propertiesWeakAxis.r.inInches

        val klr = max(lb / rx, lb / ry)

        if (klr < 1e-6) {
            val pn = Fy * ag
            val trace = DesignEquationTrace(
                symbolicEquation    = "Lb → 0 (no unbraced length) → Pn = Fy·Ag",
                substitutedEquation = "Pn = ${fmt(Fy, 0)} psi × ${fmt(ag, 3)} in²",
                result              = kips(pn),
                units               = "kips",
                codeReference       = "AISC 360 E3",
                variables           = mapOf("Fy" to Fy, "Ag" to ag)
            )
            return LimitStateResult(pn, "Axial Yielding", listOf(trace))
        }

        val fe  = PI.pow(2) * E / klr.pow(2)
        val transition = 4.71 * sqrt(E / Fy)
        val (fcr, branchSymbolic, branchRef) = if (klr <= transition) {
            Triple(0.658.pow(Fy / fe) * Fy, "Fcr = 0.658^(Fy/Fe) · Fy", "AISC 360 E3-2")
        } else {
            Triple(0.877 * fe, "Fcr = 0.877 · Fe", "AISC 360 E3-3")
        }
        val pn = fcr * ag
        val trace = DesignEquationTrace(
            symbolicEquation    = "Fe = π²E/(KL/r)²;  $branchSymbolic;  Pn = Fcr·Ag",
            substitutedEquation = "Fe = π²×${fmt(E, 0)}/${fmt(klr, 2)}² = ${fmt(fe, 0)} psi → Fcr = ${fmt(fcr, 0)} psi → Pn = ${fmt(fcr, 0)}×${fmt(ag, 3)}",
            result              = kips(pn),
            units               = "kips",
            codeReference       = "AISC 360 E3-1, $branchRef",
            variables           = mapOf("KL_r" to klr, "Fe" to fe, "Fcr" to fcr, "Ag" to ag)
        )
        return LimitStateResult(pn, "Flexural Buckling (E3)", listOf(trace))
    }

    private fun calculateAxialTension(): LimitStateResult {
        val ag = profile.area.inIn2
        // Chapter D2(a) — Yielding on gross section
        // Rupture on net section (D2(b)) requires net area Ae which depends on
        // connection details not available at the section level. Yielding governs
        // as the section-level check; rupture is a connection-level check.
        val pn = Fy * ag
        val trace = DesignEquationTrace(
            symbolicEquation    = "Pn = Fy·Ag  (gross-section yielding; net-section rupture is a connection-level check)",
            substitutedEquation = "Pn = ${fmt(Fy, 0)} psi × ${fmt(ag, 3)} in²",
            result              = kips(pn),
            units               = "kips",
            codeReference       = "AISC 360 D2-1",
            variables           = mapOf("Fy" to Fy, "Ag" to ag)
        )
        return LimitStateResult(pn, "Tension Yielding (D2a)", listOf(trace))
    }

    // ------------------------------------------------------------------
    // Chapter H3 — Torsion
    // ------------------------------------------------------------------

    private fun calculateTorsion(): LimitStateResult {
        if (profile !is SteelProfile) {
            return LimitStateResult(0.0, "Torsion N/A")
        }

        return when (profile.shapeType) {

            ShapeType.RECTANGULAR_HSS -> {
                // H3.1 — Closed section: Tn = 0.6·Fy·C, C = 2(B−t)(H−t)t
                val t = profile.webThickness.inInches
                val b = profile.flangeWidth.inInches
                val h = profile.depth.inInches
                val c = 2.0 * (b - t) * (h - t) * t
                val tn = 0.6 * Fy * c
                val trace = DesignEquationTrace(
                    symbolicEquation    = "C = 2(B−t)(H−t)t;  Tn = 0.6·Fy·C",
                    substitutedEquation = "C = 2×(${fmt(b, 3)}−${fmt(t, 3)})×(${fmt(h, 3)}−${fmt(t, 3)})×${fmt(t, 3)} = ${fmt(c, 3)} in³ → Tn = 0.6×${fmt(Fy, 0)}×${fmt(c, 3)}",
                    result              = kipFt(tn),
                    units               = "kip-ft",
                    codeReference       = "AISC 360 H3.1",
                    variables           = mapOf("B" to b, "H" to h, "t" to t, "C" to c, "Fy" to Fy)
                )
                LimitStateResult(tn, "HSS Torsional Yielding (H3.1)", listOf(trace))
            }

            ShapeType.ROUND_HSS,
            ShapeType.PIPE -> {
                // H3.1 — Round HSS and pipe: Tn = 0.6·Fy·C, C = π(D−t)²t/2
                val t = profile.webThickness.inInches
                val d = profile.depth.inInches
                val c = PI * (d - t).pow(2) * t / 2.0
                val tn = 0.6 * Fy * c
                val trace = DesignEquationTrace(
                    symbolicEquation    = "C = π(D−t)²t/2;  Tn = 0.6·Fy·C",
                    substitutedEquation = "C = π×(${fmt(d, 3)}−${fmt(t, 3)})²×${fmt(t, 3)}/2 = ${fmt(c, 3)} in³ → Tn = 0.6×${fmt(Fy, 0)}×${fmt(c, 3)}",
                    result              = kipFt(tn),
                    units               = "kip-ft",
                    codeReference       = "AISC 360 H3.1",
                    variables           = mapOf("D" to d, "t" to t, "C" to c, "Fy" to Fy)
                )
                LimitStateResult(tn, "Round HSS Torsional Yielding (H3.1)", listOf(trace))
            }

            ShapeType.WIDE_FLANGE,
            ShapeType.CHANNEL,
            ShapeType.TEE,
            ShapeType.SINGLE_ANGLE,
            ShapeType.DOUBLE_ANGLE -> {
                // Open section — AISC Design Guide 9 / simplified
                // Tn = 0.6 * Fy * J for open sections (conservative; full H3.2 needs Cw)
                val j = profile.torsionalConstantJ
                val tn = 0.6 * Fy * j
                val trace = DesignEquationTrace(
                    symbolicEquation    = "Tn = 0.6·Fy·J  (open-section approximation — pure St. Venant torsion, warping restraint per full H3.2 not included)",
                    substitutedEquation = "Tn = 0.6×${fmt(Fy, 0)} psi × ${fmt(j, 4)} in⁴",
                    result              = kipFt(tn),
                    units               = "kip-ft",
                    codeReference       = "AISC 360 H3.2 (simplified)",
                    variables           = mapOf("Fy" to Fy, "J" to j)
                )
                LimitStateResult(tn, "Open Section Torsional Yielding (H3.2 Simplified)", listOf(trace))
            }

            else -> {
                val j = profile.torsionalConstantJ
                LimitStateResult(0.6 * Fy * j, "Torsional Yielding")
            }
        }
    }
}