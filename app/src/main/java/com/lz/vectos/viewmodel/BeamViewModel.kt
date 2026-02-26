package com.lz.vectos.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lz.vectos.domain.beam.BeamCalculation
import com.lz.vectos.domain.beam.BeamCalculator
import com.lz.vectos.domain.beam.BeamInputs
import com.lz.vectos.domain.beam.LoadType
import com.lz.vectos.domain.beam.Material
import com.lz.vectos.domain.calculation.CalculationMetadata
import com.lz.vectos.domain.units.*
import com.lz.vectos.application.repository.CalculationRepository
import com.lz.vectos.export.CalculationFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

class BeamViewModel(
    private val projectViewModel: ProjectViewModel,
    private val calculationRepository: CalculationRepository
) : ViewModel() {

    var length by mutableStateOf("5.0")
    var loadValue by mutableStateOf("1000.0")
    var material by mutableStateOf(Material.STEEL)
    var momentOfInertia by mutableStateOf("0.0001")
    var loadType by mutableStateOf(LoadType.POINT_LOAD_MIDSPAN)
    var unitSystem by mutableStateOf(UnitSystem.METRIC)

    var calculationResult by mutableStateOf<BeamCalculation?>(null)
        private set

    /**
     * Exposes a formatted display model derived from the current calculation result.
     * This ensures the UI only deals with Strings and pre-formatted values.
     */
    val displayResult by derivedStateOf {
        calculationResult?.let { 
            val formatted = CalculationFormatter.format(it, unitSystem)
            BeamDisplayModel(
                calculationId = it.metadata.id.toString(),
                maxBendingMoment = formatted.moment,
                maxShear = formatted.shear,
                maxDeflection = formatted.deflection,
                timestamp = formatted.timestamp
            )
        }
    }

    private val _calculationHistory = MutableStateFlow<List<CalculationMetadata>>(emptyList())
    val calculationHistory: StateFlow<List<CalculationMetadata>> = _calculationHistory

    fun calculate() {
        val project = projectViewModel.activeProject.value
        val l = length.toDoubleOrNull() ?: return
        val p = loadValue.toDoubleOrNull() ?: return
        val i = momentOfInertia.toDoubleOrNull() ?: return

        val metadata = CalculationMetadata(
            id = UUID.randomUUID(),
            name = "Beam Calculation - ${loadType.name}",
            createdAt = LocalDateTime.now()
        )

        val inputs = BeamInputs(
            project = project,
            span = UnitConverter.toInternalBase(l, LengthUnit.METERS),
            loadValue = p,
            material = material,
            momentOfInertia = UnitConverter.toInternalBase(i, MomentOfInertiaUnit.METERS_4),
            loadType = loadType,
            unitSystem = unitSystem
        )

        val result = BeamCalculator.calculate(metadata, inputs)
        calculationResult = result

        viewModelScope.launch {
            calculationRepository.saveBeamCalculation(result)
            loadHistory()
        }
    }

    fun loadHistory() {
        val projectId = projectViewModel.activeProject.value.id
        viewModelScope.launch {
            _calculationHistory.value = calculationRepository.getCalculationsForProject(projectId)
        }
    }

    fun loadCalculation(id: UUID) {
        viewModelScope.launch {
            calculationResult = calculationRepository.getBeamCalculation(id)
        }
    }
}
