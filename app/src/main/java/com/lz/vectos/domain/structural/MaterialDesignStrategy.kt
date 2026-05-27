package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.SectionProfile
import com.lz.vectos.domain.units.*

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
class SteelDesignStrategy(private val yieldStrengthPsi: Double = 50000.0) : MaterialDesignStrategy {
    override fun computeCapacity(section: SectionProfile, code: BuildingCode): SectionCapacity {
        // Mn = Fy * Zx (Plastic moment for compact sections)
        // Convert section properties from internal Unit Models.
        
        val nominalMnLbIn = yieldStrengthPsi * section.propertiesStrongAxis.z.inIn3
        val nominalVnLbs = 0.6 * yieldStrengthPsi * section.area.inIn2 

        val nominalMn = nominalMnLbIn.lbIn
        val nominalVn = nominalVnLbs.poundsForce

        val phiM = 0.9 // TODO: Get from code if available
        val phiV = 0.9 // TODO: Get from code if available

        return SectionCapacity(
            nominalMomentCapacity = nominalMn,
            nominalShearCapacity = nominalVn,
            designMomentCapacity = nominalMn * phiM,
            designShearCapacity = nominalVn * phiV
        )
    }
}

/**
 * Simplified wood design strategy (ASD-based).
 */
class WoodDesignStrategy(
    private val allowableBendingStressPsi: Double = 1000.0,
    private val allowableShearStressPsi: Double = 150.0
) : MaterialDesignStrategy {
    override fun computeCapacity(section: SectionProfile, code: BuildingCode): SectionCapacity {
        // NDS-style: M_allow = Fb * Sx * AdjustmentFactors
        val nominalMnLbIn = allowableBendingStressPsi * section.propertiesStrongAxis.s.inIn3
        val nominalVnLbs = allowableShearStressPsi * section.area.inIn2 * (2.0 / 3.0) 

        val nominalMn = nominalMnLbIn.lbIn
        val nominalVn = nominalVnLbs.poundsForce

        // Wood often uses ASD, so Design Capacity = Nominal Capacity
        return SectionCapacity(
            nominalMomentCapacity = nominalMn,
            nominalShearCapacity = nominalVn,
            designMomentCapacity = nominalMn, 
            designShearCapacity = nominalVn
        )
    }
}
