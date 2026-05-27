package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.MaterialType

/**
 * Registry-style resolver for material design strategies.
 */
object MaterialDesignResolver {
    fun resolve(materialType: MaterialType, code: BuildingCode): MaterialDesignStrategy {
        return when (materialType) {
            MaterialType.STEEL -> SteelDesignStrategy()
            MaterialType.WOOD -> WoodDesignStrategy()
            else -> throw IllegalArgumentException("No design strategy for material: $materialType")
        }
    }
}
