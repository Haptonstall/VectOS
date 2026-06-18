package com.lz.data.persistence.room.mapper

import com.lz.data.persistence.room.entity.AiscSectionRoomEntity
import com.lz.data.persistence.room.entity.CustomSectionRoomEntity
import com.lz.data.persistence.room.entity.WoodSectionRoomEntity
import com.lz.model.structural.CustomProfile
import com.lz.model.structural.MaterialType
import com.lz.model.structural.SectionAxisProperties
import com.lz.model.structural.ShapeType
import com.lz.model.structural.SteelProfile
import com.lz.model.structural.WoodProfile
import com.lz.model.units.in2
import com.lz.model.units.in3
import com.lz.model.units.in4
import com.lz.model.units.inches

/**
 * Maps AiscSectionRoomEntity (Steel) to SteelProfile domain model.
 */
fun AiscSectionRoomEntity.toDomain(): SteelProfile {
    return SteelProfile(
        id = id,
        designation = designation,
        shapeType = when (type) {
            "W" -> ShapeType.WIDE_FLANGE
            "C" -> ShapeType.CHANNEL
            "L" -> ShapeType.ANGLE
            "HSS_Rect" -> ShapeType.RECTANGULAR_HSS
            "HSS_Round" -> ShapeType.ROUND_HSS
            else -> ShapeType.WIDE_FLANGE
        },
        area = area.in2,
        depth = depth.inches,
        webThickness = webThickness.inches,
        flangeWidth = flangeWidth.inches,
        flangeThickness = flangeThickness.inches,
        torsionalConstantJ = torsionalJ,
        warpingConstantCw = warpingCw,
        propertiesStrongAxis = SectionAxisProperties(
            i = ix.in4,
            s = sx.in3,
            z = zx.in3,
            r = rx.inches
        ),
        propertiesWeakAxis = SectionAxisProperties(
            i = iy.in4,
            s = sy.in3,
            z = zy.in3,
            r = ry.inches
        )
    )
}

/**
 * Maps WoodSectionRoomEntity to WoodProfile domain model.
 */
fun WoodSectionRoomEntity.toDomain(): WoodProfile {
    return WoodProfile(
        id = id,
        designation = designation,
        nominalWidth = nominalWidth.inches,
        nominalDepth = nominalDepth.inches,
        dressedWidth = dressedWidth.inches,
        dressedDepth = dressedDepth.inches
    )
}

/**
 * Maps CustomSectionRoomEntity to CustomProfile domain model.
 */
fun CustomSectionRoomEntity.toDomain(materialType: MaterialType): CustomProfile {
    return CustomProfile(
        id = id,
        designation = designation,
        materialType = materialType,
        shapeType = ShapeType.SOLID_RECTANGULAR, // Default for custom, can be expanded
        area = area.in2,
        depth = (depth ?: 0.0).inches,
        propertiesStrongAxis = SectionAxisProperties(
            i = ix.in4,
            s = sx.in3,
            z = (zx ?: 0.0).in3,
            r = 0.0.inches // Calculated if needed, or stored
        ),
        propertiesWeakAxis = SectionAxisProperties(
            i = (iy ?: 0.0).in4,
            s = (sy ?: 0.0).in3,
            z = (zy ?: 0.0).in3,
            r = 0.0.inches
        )
    )
}