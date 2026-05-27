package com.lz.vectos.persistence.room

import androidx.room.TypeConverter
import com.lz.vectos.domain.beam.MaterialType
import com.lz.vectos.domain.structural.BuildingCode
import com.lz.vectos.domain.structural.Standard
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Type converters for the rich structural objects and dynamic maps.
 * Uses JSON serialization for the MaterialStandards map to allow for future expansion
 * without database schema changes.
 */
class StandardTypeConverters {

    @TypeConverter
    fun fromBuildingCode(code: BuildingCode): String = code.shortName

    @TypeConverter
    fun toBuildingCode(id: String): BuildingCode = BuildingCode.fromId(id)

    @TypeConverter
    fun fromStandard(standard: Standard): String = standard.shortName

    @TypeConverter
    fun toStandard(id: String): Standard = Standard.fromId(id)

    @TypeConverter
    fun fromMaterialStandardsMap(map: Map<MaterialType, Standard>): String {
        val stringMap = map.mapKeys { it.key.name }.mapValues { it.value.shortName }
        return Json.encodeToString(stringMap)
    }

    @TypeConverter
    fun toMaterialStandardsMap(json: String): Map<MaterialType, Standard> {
        return try {
            val stringMap: Map<String, String> = Json.decodeFromString(json)
            stringMap.mapKeys { MaterialType.valueOf(it.key) }
                .mapValues { Standard.fromId(it.value) }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun fromDesignMethodology(methodology: com.lz.vectos.domain.structural.DesignMethodology): String = methodology.name

    @TypeConverter
    fun toDesignMethodology(name: String): com.lz.vectos.domain.structural.DesignMethodology = 
        com.lz.vectos.domain.structural.DesignMethodology.valueOf(name)

    @TypeConverter
    fun fromLimitState(limitState: com.lz.vectos.domain.structural.LimitState): String = limitState.name

    @TypeConverter
    fun toLimitState(name: String): com.lz.vectos.domain.structural.LimitState = 
        com.lz.vectos.domain.structural.LimitState.valueOf(name)

    @TypeConverter
    fun fromStructuralReferenceKey(key: com.lz.vectos.domain.structural.StructuralReferenceKey): String = key.name

    @TypeConverter
    fun toStructuralReferenceKey(name: String): com.lz.vectos.domain.structural.StructuralReferenceKey = 
        com.lz.vectos.domain.structural.StructuralReferenceKey.valueOf(name)

    @TypeConverter
    fun fromServiceabilityLimitType(type: com.lz.vectos.domain.structural.ServiceabilityLimitType): String = type.name

    @TypeConverter
    fun toServiceabilityLimitType(name: String): com.lz.vectos.domain.structural.ServiceabilityLimitType = 
        com.lz.vectos.domain.structural.ServiceabilityLimitType.valueOf(name)

    @TypeConverter
    fun fromLoadCategory(category: com.lz.vectos.domain.structural.LoadCategory): String = category.name

    @TypeConverter
    fun toLoadCategory(name: String): com.lz.vectos.domain.structural.LoadCategory = 
        com.lz.vectos.domain.structural.LoadCategory.valueOf(name)

    @TypeConverter
    fun fromCombinationType(type: com.lz.vectos.domain.structural.CombinationType): String = type.name

    @TypeConverter
    fun toCombinationType(name: String): com.lz.vectos.domain.structural.CombinationType = 
        com.lz.vectos.domain.structural.CombinationType.valueOf(name)
}
