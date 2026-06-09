package com.lz.data.persistence.room

import androidx.room.TypeConverter
import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.StandardReferenceKey
import com.lz.model.regulatory.codes.CodeReferenceKey
import com.lz.model.regulatory.codes.ServiceabilityLimitType
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.LimitState
import com.lz.model.structural.MaterialType
import java.util.UUID

/**
 * Room type converters for VectOS core database.
 *
 * Strategy: all enum types convert via their name string.
 * Complex domain objects (BuildingCode, Standard) are NOT converted
 * here — they are always stored as ID strings and assembled by
 * repositories via joined queries.
 */
class StandardTypeConverters {

    // --- UUID ---
    @TypeConverter
    fun fromUuid(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun toUuid(value: String): UUID = UUID.fromString(value)

    // --- Structural enums ---
    @TypeConverter
    fun fromDesignMethodology(value: DesignMethodology): String = value.name

    @TypeConverter
    fun toDesignMethodology(value: String): DesignMethodology =
        DesignMethodology.valueOf(value)

    @TypeConverter
    fun fromMaterialType(value: MaterialType): String = value.name

    @TypeConverter
    fun toMaterialType(value: String): MaterialType =
        MaterialType.valueOf(value)

    @TypeConverter
    fun fromLimitState(value: LimitState): String = value.name

    @TypeConverter
    fun toLimitState(value: String): LimitState =
        LimitState.valueOf(value)

    // --- Regulatory enums ---
    @TypeConverter
    fun fromLoadCategory(value: LoadCategory): String = value.name

    @TypeConverter
    fun toLoadCategory(value: String): LoadCategory = LoadCategory.valueOf(value)

    @TypeConverter
    fun fromCombinationType(value: CombinationType): String = value.name

    @TypeConverter
    fun toCombinationType(value: String): CombinationType = CombinationType.valueOf(value)

    @TypeConverter
    fun fromServiceabilityLimitType(value: ServiceabilityLimitType): String = value.name

    @TypeConverter
    fun toServiceabilityLimitType(value: String): ServiceabilityLimitType =
        ServiceabilityLimitType.valueOf(value)

    @TypeConverter
    fun fromCodeReferenceKey(value: CodeReferenceKey): String = value.name

    @TypeConverter
    fun toCodeReferenceKey(value: String): CodeReferenceKey = CodeReferenceKey.valueOf(value)

    @TypeConverter
    fun fromStandardReferenceKey(value: StandardReferenceKey): String = value.name

    @TypeConverter
    fun toStandardReferenceKey(value: String): StandardReferenceKey = StandardReferenceKey.valueOf(value)

}