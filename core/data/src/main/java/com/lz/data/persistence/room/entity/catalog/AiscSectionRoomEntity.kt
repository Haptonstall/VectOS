package com.lz.data.persistence.room.entity.catalog

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
    val warpingCw: Double,                // Cw
    // HSS only (null for every other type). AISC Table B4.1a local-
    // buckling slenderness for HSS (F7.2/F7.3 flange & web classification,
    // G4 shear Aw) uses the *flat* width/height between corners, not the
    // overall outside B/H already carried above as flangeWidth/depth.
    val flatWidthB: Double? = null,      // b (flat width, rect HSS only)
    val flatHeightH: Double? = null      // h (flat height, rect HSS only)
)
