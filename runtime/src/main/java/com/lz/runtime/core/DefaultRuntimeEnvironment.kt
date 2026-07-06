package com.lz.runtime.core

import com.lz.runtime.api.CapabilityRegistry
import com.lz.runtime.api.EventBus
import com.lz.runtime.api.RuntimeModuleRegistry
import com.lz.runtime.api.NavigationRegistry
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.api.RuntimeConfiguration
import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.api.RuntimeModuleLoader
import com.lz.runtime.api.RuntimeState
import com.lz.runtime.discovery.PlatformModuleDiscovery
import com.lz.runtime.events.DefaultEventBus
import com.lz.runtime.loader.DefaultRuntimeModuleLoader
import com.lz.runtime.loader.RuntimeModuleInstaller
import com.lz.runtime.registry.DefaultCapabilityRegistry
import com.lz.runtime.registry.DefaultRuntimeModuleRegistry
import com.lz.runtime.registry.DefaultNavigationRegistry
import com.lz.runtime.screen.api.ScreenRegistry
import com.lz.runtime.screen.internal.DefaultScreenRegistry
import com.lz.runtime.services.DefaultServiceRegistry
import com.lz.runtime.startup.RuntimeStartupPipeline
import kotlin.getValue

class DefaultRuntimeEnvironment(

    override val configuration: RuntimeConfiguration

) : RuntimeEnvironment {

    private var runtimeState = RuntimeState.CREATED

    override val state: RuntimeState
        get() = runtimeState

    private val serviceRegistry = DefaultServiceRegistry()

    private val moduleRegistry = DefaultRuntimeModuleRegistry()

    private val capabilityRegistry = DefaultCapabilityRegistry()

    private val navigationRegistry = DefaultNavigationRegistry()

    private val screenRegistry = DefaultScreenRegistry()

    private val eventBus = DefaultEventBus()

    private val moduleDiscovery: PlatformModuleDiscovery by lazy {
        DefaultPlatformModuleDiscovery()
    }

    private val moduleInstaller by lazy {
        RuntimeModuleInstaller(context)
    }

    private val moduleLoader: RuntimeModuleLoader by lazy {
        DefaultRuntimeModuleLoader(
            context,
            moduleInstaller
        )
    }

    private lateinit var startupPipeline: RuntimeStartupPipeline

    override val context: RuntimeContext =
        DefaultRuntimeContext(
            runtimeEnvironment = this,
            configuration = configuration,
            services = serviceRegistry,
            runtimeModuleRegistry = moduleRegistry,
            capabilityRegistry = capabilityRegistry,
            navigationRegistry = navigationRegistry,
            screenRegistry = screenRegistry,
            eventBus = eventBus
        )

    override fun start() {

        if (runtimeState != RuntimeState.CREATED)
            return

        runtimeState = RuntimeState.INITIALIZING

        registerCoreServices()

        initializeServices()

        startupPipeline.start()

        runtimeState = RuntimeState.RUNNING
    }

    override fun stop() {

        runtimeState = RuntimeState.STOPPING

        startupPipeline.stop()

        serviceRegistry
            .all()
            .reversed()
            .forEach {

                it.shutdown(context)

            }

        runtimeState = RuntimeState.STOPPED
    }

    private fun registerCoreServices() {

        serviceRegistry.register(
            RuntimeModuleRegistry::class,
            moduleRegistry
        )

        serviceRegistry.register(
            CapabilityRegistry::class,
            capabilityRegistry
        )

        serviceRegistry.register(
            NavigationRegistry::class,
            navigationRegistry
        )

        serviceRegistry.register(
            EventBus::class,
            eventBus
        )
    }

    private fun initializeServices() {

        serviceRegistry
            .all()
            .forEach {

                it.initialize(context)

            }
    }

    fun attachPipeline(

        pipeline: RuntimeStartupPipeline

    ) {

        startupPipeline = pipeline

    }
}