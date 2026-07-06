package com.lz.vectos.plugin

import com.lz.domain.module.*

class DefaultModuleBootstrapper(
    private val repository:
    RegisteredModuleRepository
) : ModuleBootstrapper {

    override suspend fun bootstrap() {

        /*
         Later:
         Discover installed dynamic features
         Read module manifests
         Register modules
        */
    }
}