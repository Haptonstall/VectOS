package com.lz.domain.module

sealed interface ToolPickerEvent {

    data class Navigate(
        val route: String
    ) : ToolPickerEvent

    data class Snackbar(
        val message: String
    ) : ToolPickerEvent
}