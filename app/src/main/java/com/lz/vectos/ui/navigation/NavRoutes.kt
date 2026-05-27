package com.lz.vectos.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ProjectLibrary : Screen("project_library")
    object ProjectSettings : Screen("project_settings")
    object ToolPicker : Screen("tool_picker")
    object BeamCalculator : Screen("beam_calculator")
    object Settings : Screen("settings")
}
