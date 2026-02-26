package com.lz.vectos.persistence.entity

import java.util.UUID

/**
 * Generic persistence model for any calculation metadata.
 * Acts as the header for specific calculator data.
 */
data class CalculationEntity(
    val id: UUID,
    val projectId: UUID,
    val name: String,
    val calculationType: String, // e.g., "BEAM_SIMPLY_SUPPORTED"
    val createdAtEpoch: Long,
    val updatedAtEpoch: Long
)
