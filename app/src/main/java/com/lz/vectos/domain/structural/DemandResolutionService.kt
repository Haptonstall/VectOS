package com.lz.vectos.domain.structural

import com.lz.vectos.viewmodel.CalculationContext

/**
 * Single entry point for resolving governing structural demands.
 * Dispatches to methodology-specific resolvers.
 */
class DemandResolutionService(
    private val asdResolver: AsdDemandResolver,
    private val lrfdResolver: LrfdDemandResolver
) {
    fun resolveDemand(
        member: StructuralMember,
        loads: List<Load>,
        context: CalculationContext
    ): StructuralDemand {
        // Validation guards
        if (member.spans.isEmpty()) {
            throw IllegalArgumentException("Structural member must have at least one span")
        }
        
        if (loads.any { it.category == null }) {
            throw IllegalArgumentException("All loads must have a declared category")
        }

        return when (context.methodology) {
            DesignMethodology.ASD -> asdResolver.resolveDemand(member, loads, context)
            DesignMethodology.LRFD -> lrfdResolver.resolveDemand(member, loads, context)
        }
    }
}
