package com.lz.runtime.api

import kotlin.reflect.KClass

/**
 * Strongly typed RuntimeEnvironment event bus.
 */
interface EventBus : RuntimeService {

    /**
     * Publishes an event.
     */
    fun publish(event: RuntimeEvent)

    /**
     * Subscribes to a specific RuntimeEvent type.
     */
    fun <T : RuntimeEvent> subscribe(
        type: KClass<T>,
        listener: (T) -> Unit
    )

    /**
     * Removes every listener for the specified event type.
     */
    fun <T : RuntimeEvent> unsubscribe(
        type: KClass<T>
    )
}

inline fun <reified T : RuntimeEvent> EventBus.subscribe(
    noinline listener: (T) -> Unit
) = subscribe(T::class, listener)