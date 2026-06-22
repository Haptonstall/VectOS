package com.lz.vectos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lz.beam.model.BeamCalculation
import com.lz.data.repository.IStructuralCodeRepository
import com.lz.domain.calculation.CalculationMetadata
import com.lz.domain.project.GeographicCoordinates
import com.lz.domain.project.Project
import com.lz.domain.project.ProjectSettings
import com.lz.domain.project.SeismicHazardData
import com.lz.domain.repository.CalculationRepository
import com.lz.domain.repository.ProjectRepository
import com.lz.model.regulatory.PrimaryBuildingCode
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.regulatory.codes.Standard
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.ProjectDesignContext
import com.lz.model.units.UnitSystem
import com.lz.vectos.domain.calculation.EngineeringCalculation
import com.lz.vectos.domain.calculation.ProjectCalculationRegistry
import com.lz.vectos.domain.provenance.CalculationAssumption
import com.lz.vectos.domain.provenance.CalculationProvenance
import com.lz.vectos.domain.versioning.CalculationVersion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val calculationRepository: CalculationRepository,
    private val structuralRepository: IStructuralCodeRepository,
    private val calculationRegistry: ProjectCalculationRegistry
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _buildingCodes = MutableStateFlow<List<BuildingCode>>(emptyList())
    val buildingCodes: StateFlow<List<BuildingCode>> = _buildingCodes.asStateFlow()

    private val _standards = MutableStateFlow<List<Standard>>(emptyList())
    val standards: StateFlow<List<Standard>> = _standards.asStateFlow()

    private val _activeProject = MutableStateFlow(createDefaultProject())
    val activeProject: StateFlow<Project> = _activeProject.asStateFlow()

    val calculations: StateFlow<Map<UUID, EngineeringCalculation>> = calculationRegistry.calculations

    init {
        loadProjects()
        loadStructuralData()
        observeActiveProject()
    }

    // --- Project loading ---

    fun loadProjects() {
        viewModelScope.launch {
            val dbProjects = projectRepository.getAllProjects()
            _projects.value = dbProjects
            if (dbProjects.isEmpty()) {
                val defaultCode = runCatching { structuralRepository.getDefaultBuildingCode() }
                    .getOrNull()
                val default = createDefaultProject().let { project ->
                    if (defaultCode != null) {
                        project.copy(
                            settings = project.settings.copy(
                                buildingCode = PrimaryBuildingCode.valueOf(defaultCode.id)
                            )
                        )
                    } else project
                }
                projectRepository.saveProject(default)
                _projects.value = listOf(default)
            }
        }
    }

    fun setActiveProject(project: Project) {
        _activeProject.value = project
    }

    private fun observeActiveProject() {
        viewModelScope.launch {
            _activeProject.collectLatest { project ->
                loadCalculationsForProject(project.id)
            }
        }
    }

    // --- Structural reference data ---

    private fun loadStructuralData() {
        viewModelScope.launch {
            _buildingCodes.value = structuralRepository.getAllBuildingCodes()
            _standards.value = structuralRepository.getAllStandards()
        }
    }

    // --- Project CRUD ---

    fun createProject(
        name: String,
        projectNumber: String?,
        description: String?,
        client: String?,
        engineer: String?,
        firmName: String?,
        streetAddress: String?,
        city: String?,
        state: String?,
        zipCode: String?,
        units: UnitSystem,
        methodology: DesignMethodology,
        buildingCode: BuildingCode
    ) {
        viewModelScope.launch {
            val project = Project(
                id = UUID.randomUUID(),
                name = name,
                projectNumber = projectNumber,
                description = description,
                clientName = client,
                engineerName = engineer,
                firmName = firmName,
                createdAt = Instant.now().toString(),
                settings = ProjectSettings(
                    buildingCode = PrimaryBuildingCode.valueOf(buildingCode.id),
                    designMethodology = methodology,
                    unitSystem = units
                ),
                coordinates = GeographicCoordinates(
                    streetAddress = streetAddress ?: "",
                    city = city ?: "",
                    state = state ?: "",
                    zipCode = zipCode ?: ""
                )
            )
            projectRepository.saveProject(project)
            loadProjects()
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            projectRepository.saveProject(project)
            if (_activeProject.value.id == project.id) {
                _activeProject.value = project
            }
            loadProjects()
        }
    }

    fun updateProjectSettings(
        units: UnitSystem,
        methodology: DesignMethodology,
        buildingCode: BuildingCode
    ) {
        val updated = _activeProject.value.copy(
            settings = _activeProject.value.settings.copy(
                unitSystem = units,
                designMethodology = methodology,
                buildingCode = PrimaryBuildingCode.valueOf(buildingCode.id)
            ),
            designContext = ProjectDesignContext(steelOverride = null)
        )
        _activeProject.value = updated
        viewModelScope.launch {
            projectRepository.saveProject(updated)
            loadProjects()
        }
    }

    // --- Calculation registry ---

    private fun loadCalculationsForProject(projectId: UUID) {
        viewModelScope.launch {
            val metadataList = calculationRepository.getCalculationsForProject(projectId)
            calculationRegistry.clear()
            metadataList.distinctBy { it.id }.forEach { metadata ->
                calculationRegistry.updateCalculation(metadata.toEngineeringCalculation(projectId))
            }
        }
    }

    /**
     * Called by BeamViewModel after a successful save to register the full
     * calculation in the project-level registry without re-fetching from DB.
     */
    fun addCalculationToRegistry(beamCalc: BeamCalculation) {
        val projectId = _activeProject.value.id
        val provenance = CalculationProvenance(
            timestamp = beamCalc.metadata.createdAt.toString(),
            projectId = projectId.toString(),
            calculatorId = "BEAM",
            buildingCode = _activeProject.value.settings.buildingCode.name,
            unitSystem = _activeProject.value.settings.unitSystem.name,
            sectionDesignation = "Beam",
            spanLength = beamCalc.member.spans.sumOf { it.length.inches }.toString(),
            loadCasesSummary = "",
            assumptions = emptyList<CalculationAssumption>(),
            acknowledgments = emptyList()
        )
        val version = CalculationVersion(
            versionNumber = 1,
            createdAt = beamCalc.metadata.createdAt.toString(),
            summaryNote = "Initial Save",
            provenance = provenance
        )
        val engineeringCalc = EngineeringCalculation(
            id = beamCalc.metadata.id,
            projectId = projectId,
            toolId = "BEAM",
            name = beamCalc.metadata.name,
            latestVersion = version,
            versionHistory = emptyList(),
            createdAt = beamCalc.metadata.createdAt,
            updatedAt = LocalDateTime.now()
        )
        calculationRegistry.updateCalculation(engineeringCalc)
    }

    fun deleteCalculation(id: UUID) {
        viewModelScope.launch {
            try {
                calculationRepository.deleteCalculation(id)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                calculationRegistry.removeCalculation(id)
                loadCalculationsForProject(_activeProject.value.id)
            }
        }
    }

    // --- Helpers ---

    private fun createDefaultProject(): Project = Project(
        id = UUID.fromString("00000000-0000-0000-0000-000000000000"),
        name = "Default Project",
        description = "A temporary project for calculations.",
        clientName = "Internal",
        engineerName = "Default User",
        createdAt = Instant.now().toString()
    )

    private fun CalculationMetadata.toEngineeringCalculation(projectId: UUID): EngineeringCalculation {
        val provenance = CalculationProvenance(
            timestamp = createdAt.toString(),
            projectId = projectId.toString(),
            calculatorId = "BEAM",
            buildingCode = _activeProject.value.settings.buildingCode.name,
            unitSystem = _activeProject.value.settings.unitSystem.name,
            sectionDesignation = "",
            spanLength = "",
            loadCasesSummary = "",
            assumptions = emptyList(),
            acknowledgments = emptyList()
        )
        val version = CalculationVersion(
            versionNumber = 1,
            createdAt = createdAt.toString(),
            summaryNote = "Loaded from database",
            provenance = provenance
        )
        return EngineeringCalculation(
            id = id,
            projectId = projectId,
            toolId = "BEAM",
            name = name,
            latestVersion = version,
            versionHistory = emptyList(),
            createdAt = createdAt,
            updatedAt = createdAt
        )
    }
}