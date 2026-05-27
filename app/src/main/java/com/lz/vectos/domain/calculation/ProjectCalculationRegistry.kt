package com.lz.vectos.domain.calculation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Project-scoped registry for all engineering calculations.
 * This registry acts as the single source of truth for calculations within a project session.
 */
class ProjectCalculationRegistry {
    private val _calculations = MutableStateFlow<Map<UUID, EngineeringCalculation>>(emptyMap())
    val calculations: StateFlow<Map<UUID, EngineeringCalculation>> = _calculations.asStateFlow()

    /**
     * Updates or adds a calculation to the registry.
     */
    fun updateCalculation(calculation: EngineeringCalculation) {
        val current = _calculations.value.toMutableMap()
        current[calculation.id] = calculation
        _calculations.value = current
    }

    /**
     * Removes a calculation from the registry.
     */
    fun removeCalculation(id: UUID) {
        val current = _calculations.value.toMutableMap()
        current.remove(id)
        _calculations.value = current
    }

    /**
     * Clears the registry (e.g., when switching projects).
     */
    fun clear() {
        _calculations.value = emptyMap()
    }

    /**
     * Filters calculations by tool type.
     */
    fun getByTool(toolId: String): List<EngineeringCalculation> {
        return _calculations.value.values.filter { it.toolId == toolId }
    }
}
