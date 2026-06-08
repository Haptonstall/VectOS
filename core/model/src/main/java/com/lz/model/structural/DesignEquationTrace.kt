package com.lz.model.structural

import kotlinx.serialization.Serializable

/**
 * Provides a traceable breakdown of a design equation or load combination result.
 */
@Serializable
data class DesignEquationTrace(
    val symbolicEquation: String,
    val substitutedEquation: String,
    val result: String,
    val units: String,
    val codeReference: String,
    val variables: Map<String, Double> = emptyMap()
)