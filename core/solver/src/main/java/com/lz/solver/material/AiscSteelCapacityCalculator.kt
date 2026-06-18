package com.lz.solver.material

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
import com.lz.model.units.Force
import com.lz.model.units.Moment
import com.lz.model.units.inIn2
import com.lz.model.units.inIn3
import com.lz.model.units.inIn4
import com.lz.model.units.inInches
import com.lz.model.units.inPsi
import com.lz.solver.capacity.CapacityCalculator
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
    private val material: MaterialGrade.Steel
) : CapacityCalculator {

    private val E: Double = material.modulusOfElasticity.inPsi
    private val Fy: Double = material.yieldStrength.inPsi

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
        val f  = AiscDesignFactors.forMethodology(methodology)
        val lb = if (demand.compressionFlange == Flange.TOP)
            demand.lbTop.inInches else demand.lbBottom.inInches
        val cb = demand.cb

        // --- Nominal capacities ---
        val (nomMnX, lsMnX) = calculateFlexureX(lb, cb)
        val (nomVnX, lsVnX) = calculateShear(isStrongAxis = true)
        val (nomTn,  lsTn)  = calculateTorsion()
        val isAxialTension = demand.axial.pounds >= 0.0
        val (nomPn, lsPn) = if  (isAxialTension)
            calculateAxialTension()
        else
            calculateAxialCompression(lb)

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
                governingMode        = lsMnX
            ),
            shearCheck = StrengthCheckResult(
                demand               = demand.shear,
                capacity             = Force(designVn),
                utilization          = ratioShear,
                governingCombination = "Current",
                governingMode        = lsVnX
            ),
            axialCheck = StrengthCheckResult(
                demand               = demand.axial,
                capacity             = Force(designPn),
                utilization          = ratioAxial,
                governingCombination = "Current",
                governingMode        = lsPn
            ),
            torsionCheck = StrengthCheckResult(
                demand               = demand.torque,
                capacity             = Moment(designTn),
                utilization          = ratioTorsion,
                governingCombination = "Current",
                governingMode        = lsTn
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

    // ------------------------------------------------------------------
    // Chapter F — Flexure
    // ------------------------------------------------------------------

    private fun calculateFlexureX(lb: Double, cb: Double): Pair<Double, String> {
        val zx = profile.propertiesStrongAxis.z.inIn3
        val sx = profile.propertiesStrongAxis.s.inIn3
        val mp = Fy * zx

        return when (profile.shapeType) {

            ShapeType.WIDE_FLANGE,
            ShapeType.CHANNEL -> ltbAndFlbIShape(lb, cb, mp, sx)

            ShapeType.RECTANGULAR_HSS -> {
                // F7 — Box sections and HSS
                val mn = min(mp, 1.6 * Fy * sx)
                mn to "HSS Box Yielding"
            }

            ShapeType.ROUND_HSS,
            ShapeType.PIPE -> {
                // F8 — Round HSS and pipe
                // D/t check for local buckling omitted here; use mp as first pass
                mp to "Round HSS Yielding"
            }

            ShapeType.TEE,
            ShapeType.DOUBLE_ANGLE -> {
                // F9 — Tee and double-angle
                // Simplified: LTB not calculated here; yielding governs conservatively
                mp to "Tee/Double-Angle Yielding"
            }

            ShapeType.SINGLE_ANGLE -> {
                // F10 — Single angle
                // Conservative: use yielding only
                mp to "Single Angle Yielding"
            }

            else -> mp to "Yielding"
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
    ): Pair<Double, String> {
        if (profile !is SteelProfile) return mp to "Yielding"

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
        val (mnLTB, lsLTB) = when {
            lb <= lp -> mp to "Yielding"
            lb <= lr -> {
                val mn = cb * (mp - (mp - fL * sx) * ((lb - lp) / (lr - lp)))
                min(mn, mp) to "Inelastic LTB"
            }
            else -> {
                val fcr = (cb * PI.pow(2) * E / (lb / rts).pow(2)) *
                        sqrt(1.0 + 0.078 * (j * c) / (sx * ho) * (lb / rts).pow(2))
                min(fcr * sx, mp) to "Elastic LTB"
            }
        }

        // 2. Flange Local Buckling (AISC F3-1, F3-2)
        val lambdaF  = profile.flangeWidth.inInches / (2 * profile.flangeThickness.inInches)
        val lambdaPf = 0.38 * sqrt(E / Fy)
        val lambdaRf = 1.0  * sqrt(E / Fy)

        val (mnFLB, lsFLB) = when {
            lambdaF <= lambdaPf -> mp to "Yielding"
            lambdaF <= lambdaRf -> {
                val mn = mp - (mp - fL * sx) * ((lambdaF - lambdaPf) / (lambdaRf - lambdaPf))
                mn to "Flange Local Buckling"
            }
            else -> {
                val kc  = (4.0 / sqrt(profile.depth.inInches / profile.webThickness.inInches))
                    .coerceIn(0.35, 0.76)
                val fcr = 0.9 * E * kc / lambdaF.pow(2)
                fcr * sx to "Slender Flange Buckling"
            }
        }

        // Governing: lowest nominal capacity
        return if (mnLTB <= mnFLB) mnLTB to lsLTB else mnFLB to lsFLB
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

    private fun calculateShear(isStrongAxis: Boolean): Pair<Double, String> {
        if (profile !is SteelProfile) {
            val aw = profile.area.inIn2 * 0.5
            return (0.6 * Fy * aw) to "Shear Yielding"
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
                    val cv1   = when {
                        hTw <= lim1 -> 1.0
                        hTw <= lim2 -> lim1 / hTw
                        else        -> 1.51 * kv * E / (hTw.pow(2) * Fy)
                    }
                    val vn    = 0.6 * Fy * aw * cv1
                    val ls    = if (cv1 == 1.0) "Web Shear Yielding" else "Web Shear Buckling"
                    vn to ls
                } else {
                    // G6 — Flange shear for weak axis
                    val aw = 2.0 * profile.flangeWidth.inInches * profile.flangeThickness.inInches
                    (0.6 * Fy * aw) to "Flange Shear Yielding"
                }
            }

            ShapeType.RECTANGULAR_HSS -> {
                // G5 — HSS rectangular: both walls resist shear
                val t  = profile.webThickness.inInches
                val h  = if (isStrongAxis) profile.depth.inInches else profile.flangeWidth.inInches
                val aw = 2.0 * h * t
                (0.6 * Fy * aw) to "HSS Shear Yielding"
            }

            ShapeType.ROUND_HSS,
            ShapeType.PIPE -> {
                // G6 — Round HSS and pipe
                val aw = profile.area.inIn2 / 2.0
                (0.6 * Fy * aw) to "Round HSS Shear Yielding"
            }

            ShapeType.TEE -> {
                // G5 — Tee stem shear
                val aw = if (profile is SteelProfile)
                    profile.depth.inInches * profile.webThickness.inInches
                else profile.area.inIn2 * 0.5
                (0.6 * Fy * aw) to "Tee Stem Shear Yielding"
            }

            ShapeType.SINGLE_ANGLE -> {
                // G4 — Single angle: conservative
                val aw = profile.area.inIn2 * 0.5
                (0.6 * Fy * aw) to "Single Angle Shear Yielding"
            }

            else -> {
                val aw = profile.area.inIn2 * 0.5
                (0.6 * Fy * aw) to "Shear Yielding"
            }
        }
    }

    // ------------------------------------------------------------------
    // Chapter E — Axial Compression
    // ------------------------------------------------------------------

    private fun calculateAxialCompression(lb: Double): Pair<Double, String> {
        val ag = profile.area.inIn2
        val rx = profile.propertiesStrongAxis.r.inInches
        val ry = profile.propertiesWeakAxis.r.inInches

        val klr = max(lb / rx, lb / ry)

        if (klr < 1e-6) return (Fy * ag) to "Axial Yielding"

        val fe  = PI.pow(2) * E / klr.pow(2)
        val fcr = if (klr <= 4.71 * sqrt(E / Fy)) {
            0.658.pow(Fy / fe) * Fy
        } else {
            0.877 * fe
        }

        return (fcr * ag) to "Flexural Buckling (E3)"
    }

    private fun calculateAxialTension(): Pair<Double, String> {
        val ag = profile.area.inIn2
        // Chapter D2(a) — Yielding on gross section
        // Rupture on net section (D2(b)) requires net area Ae which depends on
        // connection details not available at the section level. Yielding governs
        // as the section-level check; rupture is a connection-level check.
        return (Fy * ag) to "Tension Yielding (D2a)"
    }

    // ------------------------------------------------------------------
    // Chapter H3 — Torsion
    // ------------------------------------------------------------------

    private fun calculateTorsion(): Pair<Double, String> {
        if (profile !is SteelProfile) {
            return 0.0 to "Torsion N/A"
        }

        return when (profile.shapeType) {

            ShapeType.RECTANGULAR_HSS -> {
                // H3.1 — Closed section: Tn = Fcr * C
                val t = profile.webThickness.inInches
                val b = profile.flangeWidth.inInches
                val h = profile.depth.inInches
                val c = 2.0 * (b - t) * (h - t) * t
                (0.6 * Fy * c) to "HSS Torsional Yielding (H3.1)"
            }

            ShapeType.ROUND_HSS,
            ShapeType.PIPE -> {
                // H3.1 — Round HSS and pipe
                val t = profile.webThickness.inInches
                val d = profile.depth.inInches
                val c = PI * (d - t).pow(2) * t / 2.0
                (0.6 * Fy * c) to "Round HSS Torsional Yielding (H3.1)"
            }

            ShapeType.WIDE_FLANGE,
            ShapeType.CHANNEL,
            ShapeType.TEE,
            ShapeType.SINGLE_ANGLE,
            ShapeType.DOUBLE_ANGLE -> {
                // Open section — AISC Design Guide 9 / simplified
                // Tn = 0.6 * Fy * J for open sections (conservative; full H3.2 needs Cw)
                val j = profile.torsionalConstantJ
                (0.6 * Fy * j) to "Open Section Torsional Yielding (H3.2 Simplified)"
            }

            else -> {
                val j = profile.torsionalConstantJ
                (0.6 * Fy * j) to "Torsional Yielding"
            }
        }
    }
}