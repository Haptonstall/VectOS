package com.lz.vectos.domain.units

/**
 * These value classes wrap primitive Double values to provide compile-time unit safety.
 * The raw base unit value (e.g., `meters`, `newtons`) is marked `internal` to prevent
 * uncontrolled access from outside the domain layer (e.g., UI, ViewModels).
 *
 * Domain-layer calculators and the unit conversion system are permitted to access these
 * raw values, as they operate in the base unit system by design. For external consumers
 * (like UI), use the `UnitConverter` to get display values.
 */

@JvmInline
value class Length(internal val meters: Double) {
    operator fun plus(other: Length) = Length(this.meters + other.meters)
    operator fun minus(other: Length) = Length(this.meters - other.meters)
    operator fun times(multiplier: Double) = Length(this.meters * multiplier)
    operator fun div(divider: Double) = Length(this.meters / divider)
    operator fun div(other: Length): Double = this.meters / other.meters
}

@JvmInline
value class Force(internal val newtons: Double) {
    operator fun plus(other: Force) = Force(this.newtons + other.newtons)
    operator fun minus(other: Force) = Force(this.newtons - other.newtons)
    operator fun times(multiplier: Double) = Force(this.newtons * multiplier)
    operator fun div(divider: Double) = Force(this.newtons / divider)
}

@JvmInline
value class Moment(internal val newtonMeters: Double) {
    operator fun plus(other: Moment) = Moment(this.newtonMeters + other.newtonMeters)
    operator fun minus(other: Moment) = Moment(this.newtonMeters - other.newtonMeters)
}

@JvmInline
value class Pressure(internal val pascals: Double) {
    // Used for Stress and Modulus of Elasticity
}

@JvmInline
value class ForcePerLength(internal val newtonsPerMeter: Double) {
    // This operation is allowed as it's a fundamental physics definition.
    operator fun times(length: Length) = Force(this.newtonsPerMeter * length.meters)
}

@JvmInline
value class MomentOfInertia(internal val metersToFourth: Double)

/**
 * DSL for constructing unit-safe types from raw numbers.
 * Primarily for use in tests and domain setup.
 */

// Length construction
val Double.meters get() = Length(this)
val Double.mm get() = Length(this / 1000.0)
val Double.inches get() = Length(this * 0.0254)
val Double.feet get() = Length(this * 0.3048)

// Force construction
val Double.newtons get() = Force(this)
val Double.kiloNewtons get() = Force(this * 1000.0)
val Double.poundsForce get() = Force(this * 4.4482216153)

// Pressure / Stress construction
val Double.pascals get() = Pressure(this)
val Double.megaPascals get() = Pressure(this * 1_000_000.0)
val Double.gigaPascals get() = Pressure(this * 1_000_000_000.0)
val Double.psi get() = Pressure(this * 6894.75729)

// Moment of Inertia construction
val Double.m4 get() = MomentOfInertia(this)
val Double.cm4 get() = MomentOfInertia(this * 1e-8)
val Double.mm4 get() = MomentOfInertia(this * 1e-12)
val Double.in4 get() = MomentOfInertia(this * 0.0000004162314256)

/**
 * Creates a Moment from a Force and a Length.
 * This is an explicit factory function, not an operator, to enforce clarity.
 * Moment is NOT implicitly Force * Length.
 */
fun createMoment(force: Force, length: Length): Moment = Moment(force.newtons * length.meters)

/**
 * Internal accessors for converting to display units.
 * These are consumed by `UnitConverter` and are not for public use.
 * UI/ViewModel layers MUST use the `UnitConverter` to ensure consistency.
 */
internal val Length.inMeters get() = meters
internal val Length.inMm get() = meters * 1000.0
internal val Length.inInches get() = meters / 0.0254
internal val Length.inFeet get() = meters / 0.3048

internal val Force.inNewtons get() = newtons
internal val Force.inKiloNewtons get() = newtons / 1000.0
internal val Force.inPoundsForce get() = newtons / 4.4482216153

internal val Pressure.inPascals get() = pascals
internal val Pressure.inMegaPascals get() = pascals / 1_000_000.0
internal val Pressure.inGigaPascals get() = pascals / 1_000_000_000.0
internal val Pressure.inPsi get() = pascals / 6894.75729

internal val Moment.inNewtonMeters get() = newtonMeters

internal val MomentOfInertia.inM4 get() = metersToFourth
internal val MomentOfInertia.inIn4 get() = metersToFourth / 0.0000004162314256
