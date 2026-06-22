package com.lz.domain.plugin

data class InstalledModule(
    val descriptor: ModuleDescriptor,
    val installed: Boolean,
    val licensed: Boolean
)