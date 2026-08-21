package com.lz.vectos.app

import android.app.Application
import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat
import com.lz.data.persistence.room.AppDatabase
import com.lz.data.persistence.room.seeder.AiscSectionSeeder
import com.lz.data.persistence.room.seeder.BuildingCodeSeeder
import com.lz.data.persistence.room.seeder.MaterialSeeder
import com.lz.data.persistence.room.seeder.StructuralDataSeeder
import com.lz.domain.module.ModuleCatalogRepository
import com.lz.domain.module.SubscriptionRepository
import com.lz.vectos.BuildConfig
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

    @Inject lateinit var subscriptionRepository: SubscriptionRepository
    @Inject lateinit var moduleCatalogRepository: ModuleCatalogRepository

    // Application-scoped coroutine scope — cancelled only when the process dies
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Attaches installed dynamic-feature module classloaders/resources
        // to this process. Without this, a split delivered by Play (or
        // fused into a debug build) can be present on disk yet still fail
        // Class.forName() lookups at runtime — see SubscriptionAware
        // InstalledModuleRepository and AndroidRuntimeModuleProviderResolver.
        SplitCompat.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        seedDatabase()
        grantDebugSubscriptions()
    }

    /**
     * Debug-build convenience answering "how do we give the test app a
     * subscription": auto-grants every catalog module so local runs exercise
     * the same enabled/licensed path a real entitled user would hit, without
     * needing a Play Billing transaction or a debug settings toggle.
     *
     * "beam" itself doesn't currently need this — it's the loss-leader
     * module (ModuleDescriptor.requiresSubscription = false), always
     * considered licensed. This exists for testing the gating logic itself
     * and any future non-free module (e.g. "column").
     *
     * Deliberately blocking (runBlocking), not fire-and-forget on
     * applicationScope: MainActivity.onCreate() (which triggers
     * RuntimeInitializer's synchronous license check via Hilt) can start
     * immediately after Application.onCreate() returns, and an async grant
     * wouldn't be guaranteed to finish first — a race that wouldn't affect
     * beam (always licensed) but would silently break debug-testing any
     * future non-free module's gating. Both repositories are trivial
     * in-memory reads today, so blocking here costs microseconds — same
     * reasoning as the runBlocking bridge in
     * SubscriptionAwareInstalledModuleRepository, and it'll need revisiting
     * alongside that one if real persistence/network backing ever lands.
     *
     * Release builds must NOT do this — GooglePlayPurchaseManager (not yet
     * wired to a real entitlement check) is the intended real source of
     * truth there.
     */
    private fun grantDebugSubscriptions() {
        if (!BuildConfig.DEBUG) return

        try {
            kotlinx.coroutines.runBlocking {
                moduleCatalogRepository.getAvailableModules().forEach { descriptor ->
                    subscriptionRepository.grantLicense(descriptor.id)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("VectosApplication", "Debug subscription grant failed", e)
        }
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