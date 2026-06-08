package com.lz.vectos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lz.vectos.data.persistence.mapper.RoomPersistenceMapper
import com.lz.vectos.data.persistence.room.AiscSectionSeeder
import com.lz.vectos.data.persistence.room.AppDatabase
import com.lz.vectos.data.persistence.room.MaterialSeeder
import com.lz.vectos.data.persistence.room.StructuralDataSeeder
import com.lz.vectos.data.repository.CompositeSectionRepository
import com.lz.vectos.data.repository.DataStoreSettingsRepository
import com.lz.vectos.data.repository.NdsSectionRepository
import com.lz.vectos.data.repository.RoomAiscSectionRepository
import com.lz.vectos.data.repository.RoomCalculationRepository
import com.lz.data.persistence.room.repository.RoomMaterialRepository
import com.lz.vectos.data.repository.RoomProjectRepository
import com.lz.data.repository.StructuralCodeRepositoryImpl
import com.lz.vectos.domain.calculation.ProjectCalculationRegistry
import com.lz.data.persistence.room.dao.catalog.AiscSectionDao
import com.lz.vectos.presentation.BeamViewModel
import com.lz.vectos.presentation.CalculationContext
import com.lz.vectos.presentation.ProjectViewModel
import com.lz.vectos.presentation.SettingsViewModel
import com.lz.vectos.ui.HomeScreen
import com.lz.vectos.ui.SettingsScreen
import com.lz.vectos.ui.beam.BeamCalculatorScreen
import com.lz.vectos.ui.calculator.CalculatorRoute
import com.lz.vectos.ui.navigation.Screen
import com.lz.vectos.ui.project.NewProjectScreen
import com.lz.vectos.ui.project.ProjectLibraryScreen
import com.lz.vectos.ui.project.ProjectSettingsScreen
import com.lz.vectos.ui.theme.VectOSTheme
import com.lz.vectos.ui.tool.ToolPickerScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            VectOSTheme(darkTheme = false) {
                var database by remember { mutableStateOf<AppDatabase?>(null) }
                var initError by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    try {
                        val createdDatabase = withContext(Dispatchers.IO) {
                            AppDatabase.Companion.create(applicationContext)
                        }
                        database = createdDatabase

                        launch(Dispatchers.IO) {
                            StructuralDataSeeder(createdDatabase.structuralDataDao()).seed()
                            AiscSectionSeeder(
                                applicationContext,
                                createdDatabase.AiscSectionDao()
                            ).seed()
                            MaterialSeeder(createdDatabase.materialDao()).seed()
                        }
                    } catch (error: Throwable) {
                        initError = error.message ?: "Database initialization failed."
                    }
                }

                when {
                    initError != null -> ErrorScreen(message = initError!!)
                    database == null -> LoadingScreen()
                    else -> AppContent(database!!)
                }
            }
        }
    }

    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Companion.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.Companion.size(48.dp))
        }
    }

    @Composable
    private fun ErrorScreen(message: String) {
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Companion.Center
        ) {
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }
    }

    @Composable
    private fun AppContent(database: AppDatabase) {
        val structuralDataDao = remember(database) { database.structuralDataDao() }
        val structuralCodeRepository =
            remember(database) { StructuralCodeRepositoryImpl(structuralDataDao) }
        val mapper = remember(database) { RoomPersistenceMapper(structuralCodeRepository) }
        val projectRepository =
            remember(database) { RoomProjectRepository(database.projectDao(), mapper) }
        val calculationRepository = remember(database) {
            RoomCalculationRepository(
                database.calculationDao(),
                database.beamCalculationDao(),
                projectRepository,
                mapper
            )
        }
        val sectionRepository = remember(database) {
            CompositeSectionRepository(
                steelRepo = RoomAiscSectionRepository(database.aiscSectionDao()),
                woodRepo = NdsSectionRepository(applicationContext)
            )
        }
        val settingsRepository = remember { DataStoreSettingsRepository(applicationContext) }
        val calculationRegistry = remember { ProjectCalculationRegistry() }

        val navController = rememberNavController()

        val projectViewModel: ProjectViewModel = viewModel(
            factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProjectViewModel(
                        projectRepository,
                        calculationRepository,
                        structuralCodeRepository,
                        calculationRegistry
                    ) as T
                }
            }
        )

        val beamViewModel: BeamViewModel = viewModel(
            factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val materialRepository = RoomMaterialRepository(database.materialDao())
                    return BeamViewModel(
                        projectViewModel,
                        calculationRepository,
                        structuralCodeRepository,
                        sectionRepository,
                        materialRepository
                    ) as T
                }
            }
        )

        val settingsViewModel: SettingsViewModel = viewModel(
            factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(settingsRepository) as T
                }
            }
        )

        NavHost(navController = navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = projectViewModel,
                    onProjectSelected = { project ->
                        projectViewModel.setActiveProject(project)
                        navController.navigate(Screen.ProjectLibrary.route)
                    },
                    onQuickCalc = {
                        navController.navigate(Screen.BeamCalculator.route)
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNewProject = {
                        navController.navigate(Screen.NewProject.route)
                    }
                )
            }

            composable(Screen.ProjectLibrary.route) {
                ProjectLibraryScreen(
                    viewModel = projectViewModel,
                    onOpenCalculation = { calculation ->
                        // For now, assume it's a beam calculation
                        beamViewModel.loadCalculation(calculation.id)
                        navController.navigate(Screen.BeamCalculator.route)
                    },
                    onDeleteCalculation = { id ->
                        projectViewModel.deleteCalculation(id)
                    },
                    onAddCalculation = {
                        navController.navigate(Screen.ToolPicker.route)
                    },
                    onProjectSettings = {
                        navController.navigate(Screen.ProjectSettings.route)
                    },
                    onEditProject = {
                        navController.navigate(Screen.ProjectDetails.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ProjectSettings.route) {
                ProjectSettingsScreen(
                    viewModel = projectViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ProjectDetails.route) {
                val activeProject by projectViewModel.activeProject.collectAsState()
                NewProjectScreen(
                    viewModel = projectViewModel,
                    projectToEdit = activeProject,
                    onProjectCreated = {
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ToolPicker.route) {
                val activeProject by projectViewModel.activeProject.collectAsState()
                ToolPickerScreen(
                    context = CalculationContext.ProjectContext(activeProject),
                    onToolSelected = { route ->
                        when (route) {
                            is CalculatorRoute.BeamCalculator -> {
                                navController.navigate(Screen.BeamCalculator.route)
                            }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BeamCalculator.route) {
                val activeProject by projectViewModel.activeProject.collectAsState()
                BeamCalculatorScreen(
                    activeProject = activeProject,
                    viewModel = beamViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.NewProject.route) {
                NewProjectScreen(
                    viewModel = projectViewModel,
                    onProjectCreated = {
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}