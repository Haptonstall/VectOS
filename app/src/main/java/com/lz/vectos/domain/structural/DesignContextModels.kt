package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.MaterialType
import com.lz.vectos.domain.units.UnitSystem

/**
 * Single source of truth for the design environment of a project.
 * Uses the BuildingCode and Standard classes for type-safe engineering logic.
 */
data class ProjectDesignContext(
    val units: UnitSystem,
    val methodology: DesignMethodology,
    val buildingCode: BuildingCode,
    val loadingStandard: Standard,
    /**
     * Dynamic map of design standards by material type.
     * Overrides the defaults provided by [buildingCode].
     */
    val materialStandards: Map<MaterialType, Standard> = emptyMap()
) {
    /**
     * Resolves the effective standard for a given material type, 
     * falling back to the building code's default if not explicitly overridden.
     */
    fun getStandardFor(materialType: MaterialType): Standard {
        return materialStandards[materialType] 
            ?: buildingCode.defaultMaterialStandards[materialType]
            ?: throw IllegalStateException("No standard defined for $materialType in ${buildingCode.shortName}")
    }

    companion object {
        fun empty(): ProjectDesignContext {
            return ProjectDesignContext(
                units = UnitSystem.IMPERIAL,
                methodology = DesignMethodology.ASD,
                buildingCode = BuildingCode(id = "EMPTY", shortName = "None", longName = "None"),
                loadingStandard = Standard(id = "EMPTY", shortName = "None", longName = "None"),
                materialStandards = emptyMap()
            )
        }
    }

    /**
     * Validates the current context for engineering consistency.
     * Returns a list of warning/error messages.
     */
    fun validate(): List<DesignContextIssue> {
        val issues = mutableListOf<DesignContextIssue>()

        // 1. Check Loading Standard Compatibility
        if (!buildingCode.standards.contains(loadingStandard)) {
            issues.add(DesignContextIssue(
                severity = IssueSeverity.WARNING,
                message = "${loadingStandard.shortName} is not the standard loading reference for ${buildingCode.shortName} (expected ${buildingCode.standards.joinToString { it.shortName }})."
            ))
        }

        // 2. Check Material Standard Consistency & Version Alignment
        materialStandards.forEach { (type, standard) ->
            val default = buildingCode.defaultMaterialStandards[type]
            if (default != null) {
                if (default != standard) {
                    issues.add(DesignContextIssue(
                        severity = IssueSeverity.INFO,
                        message = "Using ${standard.shortName} for ${type.name} instead of the code default (${default.shortName})."
                    ))
                }
                
                // Version Alignment Check (Recommendation 1)
                // Logic: Standards older than the building code's default are usually risky.
                val defaultYear = default.shortName.filter { it.isDigit() }.toIntOrNull() ?: 0
                val selectedYear = standard.shortName.filter { it.isDigit() }.toIntOrNull() ?: 0
                
                if (selectedYear < defaultYear && selectedYear != 0) {
                    issues.add(DesignContextIssue(
                        severity = IssueSeverity.WARNING,
                        message = "${standard.shortName} is an older version than referenced by ${buildingCode.shortName} (${default.shortName}). Verify legal compliance."
                    ))
                }
            }
        }

        return issues
    }
}

/**
 * Represents a validation issue within the design context.
 */
data class DesignContextIssue(
    val severity: IssueSeverity,
    val message: String
)

enum class IssueSeverity {
    INFO,
    WARNING,
    ERROR
}
