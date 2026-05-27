package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.MaterialType
import com.lz.vectos.domain.beam.SectionProfile

/**
 * Result of a point-by-point capacity evaluation.
 */
data class PointCapacityResult(
    val x: Double,
    val Mx: Double,
    val compressionFlange: Flange,
    val Lb: Double,
    val nominalCapacity: Double,
    val designCapacity: Double,
    val utilizationRatio: Double,
    val governingLimitState: String
)

/**
 * Placeholder for material-specific capacity formulas.
 * This will be replaced by actual AISC/NDS logic.
 */
data class RawCapacityResult(
    val nominalCapacity: Double,
    val governingLimitState: String
)

/**
 * Evaluation engine that performs code checks at every discrete point.
 */
object CapacityEngine {

    /**
     * Iterates through the enriched bracing array and calculates utilization at every point.
     */
    fun evaluate(
        enrichedBracing: List<DiscreteBracingResult>,
        section: SectionProfile,
        methodology: DesignMethodology,
        // In the future, this would be a MaterialDesignStrategy implementation
        capacityCalculator: (Double, Flange) -> RawCapacityResult
    ): List<PointCapacityResult> {
        
        val phi = if (methodology == DesignMethodology.LRFD) 0.9 else 1.0 // Simplified for placeholder
        val omega = if (methodology == DesignMethodology.ASD) 1.67 else 1.0 // Simplified for placeholder

        return enrichedBracing.map { point ->
            // 1. Calculate Capacity for this specific Lb and Flange
            val raw = capacityCalculator(point.Lb, point.compressionFlange)
            
            // 2. Apply Design Factors (LRFD/ASD)
            val designCapacity = if (methodology == DesignMethodology.LRFD) {
                raw.nominalCapacity * phi
            } else {
                raw.nominalCapacity / omega
            }

            // 3. Calculate Utilization (Demand / Capacity)
            // Note: Mx is absolute because capacity is usually defined as a positive resistance
            val demand = Math.abs(point.Mx)
            val utilization = if (designCapacity > 0) demand / designCapacity else Double.POSITIVE_INFINITY

            PointCapacityResult(
                x = point.x,
                Mx = point.Mx,
                compressionFlange = point.compressionFlange,
                Lb = point.Lb,
                nominalCapacity = raw.nominalCapacity,
                designCapacity = designCapacity,
                utilizationRatio = utilization,
                governingLimitState = raw.governingLimitState
            )
        }
    }
}

/**
 * EXAMPLE: A Mock Steel Capacity Calculator (AISC Chapter F placeholder)
 */
fun mockSteelFlexuralCapacity(Lb: Double, flange: Flange): RawCapacityResult {
    // Plastic Moment (Mp) = 50 ksi * 100 in^3 = 5,000,000 lb-in
    val Mp = 50000.0 * 100.0 
    
    return when {
        Lb <= 50.0 -> RawCapacityResult(Mp, "Yielding")
        Lb <= 150.0 -> {
            val capacity = Mp * (1.0 - (Lb - 50.0) / 200.0)
            RawCapacityResult(capacity, "Inelastic LTB")
        }
        else -> {
            val capacity = Mp * (150.0 / Lb)
            RawCapacityResult(capacity, "Elastic LTB")
        }
    }
}
