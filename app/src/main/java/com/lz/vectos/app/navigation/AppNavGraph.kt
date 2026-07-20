package com.lz.vectos.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lz.domain.calculation.CalculationContext
import com.lz.domain.module.ToolPickerEvent
import com.lz.runtime.api.RuntimeEnvironment
import com.lz.runtime.compose.navigation.installRuntimeRoutes
import com.lz.vectos.presentation.ProjectViewModel
import com.lz.vectos.presentation.SettingsViewModel
import com.lz.vectos.ui.HomeScreen
import com.lz.vectos.ui.SettingsScreen
import com.lz.vectos.ui.navigation.Screen
import com.lz.vectos.ui.project.NewProjectScreen
import com.lz.vectos.ui.project.ProjectLibraryScreen
import com.lz.vectos.ui.project.ProjectSettingsScreen
import com.lz.vectos.ui.tool.ToolPickerScreen
import com.lz.vectos.ui.tool.ToolPickerViewModel

@Composable
fun AppNavGraph(

    runtime: RuntimeEnvironment,

    navController: NavHostController,

    projectViewModel: ProjectViewModel = hiltViewModel(),

    settingsViewModel: SettingsViewModel = hiltViewModel(),

    toolPickerViewModel: ToolPickerViewModel = hiltViewModel()

) {

    /*
     * Observe navigation events emitted by the Tool Picker.
     */
    LaunchedEffect(Unit) {

        toolPickerViewModel.events.collect { event ->

            when (event) {

                is ToolPickerEvent.Navigate ->

                    navController.navigate(event.route)

                is ToolPickerEvent.Snackbar -> {

                    // ScaffoldState later

                }

            }

        }

    }

    NavHost(

        navController = navController,

        startDestination = Screen.Home.route

    ) {

        composable(Screen.Home.route) {

            HomeScreen(

                viewModel = projectViewModel,

                onProjectSelected = {

                    projectViewModel.setActiveProject(it)

                    navController.navigate(
                        Screen.ProjectLibrary.route
                    )

                },

                onQuickCalc = {

                    toolPickerViewModel.loadTools(

                        CalculationContext.QuickCalcContext()

                    )

                    navController.navigate(
                        Screen.ToolPicker.route
                    )

                },

                onSettingsClick = {

                    navController.navigate(
                        Screen.Settings.route
                    )

                },

                onNewProject = {

                    navController.navigate(
                        Screen.NewProject.route
                    )

                }

            )

        }

        composable(Screen.ProjectLibrary.route) {

            ProjectLibraryScreen(

                viewModel = projectViewModel,

                onOpenCalculation = {

                    navController.navigate(
                        it.toolId
                    )

                },

                onDeleteCalculation = {

                    projectViewModel.deleteCalculation(it)

                },

                onAddCalculation = {

                    val project =
                        projectViewModel.activeProject.value

                    toolPickerViewModel.loadTools(

                        CalculationContext.ProjectContext(
                            project
                        )

                    )

                    navController.navigate(
                        Screen.ToolPicker.route
                    )

                },

                onProjectSettings = {

                    navController.navigate(
                        Screen.ProjectSettings.route
                    )

                },

                onEditProject = {

                    navController.navigate(
                        Screen.ProjectDetails.route
                    )

                },

                onBack = {

                    navController.popBackStack()

                }

            )

        }

        composable(Screen.ProjectSettings.route) {

            ProjectSettingsScreen(

                viewModel = projectViewModel,

                onBack = {

                    navController.popBackStack()

                }

            )

        }

        composable(Screen.ProjectDetails.route) {

            val project by
            projectViewModel
                .activeProject
                .collectAsState()

            NewProjectScreen(

                viewModel = projectViewModel,

                projectToEdit = project,

                onProjectCreated = {

                    navController.popBackStack()

                },

                onCancel = {

                    navController.popBackStack()

                }

            )

        }

        composable(Screen.NewProject.route) {

            NewProjectScreen(

                viewModel = projectViewModel,

                onProjectCreated = {

                    navController.popBackStack()

                },

                onCancel = {

                    navController.popBackStack()

                }

            )

        }

        composable(Screen.Settings.route) {

            SettingsScreen(

                viewModel = settingsViewModel,

                onBack = {

                    navController.popBackStack()

                }

            )

        }

        composable(Screen.ToolPicker.route) {

            val tools by
            toolPickerViewModel
                .tools
                .collectAsState()

            ToolPickerScreen(

                tools = tools,

                onModuleAction = {

                    toolPickerViewModel.onModuleAction(it)

                },

                onBack = {

                    navController.popBackStack()

                }

            )

        }

        /*
         * Runtime modules contribute their destinations here.
         */
        installRuntimeRoutes(runtime)

    }

}