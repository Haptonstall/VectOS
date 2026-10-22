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
        // Combined-force interaction check for a station. Null (the default)
        // keeps AISC 360 Eq. H1-1 — correct for steel, and what every caller
        // got before this parameter existed. A material whose code has its
        // own interaction rules (NDS 3.9 for wood) supplies its own here
        // instead of being silently judged by the steel equation.
        interactionCheckProvider: ((StationDemand) -> CodeCheck)? = null,
        capacityCalculator: (StationDemand) -> RawCapacityResult
    ): List<PointCapacityResult> {

        return demands.map { demand ->
            val raw = capacityCalculator(demand)
            val isAxialTension = demand.axial.pounds >= 0.0

            // Flexure X
            val designFlexureX = factors.apply(raw.nominalFlexureX, factors.flexure)
            val demandFlexureX = abs(demand.moment.inLbIn)
            val ratioFlexureX = checkRatio(demandFlexureX, designFlexureX, raw.limitStateFlexureX)
            val checkFlexureX =
                CodeCheck(raw.limitStateFlexureX, demandFlexureX, designFlexureX, ratioFlexureX)

            // Flexure Y (same flexure factor — AISC/NDS don't distinguish
            // strong/weak axis bending for phi/omega purposes)
            val designFlexureY = factors.apply(raw.nominalFlexureY, factors.flexure)
            val demandFlexureY = abs(demand.momentY.inLbIn)
            val ratioFlexureY = checkRatio(demandFlexureY, designFlexureY, raw.limitStateFlexureY)
            val checkFlexureY =
                CodeCheck(raw.limitStateFlexureY, demandFlexureY, designFlexureY, ratioFlexureY)

            // Shear X (Strong axis / Vy demand)
            val designShearX = factors.apply(raw.nominalShearX, factors.shear)
            val demandShearX = abs(demand.shear.inPoundsForce)
            val ratioShearX = checkRatio(demandShearX, designShearX, raw.limitStateShearX)
            val checkShearX =
                CodeCheck(raw.limitStateShearX, demandShearX, designShearX, ratioShearX)

            // Shear Y (Weak axis / Vz demand)
            val designShearY = factors.apply(raw.nominalShearY, factors.shear)
            val demandShearY = abs(demand.shearY.inPoundsForce)
            val ratioShearY = checkRatio(demandShearY, designShearY, raw.limitStateShearY)
            val checkShearY =
                CodeCheck(raw.limitStateShearY, demandShearY, designShearY, ratioShearY)

            // Axial — tension and compression can carry different factors
            // (e.g. NDS: phi=0.80 tension vs 0.90 compression)
            val axialFactor = if (isAxialTension) factors.axialTension else factors.axialCompression
            val designAxial = factors.apply(raw.nominalAxial, axialFactor)
            val demandAxial = abs(demand.axial.inPoundsForce)
            val ratioAxial = checkRatio(demandAxial, designAxial, raw.limitStateAxial)
            val checkAxial = CodeCheck(raw.limitStateAxial, demandAxial, designAxial, ratioAxial)

            // Torsion
            val designTorsion = factors.apply(raw.nominalTorsion, factors.torsion)
            val demandTorsion = abs(demand.torque.inLbIn)
            val ratioTorsion = checkRatio(demandTorsion, designTorsion, raw.limitStateTorsion)
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
            val checkInteraction = if (interactionCheckProvider != null) {
                interactionCheckProvider(demand)
            } else {
                val interactionRatio = if (ratioAxial >= 0.2) {
                    ratioAxial + (8.0 / 9.0) * (ratioFlexureX + ratioFlexureY)
                } else {
                    (ratioAxial / 2.0) + (ratioFlexureX + ratioFlexureY)
                }
                CodeCheck("Interaction Eq H1-1", interactionRatio, 1.0, interactionRatio)
            }

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
                governingDemand = governing.demand,
                governingLimitState = governing.limitState,
                compressionFlange = demand.compressionFlange,
                Lb = if (demand.compressionFlange == Flange.TOP) demand.lbTop.inInches else demand.lbBottom.inInches
            )
        }
    }

    /**
     * Utilization of one strength check. Zero capacity against a real demand
     * is a FAILURE — the member can't carry the load at all (e.g. a
     * slenderness limit exceeded, or a design value that isn't available) —
     * so it is infinite, not 0.00 as a plain `if (capacity > 0)` guard made
     * it. A check whose limit state is "N/A..." is one the material's code
     * doesn't define (torsion for wood, say): its zero capacity means "not
     * checked", and it stays 0.
     */
    private fun checkRatio(demand: Double, capacity: Double, limitState: String): Double = when {
        capacity > 0.0 -> demand / capacity
        demand > 1e-9 && !limitState.trimStart().startsWith("N/A") -> Double.POSITIVE_INFINITY
        else -> 0.0
    }
}