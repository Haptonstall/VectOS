package com.lz.vectos.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lz.domain.repository.SettingsRepository
import com.lz.model.units.UnitSystem
import com.lz.model.structural.DesignMethodology
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreSettingsRepository(private val context: Context) : SettingsRepository {

    private object PreferencesKeys {
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val DESIGN_METHODOLOGY = stringPreferencesKey("design_methodology")
    }

    override val unitSystem: Flow<UnitSystem> = context.dataStore.data
        .map { preferences ->
            val systemName = preferences[PreferencesKeys.UNIT_SYSTEM] ?: UnitSystem.METRIC.name
            UnitSystem.valueOf(systemName)
        }

    override val designMethodology: Flow<DesignMethodology> = context.dataStore.data
        .map { preferences ->
            val methodologyName = preferences[PreferencesKeys.DESIGN_METHODOLOGY] ?: DesignMethodology.ASD.name
            DesignMethodology.valueOf(methodologyName)
        }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UNIT_SYSTEM] = unitSystem.name
        }
    }

    override suspend fun setDesignMethodology(methodology: DesignMethodology) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DESIGN_METHODOLOGY] = methodology.name
        }
    }
}
