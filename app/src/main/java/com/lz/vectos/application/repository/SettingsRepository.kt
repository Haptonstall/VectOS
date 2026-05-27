package com.lz.vectos.application.repository

import com.lz.vectos.domain.units.UnitSystem
import com.lz.vectos.domain.structural.DesignMethodology
import kotlinx.coroutines.flow.Flow

/**
 * Repository for application-level settings.
 */
interface SettingsRepository {
    val unitSystem: Flow<UnitSystem>
    val designMethodology: Flow<DesignMethodology>
    suspend fun setUnitSystem(unitSystem: UnitSystem)
    suspend fun setDesignMethodology(methodology: DesignMethodology)
}
