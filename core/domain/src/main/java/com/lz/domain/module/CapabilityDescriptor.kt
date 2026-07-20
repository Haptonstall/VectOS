package com.lz.domain.module

import com.lz.runtime.api.CapabilityType

data class CapabilityDescriptor(

    val id: String,

    val displayName: String,

    val runtimeModuleId: String,

    val capabilityType: CapabilityType
)