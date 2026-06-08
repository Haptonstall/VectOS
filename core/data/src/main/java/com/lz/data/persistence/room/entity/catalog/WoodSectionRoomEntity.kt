package com.lz.data.persistence.room.entity.catalog

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing Wood sections (e.g., NDS standard dressed sizes).
 */
@Entity(tableName = "wood_sections")
data class WoodSectionRoomEntity(
    @PrimaryKey val id: String,
    val designation: String,
    val nominalWidth: Double,
    val nominalDepth: Double,
    val dressedWidth: Double,
    val dressedDepth: Double
)
