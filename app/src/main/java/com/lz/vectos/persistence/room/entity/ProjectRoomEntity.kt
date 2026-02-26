package com.lz.vectos.persistence.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectRoomEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val description: String?,
    val clientName: String?,
    val engineerName: String?,
    val createdAtEpoch: Long,
    val updatedAtEpoch: Long
)
