package com.lz.vectos.app.runtime

import android.content.Context
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.lz.domain.module.ModuleCatalogRepository
import com.lz.domain.module.SubscriptionRepository
import com.lz.runtime.api.marketplace.InstallState
import com.lz.runtime.api.marketplace.InstalledModule
import com.lz.runtime.api.marketplace.ModuleEntryPoint
import com.lz.runtime.api.marketplace.ModuleSource
import com.lz.runtime.repository.InstalledModuleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real [InstalledModuleRepository] — replaces the previous hardcoded
 * DefaultInstalledModuleRepository stub (which unconditionally claimed
 * beam was INSTALLED/enabled regardless of licensing, and crashed app
 * startup on any device where the beam dynamic feature wasn't actually
 * bundled — see the 260820 session crash investigation).
 *
 * A module is reported here only if:
 *  1. It's licensed for this user (SubscriptionRepository) — modules with
 *     [com.lz.domain.module.ModuleDescriptor.requiresSubscription] = false
 *     (e.g. "beam", the loss-leader module) are always considered licensed.
 *  2. Its dynamic feature split is confirmed physically present on-device
 *     (SplitInstallManager.installedModules) — determines InstallState.
 *
 * [RuntimeModuleInstaller] (runtime.loader — the generic, channel-agnostic
 * loader) only attempts to Class.forName() modules that come back here as
 * both `enabled` and `InstallState.INSTALLED`, so a licensed-but-not-yet-
 * downloaded module is safely skipped at boot rather than crashing — it's
 * the responsibility of an explicit "install module" flow (this app's
 * [com.lz.domain.module.ModuleInstaller]) to trigger the real download via
 * SplitInstallManager.startInstall(), after which this repository will
 * naturally report it as INSTALLED on next read.
 *
 * NOTE on suspend bridging: [ModuleCatalogRepository]/[SubscriptionRepository]
 * are suspend APIs (anticipating real persistence/network-backed licensing
 * later), but [InstalledModuleRepository.installedModules] — inherited from
 * the synchronous `runtime` module's boot chain (RuntimeEnvironment.start()
 * is not suspend) — is not. `runBlocking` here is a deliberate, narrow
 * bridge: today's implementations of both repositories are trivial in-memory
 * reads with zero real IO, so blocking costs microseconds. If/when
 * SubscriptionRepository or ModuleCatalogRepository gain real backing (Room,
 * Play Billing, a backend call), this bridge — and likely the synchronous
 * assumption running all the way up through RuntimeStartupPipeline.start()
 * — will need revisiting. Flagging here rather than silently letting it rot.
 */
@Singleton
class SubscriptionAwareInstalledModuleRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moduleCatalogRepository: ModuleCatalogRepository,
    private val subscriptionRepository: SubscriptionRepository
) : InstalledModuleRepository {

    private val splitInstallManager: SplitInstallManager by lazy {
        SplitInstallManagerFactory.create(context)
    }

    override fun installedModules(): List<InstalledModule> = runBlocking {
        val catalog = moduleCatalogRepository.getAvailableModules()
        val licensedModuleIds = subscriptionRepository.getLicensedModules()
        val physicallyInstalledFeatures = splitInstallManager.installedModules

        catalog.mapNotNull { descriptor ->
            val isLicensed = !descriptor.requiresSubscription ||
                licensedModuleIds.contains(descriptor.id)

            if (!isLicensed) return@mapNotNull null

            val isPhysicallyInstalled =
                physicallyInstalledFeatures.contains(descriptor.dynamicFeatureName)

            InstalledModule(
                moduleId = descriptor.id,
                displayName = descriptor.displayName,
                version = descriptor.version,
                installState = if (isPhysicallyInstalled) InstallState.INSTALLED else InstallState.NOT_INSTALLED,
                enabled = isLicensed,
                source = ModuleSource.DYNAMIC_FEATURE,
                featureName = descriptor.dynamicFeatureName,
                entryPoint = ModuleEntryPoint(descriptor.entryPointClassName),
                signature = null
            )
        }
    }
}
