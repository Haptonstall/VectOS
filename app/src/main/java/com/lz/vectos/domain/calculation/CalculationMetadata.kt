package com.lz.vectos.domain.calculation

import java.util.UUID
import java.time.LocalDateTime

data class CalculationMetadata(
    val id: UUID,
    val name: String,
    val createdAt: LocalDateTime
)
