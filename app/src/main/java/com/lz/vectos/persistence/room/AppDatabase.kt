package com.lz.vectos.persistence.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lz.vectos.persistence.room.dao.*
import com.lz.vectos.persistence.room.entity.*

/**
 * Room database for persistent engineering data.
 * 
 * Version 1 represents the baseline production schema:
 * - projects table
 * - calculations (metadata) table
 * - beam_calculations (payload) table
 */
@Database(
    entities = [
        ProjectRoomEntity::class,
        CalculationRoomEntity::class,
        BeamCalculationRoomEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun calculationDao(): CalculationDao
    abstract fun beamCalculationDao(): BeamCalculationDao
}

class Converters {
    @androidx.room.TypeConverter
    fun fromString(value: String): java.util.UUID {
        return java.util.UUID.fromString(value)
    }

    @androidx.room.TypeConverter
    fun uuidToString(uuid: java.util.UUID): String {
        return uuid.toString()
    }
}
