package com.lz.beam.data.persistence.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lz.beam.data.persistence.room.dao.BeamCalculationDao
import com.lz.beam.data.persistence.room.entity.BeamCalculationRoomEntity

/**
 * Room database for the Beam feature module.
 * Owns beam calculation payload storage independently of the core AppDatabase.
 *
 * Uses the same physical database file ("vectos.db") as AppDatabase so that
 * cross-database transactions via SQLite are possible when needed.
 *
 * Version history:
 *   v1 — initial beam_calculations table
 */
@Database(
    entities = [BeamCalculationRoomEntity::class],
    version = 1,
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
                "vectos.db"    // Same file as AppDatabase — intentional
            )
                .addCallback(CALLBACK)
                .build()
        }

        private val CALLBACK = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL("PRAGMA foreign_keys = ON;")
            }
        }
    }
}