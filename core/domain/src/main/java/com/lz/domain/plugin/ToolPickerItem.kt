package com.lz.domain.plugin

data class ToolPickerItem(
    val descriptor: ModuleDescriptor,
    val installed: Boolean,
    val licensed: Boolean
)