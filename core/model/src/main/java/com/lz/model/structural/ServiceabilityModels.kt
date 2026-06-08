package com.lz.model.structural

import com.lz.model.regulatory.codes.ServiceabilityCriterion
import com.lz.model.units.Length
import kotlinx.serialization.Serializable

/**
 * The computed outcome of a serviceability check against a specific criterion.
 */
@Serializable
data class ServiceabilityResult(
    val actualDeflection: Length,
    val allowableDeflection: Length,
    val utilization: Double,
    val criterion: ServiceabilityCriterion
)