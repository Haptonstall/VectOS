package com.lz.domain.plugin

sealed interface ToolPickerEvent {

    data class ShowMessage(
        val message: String
    ) : ToolPickerEvent

    data class Navigate(
        val route: String
    ) : ToolPickerEvent
}