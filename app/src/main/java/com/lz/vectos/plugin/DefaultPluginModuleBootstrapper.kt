package com.lz.vectos.plugin

import com.lz.domain.plugin.*

class DefaultPluginModuleBootstrapper(
    private val repository:
    RegisteredModuleRepository
) : PluginModuleBootstrapper {

    override suspend fun bootstrap() {

        /*
         Later:
         Discover installed dynamic features
         Read module manifests
         Register modules
        */
    }
}