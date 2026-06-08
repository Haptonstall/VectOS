package com.lz.domain.repository

import com.lz.model.structural.DesignMethodology
import com.lz.model.units.UnitSystem
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