package com.lz.vectos.plugin

import com.lz.domain.module.SubscriptionRepository

class LocalSubscriptionRepository :
    SubscriptionRepository {

    private val licensedModules =
        mutableSetOf<String>()

    override suspend fun isLicensed(
        moduleId: String
    ): Boolean {

        return licensedModules.contains(
            moduleId
        )
    }

    override suspend fun getLicensedModules():
            Set<String> {

        return licensedModules
    }

    suspend fun grantLicense(
        moduleId: String
    ) {

        licensedModules.add(
            moduleId
        )
    }
}