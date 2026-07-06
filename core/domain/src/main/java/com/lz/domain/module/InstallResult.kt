package com.lz.domain.module

sealed interface InstallResult {

    data object Success : InstallResult

    data object Cancelled : InstallResult

    data class Error(
        val message: String
    ) : InstallResult
}