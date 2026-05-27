package com.lz.vectos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lz.vectos.application.repository.CalculationRepository
import com.lz.vectos.domain.beam.*
import com.lz.vectos.domain.calculation.CalculationMetadata
import com.lz.vectos.domain.structural.*
import com.lz.vectos.domain.units.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID

import com.lz.vectos.repository.IStructuralCodeRepository

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
        StructuralMember.createSimple(length = Length(120.0), startSupport = SupportCondition.PINNED, endSupport = SupportCondition.ROLLER)
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

    // Derived properties for UI
    val strengthDesignResults: List<PointCapacityResult>
        get() {
            val result = calculationResult ?: return emptyList()
            val section = selectedSection ?: return emptyList()
            val mat = activeMaterialGrade ?: return emptyList()
            
            val calculator: CapacityCalculator? = when {
                mat is MaterialGrade.Steel && section is SectionProfile -> 
                    com.lz.vectos.domain.structural.aisc.AiscSteelCapacityCalculator(section, mat)
                mat is MaterialGrade.Wood && section is SectionProfile ->
                    com.lz.vectos.domain.structural.nds.NdsWoodCapacityCalculator(section, mat)
                else -> null
            }
            
            val analysis = if (selectedAnalysisCombination != null) {
                result.results.analysisResult.combinationResults[selectedAnalysisCombination!!.name] ?: result.results.analysisResult
            } else {
                result.results.analysisResult
            }
            
            return calculator?.evaluateAll(analysis, methodology) ?: emptyList()
        }
    val serviceabilityResults get() = calculationResult?.results?.serviceabilityResults ?: emptyList()

    val detailedStrengthResult: StrengthDesignResult?
        get() {
            val result = calculationResult ?: return null
            val section = selectedSection ?: return null
            val mat = activeMaterialGrade ?: return null
            
            val calculator: CapacityCalculator? = when (mat) {
                is MaterialGrade.Steel -> com.lz.vectos.domain.structural.aisc.AiscSteelCapacityCalculator(section, mat)
                is MaterialGrade.Wood -> com.lz.vectos.domain.structural.nds.NdsWoodCapacityCalculator(section, mat)
                else -> null
            }

            if (calculator == null) return null
            
            // Get demands for the selected combination (or governing envelope)
            val analysis = if (selectedAnalysisCombination != null) {
                result.results.analysisResult.combinationResults[selectedAnalysisCombination!!.name] ?: result.results.analysisResult
            } else {
                result.results.analysisResult
            }
            
            val demands = analysis.spanResults.flatMap { it.stationDemands }
            if (demands.isEmpty()) return null
            
            // Find governing points
            val maxFlexureDemand = demands.maxByOrNull { kotlin.math.max(kotlin.math.abs(it.moment.inLbIn), kotlin.math.abs(it.momentY.inLbIn)) } ?: demands.first()
            val maxShearDemand = demands.maxByOrNull { kotlin.math.max(kotlin.math.abs(it.shear.inPoundsForce), kotlin.math.abs(it.shearY.inPoundsForce)) } ?: demands.first()
            val maxAxialDemand = demands.maxByOrNull { kotlin.math.abs(it.axial.inPoundsForce) } ?: demands.first()
            val maxTorsionDemand = demands.maxByOrNull { kotlin.math.abs(it.torque.inLbIn) } ?: demands.first()
            
            val flexureCheck = calculator.evaluateDetailed(maxFlexureDemand, methodology).momentCheck
            val shearCheck = calculator.evaluateDetailed(maxShearDemand, methodology).shearCheck
            val axialCheck = calculator.evaluateDetailed(maxAxialDemand, methodology).axialCheck
            val torsionCheck = calculator.evaluateDetailed(maxTorsionDemand, methodology).torsionCheck
            
            val lb = if (maxFlexureDemand.compressionFlange == Flange.TOP) maxFlexureDemand.lbTop.inInches else maxFlexureDemand.lbBottom.inInches
            val cb = maxFlexureDemand.cb
            val x = maxFlexureDemand.x.inInches
            val flangeStr = if (maxFlexureDemand.compressionFlange == Flange.TOP) "Top" else "Bottom"
            val E_ksi = mat.modulusOfElasticity.inPsi / 1000.0
            
            return StrengthDesignResult(
                momentCheck = flexureCheck,
                shearCheck = shearCheck,
                axialCheck = axialCheck,
                torsionCheck = torsionCheck,
                methodology = methodology,
                designParameters = mapOf(
                    "E" to String.format(java.util.Locale.US, "%.0f ksi", E_ksi),
                    "Lb ($flangeStr)" to String.format(java.util.Locale.US, "%.2f ft", lb / 12.0),
                    "Governing Station (x)" to String.format(java.util.Locale.US, "%.2f ft", x / 12.0),
                    "Cb" to String.format(java.util.Locale.US, "%.2f", cb)
                )
            )
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

                // 1. Resolve Envelope via LoadResolutionService
                val resolvedLoadCases = if (includeSelfWeight && selectedSection != null) {
                    injectSelfWeight(loadCases, selectedSection!!)
                } else {
                    loadCases
                }

                // Filter combinations based on user selection
                val activeCombinations = combinationSet.combinations.filter { enabledCombinations.contains(it.name) }

                val analysisResult = LoadResolutionService.resolveEnvelope(
                    member = structuralMember,
                    loadCases = resolvedLoadCases,
                    combinations = activeCombinations,
                    modulusOfElasticityPsi = activeMaterialGrade?.modulusOfElasticity?.inPsi ?: selectedMaterial.defaultModulusOfElasticityPsi,
                    momentOfInertiaIn4 = if (isStrongAxis) (selectedSection?.propertiesStrongAxis?.inIn4 ?: 100.0) else (selectedSection?.propertiesWeakAxis?.inIn4 ?: 10.0),
                    braceState = getGlobalBraceState()
                )

                // 2. Perform Strength Evaluation
                val strengthResults = if (selectedSection != null && activeMaterialGrade != null) {
                    val mat = activeMaterialGrade!!
                    val calculator: CapacityCalculator? = when (mat) {
                        is MaterialGrade.Steel -> com.lz.vectos.domain.structural.aisc.AiscSteelCapacityCalculator(selectedSection!!, mat)
                        is MaterialGrade.Wood -> com.lz.vectos.domain.structural.nds.NdsWoodCapacityCalculator(selectedSection!!, mat)
                        else -> null
                    }
                    calculator?.evaluateAll(analysisResult, methodology) ?: emptyList()
                } else emptyList()

                // 3. Perform Serviceability Evaluation
                val serviceResults = ServiceabilityEvaluationService.evaluate(
                    member = structuralMember,
                    analysisResult = analysisResult,
                    buildingCode = code
                )

                val results = BeamCalculationResults(
                    analysisResult = analysisResult,
                    strengthDesignResults = strengthResults,
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
        val newSpan = SpanGeometry(
            length = Length(120.0),
            startSupport = SupportCondition.PINNED,
            endSupport = SupportCondition.ROLLER
        )
        structuralMember = structuralMember.copy(spans = structuralMember.spans + newSpan)
        activeSpanId = newSpan.id
    }

    fun removeSpan(id: UUID) {
        structuralMember = structuralMember.copy(spans = structuralMember.spans.filter { it.id != id })
        if (activeSpanId == id) activeSpanId = structuralMember.spans.firstOrNull()?.id
    }

    fun updateSpanLength(id: UUID, length: Length) {
        structuralMember = structuralMember.copy(
            spans = structuralMember.spans.map { if (it.id == id) it.copy(length = length) else it }
        )
    }

    fun updateSpanBracing(id: UUID, bracing: SpanBracing) {
        structuralMember = structuralMember.copy(
            spans = structuralMember.spans.map { if (it.id == id) it.copy(bracing = bracing) else it }
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

    fun updateSupportCondition(nodeIndex: Int, condition: SupportCondition) {
        val spans = structuralMember.spans.toMutableList()
        if (nodeIndex == 0) {
            spans[0] = spans[0].copy(startSupport = condition)
        } else if (nodeIndex <= spans.size) {
            spans[nodeIndex - 1] = spans[nodeIndex - 1].copy(endSupport = condition)
        }
        structuralMember = structuralMember.copy(spans = spans)
        calculate()
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
            val spanLen = span.length.inInches
            
            // Only add brace at nodes if they are NOT free tips (Cantilevers)
            val isStartBraced = span.startSupport != SupportCondition.FREE
            val isEndBraced = span.endSupport != SupportCondition.FREE

            if (isStartBraced) {
                globalBraces.add(NormalizedBraceState(currentX, true, true))
            }

            // Top Flange Bracing
            if (span.bracing.topType == BracingMode.CONTINUOUS) {
                // Logic in BracingLogic.kt handles Lb=0 for continuous
            }

            // Process Discrete Points for this span
            span.bracing.discretePoints.forEach { point ->
                val absoluteX = currentX + point.x.inInches
                globalBraces.add(
                    NormalizedBraceState(
                        x = absoluteX,
                        isTopBraced = point.isTopBraced,
                        isBotBraced = point.isBottomBraced
                    )
                )
            }

            // Legacy support for the single DISCRETE flag if discretePoints is empty
            if (span.bracing.discretePoints.isEmpty()) {
                if (span.bracing.topType == BracingMode.DISCRETE) {
                    globalBraces.add(NormalizedBraceState(currentX + spanLen / 2.0, true, false))
                }
                if (span.bracing.bottomType == BracingMode.DISCRETE) {
                    globalBraces.add(NormalizedBraceState(currentX + spanLen / 2.0, false, true))
                }
            }

            if (isEndBraced) {
                globalBraces.add(NormalizedBraceState(currentX + spanLen, true, true))
            }
            
            currentX += spanLen
        }
        
        // Merge braces at same X
        return globalBraces.groupBy { it.x }.map { (x, braces) ->
            NormalizedBraceState(
                x = x,
                isTopBraced = braces.any { it.isTopBraced },
                isBotBraced = braces.any { it.isBotBraced }
            )
        }.sortedBy { it.x }
    }
}
