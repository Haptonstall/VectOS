package com.lz.runtime.api

import com.lz.runtime.screen.api.ScreenRegistry

/**
 * Shared context available to every RuntimeEnvironment service.
 *
 * RuntimeContext exposes the RuntimeEnvironment itself along with the core
 * RuntimeEnvironment services. This avoids repeated service lookups throughout
 * the implementation.
 */
interface RuntimeContext {

    /**
     * RuntimeEnvironment configuration.
     */
    val configuration: RuntimeConfiguration

    /**
     * RuntimeEnvironment lifecycle state.
     */
    val state: RuntimeState

    /**
     * RuntimeEnvironment instance.
     */
    val runtimeEnvironment: RuntimeEnvironment

    /**
     * Central service registry.
     */
    val services: ServiceRegistry

    /**
     * Loaded module registry.
     */
    val runtimeModuleRegistry: RuntimeModuleRegistry

    /**
     * Capability registry.
     */
    val capabilityRegistry: CapabilityRegistry

    /**
     * Navigation registry.
     */
    val navigationRegistry: NavigationRegistry

    /**
     * RuntimeEnvironment event bus.
     */
    val eventBus: EventBus

    /**
     * Screen registry.
     */
    val screenRegistry: ScreenRegistry

}