package com.lz.data.persistence.room.entity.project

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing User-defined custom sections.
 */
@Entity(tableName = "custom_sections")
data class CustomSectionRoomEntity(
    @PrimaryKey val id: String,
    val designation: String,
    // Support for parametric dimensions (rectangular)
    val width: Double? = null,
    val depth: Double? = null,
    // Explicit gross properties (for complex or unknown shapes)
    val area: Double,
    val ix: Double,
    val sx: Double,
    val zx: Double? = null,
    val iy: Double? = null,
    val sy: Double? = null,
    val zy: Double? = null
)