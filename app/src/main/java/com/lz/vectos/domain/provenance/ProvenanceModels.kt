package com.lz.vectos.domain.provenance

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Categories for engineering assumptions.
 */
enum class AssumptionCategory {
    GEOMETRY,
    LOADING,
    MATERIAL,
    CODE,
    BOUNDARY_CONDITIONS,
    GENERAL
}

/**
 * Source of the assumption.
 */
enum class AssumptionSource {
    USER,
    CODE_DEFAULT,
    SYSTEM_ASSUMED
}

/**
 * A single engineering assumption or basis of calculation.
 */
@Serializable
data class CalculationAssumption(
    val id: String,
    val category: AssumptionCategory,
    val description: String,
    val source: AssumptionSource,
    val isEnabled: Boolean = true
)

/**
 * A snapshot record of how and why a calculation result was produced.
 */
@Serializable
data class CalculationProvenance(
    val timestamp: String,
    val projectId: String?,
    val calculatorId: String,
    val toolVersion: String = "1.0.0",
    val buildingCode: String,
    val unitSystem: String,
    val sectionDesignation: String,
    val spanLength: String,
    val loadCasesSummary: String,
    val assumptions: List<CalculationAssumption>,
    val acknowledgments: List<String>
)
