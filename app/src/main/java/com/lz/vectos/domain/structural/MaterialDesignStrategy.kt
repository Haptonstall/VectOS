package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.SectionProfile
import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Moment

/**
 * Strategy contract for material-specific design capacity calculations.
 */
interface MaterialDesignStrategy {
    fun computeCapacity(
        section: SectionProfile,
        code: BuildingCode
    ): SectionCapacity
}

/**
 * AISC-aligned steel design strategy.
 */
class SteelDesignStrategy(private val yieldStrengthPa: Double = 345e6) : MaterialDesignStrategy {
    override fun computeCapacity(section: SectionProfile, code: BuildingCode): SectionCapacity {
        // Mn = Fy * Zx (Plastic moment for compact sections)
        val nominalMn = yieldStrengthPa * section.plasticModulus
        // Vn = 0.6 * Fy * Aw (Shear capacity)
        val nominalVn = 0.6 * yieldStrengthPa * section.area 

        return SectionCapacity(
            nominalMomentCapacity = Moment(nominalMn),
            nominalShearCapacity = Force(nominalVn),
            designMomentCapacity = Moment(nominalMn * code.phiMoment),
            designShearCapacity = Force(nominalVn * code.phiShear)
        )
    }
}

/**
 * Simplified wood design strategy (ASD-based).
 */
class WoodDesignStrategy(
    private val allowableBendingStressPa: Double = 10e6, // e.g. 10 MPa
    private val allowableShearStressPa: Double = 1e6      // e.g. 1 MPa
) : MaterialDesignStrategy {
    override fun computeCapacity(section: SectionProfile, code: BuildingCode): SectionCapacity {
        // NDS-style: M_allow = Fb * Sx * AdjustmentFactors
        // For Step 30, adjustment factors are 1.0
        val nominalMn = allowableBendingStressPa * section.elasticModulus
        val nominalVn = allowableShearStressPa * section.area * (2.0 / 3.0) // Simplified for rectangular

        // Wood often uses ASD, so Design Capacity = Nominal Capacity (factors are usually pre-applied to Fb)
        return SectionCapacity(
            nominalMomentCapacity = Moment(nominalMn),
            nominalShearCapacity = Force(nominalVn),
            designMomentCapacity = Moment(nominalMn), 
            designShearCapacity = Force(nominalVn)
        )
    }
}
