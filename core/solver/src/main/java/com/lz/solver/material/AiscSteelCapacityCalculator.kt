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
    private val edition: AiscEdition = AiscEdition.AISC_360_22
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
        val (nomVnX,  lsVnX)  = calculateShear(isStrongAxis = true)
        val (nomVnY,  lsVnY)  = calculateShear(isStrongAxis = false)
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
        val shearResult   = calculateShear(isStrongAxis = true)
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
        val zx = profile.propertiesStrongAxis.z.inIn3
        val sx = profile.propertiesStrongAxis.s.inIn3
        val mp = Fy * zx

        return when (profile.shapeType) {

            ShapeType.WIDE_FLANGE,
            ShapeType.CHANNEL -> ltbAndFlbIShape(lb, cb, mp, sx)

            ShapeType.RECTANGULAR_HSS -> {
                // F7 — Box sections and HSS (trace generation out of scope for
                // this pass — see AiscSteelCapacityCalculator handoff notes)
                val mn = min(mp, 1.6 * Fy * sx)
                LimitStateResult(mn, "HSS Box Yielding")
            }

            ShapeType.ROUND_HSS,
            ShapeType.PIPE -> {
                // F8 — Round HSS and pipe
                // D/t check for local buckling omitted here; use mp as first pass
                LimitStateResult(mp, "Round HSS Yielding")
            }

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
                // G5 — HSS rectangular: both walls resist shear
                val t  = profile.webThickness.inInches
                val h  = if (isStrongAxis) profile.depth.inInches else profile.flangeWidth.inInches
                val aw = 2.0 * h * t
                LimitStateResult(0.6 * Fy * aw, "HSS Shear Yielding")
            }

            ShapeType.ROUND_HSS,
            ShapeType.PIPE -> {
                // G6 — Round HSS and pipe
                val aw = profile.area.inIn2 / 2.0
                LimitStateResult(0.6 * Fy * aw, "Round HSS Shear Yielding")
            }

            ShapeType.TEE -> {
                // G5 — Tee stem shear
                val aw = if (profile is SteelProfile)
                    profile.depth.inInches * profile.webThickness.inInches
                else profile.area.inIn2 * 0.5
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
                // H3.1 — Closed section: Tn = Fcr * C
                val t = profile.webThickness.inInches
                val b = profile.flangeWidth.inInches
                val h = profile.depth.inInches
                val c = 2.0 * (b - t) * (h - t) * t
                LimitStateResult(0.6 * Fy * c, "HSS Torsional Yielding (H3.1)")
            }

            ShapeType.ROUND_HSS,
            ShapeType.PIPE -> {
                // H3.1 — Round HSS and pipe
                val t = profile.webThickness.inInches
                val d = profile.depth.inInches
                val c = PI * (d - t).pow(2) * t / 2.0
                LimitStateResult(0.6 * Fy * c, "Round HSS Torsional Yielding (H3.1)")
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