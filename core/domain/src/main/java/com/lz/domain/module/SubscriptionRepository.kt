package com.lz.domain.module

interface SubscriptionRepository {

    suspend fun isLicensed(moduleId: String): Boolean
    suspend fun getLicensedModules(): Set<String>
    suspend fun grantLicense(moduleId: String)
}