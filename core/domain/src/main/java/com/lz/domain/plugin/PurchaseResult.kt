package com.lz.domain.plugin

sealed interface PurchaseResult {

    data object Success : PurchaseResult

    data object Cancelled : PurchaseResult

    data class Error(
        val message: String
    ) : PurchaseResult
}