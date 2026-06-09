package com.lz.data.persistence.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

// DAOs
import com.lz.data.persistence.room.dao.catalog.AiscSectionDao
import com.lz.data.persistence.room.dao.catalog.WoodSectionDao
import com.lz.data.persistence.room.dao.project.CustomSectionDao
import com.lz.data.persistence.room.dao.MaterialDao
import com.lz.data.persistence.room.dao.CodeRegistryDao
import com.lz.data.persistence.room.dao.LoadCombinationDao
import com.lz.data.persistence.room.dao.ProjectDao
import com.lz.data.persistence.room.dao.CalculationDao

// Project entities
import com.lz.data.persistence.room.entity.project.CustomSectionRoomEntity
import com.lz.data.persistence.room.entity.CalculationRoomEntity
import com.lz.data.persistence.room.entity.ProjectRoomEntity

// Catalog entities
import com.lz.data.persistence.room.entity.catalog.AiscSectionRoomEntity
import com.lz.data.persistence.room.entity.catalog.WoodSectionRoomEntity
import com.lz.data.persistence.room.entity.MaterialRoomEntity

// Regulatory entities
import com.lz.data.persistence.room.entity.BuildingCodeEntity
import com.lz.data.persistence.room.entity.StandardEntity
import com.lz.data.persistence.room.entity.BuildingCodeStandardCrossRef
import com.lz.data.persistence.room.entity.LoadCombinationSetEntity
import com.lz.data.persistence.room.entity.LoadCombinationEntity
import com.lz.data.persistence.room.entity.LoadFactorEntity
import com.lz.data.persistence.room.entity.DefaultMaterialStandardEntity
import com.lz.data.persistence.room.entity.ServiceabilityCriterionRoomEntity
import com.lz.data.persistence.room.entity.DefaultLoadCaseRoomEntity

// Converters

/**
 * Room database for VectOS persistent engineering data.
 * 
 * Version 1 represents the baseline production schema:
 * - projects table
 * - calculations (metadata) table
 * - beam_calculations (payload) table
 */
@Database(
    entities = [
        // Project data
        ProjectRoomEntity::class,
        CalculationRoomEntity::class,
        CustomSectionRoomEntity::class,
        // Catalog data (seeded, read-only)
        AiscSectionRoomEntity::class,
        WoodSectionRoomEntity::class,
        MaterialRoomEntity::class,
        // Regulatory data (seeded, read-only)
        BuildingCodeEntity::class,
        StandardEntity::class,
        BuildingCodeStandardCrossRef::class,
        LoadCombinationSetEntity::class,
        LoadCombinationEntity::class,
        LoadFactorEntity::class,
        DefaultMaterialStandardEntity::class,
        ServiceabilityCriterionRoomEntity::class,
        DefaultLoadCaseRoomEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(StandardTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    // Project DAOs
    abstract fun projectDao(): ProjectDao
    abstract fun calculationDao(): CalculationDao

    // Catalog DAOs
    abstract fun aiscSectionDao(): AiscSectionDao
    abstract fun woodSectionDao(): WoodSectionDao
    abstract fun customSectionDao(): CustomSectionDao
    abstract fun materialDao(): MaterialDao

    // Regulatory DAOs
    abstract fun codeRegistryDao(): CodeRegistryDao
    abstract fun loadCombinationDao(): LoadCombinationDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "vectos.db"
            )
            .addMigrations(*Migrations.getMigrations())
            .addCallback(CALLBACK)
            // TODO: REMOVE before production release — will destroy user data on missing migration
            .fallbackToDestructiveMigration()
            .build()
        }

        val CALLBACK = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL("PRAGMA foreign_keys = ON;")
            }
        }
    }
}

