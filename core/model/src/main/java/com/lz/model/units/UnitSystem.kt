package com.lz.model.units

import kotlinx.serialization.Serializable

/**
 * High-level categorization of units.
 */
@Serializable
enum class UnitSystem {
    METRIC,
    IMPERIAL
}

/**
 * Supported units for Length.
 */
enum class LengthUnit(val symbol: String) {
    METERS("m"),
    MILLIMETERS("mm"),
    FEET("ft"),
    INCHES("in")
}

/**
 * Supported units for Force.
 */
enum class ForceUnit(val symbol: String) {
    NEWTONS("N"),
    KILONEWTONS("kN"),
    POUNDS_FORCE("lbf"),
    KIPS("kip")
}

/**
 * Supported units for Pressure/Stress.
 */
enum class PressureUnit(val symbol: String) {
    PASCALS("Pa"),
    MEGA_PASCALS("MPa"),
    GIGA_PASCALS("GPa"),
    PSI("psi"),
    KSI("ksi")
}

/**
 * Supported units for Moment of Inertia (Area).
 */
enum class MomentOfInertiaUnit(val symbol: String) {
    METERS_4("m⁴"),
    MILLIMETERS_4("mm⁴"),
    INCHES_4("in⁴")
}

/**
 * Supported units for Bending Moment.
 */
enum class MomentUnit(val symbol: String) {
    NEWTON_METERS("N⋅m"),
    POUND_FEET("lb⋅ft"),
    POUND_INCHES("lb⋅in"),
    KIP_FEET("k-ft"),
    KIP_INCHES("k-in")
}

/**
 * Centralized converter between raw display values and internal typed base units.
 *
 * This object is the EXCLUSIVE gateway for unit conversions.
 * - [toInternalBase]: Converts raw user input into domain-safe typed objects.
 * - [toDisplayValue]: Converts internal typed objects back into raw doubles for the UI.
 */
object UnitConverter {

    // --- LENGTH CONVERSIONS ---

    fun toInternalBase(value: Double, unit: LengthUnit): Length = when (unit) {
        LengthUnit.METERS -> value.meters
        LengthUnit.MILLIMETERS -> value.mm
        LengthUnit.FEET -> value.feet
        LengthUnit.INCHES -> value.inches
    }

    fun toDisplayValue(length: Length, unit: LengthUnit): Double = when (unit) {
        LengthUnit.METERS -> length.inMeters
        LengthUnit.MILLIMETERS -> length.inMm
        LengthUnit.FEET -> length.inFeet
        LengthUnit.INCHES -> length.inInches
    }

    // --- FORCE CONVERSIONS ---

    fun toInternalBase(value: Double, unit: ForceUnit): Force = when (unit) {
        ForceUnit.NEWTONS -> value.newtons
        ForceUnit.KILONEWTONS -> value.kiloNewtons
        ForceUnit.POUNDS_FORCE -> value.poundsForce
        ForceUnit.KIPS -> value.kips
    }

    fun toDisplayValue(force: Force, unit: ForceUnit): Double = when (unit) {
        ForceUnit.NEWTONS -> force.inNewtons
        ForceUnit.KILONEWTONS -> force.inKiloNewtons
        ForceUnit.POUNDS_FORCE -> force.inPoundsForce
        ForceUnit.KIPS -> force.inKips
    }

    // --- MOMENT CONVERSIONS ---

    fun toDisplayValue(moment: Moment, unit: MomentUnit): Double = when (unit) {
        MomentUnit.NEWTON_METERS -> moment.inNewtonMeters
        MomentUnit.POUND_FEET -> moment.inLbFt
        MomentUnit.POUND_INCHES -> moment.inLbIn
        MomentUnit.KIP_FEET -> moment.inLbFt / 1000.0
        MomentUnit.KIP_INCHES -> moment.inLbIn / 1000.0
    }

    // --- PRESSURE CONVERSIONS ---

    fun toInternalBase(value: Double, unit: PressureUnit): Pressure = when (unit) {
        PressureUnit.PASCALS -> value.pascals
        PressureUnit.MEGA_PASCALS -> value.megaPascals
        PressureUnit.GIGA_PASCALS -> value.gigaPascals
        PressureUnit.PSI -> value.psi
        PressureUnit.KSI -> value.ksi
    }

    fun toDisplayValue(pressure: Pressure, unit: PressureUnit): Double = when (unit) {
        PressureUnit.PASCALS -> pressure.inPascals
        PressureUnit.MEGA_PASCALS -> pressure.inMegaPascals
        PressureUnit.GIGA_PASCALS -> pressure.inGigaPascals
        PressureUnit.PSI -> pressure.inPsi
        PressureUnit.KSI -> pressure.inKsi
    }

    // --- MOMENT OF INERTIA CONVERSIONS ---

    fun toInternalBase(value: Double, unit: MomentOfInertiaUnit): MomentOfInertia = when (unit) {
        MomentOfInertiaUnit.METERS_4 -> value.m4
        MomentOfInertiaUnit.MILLIMETERS_4 -> value.mm4
        MomentOfInertiaUnit.INCHES_4 -> value.in4
    }

    fun toDisplayValue(moi: MomentOfInertia, unit: MomentOfInertiaUnit): Double = when (unit) {
        MomentOfInertiaUnit.METERS_4 -> moi.inM4
        MomentOfInertiaUnit.MILLIMETERS_4 -> moi.inMm4
        MomentOfInertiaUnit.INCHES_4 -> moi.inIn4
    }
}