package com.lz.model.regulatory.loads

import kotlinx.serialization.Serializable

/**
 * Categorization of load combinations based on their design intent and limit state.
 */
@Serializable
enum class CombinationType {
    STRENGTH,
    SERVICEABILITY,
    STABILITY,
    EXTRAORDINARY
}