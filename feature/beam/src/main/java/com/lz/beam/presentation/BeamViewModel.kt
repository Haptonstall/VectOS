package com.lz.beam.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lz.beam.domain.BeamCalculationRepository
import com.lz.beam.model.BeamCalculation
import com.lz.beam.model.BeamCalculationInputs
import com.lz.beam.model.BeamCalculationResults
import com.lz.beam.model.SpanBracingInput
import com.lz.data.repository.CodeNotFoundException
import com.lz.data.repository.IStructuralCodeRepository
import com.lz.domain.calculation.CalculationMetadata
import com.lz.domain.material.MaterialRepository
import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.LoadCombinationSet
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.structural.BracingInput
import com.lz.model.structural.BracingResolver
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.DiscreteBracePoint
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
import com.lz.model.structural.StationDemand
import com.lz.model.structural.SpanGeometry
import com.lz.model.structural.StandardLoadCases
import com.lz.model.structural.StrengthDesignResult
import com.lz.model.structural.StructuralMember
import com.lz.model.structural.StructuralNode
import com.lz.model.structural.NodeBoundaryCondition
import com.lz.solver.analysis.UtilizationPoint
import com.lz.model.units.Length
import com.lz.model.units.MomentOfInertia
import com.lz.model.units.UnitSystem
import com.lz.model.units.inches
import com.lz.model.units.lbPerFt
import com.lz.model.units.psiModulus
import com.lz.solver.material.AiscSteelCapacityCalculator
import com.lz.solver.material.NdsWoodCapacityCalculator
import com.lz.solver.envelope.ServiceabilityEvaluationService
import com.lz.beam.solver.BeamAnalysisSolver
import com.lz.beam.solver.BeamAnalysisConfig
import com.lz.beam.runtime.BeamNavigationDestination
import com.lz.data.persistence.room.seeder.DatabaseSeedingCoordinator
import com.lz.domain.project.ActiveProjectProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.plus

/**
 * Modernized ViewModel for Beam Analysis & Design.
 * Orchestrates multi-span geometry, load combinations, and station-by-station validation.
 *
 * Not annotated @HiltViewModel: Hilt cannot see ViewModels declared inside a
 * dynamic-feature module (see RuntimeServicesEntryPoint), so this is constructed
 * manually via BeamViewModelFactory instead of hiltViewModel().
 */
class BeamViewModel @Inject constructor(
    private val activeProjectProvider: ActiveProjectProvider,
    private val beamRepository: BeamCalculationRepository,
    private val structuralRepository: IStructuralCodeRepository,
    private val sectionRepository: SectionRepository,
    private val materialRepository: MaterialRepository,
    private val seedingCoordinator: DatabaseSeedingCoordinator
) : ViewModel() {

    // --- State: Geometry & Properties ---
    var structuralMember by mutableStateOf(
        StructuralMember.createSimple(length = Length(120.0))
    )
    var activeSpanId by mutableStateOf(structuralMember.spans.firstOrNull()?.id)

    // --- State: Selections ---
    var selectedMaterial by mutableStateOf(MaterialType.STEEL)
    var activeMaterialGrade by mutableStateOf<MaterialGrade?>(null)
    var selectedSection by mutableStateOf<SectionProfile?>(null)
    var selectedCombinationSet by mutableStateOf<LoadCombinationSet?>(null)

    // --- State: Loads ---
    var loadCases by mutableStateOf(
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
    private var isLoadingSavedCalculation = false

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
                        governingLimitState = pt.governingLimitState
                    )
                }
            }
        }
    val serviceabilityResults get() = calculationResult?.results?.serviceabilityResults ?: emptyList()

    val detailedStrengthResult: StrengthDesignResult?
        get() {
            val section = selectedSection ?: return null
            val governingDemand = governingDesignPoint()?.demand ?: return null

            val calculator = when (val mat = activeMaterialGrade) {
                is MaterialGrade.Steel -> AiscSteelCapacityCalculator(section, mat)
                is MaterialGrade.Wood  -> NdsWoodCapacityCalculator(section, mat)
                else                   -> null
            } ?: return null

            return calculator.evaluateDetailed(governingDemand, methodology)
        }

    val detailedStrengthCombinationName: String?
        get() = governingDesignPoint()?.combinationName

    private fun loadInitialGeometryData() {
        viewModelScope.launch {
            seedingCoordinator.awaitSeeded()
            if (isLoadingSavedCalculation || currentCalculationId != null) return@launch
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
                seedingCoordinator.awaitSeeded()
                if (isLoadingSavedCalculation || currentCalculationId != null) return@launch

                // Respect the active project's design methodology setting —
                // this must happen before it's used below to pick the
                // default LRFD/ASD combination set.
                methodology = activeProjectProvider.activeProject.value.settings.designMethodology

                // Use the active project's own selected building code — falling
                // back to the app default only if that project hasn't set one
                // or its id no longer resolves (e.g. seed data changed).
                val projectBuildingCodeId = activeProjectProvider.activeProject.value.settings.buildingCodeId
                activeBuildingCode = try {
                    structuralRepository.getBuildingCode(projectBuildingCodeId)
                } catch (e: CodeNotFoundException) {
                    structuralRepository.getDefaultBuildingCode()
                }

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
        calculationJob?.cancel()
        calculationJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                val result = buildCurrentCalculation() ?: return@launch
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
    fun saveCalculation(
        onSaved: (BeamCalculation) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        calculationJob?.cancel()
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val calculation = buildCurrentCalculation()
                    ?: throw IllegalStateException("Calculation inputs are incomplete.")
                beamRepository.saveBeamCalculation(calculation)
                withContext(Dispatchers.Main) {
                    calculationResult = calculation
                    calculationError = null
                    onSaved(calculation)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to save calculation.")
                }
            }
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
                        value = weightValue.lbPerFt,
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

    fun loadCalculation(id: UUID) {
        viewModelScope.launch {
            isLoadingSavedCalculation = true
            try {
                val result = beamRepository.getBeamCalculation(id)
                if (result != null) {
                    seedingCoordinator.awaitSeeded()
                    if (activeBuildingCode == null) {
                        activeBuildingCode = try {
                            structuralRepository.getBuildingCode(
                                result.project.settings.buildingCodeId
                            )
                        } catch (e: CodeNotFoundException) {
                            structuralRepository.getDefaultBuildingCode()
                        }
                    }
                    currentCalculationId = id
                    calculationResult = result
                    structuralMember = result.member
                    restoreInputs(result.inputs, result.member)
                }
            } finally {
                isLoadingSavedCalculation = false
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
                globalBraces.add(NormalizedBraceState(x = currentX.inches, isTopBraced = true, isBotBraced = true))
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
                globalBraces.add(NormalizedBraceState(x = (currentX + spanLen).inches, isTopBraced = true, isBotBraced = true))
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

    private fun governingDesignPoint(): GoverningDesignPoint? {
        val result = calculationResult ?: return null
        val combinationResults = result.results.analysisResult.combinationResults
        val analysisOptions = if (combinationResults.isNotEmpty()) {
            combinationResults.map { (name, analysis) -> name to analysis }
        } else {
            listOf((result.results.analysisResult.governingCombinationName ?: "Governing Envelope") to result.results.analysisResult)
        }

        return analysisOptions.mapNotNull { (combinationName, analysis) ->
            analysis.spanResults.mapNotNull { span ->
                val point = span.utilizationDiagram.maxByOrNull { it.ratio } ?: return@mapNotNull null
                val demand = span.stationDemands.find { it.x == point.x } ?: return@mapNotNull null
                GoverningDesignPoint(
                    combinationName = combinationName,
                    point = point,
                    demand = demand
                )
            }.maxByOrNull { it.point.ratio }
        }.maxByOrNull { it.point.ratio }
    }

    private data class GoverningDesignPoint(
        val combinationName: String,
        val point: UtilizationPoint,
        val demand: StationDemand
    )

    private fun currentInputSnapshot(): BeamCalculationInputs {
        return BeamCalculationInputs(
            loadCases = loadCases,
            selectedMaterial = selectedMaterial,
            activeMaterialGrade = activeMaterialGrade,
            selectedShapeType = selectedShapeType,
            selectedSectionId = selectedSection?.id,
            selectedCombinationSetId = selectedCombinationSet?.id,
            enabledCombinationNames = enabledCombinations,
            selectedAnalysisCombinationName = selectedAnalysisCombination?.name,
            includeSelfWeight = includeSelfWeight,
            methodology = methodology,
            isStrongAxis = isStrongAxis,
            spanBracingInputs = spanBracingInputs.map { (spanId, input) ->
                SpanBracingInput(spanId, input)
            }
        )
    }

    private fun buildCurrentCalculation(): BeamCalculation? {
        val project = activeProjectProvider.activeProject.value
        val code = activeBuildingCode ?: return null
        val combinationSet = selectedCombinationSet ?: return null

        if (currentCalculationId == null) {
            currentCalculationId = UUID.randomUUID()
        }

        val metadata = calculationResult?.metadata?.copy(
            id = currentCalculationId!!,
            createdAt = calculationResult?.metadata?.createdAt ?: LocalDateTime.now()
        ) ?: CalculationMetadata(
            id = currentCalculationId!!,
            toolId = BeamNavigationDestination.id,
            name = "Beam Analysis",
            createdAt = LocalDateTime.now()
        )

        val memberSnapshot = structuralMember.copy(sectionProfileId = selectedSection?.id)
        val inputs = currentInputSnapshot()
        val resolvedLoadCases = if (includeSelfWeight && selectedSection != null) {
            injectSelfWeight(loadCases, selectedSection!!)
        } else {
            loadCases
        }
        val activeCombinations = combinationSet.combinations.filter {
            enabledCombinations.contains(it.name)
        }

        val analysisResult = BeamAnalysisSolver.solve(
            BeamAnalysisConfig(
                member = memberSnapshot,
                loadCases = resolvedLoadCases,
                combinations = activeCombinations,
                modulusOfElasticity = (activeMaterialGrade?.modulusOfElasticity?.psi
                    ?: selectedMaterial.defaultModulusOfElasticityPsi).psiModulus,
                momentOfInertiaX = (selectedSection?.propertiesStrongAxis?.i ?: MomentOfInertia(100.0)),
                momentOfInertiaY = (selectedSection?.propertiesWeakAxis?.i ?: MomentOfInertia(10.0)),
                braceState = getGlobalBraceState(),
                designMethodology = methodology,
                sectionProfile = selectedSection,
                material = activeMaterialGrade,
                buildingCode = code
            )
        )

        val serviceResults = ServiceabilityEvaluationService.evaluate(
            member = memberSnapshot,
            analysisResult = analysisResult,
            buildingCode = code
        )

        val results = BeamCalculationResults(
            analysisResult = analysisResult,
            strengthDesignResults = analysisResult.spanResults.flatMap { span ->
                span.utilizationDiagram.map { pt ->
                    PointCapacityResult(
                        demand = span.stationDemands.find { it.x == pt.x }
                            ?: span.stationDemands.firstOrNull()
                            ?: return@flatMap emptyList<PointCapacityResult>(),
                        utilizationRatio = pt.ratio,
                        designCapacity = pt.capacity,
                        governingLimitState = pt.governingLimitState
                    )
                }
            },
            serviceabilityResults = serviceResults
        )

        return BeamCalculation(
            metadata = metadata,
            project = project,
            member = memberSnapshot,
            results = results,
            inputs = inputs
        )
    }

    private suspend fun restoreInputs(inputs: BeamCalculationInputs, member: StructuralMember) {
        val savedLoadCases = inputs.loadCases.ifEmpty { loadCases }
        loadCases = savedLoadCases
        includeSelfWeight = inputs.includeSelfWeight
        methodology = inputs.methodology
        isStrongAxis = inputs.isStrongAxis
        spanBracingInputs = inputs.spanBracingInputs
            .filter { bracing -> member.spans.any { it.id == bracing.spanId } }
            .associate { it.spanId to it.input }

        selectedMaterial = inputs.selectedMaterial
        availableGrades = materialRepository.getMaterialsByType(selectedMaterial)
        activeMaterialGrade = inputs.activeMaterialGrade
            ?: availableGrades.firstOrNull()

        availableShapeTypes = sectionRepository.getShapeTypes(selectedMaterial)
        selectedShapeType = inputs.selectedShapeType ?: availableShapeTypes.firstOrNull()
        availableSections = selectedShapeType?.let {
            sectionRepository.getSections(selectedMaterial, it)
        } ?: emptyList()
        selectedSection = inputs.selectedSectionId?.let { sectionId ->
            sectionRepository.getSectionById(sectionId)
                ?: availableSections.find { it.id == sectionId }
        } ?: availableSections.firstOrNull()

        selectedCombinationSet = inputs.selectedCombinationSetId?.let { setId ->
            activeBuildingCode?.getCombinationSet(setId)
        } ?: selectedCombinationSet
        enabledCombinations = inputs.enabledCombinationNames.ifEmpty {
            selectedCombinationSet?.combinations?.map { it.name }?.toSet() ?: emptySet()
        }
        selectedAnalysisCombination = inputs.selectedAnalysisCombinationName?.let { name ->
            selectedCombinationSet?.combinations?.find { it.name == name }
        }
    }


    // Must run after every property above is initialized: loadInitialGeometryData()
    // and loadDefaultBuildingCode() both assign mutableStateOf properties declared
    // throughout this class as soon as their coroutines run (Dispatchers.Main.immediate
    // executes synchronously up to the first real suspension point). An init block
    // placed before those declarations would hit their still-null backing MutableState.
    init {
        loadInitialGeometryData()
        loadDefaultBuildingCode()
        observeInputsForAutoRecalculation()
    }

    /**
     * Automatically re-runs calculate() whenever any input it depends on
     * changes, so the user never has to press a Calculate button. Reads
     * every calculate()-relevant field inside snapshotFlow, which tracks
     * Compose state reads and re-emits on any change to any of them.
     * Debounced so rapid successive edits (e.g. typing a load value)
     * collapse into a single recalculation instead of one per keystroke.
     */
    private fun observeInputsForAutoRecalculation() {
        viewModelScope.launch {
            snapshotFlow {
                RecalculationInputs(
                    structuralMember = structuralMember,
                    spanBracingInputs = spanBracingInputs,
                    loadCases = loadCases,
                    includeSelfWeight = includeSelfWeight,
                    selectedSection = selectedSection,
                    selectedMaterial = selectedMaterial,
                    activeMaterialGrade = activeMaterialGrade,
                    selectedCombinationSet = selectedCombinationSet,
                    enabledCombinations = enabledCombinations,
                    methodology = methodology,
                    activeBuildingCode = activeBuildingCode
                )
            }
                .debounce(400)
                .distinctUntilChanged()
                .collect { calculate() }
        }
    }

    /**
     * Snapshot of every input calculate() reads. Equality here determines
     * whether a recalculation is actually triggered — anything calculate()
     * depends on must be included, or edits to it will silently not
     * auto-recalculate.
     */
    private data class RecalculationInputs(
        val structuralMember: StructuralMember,
        val spanBracingInputs: Map<UUID, BracingInput>,
        val loadCases: List<LoadCase>,
        val includeSelfWeight: Boolean,
        val selectedSection: SectionProfile?,
        val selectedMaterial: MaterialType,
        val activeMaterialGrade: MaterialGrade?,
        val selectedCombinationSet: LoadCombinationSet?,
        val enabledCombinations: Set<String>,
        val methodology: DesignMethodology,
        val activeBuildingCode: BuildingCode?
    )
}
