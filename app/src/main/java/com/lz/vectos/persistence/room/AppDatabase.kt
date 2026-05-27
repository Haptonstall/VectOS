package com.lz.vectos.persistence.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
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
        BeamCalculationRoomEntity::class,
        BuildingCodeEntity::class,
        StandardEntity::class,
        BuildingCodeStandardCrossRef::class,
        LoadCombinationSetEntity::class,
        LoadCombinationEntity::class,
        LoadFactorEntity::class,
        DefaultMaterialStandardEntity::class,
        ServiceabilityCriterionRoomEntity::class,
        DefaultLoadCaseRoomEntity::class,
        AiscSectionRoomEntity::class,
        WoodSectionRoomEntity::class,
        CustomSectionRoomEntity::class,
        MaterialRoomEntity::class
    ],
    version = 18,
    exportSchema = true
)
@TypeConverters(Converters::class, StandardTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun calculationDao(): CalculationDao
    abstract fun beamCalculationDao(): BeamCalculationDao
    abstract fun buildingCodeDao(): BuildingCodeDao
    abstract fun structuralDataDao(): StructuralDataDao
    abstract fun aiscSectionDao(): AiscSectionDao
    abstract fun woodSectionDao(): WoodSectionDao
    abstract fun customSectionDao(): CustomSectionDao
    abstract fun materialDao(): MaterialDao

    companion object {
        fun create(context: android.content.Context): AppDatabase {
            return androidx.room.Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "vectos.db"
            )
            .addMigrations(*Migrations.getMigrations())
            .addCallback(CALLBACK)
            .fallbackToDestructiveMigration()
            .build()
        }

        val CALLBACK = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL("PRAGMA foreign_keys = ON;")
            }
        }
    }
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
