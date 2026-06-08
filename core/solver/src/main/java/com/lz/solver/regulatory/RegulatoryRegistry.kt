package com.lz.solver.regulatory

import com.lz.model.regulatory.PrimaryBuildingCode
import com.lz.model.regulatory.Asce7Edition
import com.lz.model.regulatory.AiscEdition
import com.lz.model.regulatory.LoadCombination

/**
 * Defines the legal, structural configuration metadata contract for a jurisdiction.
 */
interface RegulatoryFramework {
    val code: PrimaryBuildingCode
    val baseCode: PrimaryBuildingCode?      // Allows cascading inheritance (e.g., CBC inherits from IBC)
    val loadingStandard: Asce7Edition      // Governs default load combinations
    val steelStandard: AiscEdition         // Governs default material calculation engines
    val loadCombinationOverrides: List<LoadCombination>? // Local structural adjustments (null if using standard default)
}

/**
 * Single source of truth for the VectOS regulatory framework.
 * To add a new code edition or state amendment, simply append a new entry to this map.
 */
object RegulatoryRegistry {

    private val registry: Map<PrimaryBuildingCode, RegulatoryFramework> = mapOf(

        // 1. Model Code Baselines
        PrimaryBuildingCode.IBC_2021 to object : RegulatoryFramework {
            override val code = PrimaryBuildingCode.IBC_2021
            override val baseCode = null
            override val loadingStandard = Asce7Edition.ASCE_7_16
            override val steelStandard = AiscEdition.AISC_360_16
            override val loadCombinationOverrides = null
        },

        PrimaryBuildingCode.IBC_2024 to object : RegulatoryFramework {
            override val code = PrimaryBuildingCode.IBC_2024
            override val baseCode = null
            override val loadingStandard = Asce7Edition.ASCE_7_22
            override val steelStandard = AiscEdition.AISC_360_22
            override val loadCombinationOverrides = null
        },

        // 2. Local State Amendments (Inherits properties from IBC 2024 but allows explicit overrides)
        PrimaryBuildingCode.CBC_2025 to object : RegulatoryFramework {
            override val code = PrimaryBuildingCode.CBC_2025
            override val baseCode = PrimaryBuildingCode.IBC_2024 // Cascading legal inheritance
            override val loadingStandard = Asce7Edition.ASCE_7_22
            override val steelStandard = AiscEdition.AISC_360_22

            // If California adjusts specific combination rules, declare them directly here.
            // Otherwise, set to null to fall back to the default parent standard.
            override val loadCombinationOverrides = null
        }
    )

    /**
     * Resolves the complete regulatory profile for a given building code.
     */
    fun getFramework(code: PrimaryBuildingCode): RegulatoryFramework {
        return registry[code]
            ?: throw IllegalArgumentException("Regulatory parameters for ${code.name} have not been registered.")
    }
}