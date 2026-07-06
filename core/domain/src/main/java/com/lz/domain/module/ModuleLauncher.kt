package com.lz.domain.module

interface ModuleLauncher {
    suspend fun open(
        moduleId: String
    ): String
}