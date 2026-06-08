package com.lz.beam.data.persistence.room

import androidx.room.TypeConverter
import java.util.UUID

/**
 * Type converters for the BeamDatabase.
 * Beam entities use JSON serialization for complex objects,
 * so only UUID conversion is needed at the Room level.
 */
class BeamTypeConverters {

    @TypeConverter
    fun fromUuid(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun toUuid(value: String): UUID = UUID.fromString(value)
}