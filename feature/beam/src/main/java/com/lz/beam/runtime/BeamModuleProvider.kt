package com.lz.beam.runtime

import com.lz.runtime.api.*

class BeamModuleProvider :
    RuntimeModuleProvider {

    override fun createModule(): RuntimeModule {

        return BeamRuntimeModule()

    }
}