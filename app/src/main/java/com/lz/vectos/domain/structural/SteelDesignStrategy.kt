package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.SectionOrientation
import com.lz.vectos.domain.beam.SectionProfile
import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Moment
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * AISC-aligned steel design strategy.
 * Implements yielding and lateral-torsional buckling (LTB).
 */
class SteelDesignStrategy : MaterialDesignStrategy {

    private val E = 200e9 // Pa (29,000 ksi)
    private val G = 77.2e9 // Pa (11,200 ksi)

    override fun computeCapacity(
        section: SectionProfile,
        code: BuildingCode,
        orientation: SectionOrientation,
        inputs: MaterialDesignInputs
    ): SectionCapacity {
        val steelInputs = inputs as? MaterialDesignInputs.Steel ?: MaterialDesignInputs.Steel()
        val fy = steelInputs.fyPa
        
        // Axis-specific properties
        val props = if (orientation == SectionOrientation.STRONG_AXIS) 
            section.propertiesStrongAxis else section.propertiesWeakAxis

        // 1. Resolve Flexural Limit States
        val yieldingStatus = resolveYieldMomentCapacity(props.z, fy)
        
        // LTB only applies to Strong Axis bending of doubly symmetric shapes (simplified)
        val ltbStatus = if (orientation == SectionOrientation.STRONG_AXIS) {
            resolveLTBCapacity(section, steelInputs)
        } else {
            CapacityEvaluationStatus.NotApplicable
        }

        // 2. Determine Governing Nominal Capacity (Mn)
        val limitStates = mapOf(
            "Yielding" to yieldingStatus,
            "Lateral-Torsional Buckling" to ltbStatus
        )

        // Find governing Mn (minimum of evaluated states)
        var nominalMn = yieldingStatus.value
        var governingMode = SteelFlexuralMode.YIELDING

        if (ltbStatus is CapacityEvaluationStatus.Evaluated && ltbStatus.value < nominalMn) {
            nominalMn = ltbStatus.value
            governingMode = SteelFlexuralMode.LATERAL_TORSIONAL_BUCKLING
        }

        // 3. Shear Capacity (Vn) - Simplified AISC G2
        val nominalVn = 0.6 * fy * section.area 

        return SectionCapacity(
            nominalMomentCapacity = Moment(nominalMn),
            nominalShearCapacity = Force(nominalVn),
            designMomentCapacity = Moment(nominalMn * code.phiMoment),
            designShearCapacity = Force(nominalVn * code.phiShear),
            governingMode = governingMode.label,
            evaluationSummary = limitStates
        )
    }

    private fun resolveYieldMomentCapacity(zx: Double, fy: Double): CapacityEvaluationStatus.Evaluated {
        // AISC F2-1: Mn = Fy * Zx
        return CapacityEvaluationStatus.Evaluated(fy * zx, "AISC F2-1")
    }

    private fun resolveLTBCapacity(
        section: SectionProfile,
        inputs: MaterialDesignInputs.Steel
    ): CapacityEvaluationStatus {
        val lb = inputs.unbracedLengthM ?: return CapacityEvaluationStatus.NotEvaluated("Unbraced length (Lb) required")
        val fy = inputs.fyPa
        val cb = inputs.momentGradientCb
        
        val props = section.propertiesStrongAxis
        val propsWeak = section.propertiesWeakAxis
        
        val zx = props.z
        val sx = props.s
        val ry = propsWeak.r
        val iy = propsWeak.i
        val j = section.torsionalConstantJ ?: 0.0
        val cw = section.warpingConstantCw ?: 0.0

        if (j == 0.0 || cw == 0.0) return CapacityEvaluationStatus.NotEvaluated("Torsional properties (J, Cw) missing")

        // AISC F2 Limits
        // Lp = 1.76 * ry * sqrt(E / Fy)
        val lp = 1.76 * ry * sqrt(E / fy)
        
        // Lr = 1.95 * rts * E / (0.7 * Fy) * sqrt(J*c / (Sx * ho) + sqrt(...))
        // Simplified Lr for W-shapes
        val rts = sqrt(sqrt(iy * cw) / sx) // Approximate
        val lr = 1.95 * rts * (E / (0.7 * fy)) * sqrt((j / (sx * section.depth.meters)) + sqrt((j / (sx * section.depth.meters)).let { it * it } + 6.76 * (0.7 * fy / E).let { it * it }))

        val mn = when {
            lb <= lp -> fy * zx // Plastic Region
            lb <= lr -> {
                // Inelastic LTB
                val mp = fy * zx
                cb * (mp - (mp - 0.7 * fy * sx) * ((lb - lp) / (lr - lp))).coerceAtMost(mp)
            }
            else -> {
                // Elastic LTB
                val fcr = (cb * PI * PI * E / (lb / rts).let { it * it }) * sqrt(1.0 + 0.078 * (j / (sx * section.depth.meters)) * (lb / rts).let { it * it })
                (fcr * sx).coerceAtMost(fy * zx)
            }
        }

        return CapacityEvaluationStatus.Evaluated(mn, "AISC F2-2/3/4")
    }
}
