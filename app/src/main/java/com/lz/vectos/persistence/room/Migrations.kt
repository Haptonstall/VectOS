package com.lz.vectos.persistence.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Registry for Room database migrations.
 */
object Migrations {

    /**
     * Placeholder for the first schema migration (Version 1 to 2).
     * 
     * TODO: When a schema change is required:
     * 1. Define the SQL changes here (e.g., database.execSQL("ALTER TABLE..."))
     * 2. Increment the 'version' in [AppDatabase]
     * 3. Add this object to the .addMigrations() list in [MainActivity]
     * 4. Verify the migration with a Room Migration Test.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No-op placeholder for structural readiness
        }
    }

    /**
     * Returns the list of all migrations to be applied to the database.
     * Currently empty as we are on the baseline Version 1.
     */
    fun getMigrations(): Array<Migration> {
        return arrayOf(
            // MIGRATION_1_2
        )
    }
}
