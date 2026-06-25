package com.lz.beam.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.MutableState
import com.lz.beam.model.Assumptions
import com.lz.beam.model.BeamCalculation
import com.lz.beam.model.BeamCalculationResults
import com.lz.data.repository.IStructuralCodeRepository
import com.lz.domain.calculation.CalculationMetadata
import com.lz.domain.material.MaterialRepository
import com.lz.domain.repository.CalculationRepository
import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.LoadCombinationSet
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.structural.BracingInput
import com.lz.model.structural.BracingMode
import com.lz.model.structural.BracingResolver
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.DiscreteBracePoint
import com.lz.model.structural.Flange
import com.lz.model.structural.Load
import com.lz.model.structural.LoadCase
import com.lz.model.structural.LoadDirection
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.MaterialType
import com.lz.model.structural.NormalizedBraceState
import com.lz.model.structural.PointCapacityResult
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.SectionRepository
import com.lz.model.structural.ShapeType
import com.lz.model.structural.SpanGeometry
import com.lz.model.structural.StandardLoadCases
import com.lz.model.structural.StrengthDesignResult
import com.lz.model.structural.StructuralMember
import com.lz.model.structural.StructuralNode
import com.lz.model.structural.NodeBoundaryCondition
import com.lz.model.units.ElasticModulus
import com.lz.model.units.ForcePerLength
import com.lz.model.units.Length
import com.lz.model.units.MomentOfInertia
import com.lz.model.units.UnitSystem
import com.lz.model.units.inInches
import com.lz.model.units.inLbIn
import com.lz.model.units.inPoundsForce
import com.lz.model.units.inPsi
import com.lz.model.units.inches
import com.lz.model.units.psiModulus
import com.lz.solver.capacity.CapacityCalculator
import com.lz.solver.material.AiscSteelCapacityCalculator
import com.lz.solver.material.NdsWoodCapacityCalculator
import com.lz.solver.envelope.ServiceabilityEvaluationService
import com.lz.beam.solver.BeamAnalysisSolver
import com.lz.beam.solver.BeamAnalysisConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID
import kotlin.collections.plus
import kotlin.math.abs
import kotlin.math.max

/**
 * Modernized ViewModel for Beam Analysis & Design.
 * Orchestrates multi-span geometry, load combinations, and station-by-station validation.
 */
class BeamViewModel(
    private val projectViewModel: ProjectViewModel,
    private val calculationRepository: CalculationRepository,
    private val structuralRepository: IStructuralCodeRepository,
    private val sectionRepository: SectionRepository,
    private val materialRepository: MaterialRepository
) : ViewModel() {

    // --- State: Geometry & Properties ---
    var structuralMember by mutableStateOf(
        StructuralMember.createSimple(length = Length(120.0))
    )
    var activeSpanId by mutableStateOf<UUID?>(structuralMember.spans.firstOrNull()?.id)

    // --- State: Selections ---
    var selectedMaterial by mutableStateOf<MaterialType>(MaterialType.STEEL)
    var activeMaterialGrade by mutableStateOf<MaterialGrade?>(null)
    var selectedSection by mutableStateOf<SectionProfile?>(null)
    var selectedCombinationSet by mutableStateOf<LoadCombinationSet?>(null)

    // --- State: Loads ---
    var loadCases by mutableStateOf<List<LoadCase>>(
        listOf(
            LoadCase(StandardLoadCases.DEAD, "Dead Load"),
            LoadCase(StandardLoadCases.LIVE, "Live Load"),
            LoadCase(StandardLoadCases.ROOF_LIVE, "Roof Live Load"),
            LoadCase(StandardLoadCases.SNOW, "Snow Load"),
            LoadCase(StandardLoadCases.WIND, "Wind Load"),
            LoadCase(StandardLoadCases.RAIN, "Rain Load"),
            LoadCase(StandardLoadCases.SEISMIC, "Seismic Load")
        )
    )
    var includeSelfWeight by mutableStateOf(true)
    var selectedLoad by mutableStateOf<Load?>(null)

    var unitSystem by mutableStateOf(UnitSystem.IMPERIAL)
    var activeBuildingCode by mutableStateOf<BuildingCode?>(null)
    var methodology by mutableStateOf(DesignMethodology.LRFD)
    var isStrongAxis by mutableStateOf(true)

    // --- Selection State: Selection Flows ---
    var availableMaterials by mutableStateOf<List<MaterialType>>(emptyList())
        private set
    var availableGrades by mutableStateOf<List<MaterialGrade>>(emptyList())
        private set
    var availableShapeTypes by mutableStateOf<List<ShapeType>>(emptyList())
        private set
    var availableSections by mutableStateOf<List<SectionProfile>>(emptyList())
        private set

    var selectedShapeType by mutableStateOf<ShapeType?>(null)
        private set

    var editingBracingSpanId by mutableStateOf<UUID?>(null)

    var currentCalculationId: UUID? = null

    var calculationResult by mutableStateOf<BeamCalculation?>(null)
        private set
    var calculationError by mutableStateOf<String?>(null)
        private set

    var selectedAnalysisCombination by mutableStateOf<LoadCombination?>(null)
    var enabledCombinations by mutableStateOf<Set<String>>(emptySet())

    var showBracingOnPlot by mutableStateOf(false)
    var showUtilizationOnPlot by mutableStateOf(true)
    var showJointLabels by mutableStateOf(false)
    var showLoadsOnPlot by mutableStateOf(true)

    private var calculationJob: Job? = null

    val governingCapacityResults: List<PointCapacityResult>
        get() {
            val result = calculationResult ?: return emptyList()
            val analysis = if (selectedAnalysisCombination != null) {
                result.results.analysisResult.combinationResults[selectedAnalysisCombination!!.name]
                    ?: result.results.analysisResult
            } else {
                result.results.analysisResult
            }
            // utilizationDiagram is already populated by BeamAnalysisSolver
            return analysis.spanResults.flatMap { span ->
                span.utilizationDiagram.map { pt ->
                    PointCapacityResult(
                        demand = span.stationDemands.find { it.x == pt.x }
                            ?: span.stationDemands.firstOrNull()
                            ?: return@flatMap emptyList<PointCapacityResult>(),
                        utilizationRatio = pt.ratio,
                        designCapacity = pt.capacity,
                        governingLimitState = "See Report"
                    )
                }
            }
        }
    val serviceabilityResults get() = calculationResult?.results?.serviceabilityResults ?: emptyList()

    val detailedStrengthResult: StrengthDesignResult?
        get() {
            val result = calculationResult ?: return null
            val section = selectedSection ?: return null

            val analysis = if (selectedAnalysisCombination != null) {
                result.results.analysisResult.combinationResults[selectedAnalysisCombination!!.name]
                    ?: result.results.analysisResult
            } else {
                result.results.analysisResult
            }

            // Find governing station by utilization ratio
            val governingPoint = analysis.spanResults
                .flatMap { it.utilizationDiagram }
                .maxByOrNull { it.ratio } ?: return null

            val governingDemand = analysis.spanResults
                .flatMap { it.stationDemands }
                .find { it.x == governingPoint.x } ?: return null

            val calculator: CapacityCalculator? = when (val mat = activeMaterialGrade) {
                is MaterialGrade.Steel -> AiscSteelCapacityCalculator(section, mat)
                is MaterialGrade.Wood  -> NdsWoodCapacityCalculator(section, mat)
                else                   -> null
            } ?: return null

            return calculator.evaluateDetailed(governingDemand, methodology)
        }

    private val _calculationHistory = MutableStateFlow<List<CalculationMetadata>>(emptyList())
    val calculationHistory: StateFlow<List<CalculationMetadata>> = _calculationHistory.asStateFlow()

    init {
        loadDefaultBuildingCode()
        loadInitialGeometryData()
        onMaterialSelected(MaterialType.STEEL)

        // Default: Enable all combinations (wait for building code to load)
        viewModelScope.launch {
            snapshotFlow { selectedCombinationSet }.collect { set ->
                if (set != null && enabledCombinations.isEmpty()) {
                    enabledCombinations = set.combinations.map { it.name }.toSet()
                }
            }
        }

        // Sync with Project Settings
        viewModelScope.launch {
            projectViewModel.activeProject.collect { project ->
                methodology = project.designContext.methodology
                unitSystem = project.designContext.units
                activeBuildingCode = project.designContext.buildingCode

                // Refresh combinations for new context
                val bc = project.designContext.buildingCode
                val setId = if (methodology == DesignMethodology.LRFD) bc.defaultLrfdSetId else bc.defaultAsdSetId
                selectedCombinationSet = setId?.let { bc.getCombinationSet(it) }
                    ?: bc.stateSpecificCombinations.firstOrNull { it.methodology == methodology }
                    ?: bc.stateSpecificCombinations.firstOrNull()

                enabledCombinations = selectedCombinationSet?.combinations?.map { it.name }?.toSet() ?: emptySet()
            }
        }

        // Automatic calculation when inputs change
        viewModelScope.launch {
            snapshotFlow {
                listOf(
                    structuralMember,
                    selectedSection,
                    loadCases,
                    includeSelfWeight,
                    activeMaterialGrade,
                    selectedCombinationSet,
                    methodology,
                    activeBuildingCode,
                    isStrongAxis
                )
            }.collect {
                calculate()
            }
        }
    }

    private fun loadInitialGeometryData() {
        viewModelScope.launch {
            availableMaterials = sectionRepository.getMaterials()
            if (availableMaterials.isNotEmpty()) {
                onMaterialSelected(selectedMaterial)
            }
        }
    }

    fun onMaterialSelected(material: MaterialType) {
        selectedMaterial = material
        viewModelScope.launch {
            // Load available grades/species for this material type
            availableGrades = materialRepository.getMaterialsByType(material)
            activeMaterialGrade = availableGrades.firstOrNull()

            availableShapeTypes = sectionRepository.getShapeTypes(material)
            selectedShapeType = availableShapeTypes.firstOrNull()
            selectedShapeType?.let { onShapeTypeSelected(it) }
        }
    }

    fun onGradeSelected(grade: MaterialGrade) {
        activeMaterialGrade = grade
    }

    fun onShapeTypeSelected(shapeType: ShapeType) {
        selectedShapeType = shapeType
        viewModelScope.launch {
            availableSections = sectionRepository.getSections(selectedMaterial, shapeType)
            // Auto-select the first section or W8X10 for steel
            val default = if (selectedMaterial == MaterialType.STEEL && shapeType == ShapeType.WIDE_FLANGE) {
                availableSections.find { it.designation.replace(" ", "").equals("W8X10", ignoreCase = true) } ?: availableSections.firstOrNull()
            } else {
                availableSections.firstOrNull()
            }
            default?.let { onSectionSelected(it) }
        }
    }

    private fun loadDefaultBuildingCode() {
        viewModelScope.launch {
            try {
                activeBuildingCode = structuralRepository.getDefaultBuildingCode()

                activeBuildingCode?.let { bc ->
                    val setId = if (methodology == DesignMethodology.LRFD) bc.defaultLrfdSetId else bc.defaultAsdSetId
                    selectedCombinationSet = setId?.let { bc.getCombinationSet(it) } ?: bc.stateSpecificCombinations.firstOrNull { it.methodology == methodology } ?: bc.stateSpecificCombinations.firstOrNull()

                    // Sync enabled combinations when set changes
                    enabledCombinations = selectedCombinationSet?.combinations?.map { it.name }?.toSet() ?: emptySet()
                }
            } catch (e: Exception) {
                // If DB is not yet seeded, this might fail on first run.
                // calculate() will handle the null building code.
                e.printStackTrace()
            }
        }
    }

    /**
     * Orchestrates the full analysis and design pipeline.
     */
    fun calculate() {
        val project = projectViewModel.activeProject.value
        val code = activeBuildingCode ?: return
        val combinationSet = selectedCombinationSet ?: return

        calculationJob?.cancel()
        calculationJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                if (currentCalculationId == null) {
                    currentCalculationId = UUID.randomUUID()
                }

                val metadata = CalculationMetadata(
                    id = currentCalculationId!!,
                    name = "Beam Analysis",
                    createdAt = LocalDateTime.now()
                )

                // 1. Inject self weight if needed
                val resolvedLoadCases = if (includeSelfWeight && selectedSection != null) {
                    injectSelfWeight(loadCases, selectedSection!!)
                } else {
                    loadCases
                }

                val activeCombinations = combinationSet.combinations.filter { enabledCombinations.contains(it.name) }

                val analysisResult = BeamAnalysisSolver.solve(
                    BeamAnalysisConfig(
                        member = structuralMember,
                        loadCases = resolvedLoadCases,
                        combinations = activeCombinations,
                        modulusOfElasticity = (activeMaterialGrade?.modulusOfElasticity?.psi
                            ?: selectedMaterial.defaultModulusOfElasticityPsi).psiModulus,
                        momentOfInertiaX = (selectedSection?.propertiesStrongAxis?.i ?: MomentOfInertia(100.0)),
                        momentOfInertiaY = (selectedSection?.propertiesWeakAxis?.i ?: MomentOfInertia(10.0)),
                        braceState = getGlobalBraceState(),
                        designMethodology = methodology,
                        sectionProfile = selectedSection,
                        material = activeMaterialGrade
                    )
                )

                // 2. Serviceability Evaluation - separate concern, stays as-is
                val serviceResults = ServiceabilityEvaluationService.evaluate(
                    member = structuralMember,
                    analysisResult = analysisResult,
                    buildingCode = code
                )

                val results = BeamCalculationResults(
                    analysisResult = analysisResult,
                    strengthDesignResults = analysisResult.spanResults.flatMap { it.utilizationDiagram },
                    serviceabilityResults = serviceResults
                )

                val result = BeamCalculation(
                    metadata = metadata,
                    project = project,
                    member = structuralMember,
                    results = results
                )

                withContext(Dispatchers.Main) {
                    calculationResult = result
                    calculationError = null
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    calculationError = e.message ?: "An unknown error occurred during calculation."
                }
                e.printStackTrace()
            }
        }
    }

    /**
     * Explicitly saves the current calculation state to the repository.
     */
    fun saveCalculation(onSuccess: () -> Unit = {}) {
        val result = calculationResult ?: return
        viewModelScope.launch {
            calculationRepository.saveBeamCalculation(result)
            projectViewModel.addCalculationToRegistry(result)
            loadHistory()
            onSuccess()
        }
    }

    // --- UI Event Handlers ---

    fun addSpan() {
        val lastNode = structuralMember.nodes.last()
        val newNode = StructuralNode()
        val newSpan = SpanGeometry(
            length = Length(120.0),
            startNodeId = lastNode.id,
            endNodeId = newNode.id
        )
        structuralMember = structuralMember.copy(
            nodes = structuralMember.nodes + newNode,
            spans = structuralMember.spans + newSpan
        )
        activeSpanId = newSpan.id
    }

    fun removeSpan(id: UUID) {
        val spanToRemove = structuralMember.spans.find { it.id == id } ?: return
        val nodesToRemove = mutableSetOf<UUID>()
        
        // If it's the last span, we might want to keep the start node if it's the only one left, 
        // but typically we just remove the end node of the removed span if it's not shared.
        // In a simple chain, removing a span removes its end node.
        nodesToRemove.add(spanToRemove.endNodeId)

        structuralMember = structuralMember.copy(
            nodes = structuralMember.nodes.filter { it.id !in nodesToRemove },
            spans = structuralMember.spans.filter { it.id != id }
        )
        if (activeSpanId == id) activeSpanId = structuralMember.spans.firstOrNull()?.id
    }

    fun updateSpanLength(id: UUID, length: Length) {
        structuralMember = structuralMember.copy(
            spans = structuralMember.spans.map { if (it.id == id) it.copy(length = length) else it }
        )
    }

    var spanBracingInputs by mutableStateOf<Map<UUID, BracingInput>>(emptyMap())

    fun updateSpanBracing(id: UUID, input: BracingInput) {
        spanBracingInputs = spanBracingInputs + (id to input)
        // Resolve segments and update SpanGeometry.unbracedSegments
        val span = structuralMember.spans.find { it.id == id } ?: return
        val segments = BracingResolver.resolveSegments(input, span.length)
        structuralMember = structuralMember.copy(
            spans = structuralMember.spans.map {
                if (it.id == id) it.copy(unbracedSegments = segments) else it
            }
        )
    }

    fun onSectionSelected(section: SectionProfile?) {
        selectedSection = section
    }

    fun updateOrientation(isStrong: Boolean) {
        isStrongAxis = isStrong
    }

    // --- Support Management ---

    var editingSupportNodeIndex by mutableStateOf<Int?>(null)

    fun updateBoundaryCondition(nodeIndex: Int, condition: NodeBoundaryCondition) {
        val nodes = structuralMember.nodes.toMutableList()
        if (nodeIndex >= 0 && nodeIndex < nodes.size) {
            nodes[nodeIndex] = nodes[nodeIndex].copy(boundaryCondition = condition)
            structuralMember = structuralMember.copy(nodes = nodes)
            calculate()
        }
    }

    // --- Load Management ---

    fun onAddLoad(caseId: String, load: Load) {
        loadCases = loadCases.map {
            if (it.id == caseId) it.copy(loads = it.loads + load) else it
        }
    }

    fun onRemoveLoad(caseId: String, loadId: UUID) {
        loadCases = loadCases.map {
            if (it.id == caseId) it.copy(loads = it.loads.filter { load -> load.id != loadId }) else it
        }
        if (selectedLoad?.id == loadId) selectedLoad = null
    }

    fun onSelectLoad(caseId: String, loadId: UUID) {
        selectedLoad = loadCases.find { it.id == caseId }?.loads?.find { it.id == loadId }
    }

    private fun injectSelfWeight(cases: List<LoadCase>, section: SectionProfile): List<LoadCase> {
        val density = activeMaterialGrade?.densityPcf ?: selectedMaterial.defaultDensityPcf
        val weightValue = section.getWeightLbFt(density)
        return cases.map { case ->
            if (case.id == StandardLoadCases.DEAD) {
                val selfWeightLoads = structuralMember.spans.map { span ->
                    Load.UniformDistributedLoad(
                        value = ForcePerLength(weightValue),
                        spanId = span.id,
                        locationStart = 0.0.inches,
                        locationEnd = span.length,
                        category = LoadCategory.DEAD,
                        direction = LoadDirection.VERTICAL_DOWN,
                        id = UUID.nameUUIDFromBytes("self_weight_${span.id}".toByteArray())
                    )
                }
                case.copy(loads = case.loads + selfWeightLoads)
            } else case
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
            val result = calculationRepository.getBeamCalculation(id)
            if (result != null) {
                currentCalculationId = id
                calculationResult = result
                structuralMember = result.member
            }
        }
    }

    fun toggleCombination(name: String) {
        enabledCombinations = if (enabledCombinations.contains(name)) {
            enabledCombinations - name
        } else {
            enabledCombinations + name
        }
        calculate()
    }

    /**
     * Converts per-span bracing definitions into a global member brace state.
     */
    private fun getGlobalBraceState(): List<NormalizedBraceState> {
        val globalBraces = mutableListOf<NormalizedBraceState>()
        var currentX = 0.0

        structuralMember.spans.forEach { span ->
            val spanLen = span.length.inches
            val input = spanBracingInputs[span.id]

            val startNode = structuralMember.nodes.find { it.id == span.startNodeId }
            val endNode = structuralMember.nodes.find { it.id == span.endNodeId }

            if (startNode?.boundaryCondition?.isConstrained() == true) {
                globalBraces.add(NormalizedBraceState(currentX.inches, true, true))
            }

            // Extract discrete points from BracingInput if available
            val discretePoints: List<DiscreteBracePoint> = when (input) {
                is BracingInput.Steel    -> input.discreteTable
                is BracingInput.Wood     -> input.discreteTable
                is BracingInput.Aluminum -> input.discreteTable
                is BracingInput.Masonry  -> input.discreteTable
                null                     -> emptyList()
            }
            discretePoints.forEach { point ->
                globalBraces.add(NormalizedBraceState(
                    x           = (currentX + point.x.inches).inches,
                    isTopBraced = point.isTopBraced,
                    isBotBraced = point.isBottomBraced
                ))
            }

            if (endNode?.boundaryCondition?.isConstrained() == true) {
                globalBraces.add(NormalizedBraceState((currentX + spanLen).inches, true, true))
            }

            currentX += spanLen
        }

        return globalBraces.groupBy { it.x.inches }.map { (x, braces) ->
            NormalizedBraceState(
                x           = x.inches,
                isTopBraced = braces.any { it.isTopBraced },
                isBotBraced = braces.any { it.isBotBraced }
            )
        }.sortedBy { it.x.inches }
    }
}