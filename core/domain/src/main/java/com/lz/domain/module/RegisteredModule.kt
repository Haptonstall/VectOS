package com.lz.domain.module

data class RegisteredModule(
    val descriptor: ModuleDescriptor,
    val navigationContributor: String
)