package com.lz.vectos.di

import android.content.Context
import com.lz.domain.module.ModuleCatalogRepository
import com.lz.domain.module.ModuleInstaller
import com.lz.domain.module.ModuleLauncher
import com.lz.domain.module.ModuleRegistry
import com.lz.domain.module.PurchaseManager
import com.lz.domain.module.RegisteredModuleRepository
import com.lz.domain.module.SubscriptionRepository
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.vectos.app.platform.RuntimeInitializer
import com.lz.vectos.app.runtime.RuntimeModuleInstaller
import com.lz.vectos.app.runtime.RuntimeModuleLauncher
import com.lz.vectos.app.runtime.RuntimeModuleRegistryAdapter
import com.lz.vectos.plugin.GooglePlayPurchaseManager
import com.lz.vectos.plugin.LocalModuleCatalogRepository
import com.lz.vectos.plugin.LocalRegisteredModuleRepository
import com.lz.vectos.plugin.LocalSubscriptionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModuleBindings {

    @Provides
    @Singleton
    fun provideRuntimeEnvironment(
        @ApplicationContext context: Context
    ): RuntimeEnvironment {
        return RuntimeInitializer.initialize(context)
    }

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
        runtime: RuntimeEnvironment
    ): ModuleRegistry {
        return RuntimeModuleRegistryAdapter(
            runtime
        )
    }

    @Provides
    @Singleton
    fun provideModuleInstaller(
        runtime: RuntimeEnvironment
    ): ModuleInstaller = RuntimeModuleInstaller(runtime)

    @Provides
    @Singleton
    fun provideModuleLauncher(
        runtime: RuntimeEnvironment
    ): ModuleLauncher = RuntimeModuleLauncher(runtime)

    @Provides
    @Singleton
    fun providePurchaseManager(
        subscriptions: SubscriptionRepository
    ): PurchaseManager = GooglePlayPurchaseManager(subscriptions)
}