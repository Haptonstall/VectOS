package com.lz.vectos.input.model

/**
 * PURE KOTLIN INPUT SPECIFICATION MODELS
 * No Android or Compose dependencies.
 */

enum class InputVisibility {
    REQUIRED,
    OPTIONAL,
    ADVANCED
}

enum class InputKeyboardType {
    TEXT,
    NUMBER,
    DECIMAL
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()
}

interface ValidationRule {
    fun validate(input: String): ValidationResult
}

data class UnitOption(
    val id: String,
    val label: String
)

data class CalculationInputSpec(
    val id: String,
    val label: String,
    val unitOptions: List<UnitOption> = emptyList(),
    val defaultUnit: UnitOption? = null,
    val keyboardType: InputKeyboardType = InputKeyboardType.DECIMAL,
    val validationRules: List<ValidationRule> = emptyList(),
    val isRequired: Boolean = true,
    val visibility: InputVisibility = InputVisibility.REQUIRED
)

/**
 * Common Validation Rules
 */
class NotBlankRule(private val errorMessage: String = "Cannot be empty") : ValidationRule {
    override fun validate(input: String): ValidationResult {
        return if (input.isNotBlank()) ValidationResult.Valid else ValidationResult.Invalid(errorMessage)
    }
}

class PositiveNumberRule(private val errorMessage: String = "Must be a positive number") : ValidationRule {
    override fun validate(input: String): ValidationResult {
        val value = input.toDoubleOrNull()
        return if (value != null && value > 0) ValidationResult.Valid else ValidationResult.Invalid(errorMessage)
    }
}
