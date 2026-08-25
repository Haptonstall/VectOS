package com.lz.beam.runtime

import com.lz.runtime.api.*
import com.lz.common.Version

val BeamDescriptor = RuntimeModuleDescriptor(

    id = "beam",
    displayName = "Beam Calculator",
    description = "Structural beam analysis and design.",
    version = Version(1,0,0),
    publisher = "LZ Engineering",
    iconKey = "beam",
    entryPointClassName = "com.lz.beam.api.BeamRuntimeModuleProvider",
    capabilityTypes = setOf(
        CapabilityType.CALCULATOR
    ),

    minimumRuntimeEnvironmentVersion = Version(1,0,0)
)