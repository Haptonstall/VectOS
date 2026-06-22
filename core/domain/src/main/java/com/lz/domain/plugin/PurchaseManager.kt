package com.lz.domain.plugin

interface PurchaseManager {
    suspend fun purchase(
        moduleId: String
    ): PurchaseResult
}