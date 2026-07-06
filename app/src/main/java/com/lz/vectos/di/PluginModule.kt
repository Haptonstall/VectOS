package com.lz.vectos.di

import com.lz.domain.module.ModuleCatalogRepository
import com.lz.domain.module.ModuleInstaller
import com.lz.domain.module.ModuleLauncher
import com.lz.domain.module.ModuleRegistry
import com.lz.domain.module.PurchaseManager
import com.lz.domain.module.RegisteredModuleRepository
import com.lz.domain.module.SubscriptionRepository
import com.lz.vectos.plugin.DefaultModuleRegistry
import com.lz.vectos.plugin.DynamicFeatureModuleInstaller
import com.lz.vectos.plugin.GooglePlayPurchaseManager
import com.lz.vectos.plugin.LocalModuleCatalogRepository
import com.lz.vectos.plugin.LocalRegisteredModuleRepository
import com.lz.vectos.plugin.LocalSubscriptionRepository
import com.lz.vectos.plugin.ProductionModuleLauncher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PluginModule {

    @Provides
    @Singleton
    fun provideRegisteredModuleRepository(): RegisteredModuleRepository =
        LocalRegisteredModuleRepository()

    @Provides
    @Singleton
    fun provideSubscriptionRepository(): SubscriptionRepository =
        LocalSubscriptionRepository()

    @Provides
    @Singleton
    fun provideModuleCatalogRepository(): ModuleCatalogRepository =
        LocalModuleCatalogRepository()

    @Provides
    @Singleton
    fun provideModuleRegistry(
        repository: RegisteredModuleRepository
    ): ModuleRegistry = DefaultModuleRegistry(repository)

    @Provides
    @Singleton
    fun provideModuleInstaller(
        repository: RegisteredModuleRepository
    ): ModuleInstaller = DynamicFeatureModuleInstaller(repository)

    @Provides
    @Singleton
    fun provideModuleLauncher(
        repository: RegisteredModuleRepository
    ): ModuleLauncher = ProductionModuleLauncher(repository)

    @Provides
    @Singleton
    fun providePurchaseManager(
        subscriptions: SubscriptionRepository
    ): PurchaseManager = GooglePlayPurchaseManager(subscriptions)
}