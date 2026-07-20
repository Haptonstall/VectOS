package com.lz.data.persistence.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "calculations")
data class CalculationRoomEntity(
    @PrimaryKey val id: UUID,
    val projectId: UUID,
    val toolId: String,
    val name: String,
    val calculationType: String,
    val createdAtEpoch: Long,
    val updatedAtEpoch: Long
)