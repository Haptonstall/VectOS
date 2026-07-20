package com.lz.domain.module

/**
 * Opens the default entry point for a module.
 *
 * Returns the navigation route that the application shell
 * should navigate to.
 */
interface ModuleLauncher {
    suspend fun open(
        moduleId: String
    ): String
}