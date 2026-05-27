package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.MaterialType
import com.lz.vectos.domain.beam.SectionProfile
import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Moment

/**
 * Pure Kotlin service to resolve section capacities based on material and building code.
 */
object SectionCapacityService {

    /**
     * Resolves capacities for a given section profile and building code.
     */
    fun resolve(
        section: SectionProfile,
        code: BuildingCode,
        yieldStrengthPa: Double = 345e6 // Default 50 ksi for steel
    ): SectionCapacity {
        return when (section.materialType) {
            MaterialType.STEEL -> resolveSteelCapacity(section, code, yieldStrengthPa)
            else -> resolveSteelCapacity(section, code, yieldStrengthPa) // Fallback
        }
    }

    private fun resolveSteelCapacity(
        section: SectionProfile,
        code: BuildingCode,
        fy: Double
    ): SectionCapacity {
        // Simplified AISC LRFD equations
        // Mn = Fy * Zx (Plastic moment)
        val nominalMn = fy * section.plasticModulus
        // Vn = 0.6 * Fy * Aw (Shear capacity, simplified)
        val nominalVn = 0.6 * fy * section.area 

        return SectionCapacity(
            nominalMomentCapacity = Moment(nominalMn),
            nominalShearCapacity = Force(nominalVn),
            designMomentCapacity = Moment(nominalMn * code.phiMoment),
            designShearCapacity = Force(nominalVn * code.phiShear)
        )
    }
}
