package com.lz.runtime.api

/**
 * Responsible for discovering installed modules.
 *
 * The RuntimeEnvironment delegates module discovery to the RuntimeModuleLoader.
 *
 * Different implementations may discover modules using:
 *
 *  • Android Dynamic Features
 *  • APK scanning
 *  • Marketplace downloads
 *  • Unit test providers
 */
interface RuntimeModuleLoader : RuntimeService {

    /**
     * Discovers every installed RuntimeModuleProvider.
     */
    fun discoverProviders(): List<RuntimeModuleProvider>

    /**
     * Creates every installed RuntimeModule.
     */
    fun loadModules(): List<RuntimeModule>
}