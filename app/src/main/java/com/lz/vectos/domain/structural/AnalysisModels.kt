package com.lz.vectos.domain.structural

import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment

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
    val maxMomentZ: GoverningEffect<Moment>,
    val maxMomentY: GoverningEffect<Moment>,
    val maxShearY: GoverningEffect<Force>,
    val maxShearZ: GoverningEffect<Force>,
    val maxTorsion: GoverningEffect<Moment>,
    val maxAxial: GoverningEffect<Force>,
    val maxDeflection: GoverningEffect<Length>
)
