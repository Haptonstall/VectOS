package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.Moment

/**
 * Represents the governing (worst-case) effect for a specific response type.
 */
data class GoverningEffect<T>(
    val value: T,
    val location: Double, // Meters from start
    val combinationName: String
)

/**
 * Analysis envelope results across all evaluated combinations.
 */
data class AnalysisEnvelope(
    val maxMoment: GoverningEffect<Moment>,
    val maxShear: GoverningEffect<Force>,
    val maxDeflection: GoverningEffect<Length>
)
