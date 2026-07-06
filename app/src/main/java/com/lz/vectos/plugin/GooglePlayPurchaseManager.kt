package com.lz.vectos.plugin

import com.lz.domain.module.*

class GooglePlayPurchaseManager(
    private val subscriptions: SubscriptionRepository
) : PurchaseManager {

    override suspend fun purchase(moduleId: String): PurchaseResult {
        subscriptions.grantLicense(moduleId)
        return PurchaseResult.Success
    }
}