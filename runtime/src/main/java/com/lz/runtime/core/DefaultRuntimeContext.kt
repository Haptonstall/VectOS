package com.lz.runtime.core

import com.lz.runtime.api.CapabilityRegistry
import com.lz.runtime.api.EventBus
import com.lz.runtime.api.RuntimeModuleRegistry
import com.lz.runtime.api.NavigationRegistry
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.api.RuntimeConfiguration
import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.api.RuntimeState
import com.lz.runtime.api.ServiceRegistry
import com.lz.runtime.screen.api.ScreenRegistry
import com.lz.runtime.services.DefaultServiceRegistry

class DefaultRuntimeContext(

    override val runtimeEnvironment: RuntimeEnvironment,

    override val configuration: RuntimeConfiguration,

    override val services: ServiceRegistry,

    override val runtimeModuleRegistry: RuntimeModuleRegistry,

    override val capabilityRegistry: CapabilityRegistry,

    override val navigationRegistry: NavigationRegistry,

    override val screenRegistry: ScreenRegistry,

    override val eventBus: EventBus

) : RuntimeContext {

    override val state: RuntimeState
        get() = runtimeEnvironment.state
}