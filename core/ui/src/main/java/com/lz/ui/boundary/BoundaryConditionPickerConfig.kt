package com.lz.ui.boundary

data class BoundaryConditionPickerConfig(
    val title: String,
    val presetOptions: List<BoundaryPresetOption>,
    val dofEditorConfig: DofEditorConfig,
    val allowCustomDofs: Boolean = true
)