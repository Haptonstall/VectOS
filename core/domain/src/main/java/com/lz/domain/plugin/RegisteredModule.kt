package com.lz.domain.plugin

data class RegisteredModule(
    val descriptor: ModuleDescriptor,
    val route: String
)