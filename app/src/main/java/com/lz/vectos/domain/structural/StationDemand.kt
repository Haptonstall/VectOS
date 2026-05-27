package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.*
import com.lz.vectos.util.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents the 6-DOF internal forces (demands) at a specific station along a member.
 * This is used for capacity checks and diagram generation.
 */
@Serializable
data class StationDemand(
    val x: Length,
    val moment: Moment,
    val shear: Force,
    val axial: Force = Force(0.0),
    val momentY: Moment = Moment(0.0),
    val shearY: Force = Force(0.0),
    val torque: Moment = Moment(0.0),
    val deflection: Length = 0.0.inches,
    val allowableDeflection: Length = Double.POSITIVE_INFINITY.inches,
    val cb: Double = 1.0,
    val lbTop: Length = 0.0.inches,
    val lbBottom: Length = 0.0.inches,
    val compressionFlange: Flange = Flange.TOP,
    @Serializable(with = UUIDSerializer::class)
    val spanId: UUID
)

@Serializable
enum class Flange {
    TOP, BOTTOM
}
