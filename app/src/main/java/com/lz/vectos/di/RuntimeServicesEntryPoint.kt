package com.lz.vectos.di

import com.lz.data.persistence.room.AppDatabase
import com.lz.data.repository.CalculationWriter
import com.lz.data.repository.IStructuralCodeRepository
import com.lz.domain.material.MaterialRepository
import com.lz.domain.project.ActiveProjectProvider
import com.lz.domain.repository.ProjectRepository
import com.lz.model.structural.SectionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import android.content.Context

/**
 * Exposes :app's app-level singletons to code running outside :app's own
 * Hilt component graph.
 *
 * Hilt's ViewModel injection (@HiltViewModel / hiltViewModel()) and any
 * @EntryPoint interface declared inside a dynamic-feature module are both
 * invisible to :app's generated SingletonComponent: that component is
 * generated when :app itself compiles, which happens BEFORE any dynamic
 * feature module (feature modules depend on :app, never the reverse, so
 * :app never has a feature module on its compile classpath).
 *
 * :app has no dependency on, or knowledge of, any specific feature module
 * (beam or otherwise) -- this interface only publishes the common services
 * a runtime-loaded calculator module is likely to need. Each feature
 * module pulls what it needs via RuntimeServicesEntryPoint.from(context)
 * and wires it into a hand-written ViewModelProvider.Factory (see e.g.
 * feature/beam's BeamViewModelFactory) instead of relying on Hilt's
 * ViewModel injection.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RuntimeServicesEntryPoint {

    fun activeProjectProvider(): ActiveProjectProvider

    fun structuralCodeRepository(): IStructuralCodeRepository

    fun sectionRepository(): SectionRepository

    fun materialRepository(): MaterialRepository

    fun calculationWriter(): CalculationWriter

    fun projectRepository(): ProjectRepository

    fun appDatabase(): AppDatabase

    companion object {
        fun from(context: Context): RuntimeServicesEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                RuntimeServicesEntryPoint::class.java
            )
    }
}