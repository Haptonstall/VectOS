package com.lz.vectos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lz.data.repository.IStructuralCodeRepository
import com.lz.domain.calculation.CalculationMetadata
import com.lz.domain.project.ActiveProjectProvider
import com.lz.domain.project.GeographicCoordinates
import com.lz.domain.project.Project
import com.lz.domain.project.ProjectSettings
import com.lz.domain.repository.CalculationRepository
import com.lz.domain.repository.ProjectRepository
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.structural.DesignMethodology
import com.lz.model.units.UnitSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val calculationRepository: CalculationRepository,
    private val activeProjectProvider: ActiveProjectProvider,
    private val structuralRepository: IStructuralCodeRepository
) : ViewModel() {

    // -------------------------------------------------------------------------
    // Projects
    // -------------------------------------------------------------------------

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    // -------------------------------------------------------------------------
    // Reference Data
    // -------------------------------------------------------------------------

    private val _buildingCodes = MutableStateFlow<List<BuildingCode>>(emptyList())
    val buildingCodes: StateFlow<List<BuildingCode>> = _buildingCodes.asStateFlow()

    // -------------------------------------------------------------------------
    // Active Project
    // -------------------------------------------------------------------------

    val activeProject: StateFlow<Project>
        get() = activeProjectProvider.activeProject

    // -------------------------------------------------------------------------
    // Calculations
    // -------------------------------------------------------------------------

    private val _calculations =
        MutableStateFlow<List<CalculationMetadata>>(emptyList())

    val calculations: StateFlow<List<CalculationMetadata>> =
        _calculations.asStateFlow()

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------

    init {
        refreshProjects()
        loadReferenceData()
    }

    private fun loadReferenceData() {
        viewModelScope.launch {
            _buildingCodes.value = structuralRepository.getAllBuildingCodes()
        }
    }

    // -------------------------------------------------------------------------
    // Project Loading
    // -------------------------------------------------------------------------

    fun refreshProjects() {

        viewModelScope.launch {

            _projects.value =
                projectRepository.getAllProjects()

        }

    }

    fun refreshCalculations() {

        val project =
            activeProject.value

        viewModelScope.launch {

            _calculations.value =
                calculationRepository
                    .getCalculationsForProject(project.id)

        }

    }

    // -------------------------------------------------------------------------
    // Active Project
    // -------------------------------------------------------------------------

    fun setActiveProject(
        project: Project
    ) {

        /*
         * ActiveProjectProvider implementation is responsible
         * for updating the StateFlow.
         *
         * If your provider exposes a setter, call it here.
         */

        activeProjectProvider.setActiveProject(project)

        refreshCalculations()

    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    fun saveProject(
        project: Project
    ) {

        viewModelScope.launch {

            projectRepository.saveProject(project)

            refreshProjects()

        }

    }

    fun createProject(
        name: String,
        projectNumber: String = "",
        description: String = "",
        client: String = "",
        engineer: String = "",
        firmName: String = "",
        streetAddress: String = "",
        city: String = "",
        state: String = "",
        zipCode: String = "",
        units: UnitSystem = UnitSystem.IMPERIAL,
        methodology: DesignMethodology = DesignMethodology.ASD,
        buildingCode: BuildingCode
    ) {
        val project = Project(
            name = name,
            projectNumber = projectNumber,
            description = description,
            clientName = client,
            engineerName = engineer,
            firmName = firmName,
            createdAt = LocalDateTime.now(),
            settings = ProjectSettings(
                unitSystem = units,
                designMethodology = methodology,
                buildingCode = com.lz.model.regulatory.PrimaryBuildingCode.valueOf(buildingCode.id)
            ),
            coordinates = GeographicCoordinates(
                streetAddress = streetAddress,
                city = city,
                state = state,
                zipCode = zipCode
            )
        )
        saveProject(project)
    }

    fun updateProject(project: Project) {
        saveProject(project)
    }

    fun updateProjectSettings(
        units: UnitSystem,
        methodology: DesignMethodology,
        buildingCode: BuildingCode
    ) {
        val currentProject = activeProject.value
        val updatedProject = currentProject.copy(
            settings = currentProject.settings.copy(
                unitSystem = units,
                designMethodology = methodology,
                buildingCode = com.lz.model.regulatory.PrimaryBuildingCode.valueOf(buildingCode.id)
            )
        )
        updateProject(updatedProject)
        // Also update the active project state in the provider
        setActiveProject(updatedProject)
    }

    fun deleteProject(
        project: Project
    ) {

        viewModelScope.launch {

            projectRepository.deleteProject(project.id)

            refreshProjects()

        }

    }

    // -------------------------------------------------------------------------
    // Calculations
    // -------------------------------------------------------------------------

    fun deleteCalculation(
        metadata: CalculationMetadata
    ) {

        viewModelScope.launch {

            calculationRepository
                .deleteCalculation(metadata.id)

            refreshCalculations()

        }

    }

    fun reload() {

        refreshProjects()

        refreshCalculations()

    }
}