package com.lz.vectos.domain.structural.aisc

import com.lz.vectos.domain.beam.SectionProfile
import com.lz.vectos.domain.beam.ShapeType
import com.lz.vectos.domain.structural.MaterialGrade
import com.lz.vectos.domain.structural.RawCapacityResult
import com.lz.vectos.domain.structural.StationDemand
import com.lz.vectos.domain.structural.Flange
import com.lz.vectos.domain.structural.CapacityCalculator
import com.lz.vectos.domain.structural.PointCapacityResult
import com.lz.vectos.domain.structural.StrengthDesignResult
import com.lz.vectos.domain.structural.DesignMethodology
import com.lz.vectos.domain.structural.CapacityEngine
import com.lz.vectos.domain.structural.analysis.BeamAnalysisResult
import com.lz.vectos.domain.units.*
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.abs

class AiscSteelCapacityCalculator(
    private val profile: SectionProfile,
    private val material: MaterialGrade.Steel
) : CapacityCalculator {
    private val E = material.modulusOfElasticity.inPsi
    val Fy = material.yieldStrength.inPsi

    override fun evaluateAll(
        analysisResult: BeamAnalysisResult,
        methodology: DesignMethodology
    ): List<PointCapacityResult> {
        return CapacityEngine.evaluate(
            demands = analysisResult.spanResults.flatMap { it.stationDemands },
            section = profile,
            methodology = methodology,
            capacityCalculator = { evaluate(it) }
        )
    }

    override fun evaluate(demand: StationDemand): RawCapacityResult {
        val lb = if (demand.compressionFlange == Flange.TOP) demand.lbTop.inInches else demand.lbBottom.inInches
        val cb = demand.cb

        val flexureX = calculateFlexureX(lb, cb)
        val flexureY = calculateFlexureY()
        val shearX = calculateShear(isStrongAxis = true)
        val shearY = calculateShear(isStrongAxis = false)
        val axial = calculateAxial(lb)
        val torsion = calculateTorsion()

        return RawCapacityResult(
            nominalFlexureX = flexureX.first,
            limitStateFlexureX = flexureX.second,
            nominalFlexureY = flexureY.first,
            limitStateFlexureY = flexureY.second,
            nominalShearX = shearX.first,
            limitStateShearX = shearX.second,
            nominalShearY = shearY.first,
            limitStateShearY = shearY.second,
            nominalAxial = axial.first,
            limitStateAxial = axial.second,
            nominalTorsion = torsion.first,
            limitStateTorsion = torsion.second,
            allowableDeflection = demand.allowableDeflection.inInches,
            limitStateDeflection = "Deflection Limit"
        )
    }

    private fun calculateFlexureX(lb: Double, cb: Double): Pair<Double, String> {
        val zx = profile.propertiesStrongAxis.z.inIn3
        val sx = profile.propertiesStrongAxis.s.inIn3
        val iy = profile.propertiesWeakAxis.i.inIn4
        val ry = profile.propertiesWeakAxis.r.inInches
        val j = if (profile is com.lz.vectos.domain.beam.SteelProfile) profile.torsionalConstantJ else 0.0
        val cw = if (profile is com.lz.vectos.domain.beam.SteelProfile) profile.warpingConstantCw else 0.0

        val mp = Fy * zx

        var mn = mp
        var limitState = "Yielding"

        if (profile.shapeType == ShapeType.WIDE_FLANGE || profile.shapeType == ShapeType.CHANNEL) {
            val ho = if (profile is com.lz.vectos.domain.beam.SteelProfile) profile.depth.inInches - profile.flangeThickness.inInches else profile.depth.inInches * 0.95
            
            // Approximation for rts if not explicitly known:
            val rts = sqrt(sqrt(iy * cw) / sx)
            val c = if (profile.shapeType == ShapeType.CHANNEL) (ho / 2.0) * sqrt(iy / cw) else 1.0

            val lp = 1.76 * ry * sqrt(E / Fy)
            
            val lrPart = (j * c) / (sx * ho)
            val fL = 0.7 * Fy
            val lr = 1.95 * rts * (E / fL) * sqrt(lrPart + sqrt(lrPart.pow(2) + 6.76 * (fL / E).pow(2)))

            // 1. Lateral-Torsional Buckling (LTB)
            var mnLTB = mp
            var limitStateLTB = "Yielding"
            
            if (lb > lp && lb <= lr) {
                // Inelastic LTB
                mnLTB = cb * (mp - (mp - fL * sx) * ((lb - lp) / (lr - lp)))
                mnLTB = minOf(mnLTB, mp)
                limitStateLTB = "Inelastic LTB"
            } else if (lb > lr) {
                // Elastic LTB
                val fcr = (cb * PI.pow(2) * E / (lb / rts).pow(2)) * sqrt(1.0 + 0.078 * (j * c) / (sx * ho) * (lb / rts).pow(2))
                mnLTB = fcr * sx
                mnLTB = minOf(mnLTB, mp)
                limitStateLTB = "Elastic LTB"
            }

            // 2. Flange Local Buckling (FLB)
            var mnFLB = mp
            var limitStateFLB = "Yielding"
            
            if (profile is com.lz.vectos.domain.beam.SteelProfile) {
                val lambdaF = profile.flangeWidth.inInches / (2 * profile.flangeThickness.inInches)
                val lambdaPf = 0.38 * sqrt(E / Fy)
                val lambdaRf = 1.0 * sqrt(E / Fy)
                
                if (lambdaF > lambdaPf && lambdaF <= lambdaRf) {
                    // Noncompact Flange
                    mnFLB = mp - (mp - fL * sx) * ((lambdaF - lambdaPf) / (lambdaRf - lambdaPf))
                    limitStateFLB = "Flange Local Buckling"
                } else if (lambdaF > lambdaRf) {
                    // Slender Flange
                    val kc = 4.0 / sqrt(profile.depth.inInches / profile.webThickness.inInches)
                    val kcConstrained = kc.coerceIn(0.35, 0.76)
                    val fcrFLB = 0.9 * E * kcConstrained / lambdaF.pow(2)
                    mnFLB = fcrFLB * sx
                    limitStateFLB = "Slender Flange Buckling"
                }
            }

            // Governing Limit State
            if (mnLTB <= mnFLB && mnLTB < mn) {
                mn = mnLTB
                limitState = limitStateLTB
            } else if (mnFLB < mnLTB && mnFLB < mn) {
                mn = mnFLB
                limitState = limitStateFLB
            }
        }
        
        return mn to limitState
    }

    private fun calculateFlexureY(): Pair<Double, String> {
        val zy = profile.propertiesWeakAxis.z.inIn3
        val sy = profile.propertiesWeakAxis.s.inIn3
        
        val mp = minOf(Fy * zy, 1.6 * Fy * sy) // max limit per AISC
        
        var mn = mp
        var limitState = "Weak-Axis Yielding"
        
        if (profile is com.lz.vectos.domain.beam.SteelProfile && (profile.shapeType == ShapeType.WIDE_FLANGE || profile.shapeType == ShapeType.CHANNEL)) {
            val lambdaF = profile.flangeWidth.inInches / (2 * profile.flangeThickness.inInches)
            val lambdaPf = 0.38 * sqrt(E / Fy)
            val lambdaRf = 1.0 * sqrt(E / Fy)
            
            if (lambdaF > lambdaPf && lambdaF <= lambdaRf) {
                val mnFLB = mp - (mp - 0.7 * Fy * sy) * ((lambdaF - lambdaPf) / (lambdaRf - lambdaPf))
                if (mnFLB < mn) {
                    mn = mnFLB
                    limitState = "Flange Local Buckling"
                }
            } else if (lambdaF > lambdaRf) {
                val fcr = 0.69 * E / lambdaF.pow(2)
                val mnFLB = fcr * sy
                if (mnFLB < mn) {
                    mn = mnFLB
                    limitState = "Slender Flange Buckling"
                }
            }
        }
        
        return mn to limitState
    }

    private fun calculateShear(isStrongAxis: Boolean): Pair<Double, String> {
        val area = profile.area.inIn2
        var aw = area * 0.5 // Default fallback
        var cv1 = 1.0

        if (profile is com.lz.vectos.domain.beam.SteelProfile) {
            if (isStrongAxis) {
                // Strong axis shear is resisted by the web
                aw = profile.depth.inInches * profile.webThickness.inInches
                val hw = profile.depth.inInches - 2 * profile.flangeThickness.inInches
                val h_tw = hw / profile.webThickness.inInches
                
                val kv = 5.34 // Assuming no stiffeners
                val limit1 = 1.10 * sqrt(kv * E / Fy)
                val limit2 = 1.37 * sqrt(kv * E / Fy)
                
                cv1 = if (h_tw <= limit1) {
                    1.0
                } else if (h_tw <= limit2) {
                    limit1 / h_tw
                } else {
                    1.51 * kv * E / (h_tw.pow(2) * Fy)
                }
            } else {
                // Weak axis shear is resisted by the flanges
                aw = 2.0 * profile.flangeWidth.inInches * profile.flangeThickness.inInches
                cv1 = 1.0 // Typically yielding governs for flanges
            }
        }

        val vn = 0.6 * Fy * aw * cv1
        val limitState = if (isStrongAxis) {
            if (cv1 == 1.0) "Web Shear Yielding" else "Web Shear Buckling"
        } else {
            "Flange Shear Yielding"
        }
        return vn to limitState
    }

    private fun calculateAxial(lb: Double): Pair<Double, String> {
        val ag = profile.area.inIn2
        val rx = profile.propertiesStrongAxis.r.inInches
        val ry = profile.propertiesWeakAxis.r.inInches

        // Use largest slenderness
        val kl_r = max(lb / rx, lb / ry)
        
        val fe = (PI.pow(2) * E) / kl_r.pow(2)
        val fcr = if (kl_r <= 4.71 * sqrt(E / Fy)) {
            (0.658.pow(Fy / fe)) * Fy
        } else {
            0.877 * fe
        }

        return (fcr * ag) to "Flexural Buckling"
    }

    private fun calculateTorsion(): Pair<Double, String> {
        val j = if (profile is com.lz.vectos.domain.beam.SteelProfile) profile.torsionalConstantJ else 0.0
        
        return if (profile.shapeType == ShapeType.RECTANGULAR_HSS || profile.shapeType == ShapeType.ROUND_HSS || profile.shapeType == ShapeType.PIPE) {
            // Closed shape: Tn = Fcr * C (Section H3.1)
            // Approximate C if not explicitly in profile properties
            var c = j / 2.0 // rough fallback
            if (profile is com.lz.vectos.domain.beam.SteelProfile) {
                if (profile.shapeType == ShapeType.RECTANGULAR_HSS) {
                    val t = profile.webThickness.inInches
                    val b = profile.flangeWidth.inInches
                    val h = profile.depth.inInches
                    c = 2.0 * (b - t) * (h - t) * t
                } else if (profile.shapeType == ShapeType.ROUND_HSS || profile.shapeType == ShapeType.PIPE) {
                    val t = profile.webThickness.inInches
                    val d = profile.depth.inInches
                    c = PI * (d - t).pow(2) * t / 2.0
                }
            }
            // For compact HSS, Fcr = 0.6 * Fy
            val tn = 0.6 * Fy * c
            tn to "Torsional Yielding (HSS)"
        } else {
            // Open shape: Issue warning and use highly simplified yielding
            val tn = 0.6 * Fy * j
            tn to "Torsional Yielding (Warning: Open Shape)"
        }
    }

    override fun evaluateDetailed(demand: StationDemand, methodology: DesignMethodology): StrengthDesignResult {
        val lb = if (demand.compressionFlange == Flange.TOP) demand.lbTop.inInches else demand.lbBottom.inInches
        val cb = demand.cb

        val phiFlexure = if (methodology == DesignMethodology.LRFD) 0.9 else 1.0
        val omegaFlexure = if (methodology == DesignMethodology.ASD) 1.67 else 1.0
        val phiShear = if (methodology == DesignMethodology.LRFD) 0.9 else 1.0
        val omegaShear = if (methodology == DesignMethodology.ASD) 1.67 else 1.0
        val phiAxial = if (methodology == DesignMethodology.LRFD) 0.9 else 1.0
        val omegaAxial = if (methodology == DesignMethodology.ASD) 1.67 else 1.0
        val phiTorsion = if (methodology == DesignMethodology.LRFD) 0.9 else 1.0
        val omegaTorsion = if (methodology == DesignMethodology.ASD) 1.67 else 1.0

        val flexureX = calculateFlexureX(lb, cb)
        val flexureY = calculateFlexureY()
        val shearX = calculateShear(isStrongAxis = true)
        val axial = calculateAxial(lb)
        val torsion = calculateTorsion()

        val mn = flexureX.first
        val limitStateFlexure = flexureX.second
        val designMn = if (methodology == DesignMethodology.LRFD) mn * phiFlexure else mn / omegaFlexure
        val ratioFlexure = abs(demand.moment.lbIn) / designMn

        val vn = shearX.first
        val limitStateShear = shearX.second
        val designVn = if (methodology == DesignMethodology.LRFD) vn * phiShear else vn / omegaShear
        val ratioShear = abs(demand.shear.pounds) / designVn

        val pn = axial.first
        val limitStateAxial = axial.second
        val designPn = if (methodology == DesignMethodology.LRFD) pn * phiAxial else pn / omegaAxial
        val ratioAxial = abs(demand.axial.pounds) / designPn

        val tn = torsion.first
        val limitStateTorsion = torsion.second
        val designTn = if (methodology == DesignMethodology.LRFD) tn * phiTorsion else tn / omegaTorsion
        val ratioTorsion = abs(demand.torque.lbIn) / designTn

        // Interaction (Simplified H1.1)
        val ratioTotal = if (ratioAxial >= 0.2) {
            ratioAxial + (8.0/9.0) * ratioFlexure
        } else {
            (ratioAxial / 2.0) + ratioFlexure
        }

        val momentCheck = com.lz.vectos.domain.structural.StrengthCheckResult(
            demand = demand.moment,
            capacity = designMn.lbIn,
            utilization = ratioFlexure,
            governingCombination = "Current",
            governingMode = limitStateFlexure
        )

        val shearCheck = com.lz.vectos.domain.structural.StrengthCheckResult(
            demand = demand.shear,
            capacity = designVn.poundsForce,
            utilization = ratioShear,
            governingCombination = "Current",
            governingMode = limitStateShear
        )

        val axialCheck = com.lz.vectos.domain.structural.StrengthCheckResult(
            demand = demand.axial,
            capacity = designPn.poundsForce,
            utilization = ratioAxial,
            governingCombination = "Current",
            governingMode = limitStateAxial
        )

        val torsionCheck = com.lz.vectos.domain.structural.StrengthCheckResult(
            demand = demand.torque,
            capacity = designTn.lbIn,
            utilization = ratioTorsion,
            governingCombination = "Current",
            governingMode = limitStateTorsion
        )

        return com.lz.vectos.domain.structural.StrengthDesignResult(
            momentCheck = momentCheck,
            shearCheck = shearCheck,
            axialCheck = axialCheck,
            torsionCheck = torsionCheck,
            methodology = methodology,
            designParameters = mapOf("Lb" to String.format(java.util.Locale.US, "%.1f in", lb), "Cb" to String.format(java.util.Locale.US, "%.2f", cb))
        )
    }
}
