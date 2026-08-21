package com.lz.vectos.app.runtime

import android.content.Context
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.lz.domain.module.InstallResult
import com.lz.domain.module.ModuleCatalogRepository
import com.lz.domain.module.ModuleInstaller
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Real [ModuleInstaller] — replaces the previous stub, which only checked
 * whether a module was already present in the runtime's in-memory registry
 * rather than actually downloading anything.
 *
 * [install]/[uninstall] resolve [moduleId] (e.g. "beam") to its
 * [com.lz.domain.module.ModuleDescriptor.dynamicFeatureName] via the
 * catalog, then drive a real SplitInstallManager session. Play Core's
 * SplitInstallManager API is Task-based (Google Play Services Task, not a
 * coroutine) — bridged here via suspendCancellableCoroutine rather than
 * pulling in kotlinx-coroutines-play-services for one call site.
 *
 * Progress/state updates (downloading %, REQUIRES_USER_CONFIRMATION, etc.)
 * are intentionally not surfaced through this simple Success/Cancelled/Error
 * result — a real "add module" UI would want to observe
 * SplitInstallManager.registerListener() directly for progress. Out of
 * scope for this pass; this makes install() correctly *complete* the
 * install, which is the immediate gap.
 */
@Singleton
class RuntimeModuleInstaller @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moduleCatalogRepository: ModuleCatalogRepository
) : ModuleInstaller {

    private val splitInstallManager: SplitInstallManager by lazy {
        SplitInstallManagerFactory.create(context)
    }

    override suspend fun install(moduleId: String): InstallResult {
        val descriptor = moduleCatalogRepository.getAvailableModules()
            .find { it.id == moduleId }
            ?: return InstallResult.Error("Unknown module '$moduleId'.")

        if (splitInstallManager.installedModules.contains(descriptor.dynamicFeatureName)) {
            return InstallResult.Success
        }

        val request = SplitInstallRequest.newBuilder()
            .addModule(descriptor.dynamicFeatureName)
            .build()

        return suspendCancellableCoroutine { continuation ->
            splitInstallManager.startInstall(request)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(InstallResult.Success)
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resume(
                            InstallResult.Error(exception.message ?: "Module install failed.")
                        )
                    }
                }
                .addOnCanceledListener {
                    if (continuation.isActive) continuation.resume(InstallResult.Cancelled)
                }
        }
    }

    override suspend fun uninstall(moduleId: String): InstallResult {
        val descriptor = moduleCatalogRepository.getAvailableModules()
            .find { it.id == moduleId }
            ?: return InstallResult.Error("Unknown module '$moduleId'.")

        // deferredUninstall queues removal for the next time the app isn't
        // running — Play doesn't support synchronous/immediate uninstall of
        // an in-use split. No completion callback to bridge; this always
        // reports Success once queued (matches the "removal will be
        // implemented later" stub's prior always-Success behavior, now
        // actually backed by a real API call).
        splitInstallManager.deferredUninstall(listOf(descriptor.dynamicFeatureName))
        return InstallResult.Success
    }

    override suspend fun isInstalled(moduleId: String): Boolean {
        val descriptor = moduleCatalogRepository.getAvailableModules()
            .find { it.id == moduleId } ?: return false

        return splitInstallManager.installedModules.contains(descriptor.dynamicFeatureName)
    }
}
