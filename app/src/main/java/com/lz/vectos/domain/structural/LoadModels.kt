package com.lz.vectos.domain.structural

/**
 * Direction of the load relative to the member.
 */
enum class LoadDirection {
    VERTICAL_DOWN,
    VERTICAL_UP,
    LATERAL_LEFT,
    LATERAL_RIGHT
}

/**
 * Base abstraction for all structural loads.
 * Positions are normalized to the member length (base units).
 */
sealed class Load {
    abstract val locationStart: Double
    abstract val locationEnd: Double
    abstract val direction: LoadDirection
    
    data class PointLoad(
        val value: Double, // Force (N or lb)
        override val locationStart: Double,
        override val direction: LoadDirection = LoadDirection.VERTICAL_DOWN
    ) : Load() {
        override val locationEnd: Double = locationStart
    }

    data class UniformDistributedLoad(
        val value: Double, // Force per length (N/m or lb/ft)
        override val locationStart: Double = 0.0,
        override val locationEnd: Double,
        override val direction: LoadDirection = LoadDirection.VERTICAL_DOWN
    ) : Load()

    data class TrapezoidalLoad(
        val valueStart: Double,
        val valueEnd: Double,
        override val locationStart: Double,
        override val locationEnd: Double,
        override val direction: LoadDirection = LoadDirection.VERTICAL_DOWN
    ) : Load()
}
