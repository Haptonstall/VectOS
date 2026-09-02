package com.lz.beam.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.lz.beam.data.persistence.room.BeamDatabaseHolder
import com.lz.beam.data.repository.BeamPersistenceMapper
import com.lz.beam.data.repository.RoomBeamCalculationRepository
import com.lz.vectos.di.RuntimeServicesEntryPoint
import kotlinx.serialization.json.Json

/**
 * Builds BeamViewModel by hand.
 *
 * BeamViewModel isn't Hilt-managed (see its class doc / BeamEntryPoint for
 * why). This factory pulls the four :app-hosted dependencies via
 * BeamEntryPoint, constructs the one feature-local dependency
 * (RoomBeamCalculationRepository, which needs BeamDatabase — a Room
 * database that only exists inside this module) by hand, and wires them
 * into BeamViewModel.
 */
class BeamViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val entryPoint = RuntimeServicesEntryPoint.from(context)
    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        require(modelClass == BeamViewModel::class.java) {
            "BeamViewModelFactory only creates BeamViewModel, got $modelClass"
        }

        val appDatabase = entryPoint.appDatabase()
        val beamDatabase = BeamDatabaseHolder.get(appContext)

        val beamRepository = RoomBeamCalculationRepository(
            beamCalculationDao = beamDatabase.beamCalculationDao(),
            calculationDao = appDatabase.calculationDao(),
            projectRepository = entryPoint.projectRepository(),
            calculationWriter = entryPoint.calculationWriter(),
            beamMapper = BeamPersistenceMapper(Json { ignoreUnknownKeys = true })
        )

        @Suppress("UNCHECKED_CAST")
        return BeamViewModel(
            activeProjectProvider = entryPoint.activeProjectProvider(),
            beamRepository = beamRepository,
            structuralRepository = entryPoint.structuralCodeRepository(),
            sectionRepository = entryPoint.sectionRepository(),
            materialRepository = entryPoint.materialRepository(),
            seedingCoordinator = entryPoint.databaseSeedingCoordinator()
        ) as T
    }
}
