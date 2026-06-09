package com.lz.model.structural

import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment
import com.lz.model.units.inches
import com.lz.model.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class Flange {
    TOP, BOTTOM, NONE
}

/**
 * Represents the 6-DOF internal forces (demands) and localized structural state
 * at a specific cross-sectional station along a structural member.
 */

@Serializable
data class StationDemand(
    @Serializable(with = UUIDSerializer::class)
    val spanId: UUID,

    val x: Length, // Distance from start of the current span

    // --- Major Axis (Strong Axis) Demands ---
    val moment: Moment,
    val shear: Force,
    val deflection: Length = 0.0.inches,

    // --- Minor Axis (Weak Axis) Demands ---
    val momentY: Moment = Moment(0.0),
    val shearY: Force = Force(0.0),
    val deflectionY: Length = 0.0.inches,

    // --- Axial & Torsional Demands ---
    val axial: Force = Force(0.0),
    val torque: Moment = Moment(0.0),

    // --- Serviceability Verification Benchmarks ---
    val allowableDeflection: Length = Double.POSITIVE_INFINITY.inches,
    val allowableDeflectionY: Length = Double.POSITIVE_INFINITY.inches,

    // --- Localized Stability State at Point X ---
    val cb: Double = 1.0,
    val lbTop: Length = 0.0.inches,
    val lbBottom: Length = 0.0.inches,
    val compressionFlange: Flange = Flange.NONE
)