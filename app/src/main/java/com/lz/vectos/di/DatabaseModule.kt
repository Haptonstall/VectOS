package com.lz.vectos.di

import android.content.Context
import dagger.hilt.components.SingletonComponent
import com.lz.data.persistence.room.AppDatabase
import com.lz.data.project.DefaultActiveProjectProvider
import com.lz.data.repository.CalculationWriter
import com.lz.data.repository.CompositeSectionRepository
import com.lz.data.repository.DataStoreSettingsRepository
import com.lz.data.repository.IStructuralCodeRepository
import com.lz.data.repository.NdsSectionRepository
import com.lz.data.repository.RoomAiscSectionRepository
import com.lz.data.repository.RoomCalculationRepository
import com.lz.data.repository.RoomCalculationWriter
import com.lz.data.repository.RoomProjectRepository
import com.lz.data.repository.StructuralCodeRepositoryImpl
import com.lz.data.persistence.room.repository.RoomMaterialRepository
import com.lz.domain.material.MaterialRepository
import com.lz.domain.project.ActiveProjectProvider
import com.lz.domain.repository.CalculationRepository
import com.lz.domain.repository.ProjectRepository
import com.lz.domain.repository.SettingsRepository
import com.lz.model.structural.SectionRepository
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
    fun provideCalculationRepository(db: AppDatabase): CalculationRepository =
        RoomCalculationRepository(db.calculationDao())

    @Provides
    @Singleton
    fun provideActiveProjectProvider(): ActiveProjectProvider =
        DefaultActiveProjectProvider()

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

    @Provides
    @Singleton
    fun provideCalculationWriter(db: AppDatabase): CalculationWriter =
        RoomCalculationWriter(db.calculationDao())

    @Provides
    @Singleton
    fun provideMaterialRepository(db: AppDatabase): MaterialRepository =
        RoomMaterialRepository(db.materialDao())

    @Provides
    @Singleton
    fun provideSectionRepository(
        db: AppDatabase,
        @ApplicationContext context: Context
    ): SectionRepository =
        CompositeSectionRepository(
            steelSectionRepository = RoomAiscSectionRepository(db.aiscSectionDao()),
            woodSectionRepository = NdsSectionRepository(context)
        )
}