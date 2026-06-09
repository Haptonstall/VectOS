package com.lz.vectos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
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

/**
 * Single-activity entry point for VectOS.
 *
 * Responsibilities here are intentionally minimal:
 *   - Install splash screen
 *   - Set up edge-to-edge display
 *   - Host the Compose NavHost
 *
 * Database initialization, seeding, and dependency wiring are handled by:
 *   - DatabaseModule (Hilt) — provides AppDatabase, BeamDatabase, DAOs
 *   - VectosApplication.onCreate() — triggers seeders via AppInitializer
 *   - @HiltViewModel annotated ViewModels — injected automatically by hiltViewModel()
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VectOSTheme(darkTheme = false) {
                // ViewModels are provided by Hilt — no manual factory construction needed.
                // Hilt resolves all constructor dependencies declared in DatabaseModule.
                val projectViewModel: ProjectViewModel = hiltViewModel()
                val beamViewModel: BeamViewModel       = hiltViewModel()
                val settingsViewModel: SettingsViewModel = hiltViewModel()

                val navController = rememberNavController()

                NavHost(
                    navController    = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel         = projectViewModel,
                            onProjectSelected = { project ->
                                projectViewModel.setActiveProject(project)
                                navController.navigate(Screen.ProjectLibrary.route)
                            },
                            onQuickCalc       = {
                                navController.navigate(Screen.BeamCalculator.route)
                            },
                            onSettingsClick   = {
                                navController.navigate(Screen.Settings.route)
                            },
                            onNewProject      = {
                                navController.navigate(Screen.NewProject.route)
                            }
                        )
                    }

                    composable(Screen.ProjectLibrary.route) {
                        ProjectLibraryScreen(
                            viewModel           = projectViewModel,
                            onOpenCalculation   = { calculation ->
                                beamViewModel.loadCalculation(calculation.id)
                                navController.navigate(Screen.BeamCalculator.route)
                            },
                            onDeleteCalculation = { id ->
                                projectViewModel.deleteCalculation(id)
                            },
                            onAddCalculation    = {
                                navController.navigate(Screen.ToolPicker.route)
                            },
                            onProjectSettings   = {
                                navController.navigate(Screen.ProjectSettings.route)
                            },
                            onEditProject       = {
                                navController.navigate(Screen.ProjectDetails.route)
                            },
                            onBack              = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.ProjectSettings.route) {
                        ProjectSettingsScreen(
                            viewModel = projectViewModel,
                            onBack    = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.ProjectDetails.route) {
                        val activeProject by projectViewModel.activeProject.collectAsState()
                        NewProjectScreen(
                            viewModel        = projectViewModel,
                            projectToEdit    = activeProject,
                            onProjectCreated = { navController.popBackStack() },
                            onCancel         = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack    = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.ToolPicker.route) {
                        val activeProject by projectViewModel.activeProject.collectAsState()
                        ToolPickerScreen(
                            context        = CalculationContext.ProjectContext(activeProject),
                            onToolSelected = { route ->
                                when (route) {
                                    is CalculatorRoute.BeamCalculator ->
                                        navController.navigate(Screen.BeamCalculator.route)
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.BeamCalculator.route) {
                        val activeProject by projectViewModel.activeProject.collectAsState()
                        BeamCalculatorScreen(
                            activeProject = activeProject,
                            viewModel     = beamViewModel,
                            onBack        = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.NewProject.route) {
                        NewProjectScreen(
                            viewModel        = projectViewModel,
                            onProjectCreated = { navController.popBackStack() },
                            onCancel         = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}