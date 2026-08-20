package com.lz.data.persistence.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

    /**
     * v1 -> v2:
     *  - standards: add material_type / edition_family / edition_key
     *    (nullable, additive — see StandardEntity KDoc). Seeded standards
     *    catalog is small and fully re-seeded on next app start, so no
     *    backfill UPDATE is needed for these three.
     *  - projects: buildingCode (PrimaryBuildingCode enum, stored as TEXT
     *    via Room's native enum support — 3 possible values: IBC_2021,
     *    IBC_2024, CBC_2025) is retired in favor of buildingCodeId (a real
     *    com.lz.model.regulatory.codes.BuildingCode.id string, matching the
     *    seeded catalog directly). CBC_2025 has no seeded DB equivalent —
     *    the closest real code is CBC_2022 — so existing CBC_2025 projects
     *    are remapped to that rather than left dangling on an id the
     *    catalog can't resolve.
     *
     *    Rebuilds the table (create-copy-drop-rename) rather than using
     *    `ALTER TABLE ... DROP COLUMN`/`RENAME COLUMN`: those require
     *    SQLite 3.35+, which isn't guaranteed on the system SQLite bundled
     *    with minSdk 28 (Android 9) devices this app supports.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE standards ADD COLUMN material_type TEXT")
            db.execSQL("ALTER TABLE standards ADD COLUMN edition_family TEXT")
            db.execSQL("ALTER TABLE standards ADD COLUMN edition_key TEXT")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `projects_new` (
                    `id` TEXT NOT NULL, `name` TEXT NOT NULL, `projectNumber` TEXT,
                    `description` TEXT, `clientName` TEXT, `engineerName` TEXT, `firmName` TEXT,
                    `createdAtIso` TEXT NOT NULL, `buildingCodeId` TEXT NOT NULL,
                    `designMethodology` TEXT NOT NULL, `unitSystem` TEXT NOT NULL,
                    `riskCategory` TEXT NOT NULL, `isWindDesignEnabled` INTEGER NOT NULL,
                    `isSeismicDesignEnabled` INTEGER NOT NULL, `streetAddress` TEXT NOT NULL,
                    `city` TEXT NOT NULL, `state` TEXT NOT NULL, `zipCode` TEXT NOT NULL,
                    `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `elevationFeet` REAL NOT NULL,
                    `isGeocoded` INTEGER NOT NULL, `seismicSs` REAL NOT NULL, `seismicS1` REAL NOT NULL,
                    `seismicSds` REAL NOT NULL, `seismicSd1` REAL NOT NULL,
                    `seismicDesignCategory` TEXT NOT NULL, `isSeismicAuthoritativeOverride` INTEGER NOT NULL,
                    `steelStandardOverride` TEXT, PRIMARY KEY(`id`)
                )
            """.trimIndent())

            db.execSQL("""
                INSERT INTO projects_new
                SELECT id, name, projectNumber, description, clientName, engineerName, firmName,
                       createdAtIso,
                       CASE buildingCode
                           WHEN 'CBC_2025' THEN 'CBC_2022'
                           ELSE buildingCode
                       END,
                       designMethodology, unitSystem, riskCategory, isWindDesignEnabled,
                       isSeismicDesignEnabled, streetAddress, city, state, zipCode, latitude,
                       longitude, elevationFeet, isGeocoded, seismicSs, seismicS1, seismicSds,
                       seismicSd1, seismicDesignCategory, isSeismicAuthoritativeOverride,
                       steelStandardOverride
                FROM projects
            """.trimIndent())

            db.execSQL("DROP TABLE projects")
            db.execSQL("ALTER TABLE projects_new RENAME TO projects")
        }
    }

    fun getMigrations(): Array<Migration> = arrayOf(MIGRATION_1_2)
}