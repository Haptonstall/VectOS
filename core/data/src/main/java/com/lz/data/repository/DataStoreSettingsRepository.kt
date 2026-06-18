package com.lz.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lz.domain.repository.SettingsRepository
import com.lz.model.structural.DesignMethodology
import com.lz.model.units.UnitSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreSettingsRepository(private val context: Context) : SettingsRepository {

    private object Keys {
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val DESIGN_METHODOLOGY = stringPreferencesKey("design_methodology")
    }

    override val unitSystem: Flow<UnitSystem> = context.dataStore.data
        .map { prefs ->
            UnitSystem.valueOf(prefs[Keys.UNIT_SYSTEM] ?: UnitSystem.IMPERIAL.name)
        }

    override val designMethodology: Flow<DesignMethodology> = context.dataStore.data
        .map { prefs ->
            DesignMethodology.valueOf(prefs[Keys.DESIGN_METHODOLOGY] ?: DesignMethodology.ASD.name)
        }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        context.dataStore.edit { it[Keys.UNIT_SYSTEM] = unitSystem.name }
    }

    override suspend fun setDesignMethodology(methodology: DesignMethodology) {
        context.dataStore.edit { it[Keys.DESIGN_METHODOLOGY] = methodology.name }
    }
}