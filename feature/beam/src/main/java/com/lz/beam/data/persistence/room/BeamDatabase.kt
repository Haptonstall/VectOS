package com.lz.beam.data.persistence.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lz.beam.data.persistence.room.dao.BeamCalculationDao
import com.lz.beam.data.persistence.room.entity.BeamCalculationRoomEntity

/**
 * Room database for the Beam feature module.
 * Owns beam calculation payload storage independently of the core AppDatabase.
 *
 * Uses its own physical database file ("vectos_beam.db"), separate from
 * AppDatabase's "vectos.db". Two independent RoomDatabase subclasses cannot
 * safely share one physical file — each Room class tracks its own schema
 * identity hash inside the file it opens, and AppDatabase (a different
 * entity set, currently at version 2) was previously sharing this same
 * "vectos.db" file, which caused Room's schema validation to fail
 * intermittently depending on which database initialized its identity hash
 * first. Cross-database writes are non-atomic as a result (see
 * RoomBeamCalculationRepository's metadata-first-then-payload ordering for
 * the mitigation), but each database's own internal consistency is now sound.
 *
 * Version history:
 *   v1 — initial beam_calculations table
 *   v2 — persisted editable beam input snapshot
 */
@Database(
    entities = [BeamCalculationRoomEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(BeamTypeConverters::class)
abstract class BeamDatabase : RoomDatabase() {

    abstract fun beamCalculationDao(): BeamCalculationDao

    companion object {

        fun create(context: Context): BeamDatabase {
            return Room.databaseBuilder(
                context,
                BeamDatabase::class.java,
                "vectos_beam.db"
            )
                .addMigrations(MIGRATION_1_2)
                .addCallback(CALLBACK)
                .build()
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE beam_calculations ADD COLUMN inputsJson TEXT NOT NULL DEFAULT '{}'")
            }
        }

        private val CALLBACK = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL("PRAGMA foreign_keys = ON;")
            }
        }
    }
}
