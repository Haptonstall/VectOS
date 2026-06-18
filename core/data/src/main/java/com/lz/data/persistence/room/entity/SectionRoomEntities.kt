package com.lz.data.persistence.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an AISC steel section from the v15.0 database.
 */
@Entity(tableName = "aisc_sections")
data class AiscSectionRoomEntity(
    @PrimaryKey val id: String,          // EDI_Std_Nomenclature
    val designation: String,             // AISC_Manual_Label
    val type: String,                    // e.g., "W", "C", "L"
    val area: Double,                    // A
    val depth: Double,                   // d
    val webThickness: Double,            // tw
    val flangeWidth: Double,             // bf
    val flangeThickness: Double,         // tf
    val ix: Double,                      // Ix
    val sx: Double,                      // Sx
    val zx: Double,                      // Zx
    val rx: Double,                      // rx
    val iy: Double,                      // Iy
    val sy: Double,                      // Sy
    val zy: Double,                      // Zy
    val ry: Double,                      // ry
    val torsionalJ: Double,              // J
    val warpingCw: Double                // Cw
)

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