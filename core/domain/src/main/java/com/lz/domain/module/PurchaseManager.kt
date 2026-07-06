package com.lz.domain.module

interface PurchaseManager {
    suspend fun purchase(
        moduleId: String
    ): PurchaseResult
}