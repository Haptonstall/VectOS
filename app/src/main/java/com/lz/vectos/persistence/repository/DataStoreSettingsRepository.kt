package com.lz.vectos.persistence.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lz.vectos.application.repository.SettingsRepository
import com.lz.vectos.domain.units.UnitSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreSettingsRepository(private val context: Context) : SettingsRepository {

    private object PreferencesKeys {
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
    }

    override val unitSystem: Flow<UnitSystem> = context.dataStore.data
        .map { preferences ->
            val systemName = preferences[PreferencesKeys.UNIT_SYSTEM] ?: UnitSystem.METRIC.name
            UnitSystem.valueOf(systemName)
        }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UNIT_SYSTEM] = unitSystem.name
        }
    }
}
