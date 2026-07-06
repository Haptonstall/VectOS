package com.lz.runtime.services

import com.lz.runtime.api.RuntimeService
import com.lz.runtime.api.ServiceRegistry
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Thread-safe implementation of the RuntimeEnvironment ServiceRegistry.
 */
class DefaultServiceRegistry : ServiceRegistry {

    private val services =
        ConcurrentHashMap<KClass<*>, RuntimeService>()

    override fun <T : RuntimeService> register(
        contract: KClass<T>,
        service: T
    ) {

        services[contract] = service
    }

    override fun <T : RuntimeService> unregister(
        contract: KClass<T>
    ) {

        services.remove(contract)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : RuntimeService> get(
        contract: KClass<T>
    ): T? {

        return services[contract] as? T
    }

    override fun <T : RuntimeService> contains(
        contract: KClass<T>
    ): Boolean {

        return services.containsKey(contract)
    }

    override fun all(): Collection<RuntimeService> {

        return services.values
    }
}