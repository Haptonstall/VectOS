package com.lz.vectos.viewmodel

/**
 * UI-facing display model for Beam calculation results.
 * Contains only pre-formatted Strings ready for display in Compose.
 * This prevents formatting logic and unit math from leaking into the UI layer.
 */
data class BeamDisplayModel(
    val calculationId: String,
    val maxBendingMoment: String,
    val maxShear: String,
    val maxDeflection: String,
    val timestamp: String
)
