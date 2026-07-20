package com.lz.model.structural

import kotlinx.serialization.Serializable
import com.lz.model.regulatory.AiscEdition
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.regulatory.codes.StandardEdition

/**
 * Represents a validation warning or error within the active engineering configuration.
 */
@Serializable
data class DesignContextIssue(
    val severity: IssueSeverity,
    val message: String
)

@Serializable
enum class IssueSeverity {
    INFO,
    WARNING,
    ERROR
}

/**
 * Single, type-safe source of truth managing material standard overrides
 * for downstream calculation solvers.
 */
@Serializable
data class ProjectDesignContext(
    // Material standard overrides. If a material key is missing,
    // the engine automatically infers the legally mandated default via the BuildingCode.
    val steelOverride: AiscEdition? = null
    // val concreteOverride: Aci318Edition? = null,
    // val woodOverride: NdsEdition? = null
) {

    /**
     * Resolves the active, legally binding AISC edition for steel calculations,
     * checking for explicit user overrides before falling back to the building code baseline.
     */
    fun resolveSteelEdition(buildingCode: BuildingCode): AiscEdition {
        return steelOverride ?: resolveDefaultSteelEdition(buildingCode)
    }

    /**
     * Validates the material configurations for engineering consistency
     * and flags legacy code usage rules.
     */
    fun validate(buildingCode: BuildingCode): List<DesignContextIssue> {
        val issues = mutableListOf<DesignContextIssue>()
        val activeSteel = resolveSteelEdition(buildingCode)
        val defaultSteel = resolveDefaultSteelEdition(buildingCode)

        // Check if user is manually forcing an older version of the material code than the IBC baseline
        if (steelOverride != null && activeSteel.publicationYear < defaultSteel.publicationYear) {
            issues.add(
                DesignContextIssue(
                    severity = IssueSeverity.WARNING,
                    message = "${activeSteel.label} is older than the default reference standard mandated by ${buildingCode.shortName} (${defaultSteel.name}). Verify local jurisdiction compliance."
                )
            )
        } else if (steelOverride != null && activeSteel != defaultSteel) {
            issues.add(
                DesignContextIssue(
                    severity = IssueSeverity.INFO,
                    message = "Custom material override active: Designing steel elements via ${activeSteel.label}."
                )
            )
        }

        return issues
    }

    private fun resolveDefaultSteelEdition(buildingCode: BuildingCode): AiscEdition {
        val defaultStandardEdition = buildingCode.defaultMaterialStandards[MaterialType.STEEL]?.edition
        val defaultSteelEdition = (defaultStandardEdition as? StandardEdition.Aisc360)?.edition
        if (defaultSteelEdition != null) return defaultSteelEdition

        val associatedSteelEdition = buildingCode.standards.firstNotNullOfOrNull {
            (it.edition as? StandardEdition.Aisc360)?.edition
        }
        if (associatedSteelEdition != null) return associatedSteelEdition

        val inheritedSteelEdition = buildingCode.baseCode?.let { resolveDefaultSteelEdition(it) }
        if (inheritedSteelEdition != null) return inheritedSteelEdition

        throw IllegalStateException(
            "Building code ${buildingCode.id} does not define a default AISC 360 steel standard."
        )
    }
}
