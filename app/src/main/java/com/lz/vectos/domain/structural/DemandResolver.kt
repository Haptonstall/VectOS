package com.lz.vectos.domain.structural

import com.lz.vectos.viewmodel.CalculationContext

/**
 * Common contract for resolving structural demands from geometry and loads.
 */
interface DemandResolver {
    fun resolveDemand(
        member: StructuralMember,
        loads: List<Load>,
        context: CalculationContext
    ): StructuralDemand
}

/**
 * ASD-specific implementation of the demand resolver.
 */
class AsdDemandResolver : DemandResolver {
    override fun resolveDemand(
        member: StructuralMember,
        loads: List<Load>,
        context: CalculationContext
    ): StructuralDemand {
        val asceEdition = (context as? CalculationContext.ProjectContext)?.project?.designContext?.asceEdition 
            ?: AsceEdition.ASCE_7_16
            
        val result = AsdLoadCombinationEngine.resolve(member, loads, asceEdition)
        
        return StructuralDemand(
            moment = result.moment,
            shear = result.shear,
            methodology = DesignMethodology.ASD,
            governingCombination = result.controllingCombination,
            trace = result.trace
        )
    }
}

/**
 * LRFD-specific implementation of the demand resolver.
 */
class LrfdDemandResolver : DemandResolver {
    override fun resolveDemand(
        member: StructuralMember,
        loads: List<Load>,
        context: CalculationContext
    ): StructuralDemand {
        val asceEdition = (context as? CalculationContext.ProjectContext)?.project?.designContext?.asceEdition 
            ?: AsceEdition.ASCE_7_16
            
        val result = LrfdLoadCombinationEngine.resolve(member, loads, asceEdition)
        
        return StructuralDemand(
            moment = result.moment,
            shear = result.shear,
            methodology = DesignMethodology.LRFD,
            governingCombination = result.controllingCombination,
            trace = result.trace
        )
    }
}
