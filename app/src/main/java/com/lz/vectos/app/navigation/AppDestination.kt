package com.lz.vectos.app.navigation

/**
 * Destinations owned by the application shell.
 *
 * Engineering modules NEVER appear here.
 */
sealed class AppDestination(

    val route: String

) {

    data object Home :

        AppDestination("home")

    data object Projects :

        AppDestination("projects")

    data object ProjectSettings :

        AppDestination("project_settings")

    data object EditProject :

        AppDestination("edit_project")

    data object NewProject :

        AppDestination("new_project")

    data object ToolPicker :

        AppDestination("tool_picker")

    data object Settings :

        AppDestination("settings")

}