package com.lz.runtime.registry

import java.util.concurrent.ConcurrentHashMap

/**
 * Reusable thread-safe storage for runtimeEnvironment registries.
 */
class RegistryStore<T : Any>(
    private val keySelector: (T) -> String
) {

    private val items =
        ConcurrentHashMap<String, T>()

    fun register(item: T) {
        items[keySelector(item)] = item
    }

    fun unregister(key: String) {
        items.remove(key)
    }

    fun get(key: String): T? =
        items[key]

    fun contains(key: String): Boolean =
        items.containsKey(key)

    fun all(): List<T> =
        items.values.toList()

    fun clear() {
        items.clear()
    }
}