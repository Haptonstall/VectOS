package com.lz.vectos.domain.structural

import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.Flange
import com.lz.model.units.inInches
import com.lz.model.units.inLbIn
import com.lz.model.units.inPoundsForce
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.StationDemand
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Details of a single code check.
 */
@Serializable
data class CodeCheck(
    val limitState: String = "N/A",
    val demand: Double = 0.0,
    val capacity: Double = 0.0,
    val ratio: Double = 0.0,
    val isServiceability: Boolean = false
)

/**
 * Result of a point-by-point capacity evaluation across all limit states.
 */
@Serializable
data class PointCapacityResult(
    val demand: StationDemand,
    
    // Detailed Checks
    val flexureCheckX: CodeCheck = CodeCheck(),
    val flexureCheckY: CodeCheck = CodeCheck(),
    val shearCheckX: CodeCheck = CodeCheck(),
    val shearCheckY: CodeCheck = CodeCheck(),
    val axialCheck: CodeCheck = CodeCheck(),
    val torsionCheck: CodeCheck = CodeCheck(),
    val deflectionCheck: CodeCheck = CodeCheck(),
    val interactionCheck: CodeCheck = CodeCheck(),
    
    // Top-level governing values for easy consumption
    val designCapacity: Double = 0.0,
    val utilizationRatio: Double = 0.0,
    val governingLimitState: String = "N/A",
    val compressionFlange: Flange = Flange.TOP,
    val Lb: Double = 0.0
)

/**
 * Container for nominal capacities and allowable limits calculated by the material strategy.
 */
data class RawCapacityResult(
    val nominalFlexureX: Double = 0.0,
    val limitStateFlexureX: String = "Flexure X",
    val nominalFlexureY: Double = 0.0,
    val limitStateFlexureY: String = "Flexure Y",
    val nominalShearX: Double = 0.0,
    val limitStateShearX: String = "Shear X",
    val nominalShearY: Double = 0.0,
    val limitStateShearY: String = "Shear Y",
    val nominalAxial: Double = 0.0,
    val limitStateAxial: String = "Axial",
    val nominalTorsion: Double = 0.0,
    val limitStateTorsion: String = "Torsion",
    val allowableDeflection: Double = Double.POSITIVE_INFINITY,
    val limitStateDeflection: String = "Deflection"
) {
    // Backward compatibility property aliases
    val nominalCapacity: Double get() = nominalFlexureX
    val governingLimitState: String get() = limitStateFlexureX
}

/**
 * Evaluation engine that performs comprehensive code checks at every discrete point.
 */
object CapacityEngine {

    /**
     * Iterates through the enriched station demands and calculates utilization 
     * for all limit states (shear, axial, flexure, torsion, and serviceability) at every point.
     */
    fun evaluate(
        demands: List<StationDemand>,
        section: SectionProfile,
        methodology: DesignMethodology,
        capacityCalculator: (StationDemand) -> RawCapacityResult
    ): List<PointCapacityResult> {
        
        // Simplified factors for placeholder; normally pulled from code specifications
        val phiFlexure = if (methodology == DesignMethodology.LRFD) 0.9 else 1.0
        val omegaFlexure = if (methodology == DesignMethodology.ASD) 1.67 else 1.0
        
        val phiShear = if (methodology == DesignMethodology.LRFD) 0.9 else 1.0
        val omegaShear = if (methodology == DesignMethodology.ASD) 1.67 else 1.0
        
        val phiAxial = if (methodology == DesignMethodology.LRFD) 0.9 else 1.0
        val omegaAxial = if (methodology == DesignMethodology.ASD) 1.67 else 1.0

        val phiTorsion = if (methodology == DesignMethodology.LRFD) 0.9 else 1.0
        val omegaTorsion = if (methodology == DesignMethodology.ASD) 1.67 else 1.0

        return demands.map { demand ->
            val raw = capacityCalculator(demand)
            
            // Flexure X
            val designFlexureX = if (methodology == DesignMethodology.LRFD) raw.nominalFlexureX * phiFlexure else raw.nominalFlexureX / omegaFlexure
            val demandFlexureX = abs(demand.moment.inLbIn)
            val ratioFlexureX = if (designFlexureX > 0) demandFlexureX / designFlexureX else 0.0
            val checkFlexureX = CodeCheck(raw.limitStateFlexureX, demandFlexureX, designFlexureX, ratioFlexureX)
            
            // Flexure Y
            val designFlexureY = if (methodology == DesignMethodology.LRFD) raw.nominalFlexureY * phiFlexure else raw.nominalFlexureY / omegaFlexure
            val demandFlexureY = abs(demand.momentY.inLbIn)
            val ratioFlexureY = if (designFlexureY > 0) demandFlexureY / designFlexureY else 0.0
            val checkFlexureY = CodeCheck(raw.limitStateFlexureY, demandFlexureY, designFlexureY, ratioFlexureY)
            
            // Shear X (Strong axis / Vy demand)
            val designShearX = if (methodology == DesignMethodology.LRFD) raw.nominalShearX * phiShear else raw.nominalShearX / omegaShear
            val demandShearX = abs(demand.shear.inPoundsForce)
            val ratioShearX = if (designShearX > 0) demandShearX / designShearX else 0.0
            val checkShearX = CodeCheck(raw.limitStateShearX, demandShearX, designShearX, ratioShearX)
            
            // Shear Y (Weak axis / Vz demand)
            val designShearY = if (methodology == DesignMethodology.LRFD) raw.nominalShearY * phiShear else raw.nominalShearY / omegaShear
            val demandShearY = abs(demand.shearY.inPoundsForce)
            val ratioShearY = if (designShearY > 0) demandShearY / designShearY else 0.0
            val checkShearY = CodeCheck(raw.limitStateShearY, demandShearY, designShearY, ratioShearY)
            
            // Axial
            val designAxial = if (methodology == DesignMethodology.LRFD) raw.nominalAxial * phiAxial else raw.nominalAxial / omegaAxial
            val demandAxial = abs(demand.axial.inPoundsForce)
            val ratioAxial = if (designAxial > 0) demandAxial / designAxial else 0.0
            val checkAxial = CodeCheck(raw.limitStateAxial, demandAxial, designAxial, ratioAxial)
            
            // Torsion
            val designTorsion = if (methodology == DesignMethodology.LRFD) raw.nominalTorsion * phiTorsion else raw.nominalTorsion / omegaTorsion
            val demandTorsion = abs(demand.torque.inLbIn)
            val ratioTorsion = if (designTorsion > 0) demandTorsion / designTorsion else 0.0
            val checkTorsion = CodeCheck(raw.limitStateTorsion, demandTorsion, designTorsion, ratioTorsion)

            // Deflection (Serviceability)
            val demandDeflection = abs(demand.deflection.inInches)
            val capacityDeflection = raw.allowableDeflection
            val ratioDeflection = if (capacityDeflection > 0 && capacityDeflection != Double.POSITIVE_INFINITY) demandDeflection / capacityDeflection else 0.0
            val checkDeflection = CodeCheck(raw.limitStateDeflection, demandDeflection, capacityDeflection, ratioDeflection, isServiceability = true)

            // Interaction (Chapter H1.1)
            val interactionRatio = if (ratioAxial >= 0.2) {
                ratioAxial + (8.0 / 9.0) * (ratioFlexureX + ratioFlexureY)
            } else {
                (ratioAxial / 2.0) + (ratioFlexureX + ratioFlexureY)
            }
            val checkInteraction = CodeCheck("Interaction Eq H1-1", interactionRatio, 1.0, interactionRatio)

            // Find Governing Limit State
            val checks = listOf(
                checkFlexureX, checkFlexureY, 
                checkShearX, checkShearY, 
                checkAxial, checkTorsion, 
                checkDeflection, checkInteraction
            )
            
            val governing = checks.maxByOrNull { it.ratio } ?: checkInteraction

            PointCapacityResult(
                demand = demand,
                flexureCheckX = checkFlexureX,
                flexureCheckY = checkFlexureY,
                shearCheckX = checkShearX,
                shearCheckY = checkShearY,
                axialCheck = checkAxial,
                torsionCheck = checkTorsion,
                deflectionCheck = checkDeflection,
                interactionCheck = checkInteraction,
                designCapacity = governing.capacity,
                utilizationRatio = governing.ratio,
                governingLimitState = governing.limitState,
                compressionFlange = demand.compressionFlange,
                Lb = if (demand.compressionFlange == Flange.TOP) demand.lbTop.inInches else demand.lbBottom.inInches
            )
        }
    }
}


