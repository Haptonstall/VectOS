package com.lz.vectos.persistence.entity

import java.util.UUID

/**
 * Persistence model for a Project.
 * Stores core metadata using primitive types.
 */
data class ProjectEntity(
    val id: UUID,
    val name: String,
    val description: String?,
    val clientName: String?,
    val engineerName: String?,
    val createdAtEpoch: Long,
    val updatedAtEpoch: Long
)
