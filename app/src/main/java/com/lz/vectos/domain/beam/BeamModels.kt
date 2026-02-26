package com.lz.vectos.domain.beam

import com.lz.vectos.domain.project.Project
import com.lz.vectos.domain.calculation.CalculationMetadata
import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Pressure
import com.lz.vectos.domain.units.Moment
import com.lz.vectos.domain.units.MomentOfInertia
import com.lz.vectos.domain.units.UnitSystem

/**
 * Structural materials used in beam calculations.
 * Modulus of Elasticity is stored in base units (Pascals).
 */
enum class Material(val modulusOfElasticity: Pressure) {
    STEEL(Pressure(200.0 * 1e9)),
    CONCRETE(Pressure(25.0 * 1e9)),
    ALUMINUM(Pressure(69.0 * 1e9))
}

/**
 * Supported load configurations for the beam calculator.
 */
enum class LoadType {
    POINT_LOAD_MIDSPAN,
    UNIFORMLY_DISTRIBUTED_LOAD
}

/**
 * Encapsulates all input parameters for a beam calculation.
 * Values are stored using the units system to ensure correctness.
 */
data class BeamInputs(
    val project: Project,
    val span: Length,
    val loadValue: Double, // The numeric component of the load, context-dependent (Force or ForcePerLength)
    val material: Material,
    val momentOfInertia: MomentOfInertia,
    val loadType: LoadType,
    val unitSystem: UnitSystem
)

/**
 * Encapsulates the output of a beam calculation.
 * 
 * Note on Deflection:
 * [maxDeflection] represents the absolute maximum vertical displacement
 * along the beam's length. It is expressed in internal base length units (Meters).
 */
data class BeamResults(
    val maxBendingMoment: Moment,
    val maxShear: Force,
    val maxDeflection: Length
)

/**
 * States the engineering assumptions used by the [BeamCalculator].
 */
data class Assumptions(
    val linearElastic: Boolean = true,
    val smallDeflection: Boolean = true,
    val simplySupported: Boolean = true
)

/**
 * The complete record of a beam calculation, including metadata, inputs, results, and assumptions.
 */
data class BeamCalculation(
    val metadata: CalculationMetadata,
    val inputs: BeamInputs,
    val results: BeamResults,
    val assumptions: Assumptions
)
