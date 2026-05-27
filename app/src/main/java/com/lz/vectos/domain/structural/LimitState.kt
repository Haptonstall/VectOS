package com.lz.vectos.domain.structural

import kotlinx.serialization.Serializable

/**
 * Defines the engineering limit states for analysis and design.
 */
@Serializable
enum class LimitState {
    STRENGTH,
    SERVICEABILITY
}
