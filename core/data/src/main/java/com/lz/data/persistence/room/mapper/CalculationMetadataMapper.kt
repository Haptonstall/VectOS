package com.lz.data.persistence.room.mapper

import com.lz.data.persistence.room.entity.CalculationRoomEntity
import com.lz.domain.calculation.CalculationMetadata
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun CalculationRoomEntity.toMetadataDomain(): CalculationMetadata = CalculationMetadata(
    id = id,
    name = name,
    createdAt = LocalDateTime.ofInstant(
        Instant.ofEpochSecond(createdAtEpoch),
        ZoneOffset.UTC
    )
)
