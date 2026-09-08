package com.lz.model.structural

import com.lz.model.regulatory.codes.ServiceabilityCriterion
import com.lz.model.units.Length
import com.lz.model.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * The computed outcome of a serviceability check against a specific criterion.
 *
 * [spanId] is null for results computed before per-span evaluation existed
 * (older persisted calculations) — treat null as "member-wide, not tied to
 * one span" rather than an error.
 */
@Serializable
data class ServiceabilityResult(
    val actualDeflection: Length,
    val allowableDeflection: Length,
    val utilization: Double,
    val criterion: ServiceabilityCriterion,
    @Serializable(with = UUIDSerializer::class)
    val spanId: UUID? = null
)