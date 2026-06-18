package com.lz.vectos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lz.data.repository.IStructuralCodeRepository
import com.lz.model.structural.MaterialType
import com.lz.domain.project.Project
import com.lz.domain.repository.CalculationRepository
import com.lz.model.structural.ProjectDesignContext
import com.lz.model.structural.DesignMethodology
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.units.UnitSystem
import com.lz.domain.repository.ProjectRepository
import com.lz.model.regulatory.codes.Standard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

import com.lz.vectos.domain.calculation.ProjectCalculationRegistry
import com.lz.vectos.domain.calculation.EngineeringCalculation
import com.lz.vectos.domain.provenance.CalculationProvenance
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

class ProjectViewModel(
    private val projectRepository: ProjectRepository,
    private val calculationRepository: CalculationRepository,
    private val structuralRepository: IStructuralCodeRepository,
    private val calculationRegistry: ProjectCalculationRegistry
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    private val _buildingCodes = MutableStateFlow<List<BuildingCode>>(emptyList())
    val buildingCodes: StateFlow<List<BuildingCode>> = _buildingCodes.asStateFlow()

    private val _standards = MutableStateFlow<List<Standard>>(emptyList())
    val standards: StateFlow<List<Standard>> = _standards.asStateFlow()

    private val _activeProject = MutableStateFlow(createDefaultProject())
    val activeProject: StateFlow<Project> = _activeProject

    val calculations: StateFlow<Map<UUID, EngineeringCalculation>> = calculationRegistry.calculations

    init {
        loadProjects()
        loadStructuralData()
        observeActiveProject()
    }

    private fun observeActiveProject() {
        viewModelScope.launch {
            _activeProject.collectLatest { project ->
                loadCalculationsForActiveProject(project.id)
            }
        }
    }

    private fun loadCalculationsForActiveProject(projectId: UUID) {
        viewModelScope.launch {
            val metadataList = calculationRepository.getCalculationsForProject(projectId)
            // Ensure we don't process duplicate metadata entries (possible DB anomalies)
            val distinctMetadata = metadataList.distinctBy { it.id }
            calculationRegistry.clear()
            distinctMetadata.forEach { metadata ->
                // Fetch the full calculation to populate the registry with latest version info
                val beamCalc = calculationRepository.getBeamCalculation(metadata.id)
                if (beamCalc != null) {
                    addCalculationToRegistry(beamCalc)
                }
            }
        }
    }

    fun addCalculationToRegistry(beamCalc: BeamCalculation) {
        val engineeringCalc = EngineeringCalculation(
            id = beamCalc.metadata.id,
            projectId = _activeProject.value.id,
            toolId = "BEAM",
            name = beamCalc.metadata.name,
            latestVersion = CalculationVersion(
                versionNumber = 1,
                createdAt = beamCalc.metadata.createdAt.toString(),
                summaryNote = "Initial Save",
                provenance = CalculationProvenance(
                    timestamp = beamCalc.metadata.createdAt.toString(),
                    projectId = _activeProject.value.id.toString(),
                    calculatorId = "BEAM",
                    buildingCode = _activeProject.value.designContext.buildingCode.shortName,
                    unitSystem = _activeProject.value.designContext.units.name,
                    sectionDesignation = beamCalc.member.spans.firstOrNull()?.let { "Beam" }
                        ?: "Unknown",
                    spanLength = beamCalc.member.spans.sumOf { it.length.inches }.toString(),
                    loadCasesSummary = "",
                    assumptions = emptyList(),
                    acknowledgments = emptyList()
                )
            ),
            versionHistory = emptyList(),
            createdAt = beamCalc.metadata.createdAt,
            updatedAt = java.time.LocalDateTime.now()
        )
        calculationRegistry.updateCalculation(engineeringCalc)
    }

    fun deleteCalculation(id: UUID) {
        viewModelScope.launch {
            try {
                calculationRepository.deleteCalculation(id)
                calculationRegistry.removeCalculation(id)
                // Refresh calculations from repository to ensure UI reflects DB state
                loadCalculationsForActiveProject(_activeProject.value.id)
            } catch (e: Exception) {
                // Log the error and ensure registry is updated if possible
                e.printStackTrace()
                calculationRegistry.removeCalculation(id)
                // Still attempt a refresh in case DB state diverged
                loadCalculationsForActiveProject(_activeProject.value.id)
            }
        }
    }

    private fun loadStructuralData() {
        viewModelScope.launch {
            _buildingCodes.value = structuralRepository.getAllBuildingCodes()
            _standards.value = structuralRepository.getAllStandards()
        }
    }

    fun loadProjects() {
        viewModelScope.launch {
            val dbProjects = projectRepository.getAllProjects()
            _projects.value = dbProjects
            
            // Ensure default project exists if none - wait for structural data if possible
            if (dbProjects.isEmpty()) {
                val codes = structuralRepository.getAllBuildingCodes()
                if (codes.isNotEmpty()) {
                    val defaultCode = structuralRepository.getDefaultBuildingCode()
                    val default = createDefaultProject().copy(
                        designContext = ProjectDesignContext(
                            units = UnitSystem.IMPERIAL,
                            methodology = DesignMethodology.ASD,
                            buildingCode = defaultCode,
                            loadingStandard = defaultCode.standards.firstOrNull() ?: Standard(id = "EMPTY", shortName = "None", longName = "None"),
                            materialStandards = emptyMap()
                        )
                    )
                    projectRepository.saveProject(default)
                    _projects.value = listOf(default)
                }
            }
        }
    }

    fun createProject(
        name: String, 
        projectNumber: String?,
        siteLocation: String?,
        description: String?, 
        client: String?, 
        engineer: String?,
        units: UnitSystem,
        methodology: DesignMethodology,
        buildingCode: BuildingCode,
        loadingStandard: Standard,
        materialStandards: Map<MaterialType, Standard>
    ) {
        viewModelScope.launch {
            val project = Project(
                id = UUID.randomUUID(),
                name = name,
                projectNumber = projectNumber,
                siteLocation = siteLocation,
                description = description,
                clientName = client,
                engineerName = engineer,
                createdAt = LocalDateTime.now(),
                designContext = ProjectDesignContext(
                    units = units,
                    methodology = methodology,
                    buildingCode = buildingCode,
                    loadingStandard = loadingStandard,
                    materialStandards = materialStandards
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

    private fun createDefaultProject(): Project {
        return Project(
            id = UUID.fromString("00000000-0000-0000-0000-000000000000"), // Static ID for default
            name = "Default Project",
            description = "A temporary project for calculations.",
            clientName = "Internal",
            engineerName = "Default User",
            createdAt = LocalDateTime.now(),
            designContext = ProjectDesignContext.empty()
        )
    }

    fun updateProjectSettings(
        units: UnitSystem,
        methodology: DesignMethodology,
        buildingCode: BuildingCode,
        loadingStandard: Standard,
        materialStandards: Map<MaterialType, Standard>
    ) {
        val current = _activeProject.value
        
        val updated = current.copy(
            designContext = ProjectDesignContext(
                units = units,
                methodology = methodology,
                buildingCode = buildingCode,
                loadingStandard = loadingStandard,
                materialStandards = materialStandards
            )
        )
        _activeProject.value = updated
        viewModelScope.launch {
            projectRepository.saveProject(updated)
            loadProjects()
        }
    }

    fun setActiveProject(project: Project) {
        _activeProject.value = project
    }
}
