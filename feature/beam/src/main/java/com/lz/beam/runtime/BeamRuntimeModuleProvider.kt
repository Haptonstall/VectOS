package com.lz.beam.runtime

import com.lz.runtime.api.RuntimeModule
import com.lz.runtime.api.RuntimeModuleProvider

class BeamRuntimeModuleProvider : RuntimeModuleProvider {

    override fun createModule(): RuntimeModule {

        return BeamRuntimeModule()

    }

}