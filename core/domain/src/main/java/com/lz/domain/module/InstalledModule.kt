package com.lz.domain.module

data class InstalledModule(
    val descriptor: ModuleDescriptor,
    val installed: Boolean,
    val licensed: Boolean
)