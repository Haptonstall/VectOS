package com.lz.vectos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lz.domain.repository.SettingsRepository
import com.lz.model.units.UnitSystem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val unitSystem: StateFlow<UnitSystem> = settingsRepository.unitSystem
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UnitSystem.IMPERIAL
        )

    fun setUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            settingsRepository.setUnitSystem(unitSystem)
        }
    }
}
