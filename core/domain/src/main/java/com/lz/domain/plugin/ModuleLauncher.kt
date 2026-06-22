package com.lz.domain.plugin

interface ModuleLauncher {
    suspend fun open(
        moduleId: String
    ): String
}