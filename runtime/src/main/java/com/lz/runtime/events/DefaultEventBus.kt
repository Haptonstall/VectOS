package com.lz.runtime.events

import com.lz.runtime.api.EventBus
import com.lz.runtime.api.RuntimeEvent
import com.lz.runtime.core.AbstractRuntimeService
import kotlin.reflect.KClass

class DefaultEventBus :
    AbstractRuntimeService(
        "runtimeEnvironment.eventBus"
    ),
    EventBus {

    private val listeners =
        mutableMapOf<KClass<*>, MutableList<(RuntimeEvent) -> Unit>>()

    override fun publish(
        event: RuntimeEvent
    ) {

        listeners[event::class]?.forEach {
            it(event)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : RuntimeEvent> subscribe(
        type: KClass<T>,
        listener: (T) -> Unit
    ) {

        val list = listeners.getOrPut(type) {
            mutableListOf()
        }

        list.add(listener as (RuntimeEvent) -> Unit)
    }

    override fun <T : RuntimeEvent> unsubscribe(
        type: KClass<T>
    ) {

        listeners.remove(type)
    }

    override fun onShutdown() {

        listeners.clear()
    }
}