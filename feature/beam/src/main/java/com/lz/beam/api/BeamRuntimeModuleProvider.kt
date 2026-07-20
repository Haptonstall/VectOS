package com.lz.beam.api

import com.lz.beam.runtime.BeamRuntimeModule
import com.lz.runtime.api.RuntimeModule
import com.lz.runtime.api.RuntimeModuleProvider

class BeamRuntimeModuleProvider : RuntimeModuleProvider {

    override fun createModule(): RuntimeModule {

        return BeamRuntimeModule()

    }

}