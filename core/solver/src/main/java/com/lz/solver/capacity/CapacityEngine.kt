package com.lz.solver.capacity

import com.lz.model.structural.CodeCheck
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.Flange
import com.lz.model.structural.PointCapacityResult
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.StationDemand
import com.lz.model.units.inInches
import com.lz.model.units.inLbIn
import com.lz.model.units.inPoundsForce
import kotlin.math.abs

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
     *
     * [factors] supplies the real, material-specific per-limit-state design
     * factors (see [DesignFactorSet]) — sourced from the same calculator that
     * produced [capacityCalculator]'s nominal values, so ASD/LRFD factoring
     * here always matches what that material's code actually specifies.
     */
    fun evaluate(
        demands: List<StationDemand>,
        section: SectionProfile,
        methodology: DesignMethodology,
        factors: DesignFactorSet,
        capacityCalculator: (StationDemand) -> RawCapacityResult
    ): List<PointCapacityResult> {

        return demands.map { demand ->
            val raw = capacityCalculator(demand)
            val isAxialTension = demand.axial.pounds >= 0.0

            // Flexure X
            val designFlexureX = factors.apply(raw.nominalFlexureX, factors.flexure)
            val demandFlexureX = abs(demand.moment.inLbIn)
            val ratioFlexureX = if (designFlexureX > 0) demandFlexureX / designFlexureX else 0.0
            val checkFlexureX =
                CodeCheck(raw.limitStateFlexureX, demandFlexureX, designFlexureX, ratioFlexureX)

            // Flexure Y (same flexure factor — AISC/NDS don't distinguish
            // strong/weak axis bending for phi/omega purposes)
            val designFlexureY = factors.apply(raw.nominalFlexureY, factors.flexure)
            val demandFlexureY = abs(demand.momentY.inLbIn)
            val ratioFlexureY = if (designFlexureY > 0) demandFlexureY / designFlexureY else 0.0
            val checkFlexureY =
                CodeCheck(raw.limitStateFlexureY, demandFlexureY, designFlexureY, ratioFlexureY)

            // Shear X (Strong axis / Vy demand)
            val designShearX = factors.apply(raw.nominalShearX, factors.shear)
            val demandShearX = abs(demand.shear.inPoundsForce)
            val ratioShearX = if (designShearX > 0) demandShearX / designShearX else 0.0
            val checkShearX =
                CodeCheck(raw.limitStateShearX, demandShearX, designShearX, ratioShearX)

            // Shear Y (Weak axis / Vz demand)
            val designShearY = factors.apply(raw.nominalShearY, factors.shear)
            val demandShearY = abs(demand.shearY.inPoundsForce)
            val ratioShearY = if (designShearY > 0) demandShearY / designShearY else 0.0
            val checkShearY =
                CodeCheck(raw.limitStateShearY, demandShearY, designShearY, ratioShearY)

            // Axial — tension and compression can carry different factors
            // (e.g. NDS: phi=0.80 tension vs 0.90 compression)
            val axialFactor = if (isAxialTension) factors.axialTension else factors.axialCompression
            val designAxial = factors.apply(raw.nominalAxial, axialFactor)
            val demandAxial = abs(demand.axial.inPoundsForce)
            val ratioAxial = if (designAxial > 0) demandAxial / designAxial else 0.0
            val checkAxial = CodeCheck(raw.limitStateAxial, demandAxial, designAxial, ratioAxial)

            // Torsion
            val designTorsion = factors.apply(raw.nominalTorsion, factors.torsion)
            val demandTorsion = abs(demand.torque.inLbIn)
            val ratioTorsion = if (designTorsion > 0) demandTorsion / designTorsion else 0.0
            val checkTorsion =
                CodeCheck(raw.limitStateTorsion, demandTorsion, designTorsion, ratioTorsion)

            // Deflection (Serviceability)
            val demandDeflection = abs(demand.deflection.inInches)
            val capacityDeflection = raw.allowableDeflection
            val ratioDeflection = if (capacityDeflection > 0 && capacityDeflection != Double.POSITIVE_INFINITY) demandDeflection / capacityDeflection else 0.0
            val checkDeflection = CodeCheck(
                raw.limitStateDeflection,
                demandDeflection,
                capacityDeflection,
                ratioDeflection,
                isServiceability = true
            )

            // Interaction (Chapter H1.1)
            val interactionRatio = if (ratioAxial >= 0.2) {
                ratioAxial + (8.0 / 9.0) * (ratioFlexureX + ratioFlexureY)
            } else {
                (ratioAxial / 2.0) + (ratioFlexureX + ratioFlexureY)
            }
            val checkInteraction =
                CodeCheck("Interaction Eq H1-1", interactionRatio, 1.0, interactionRatio)

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