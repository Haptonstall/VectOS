package com.lz.model.structural

import com.lz.model.regulatory.LoadCategory
import com.lz.model.units.Force
import com.lz.model.units.ForcePerLength
import com.lz.model.units.Length
import com.lz.model.units.Moment
import com.lz.model.units.Pressure
import com.lz.model.units.inches
import com.lz.model.units.times
import com.lz.model.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Direction of the load relative to the member.
 */
@Serializable
enum class LoadDirection {
    VERTICAL_DOWN,
    VERTICAL_UP,
    LATERAL_LEFT,
    LATERAL_RIGHT,
    AXIAL_COMPRESSION,
    AXIAL_TENSION,
    MOMENT_CLOCKWISE,
    MOMENT_COUNTER_CLOCKWISE,
    TORSION_CLOCKWISE,
    TORSION_COUNTER_CLOCKWISE
}

/**
 * Base abstraction for all structural loads.
 * Positions and magnitudes are strongly typed using the unit system.
 */
@Serializable
sealed class Load {
    @Serializable(with = UUIDSerializer::class)
    abstract val id: UUID
    @Serializable(with = UUIDSerializer::class)
    abstract val spanId: UUID
    abstract val locationStart: Length
    abstract val locationEnd: Length
    abstract val direction: LoadDirection
    abstract val category: LoadCategory

    @Serializable
    data class PointLoad(
        val value: Force,
        @Serializable(with = UUIDSerializer::class)
        override val spanId: UUID,
        override val locationStart: Length,
        override val category: LoadCategory = LoadCategory.DEAD,
        override val direction: LoadDirection = LoadDirection.VERTICAL_DOWN,
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID()
    ) : Load() {
        override val locationEnd: Length = locationStart
    }

    @Serializable
    data class UniformDistributedLoad(
        val value: ForcePerLength,
        @Serializable(with = UUIDSerializer::class)
        override val spanId: UUID,
        override val locationStart: Length = 0.0.inches,
        override val locationEnd: Length,
        override val category: LoadCategory = LoadCategory.DEAD,
        override val direction: LoadDirection = LoadDirection.VERTICAL_DOWN,
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID()
    ) : Load()

    @Serializable
    data class AxialLoad(
        val value: Force,
        @Serializable(with = UUIDSerializer::class)
        override val spanId: UUID,
        override val category: LoadCategory = LoadCategory.DEAD,
        override val direction: LoadDirection = LoadDirection.AXIAL_COMPRESSION,
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID()
    ) : Load() {
        override val locationStart: Length = 0.0.inches
        override val locationEnd: Length = 0.0.inches
    }

    @Serializable
    data class TrapezoidalLoad(
        val valueStart: ForcePerLength,
        val valueEnd: ForcePerLength,
        @Serializable(with = UUIDSerializer::class)
        override val spanId: UUID,
        override val locationStart: Length,
        override val locationEnd: Length,
        override val category: LoadCategory = LoadCategory.DEAD,
        override val direction: LoadDirection = LoadDirection.VERTICAL_DOWN,
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID()
    ) : Load()

    @Serializable
    data class PointMoment(
        val value: Moment,
        @Serializable(with = UUIDSerializer::class)
        override val spanId: UUID,
        override val locationStart: Length,
        override val category: LoadCategory = LoadCategory.DEAD,
        override val direction: LoadDirection = LoadDirection.MOMENT_CLOCKWISE,
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID()
    ) : Load() {
        override val locationEnd: Length = locationStart
    }

    @Serializable
    data class PointTorque(
        val value: Moment,
        @Serializable(with = UUIDSerializer::class)
        override val spanId: UUID,
        override val locationStart: Length,
        override val category: LoadCategory = LoadCategory.DEAD,
        override val direction: LoadDirection = LoadDirection.TORSION_CLOCKWISE,
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID()
    ) : Load() {
        override val locationEnd: Length = locationStart
    }

    @Serializable
    data class TributaryLoad(
        val pressure: Pressure,
        val widthStart: Length,
        val widthEnd: Length,
        @Serializable(with = UUIDSerializer::class)
        override val spanId: UUID,
        override val locationStart: Length,
        override val locationEnd: Length,
        override val category: LoadCategory = LoadCategory.DEAD,
        override val direction: LoadDirection = LoadDirection.VERTICAL_DOWN,
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID()
    ) : Load() {
        fun toTrapezoidal(): TrapezoidalLoad {
            val startForce = pressure * widthStart
            val endForce = pressure * widthEnd
            return TrapezoidalLoad(
                valueStart = startForce,
                valueEnd = endForce,
                spanId = spanId,
                locationStart = locationStart,
                locationEnd = locationEnd,
                category = category,
                direction = direction,
                id = id
            )
        }
    }
}