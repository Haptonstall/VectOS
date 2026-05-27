package com.lz.vectos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lz.vectos.application.repository.SettingsRepository
import com.lz.vectos.domain.units.UnitSystem
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
