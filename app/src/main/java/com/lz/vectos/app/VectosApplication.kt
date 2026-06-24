package com.lz.vectos.app

import android.app.Application
import com.lz.data.persistence.room.AppDatabase
import com.lz.data.persistence.room.seeder.AiscSectionSeeder
import com.lz.data.persistence.room.seeder.BuildingCodeSeeder
import com.lz.data.persistence.room.seeder.MaterialSeeder
import com.lz.data.persistence.room.seeder.StructuralDataSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application entry point.
 *
 * @HiltAndroidApp triggers Hilt's code generation and initializes the
 * dependency injection component hierarchy for the entire app.
 *
 * Database seeding runs once here at application start on an IO coroutine.
 * Each seeder checks its own idempotency — running multiple times is safe.
 */
@HiltAndroidApp
class VectosApplication : Application() {

    // Hilt injects AppDatabase via DatabaseModule
    @Inject lateinit var database: AppDatabase

    // Application-scoped coroutine scope — cancelled only when the process dies
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        seedDatabase()
    }

    private fun seedDatabase() {
        applicationScope.launch {
            try {
                // Order matters — BuildingCodeSeeder must run before StructuralDataSeeder
                // since structural data references building code IDs
                BuildingCodeSeeder(database.codeRegistryDao()).seed()
                StructuralDataSeeder(database.loadCombinationDao()).seed()
                AiscSectionSeeder(applicationContext, database.aiscSectionDao()).seed()
                MaterialSeeder(database.materialDao()).seed()
            } catch (e: Exception) {
                // Seeding failure is non-fatal — app can still run with empty catalog data.
                // Log here and surface in a diagnostics screen if needed.
                android.util.Log.e("VectosApplication", "Database seeding failed", e)
            }
        }
    }
}