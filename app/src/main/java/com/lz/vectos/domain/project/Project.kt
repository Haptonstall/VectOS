package com.lz.vectos.domain.project

import java.util.UUID
import java.time.LocalDateTime

data class Project(
    val id: UUID,
    val name: String,
    val description: String?,
    val clientName: String?,
    val engineerName: String?,
    val createdAt: LocalDateTime
)
