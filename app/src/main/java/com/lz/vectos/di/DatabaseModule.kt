package com.lz.vectos.di

import android.content.Context
import dagger.hilt.components.SingletonComponent
import com.lz.data.persistence.room.AppDatabase
import com.lz.data.repository.DataStoreSettingsRepository
import com.lz.data.repository.IStructuralCodeRepository
import com.lz.data.repository.RoomProjectRepository
import com.lz.data.repository.StructuralCodeRepositoryImpl
import com.lz.domain.repository.ProjectRepository
import com.lz.domain.repository.SettingsRepository
import com.lz.vectos.domain.calculation.ProjectCalculationRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.create(context)

    @Provides
    @Singleton
    fun provideProjectRepository(db: AppDatabase): ProjectRepository =
        RoomProjectRepository(db.projectDao())

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository =
        DataStoreSettingsRepository(context)

    @Provides
    @Singleton
    fun provideCalculationRegistry(): ProjectCalculationRegistry = ProjectCalculationRegistry()

    @Provides
    @Singleton
    fun provideStructuralCodeRepository(db: AppDatabase): IStructuralCodeRepository =
        StructuralCodeRepositoryImpl(db.codeRegistryDao(), db.loadCombinationDao())
}