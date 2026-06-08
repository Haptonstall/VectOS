package com.lz.model.units

import kotlinx.serialization.Serializable

/**
 * These value classes wrap primitive Double values to provide compile-time unit safety.
 *
 * INTERNAL BASE UNITS (Imperial):
 * - Length: Inches (in)
 * - Force: Pounds (lbf)
 * - Moment: Pound-Inches (lb-in)
 * - Pressure: PSI (lb/in²)
 * - ForcePerLength: PLI (lb/in)
 * - MomentOfInertia: in⁴
 *
 * Domain-layer calculators and the unit conversion system are permitted to access these
 * raw values, as they operate in the base unit system by design. For external consumers
 * (like UI), use the `UnitConverter` to get display values.
 */

@Serializable
@JvmInline
value class Length(public val inches: Double) : Comparable<Length> {
    override fun compareTo(other: Length): Int = this.inches.compareTo(other.inches)
    operator fun plus(other: Length) = Length(this.inches + other.inches)
    operator fun minus(other: Length) = Length(this.inches - other.inches)
    operator fun times(multiplier: Double) = Length(this.inches * multiplier)
    operator fun div(divider: Double) = Length(this.inches / divider)
    operator fun div(other: Length): Double = this.inches / other.inches
    operator fun rem(other: Length): Double = this.inches % other.inches
}

@Serializable
@JvmInline
value class Force(public val pounds: Double) : Comparable<Force> {
    override fun compareTo(other: Force): Int = this.pounds.compareTo(other.pounds)
    operator fun plus(other: Force) = Force(this.pounds + other.pounds)
    operator fun minus(other: Force) = Force(this.pounds - other.pounds)
    operator fun times(multiplier: Double) = Force(this.pounds * multiplier)
    operator fun div(divider: Double) = Force(this.pounds / divider)
}

@Serializable
@JvmInline
value class Moment(public val lbIn: Double) : Comparable<Moment> {
    override fun compareTo(other: Moment): Int = this.lbIn.compareTo(other.lbIn)
    operator fun plus(other: Moment) = Moment(this.lbIn + other.lbIn)
    operator fun minus(other: Moment) = Moment(this.lbIn - other.lbIn)
    operator fun times(multiplier: Double) = Moment(this.lbIn * multiplier)
}

@Serializable
@JvmInline
value class Pressure(public val psi: Double) : Comparable<Pressure> {
    override fun compareTo(other: Pressure): Int = this.psi.compareTo(other.psi)
}

@Serializable
@JvmInline
value class Stress(public val psi: Double) : Comparable<Stress> {
    override fun compareTo(other: Stress): Int = this.psi.compareTo(other.psi)
}

@Serializable
@JvmInline
value class ElasticModulus(public val psi: Double) : Comparable<ElasticModulus> {
    override fun compareTo(other: ElasticModulus): Int = this.psi.compareTo(other.psi)
}

@Serializable
@JvmInline
value class ForcePerLength(public val pli: Double) {
    // This operation is allowed as it's a fundamental physics definition.
    operator fun times(length: Length) = Force(this.pli * length.inches)
}

// ForcePerLength construction
val Double.lbPerIn get() = ForcePerLength(this)
val Double.lbPerFt get() = ForcePerLength(this / 12.0)
val Double.kipPerIn get() = ForcePerLength(this * 1000.0)
val Double.kipPerFt get() = ForcePerLength(this * 1000.0 / 12.0)

@Serializable
@JvmInline
value class MomentOfInertia(public val inchesToFourth: Double)

/**
 * DSL for constructing unit-safe types from raw numbers.
 * Primarily for use in tests and domain setup.
 */

// Length construction
val Double.inches get() = Length(this)
val Double.feet get() = Length(this * 12.0)
val Double.meters get() = Length(this / 0.0254)
val Double.mm get() = Length(this / 25.4)

// Force construction
val Double.poundsForce get() = Force(this)
val Double.kips get() = Force(this * 1000.0)
val Double.newtons get() = Force(this / 4.4482216153)
val Double.kiloNewtons get() = Force(this * 1000.0 / 4.4482216153)

// Moment construction
val Double.lbIn get() = Moment(this)
val Double.lbFt get() = Moment(this * 12.0)
val Double.kipIn get() = Moment(this * 1000.0)
val Double.kipFt get() = Moment(this * 12000.0)

// Pressure / Stress / Modulus construction
val Double.psi get() = Pressure(this)
val Double.ksi get() = Pressure(this * 1000.0)
val Double.psf get() = Pressure(this / 144.0)

val Double.psiStress get() = Stress(this)
val Double.ksiStress get() = Stress(this * 1000.0)

val Double.psiModulus get() = ElasticModulus(this)
val Double.ksiModulus get() = ElasticModulus(this * 1000.0)

val Double.pascals get() = Pressure(this / 6894.75729)
val Double.megaPascals get() = Pressure(this * 1_000_000.0 / 6894.75729)
val Double.gigaPascals get() = Pressure(this * 1_000_000_000.0 / 6894.75729)

// Section Properties construction
@Serializable
@JvmInline
value class Area(public val squareInches: Double)

val Double.in2 get() = Area(this)
val Double.cm2 get() = Area(this / 6.4516)

@Serializable
@JvmInline
value class SectionModulus(public val cubicInches: Double)

val Double.in3 get() = SectionModulus(this)
val Double.cm3 get() = SectionModulus(this / 16.387064)

// Moment of Inertia construction
val Double.in4 get() = MomentOfInertia(this)
val Double.m4 get() = MomentOfInertia(this / 0.0000004162314256)
val Double.cm4 get() = MomentOfInertia(this / 41.62314256)
val Double.mm4 get() = MomentOfInertia(this / 416231.4256)

/**
 * Creates a Moment from a Force and a Length.
 * This is an explicit factory function, not an operator, to enforce clarity.
 * Moment is NOT implicitly Force * Length.
 */
fun createMoment(force: Force, length: Length): Moment = Moment(force.pounds * length.inches)

operator fun Pressure.times(width: Length): ForcePerLength = ForcePerLength(this.psi * width.inches)

/**
 * Internal accessors for converting to display units.
 * These are consumed by `UnitConverter` and are not for public use.
 * UI/ViewModel layers MUST use the `UnitConverter` to ensure consistency.
 */
val Length.inInches get() = inches
val Length.inFeet get() = inches / 12.0
val Length.inMeters get() = inches * 0.0254
val Length.inMm get() = inches * 25.4

val Force.inPoundsForce get() = pounds
val Force.inKips get() = pounds / 1000.0
val Force.inNewtons get() = pounds * 4.4482216153
val Force.inKiloNewtons get() = pounds * 4.4482216153 / 1000.0

val Pressure.inPsi get() = psi
val Pressure.inKsi get() = psi / 1000.0
val Pressure.inPsf get() = psi * 144.0

val Stress.inPsi get() = psi
val Stress.inKsi get() = psi / 1000.0

val ElasticModulus.inPsi get() = psi
val ElasticModulus.inKsi get() = psi / 1000.0

val Moment.inLbIn get() = lbIn
val Moment.inLbFt get() = lbIn / 12.0
val Moment.inNewtonMeters get() = lbIn * (4.4482216153 * 0.0254)

val MomentOfInertia.inIn4 get() = inchesToFourth
val MomentOfInertia.inM4 get() = inchesToFourth * 0.0000004162314256
val MomentOfInertia.inMm4 get() = inchesToFourth * 416231.4256

val Pressure.inPascals get() = psi * 6894.75729
val Pressure.inMegaPascals get() = psi * 6.89475729 / 1000.0 // Corrected from Pa to MPa
val Pressure.inGigaPascals get() = psi * 6.89475729 / 1_000_000.0

val Area.inIn2 get() = squareInches
val Area.inCm2 get() = squareInches * 6.4516

val SectionModulus.inIn3 get() = cubicInches
val SectionModulus.inCm3 get() = cubicInches * 16.387064

val ForcePerLength.inLbPerIn get() = pli
val ForcePerLength.inLbPerFt get() = pli * 12.0
val ForcePerLength.inKipPerIn get() = pli / 1000.0
val ForcePerLength.inKipPerFt get() = pli * 12.0 / 1000.0
val ForcePerLength.inNewtonsPerMeter get() = pli * (4.4482216153 / 0.0254)