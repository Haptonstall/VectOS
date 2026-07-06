package com.lz.domain.module

data class ToolPickerItem(
    val descriptor: ModuleDescriptor,
    val installed: Boolean,
    val licensed: Boolean
)