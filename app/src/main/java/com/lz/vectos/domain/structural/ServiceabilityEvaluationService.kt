package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Length

/**
 * Pure Kotlin service to evaluate serviceability responses against code limits.
 */
object ServiceabilityEvaluationService {

    /**
     * Evaluates deflection results for a member based on building code criteria.
     */
    fun evaluate(
        member: StructuralMember,
        loadCases: List<LoadCase>,
        buildingCode: BuildingCode,
        e: Double,
        i: Double
    ): List<ServiceabilityResult> {
        val span = member.length.meters
        
        return buildingCode.serviceabilityCriteria.map { criterion ->
            // Resolve actual deflection for the criterion's load case
            val actualDeflectionM = if (criterion.loadCaseId == null) {
                // Total Deflection (Unfactored sum of all cases)
                loadCases.sumOf { case -> 
                    case.loads.sumOf { load -> computeDeflection(load, span, e, i) }
                }
            } else {
                // Specific Case Deflection (e.g. LIVE only)
                val case = loadCases.find { it.id == criterion.loadCaseId }
                case?.loads?.sumOf { load -> computeDeflection(load, span, e, i) } ?: 0.0
            }

            val allowableDeflectionM = if (criterion.spanDenominator > 0) span / criterion.spanDenominator else 0.0

            ServiceabilityResult(
                actualDeflection = Length(actualDeflectionM),
                allowableDeflection = Length(allowableDeflectionM),
                utilization = if (allowableDeflectionM > 0) actualDeflectionM / allowableDeflectionM else 0.0,
                criterion = criterion
            )
        }
    }

    private fun computeDeflection(load: Load, l: Double, e: Double, i: Double): Double {
        if (l <= 0 || e * i <= 0) return 0.0
        return when (load) {
            is Load.PointLoad -> {
                val p = load.value
                val a = load.locationStart
                val b = l - a
                (p * a * b * (l + a)) * Math.sqrt(3 * a * (l + a)) / (27 * e * i * l)
            }
            is Load.UniformDistributedLoad -> {
                (5.0 * load.value * Math.pow(l, 4.0)) / (384.0 * e * i)
            }
            else -> 0.0
        }
    }
}
