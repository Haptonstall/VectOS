package com.lz.vectos.application.repository

import com.lz.vectos.domain.units.UnitSystem
import kotlinx.coroutines.flow.Flow

/**
 * Repository for application-level settings.
 */
interface SettingsRepository {
    val unitSystem: Flow<UnitSystem>
    suspend fun setUnitSystem(unitSystem: UnitSystem)
}
