package com.lz.vectos.app.platform

import android.content.Context
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.boot.RuntimeBootstrapper
import com.lz.runtime.loader.DefaultRuntimeModuleLoader
import com.lz.runtime.loader.RuntimeModuleInstaller
import com.lz.runtime.repository.InstalledModuleRepository
import com.lz.vectos.app.runtime.AndroidRuntimeModuleProviderResolver

object RuntimeInitializer {

    @Volatile
    private var runtime: RuntimeEnvironment? = null

    fun initialize(
        context: Context,
        installedModuleRepository: InstalledModuleRepository
    ): RuntimeEnvironment {

        runtime?.let { return it }

        synchronized(this) {

            runtime?.let { return it }

            val configuration =
                RuntimeConfigurationFactory.create(context)

            val moduleLoader =
                DefaultRuntimeModuleLoader(
                    AndroidRuntimeModuleProviderResolver()
                )

            val installer =
                RuntimeModuleInstaller(
                    installedModuleRepository,
                    moduleLoader
                )

            val runtimeEnvironment =
                RuntimeBootstrapper.create(
                    configuration,
                    installer
                )

            runtimeEnvironment.start()

            runtime = runtimeEnvironment

            return runtimeEnvironment

        }

    }

    fun runtime(): RuntimeEnvironment =
        checkNotNull(runtime) {
            "RuntimeEnvironment has not been initialized."
        }

    fun shutdown() {

        runtime?.stop()

        runtime = null

    }

}
