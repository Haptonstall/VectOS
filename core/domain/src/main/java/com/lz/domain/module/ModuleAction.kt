package com.lz.domain.module

sealed interface ModuleAction {

    data class Open(
        val moduleId: String
    ) : ModuleAction

    data class Install(
        val moduleId: String
    ) : ModuleAction

    data class Purchase(
        val moduleId: String
    ) : ModuleAction
}