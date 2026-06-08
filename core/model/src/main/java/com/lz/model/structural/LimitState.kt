package com.lz.model.structural

import kotlinx.serialization.Serializable

/**
 * Defines the high-level engineering limit state categories.
 */
@Serializable
enum class LimitState {
    STRENGTH,
    SERVICEABILITY
}

/**
 * Defines the explicit structural limit states evaluated during analysis and design.
 * This maps directly to specific code clauses and calculation methods.
 */
@Serializable
sealed interface DetailedLimitState {
    val category: LimitState

    // --- STRENGTH LIMIT STATES ---
    @Serializable
    data object Flexure : DetailedLimitState {
        override val category = LimitState.STRENGTH
    }

    @Serializable
    data object Shear : DetailedLimitState {
        override val category = LimitState.STRENGTH
    }

    @Serializable
    data object AxialTension : DetailedLimitState {
        override val category = LimitState.STRENGTH
    }

    @Serializable
    data object AxialCompression : DetailedLimitState {
        override val category = LimitState.STRENGTH
    }

    @Serializable
    data object Torsion : DetailedLimitState {
        override val category = LimitState.STRENGTH
    }

    // --- SERVICEABILITY LIMIT STATES ---
    @Serializable
    data class Deflection(val criteria: DeflectionCriteria) : DetailedLimitState {
        override val category = LimitState.SERVICEABILITY
    }

    @Serializable
    data object Vibration : DetailedLimitState {
        override val category = LimitState.SERVICEABILITY
    }

    @Serializable
    data object Cracking : DetailedLimitState { // Crucial for ACI 318 concrete checks
        override val category = LimitState.SERVICEABILITY
    }
}

/**
 * Specific tracking for deflection configurations to feed span-to-deflection limit checks.
 */
@Serializable
enum class DeflectionCriteria {
    ImmediateLiveLoad,  // e.g., L/360
    TotalLongTerm,      // e.g., L/240 (Dead + Live considering creep)
    DeadLoadOnly,
    WindDrift
}