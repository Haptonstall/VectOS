package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.SectionOrientation
import com.lz.vectos.domain.beam.SectionProfile
import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Moment

/**
 * NDS-aligned wood design strategy (ASD-based).
 */
class WoodDesignStrategy : MaterialDesignStrategy {
    override fun computeCapacity(
        section: SectionProfile,
        code: BuildingCode,
        orientation: SectionOrientation,
        inputs: MaterialDesignInputs
    ): SectionCapacity {
        val woodInputs = inputs as? MaterialDesignInputs.Wood ?: MaterialDesignInputs.Wood()
        
        // Use strong axis properties for now (Step 31B constraint)
        val props = section.propertiesStrongAxis

        // ASD Flexure: M_allow = Fb' * Sx
        val fbPrime = woodInputs.referenceFbPa
        val allowableM = fbPrime * props.s
        
        // ASD Shear: V_allow = Fv' * Area
        val fvPrime = woodInputs.referenceFvPa
        val allowableV = fvPrime * section.area

        return SectionCapacity(
            nominalMomentCapacity = Moment(allowableM),
            nominalShearCapacity = Force(allowableV),
            nominalAxialCapacity = Force(0.0), // Placeholder
            designMomentCapacity = Moment(allowableM),
            designShearCapacity = Force(allowableV),
            designAxialCapacity = Force(0.0),
            evaluationSummary = mapOf(
                "NDS Flexure" to CapacityEvaluationStatus.Evaluated(allowableM, "NDS 3.3.1"),
                "NDS Shear" to CapacityEvaluationStatus.Evaluated(allowableV, "NDS 3.4.1")
            )
        )
    }
}
