package com.lz.runtime.api.marketplace

data class InstalledModule(

    val moduleId: String,

    val installed: Boolean,

    val version: String
)