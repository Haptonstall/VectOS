package com.lz.vectos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.lz.vectos.persistence.repository.RoomCalculationRepository
import com.lz.vectos.persistence.repository.RoomProjectRepository
import com.lz.vectos.persistence.room.AppDatabase
import com.lz.vectos.persistence.room.Migrations
import com.lz.vectos.ui.beam.BeamCalculatorScreen
import com.lz.vectos.ui.theme.VectOSTheme
import com.lz.vectos.viewmodel.BeamViewModel
import com.lz.vectos.viewmodel.ProjectViewModel

class MainActivity : ComponentActivity() {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "vectos.db"
        )
        .addMigrations(*Migrations.getMigrations()) // References migration registry
        .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Repositories wired with Room DAOs
        val projectRepository = RoomProjectRepository(database.projectDao())
        val calculationRepository = RoomCalculationRepository(
            database.calculationDao(),
            database.beamCalculationDao(),
            projectRepository
        )
        
        enableEdgeToEdge()
        setContent {
            VectOSTheme {
                val projectViewModel: ProjectViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ProjectViewModel(projectRepository) as T
                        }
                    }
                )
                
                val beamViewModel: BeamViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return BeamViewModel(projectViewModel, calculationRepository) as T
                        }
                    }
                )
                
                val activeProject by projectViewModel.activeProject.collectAsState()
                
                BeamCalculatorScreen(
                    activeProject = activeProject,
                    viewModel = beamViewModel
                )
            }
        }
    }
}
