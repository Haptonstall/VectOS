package com.lz.data.persistence.room

import androidx.room.migration.Migration

/**
 * Registry for Room database migrations.
 *
 * The production schema baseline is version 1 (see schemas/.../1.json).
 * When a schema change is required:
 * 1. Bump `version` in [AppDatabase].
 * 2. Add a Migration(oldVersion, newVersion) here with the exact SQL diff.
 * 3. Add it to [getMigrations].
 * 4. Add a MigrationTestHelper instrumented test asserting the migrated
 *    schema matches the newly exported schema JSON.
 */
object Migrations {
    fun getMigrations(): Array<Migration> = emptyArray()
}