package com.lz.runtime.api

/**
 * Base contract implemented by every RuntimeEnvironment service.
 *
 * Examples:
 *
 * RuntimeModuleRegistry
 * NavigationRegistry
 * EventBus
 * LicenseManager
 * Marketplace
 */
interface RuntimeService {

    /**
     * Unique service identifier.
     */
    val id: String

    /**
     * Called during RuntimeEnvironment startup.
     */
    fun initialize(context: RuntimeContext)

    /**
     * Called during RuntimeEnvironment shutdown.
     */
    fun shutdown(context: RuntimeContext)
}