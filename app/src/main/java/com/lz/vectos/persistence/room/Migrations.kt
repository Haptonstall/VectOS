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
            db.execSQL("CREATE TABLE IF NOT EXISTS `standards` (`shortName` TEXT NOT NULL, `longName` TEXT NOT NULL, `referencesJson` TEXT NOT NULL, PRIMARY KEY(`shortName`))")
            db.execSQL("CREATE TABLE IF NOT EXISTS `building_codes` (`shortName` TEXT NOT NULL, `longName` TEXT NOT NULL, `baseCodeName` TEXT, `referencesJson` TEXT NOT NULL, PRIMARY KEY(`shortName`), FOREIGN KEY(`baseCodeName`) REFERENCES `building_codes`(`shortName`) ON UPDATE NO ACTION ON DELETE SET NULL )")
            db.execSQL("CREATE TABLE IF NOT EXISTS `building_code_standards` (`buildingCodeName` TEXT NOT NULL, `standardName` TEXT NOT NULL, PRIMARY KEY(`buildingCodeName`, `standardName`), FOREIGN KEY(`buildingCodeName`) REFERENCES `building_codes`(`shortName`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`standardName`) REFERENCES `standards`(`shortName`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_building_code_standards_standardName` ON `building_code_standards` (`standardName`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `default_material_standards` (`buildingCodeName` TEXT NOT NULL, `materialType` TEXT NOT NULL, `standardName` TEXT NOT NULL, PRIMARY KEY(`buildingCodeName`, `materialType`), FOREIGN KEY(`buildingCodeName`) REFERENCES `building_codes`(`shortName`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`standardName`) REFERENCES `standards`(`shortName`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_default_material_standards_standardName` ON `default_material_standards` (`standardName`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `load_combinations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `buildingCodeName` TEXT NOT NULL, `name` TEXT NOT NULL, `methodology` TEXT NOT NULL, `equation` TEXT NOT NULL, `codeReference` TEXT NOT NULL, `limitState` TEXT NOT NULL, `factorsJson` TEXT NOT NULL, FOREIGN KEY(`buildingCodeName`) REFERENCES `building_codes`(`shortName`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_load_combinations_buildingCodeName` ON `load_combinations` (`buildingCodeName`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `serviceability_criteria` (`id` TEXT NOT NULL, `buildingCodeName` TEXT NOT NULL, `name` TEXT NOT NULL, `loadCaseId` TEXT NOT NULL, `spanDenominator` REAL NOT NULL, `description` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`buildingCodeName`) REFERENCES `building_codes`(`shortName`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_serviceability_criteria_buildingCodeName` ON `serviceability_criteria` (`buildingCodeName`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `default_load_cases` (`buildingCodeName` TEXT NOT NULL, `loadCaseId` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`buildingCodeName`, `loadCaseId`), FOREIGN KEY(`buildingCodeName`) REFERENCES `building_codes`(`shortName`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `projects` ADD COLUMN `projectNumber` TEXT")
            db.execSQL("ALTER TABLE `projects` ADD COLUMN `siteLocation` TEXT")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `materials` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `yieldStrengthPsi` REAL NOT NULL, `ultimateStrengthPsi` REAL NOT NULL, `modulusOfElasticityPsi` REAL NOT NULL, `shearModulusPsi` REAL NOT NULL, `densityPcf` REAL NOT NULL, PRIMARY KEY(`id`))")
        }
    }

    /**
     * Returns the list of all migrations to be applied to the database.
     */
    fun getMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2,
            MIGRATION_3_4,
            MIGRATION_4_5
        )
    }
}
