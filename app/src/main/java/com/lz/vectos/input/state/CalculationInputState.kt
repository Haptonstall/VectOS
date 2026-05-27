package com.lz.vectos.input.state

import com.lz.vectos.input.model.CalculationInputSpec
import com.lz.vectos.input.model.UnitOption
import com.lz.vectos.input.model.ValidationResult

/**
 * VIEWMODEL-OWNED INPUT STATE
 * Tracks the live state of a single input field.
 */
data class CalculationInputState(
    val spec: CalculationInputSpec,
    val rawValue: String = "",
    val selectedUnit: UnitOption? = spec.defaultUnit,
    val validationResult: ValidationResult = ValidationResult.Valid
) {
    val isValid: Boolean get() = validationResult is ValidationResult.Valid
    
    val errorMessage: String? get() = (validationResult as? ValidationResult.Invalid)?.errorMessage
}

/**
 * UI -> VIEWMODEL EVENT CONTRACT
 */
sealed class InputEvent {
    data class ValueChanged(val inputId: String, val newValue: String) : InputEvent()
    data class UnitChanged(val inputId: String, val newUnit: UnitOption) : InputEvent()
    data class FocusLost(val inputId: String) : InputEvent()
    data class VisibilityToggled(val inputId: String) : InputEvent()
}
