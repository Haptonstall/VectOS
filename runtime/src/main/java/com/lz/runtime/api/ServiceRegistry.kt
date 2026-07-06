package com.lz.runtime.api

import kotlin.reflect.KClass

/**
 * Central registry of RuntimeEnvironment services.
 *
 * Services are always registered against the public contract they
 * implement rather than their concrete implementation type.
 *
 * Example:
 *
 * register(
 *      LicenseManager::class,
 *      GooglePlayLicenseManager(...)
 * )
 *
 * retrieve:
 *
 * val manager = services.get<LicenseManager>()
 */
interface ServiceRegistry {

    /**
     * Registers a RuntimeEnvironment service implementation.
     *
     * @param contract Public interface implemented by the service.
     * @param service Concrete implementation.
     */
    fun <T : RuntimeService> register(
        contract: KClass<T>,
        service: T
    )

    /**
     * Removes a RuntimeEnvironment service.
     */
    fun <T : RuntimeService> unregister(
        contract: KClass<T>
    )

    /**
     * Retrieves a RuntimeEnvironment service.
     */
    fun <T : RuntimeService> get(
        contract: KClass<T>
    ): T?

    /**
     * Returns true if the service exists.
     */
    fun <T : RuntimeService> contains(
        contract: KClass<T>
    ): Boolean

    /**
     * Returns every registered service.
     */
    fun all(): Collection<RuntimeService>
}

/**
 * Reified helpers.
 */

inline fun <reified T : RuntimeService> ServiceRegistry.get(): T? =
    get(T::class)

inline fun <reified T : RuntimeService> ServiceRegistry.contains(): Boolean =
    contains(T::class)

inline fun <reified T : RuntimeService> ServiceRegistry.unregister() =
    unregister(T::class)

inline fun <reified T : RuntimeService> ServiceRegistry.register(
    service: T
) = register(T::class, service)