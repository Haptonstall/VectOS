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
import com.lz.beam.model.SpanDeflectionOverride
import com.lz.data.repository.CodeNotFoundException
import com.lz.data.repository.IStructuralCodeRepository
import com.lz.domain.calculation.CalculationMetadata
import com.lz.domain.material.MaterialRepository
import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.LoadCombinationSet
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.regulatory.codes.ServiceabilityCriterion
import com.lz.model.regulatory.codes.StandardEdition
import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.regulatory.nds.NdsEdition
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
import com.lz.model.structural.StrengthCheckResult
import com.lz.model.structural.StrengthDesignResult
import com.lz.model.structural.StructuralMember
import com.lz.model.structural.StructuralNode
import com.lz.model.structural.NodeBoundaryCondition
import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodSpecies
import com.lz.solver.analysis.AnalysisResult
import com.lz.solver.analysis.UtilizationPoint
import com.lz.solver.capacity.CapacityCalculator
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.MomentOfInertia
import com.lz.model.units.UnitSystem
import com.lz.model.units.inches
import com.lz.model.units.lbPerFt
import com.lz.model.units.psiModulus
import com.lz.solver.material.AiscSteelCapacityCalculator
import com.lz.solver.material.CodeDataBlockedException
import com.lz.solver.material.NdsCodeData
import com.lz.solver.material.NdsWoodCapacityCalculator
import com.lz.solver.material.WoodPropertyService
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
import kotlin.math.abs

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

    // Manual overrides for the NDS 4.3.1 wood adjustment factors (CD, CM,
    // Ct, CL, CF, Cfu, Ci, Cr, CP, Cb). Default (all 1.0) leaves every
    // factor either unapplied or auto-calculated by the solver (CL/CF/CP).
    // Only meaningful for wood materials — steel/aluminum/masonry calculators
    // never read this.
    var ndsAdjustmentFactors by mutableStateOf(NdsAdjustmentFactors())
        private set

    fun onAdjustmentFactorsChanged(factors: NdsAdjustmentFactors) {
        ndsAdjustmentFactors = factors
    }

    // Prompted once on entry for a brand-new Quick Calc calculation so the
    // ASD/LRFD choice is front-and-center rather than left to silently
    // default to LRFD and only discoverable buried in settings. Suppressed
    // for a calculation launched from within a project — see
    // loadDefaultBuildingCode(), which reverts methodology to that
    // project's own default instead — and suppressed when loading an
    // existing saved calculation instead — see loadCalculation() — since its
    // methodology is already known from what was saved.
    var showMethodologyPrompt by mutableStateOf(true)
        private set

    fun selectMethodology(selected: DesignMethodology) {
        methodology = selected
        showMethodologyPrompt = false
    }

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
    var editingDeflectionSpanId by mutableStateOf<UUID?>(null)

    var currentCalculationId: UUID? = null

    var calculationResult by mutableStateOf<BeamCalculation?>(null)
        private set
    var calculationError by mutableStateOf<String?>(null)
        private set

    // True whenever the current inputs (and the calculationResult they
    // produced) differ from what's actually persisted — i.e. there's
    // something on screen that would be lost by navigating away. Compared
    // against RecalculationInputs snapshots rather than a plain toggle so a
    // freshly-loaded calculation's own auto-recalculation (see
    // observeInputsForAutoRecalculation) doesn't itself get misread as an
    // unsaved edit.
    var hasUnsavedChanges by mutableStateOf(false)
        private set
    private var savedInputsSnapshot: RecalculationInputs? = null

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

    /**
     * Where along the member (inches, global x — the same coordinate the
     * Analysis tab's station picker uses) the user asked the Design tab to
     * show the calculation. Null means "worst case": the governing point for
     * bending / axial / combined checks, and the largest-shear station for
     * shear. Pure view state — changing it doesn't recalculate anything.
     */
    var designLocationInches by mutableStateOf<Double?>(null)
        private set

    fun onDesignLocationChanged(xInches: Double?) {
        designLocationInches = xInches
    }

    /**
     * The NDS edition the project's building code points at (IBC 2015 -> NDS
     * 2015, IBC 2018 / 2021 -> NDS 2018, IBC 2024 -> NDS 2024). Null when the
     * code has no wood standard. The Design tab's detail calculator used to be
     * built without it and so always ran as NDS 2018 regardless of the project.
     */
    val activeNdsEdition: NdsEdition?
        get() = (activeBuildingCode?.defaultMaterialStandards?.get(MaterialType.WOOD)?.edition
            as? StandardEdition.Nds)?.edition

    /** e.g. "IBC 2024" — shown next to the standard edition on the Design tab. */
    val activeBuildingCodeName: String?
        get() = activeBuildingCode?.shortName

    private fun detailCalculator(section: SectionProfile): CapacityCalculator? =
        when (val mat = activeMaterialGrade) {
            is MaterialGrade.Steel -> AiscSteelCapacityCalculator(section, mat, memberIsStrongAxis = isStrongAxis)
            is MaterialGrade.Wood  ->
                if (NdsCodeData.blockReason(activeNdsEdition ?: NdsEdition.NDS_2018, mat.species, mat.grade) != null) null
                else NdsWoodCapacityCalculator(
                    section, mat,
                    adjustmentFactors = ndsAdjustmentFactors,
                    edition = activeNdsEdition ?: NdsEdition.NDS_2018,
                    memberIsStrongAxis = isStrongAxis,
                    defaultMethodology = methodology
                )
            else                   -> null
        }

    val detailedStrengthResult: StrengthDesignResult?
        get() {
            val section = selectedSection ?: return null
            val demand = designPoint()?.demand ?: return null
            val calculator = detailCalculator(section) ?: return null
            return calculator.evaluateDetailed(demand, methodology)
        }

    val detailedStrengthCombinationName: String?
        get() = designPoint()?.combinationName

    /** Global x (inches) of the station the bending / axial / combined cards describe. */
    val detailedStrengthLocationInches: Double?
        get() = designPoint()?.demand?.x?.inches

    /**
     * Shear gets its own station. Shear capacity is a property of the section
     * (it doesn't vary along the member), so the worst case is simply the
     * largest |V| over every enabled combination, span and station — which is
     * not generally where the governing bending point is (a simply supported
     * beam's shear peaks at a support, its moment at midspan). When the user
     * has picked a location, shear is shown there like everything else.
     */
    private fun shearDesignPoint(): ShearDesignPoint? {
        if (designLocationInches != null) {
            designPoint()?.let { return ShearDesignPoint(it.combinationName, it.demand) }
        }
        val result = calculationResult ?: return null
        var best: ShearDesignPoint? = null
        var bestV = -1.0
        for ((name, analysis) in analysisOptions(result)) {
            for (span in analysis.spanResults) {
                for (d in span.stationDemands) {
                    val v = abs(d.shear.pounds)
                    if (v > bestV) { bestV = v; best = ShearDesignPoint(name, d) }
                }
            }
        }
        return best
    }

    val detailedShearCheck: StrengthCheckResult<Force>?
        get() {
            val section = selectedSection ?: return null
            val point = shearDesignPoint() ?: return null
            val calculator = detailCalculator(section) ?: return null
            return calculator.evaluateDetailed(point.demand, methodology).shearCheck
                .copy(governingCombination = point.combinationName)
        }

    val detailedShearLocationInches: Double?
        get() = shearDesignPoint()?.demand?.x?.inches

    /** True when the shear card shows the worst-case station rather than a user-chosen location. */
    val detailedShearIsWorstCase: Boolean
        get() = designLocationInches == null

    /** Span the design point (the user's location, or the governing point) falls in — used to
     *  scope the Design tab's serviceability section to that same span. */
    val governingSpanId: UUID?
        get() = designPoint()?.demand?.spanId

    private fun analysisOptions(result: BeamCalculation): List<Pair<String, AnalysisResult>> {
        val combinationResults = result.results.analysisResult.combinationResults
        return if (combinationResults.isNotEmpty()) {
            combinationResults.map { (name, analysis) -> name to analysis }
        } else {
            listOf(
                (result.results.analysisResult.governingCombinationName ?: "Governing Envelope") to
                    result.results.analysisResult
            )
        }
    }

    /** The point the bending / axial / combined cards describe: the user's location if set, else the governing point. */
    private fun designPoint(): GoverningDesignPoint? {
        val loc = designLocationInches ?: return governingDesignPoint()
        return pointAtLocation(loc) ?: governingDesignPoint()
    }

    /**
     * The design point nearest [xInches] — across every combination and span,
     * the utilization-diagram station closest to that location, and where
     * several combinations land on the same station, the one that governs
     * there. (Stations are the solver's sample points; the nearest is used,
     * not an interpolation, so every force on the card is a real solver value.)
     */
    private fun pointAtLocation(xInches: Double): GoverningDesignPoint? {
        val result = calculationResult ?: return null
        data class Candidate(val dist: Double, val point: GoverningDesignPoint)
        val candidates = mutableListOf<Candidate>()
        for ((name, analysis) in analysisOptions(result)) {
            for (span in analysis.spanResults) {
                val pt = span.utilizationDiagram.minByOrNull { abs(it.x.inches - xInches) } ?: continue
                val demand = span.stationDemands.find { it.x == pt.x } ?: continue
                candidates += Candidate(abs(pt.x.inches - xInches), GoverningDesignPoint(name, pt, demand))
            }
        }
        val minDist = candidates.minOfOrNull { it.dist } ?: return null
        return candidates.filter { it.dist <= minDist + 1e-6 }.maxByOrNull { it.point.point.ratio }?.point
    }

    private data class ShearDesignPoint(val combinationName: String, val demand: StationDemand)

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
        // A wood grade selected under the old shape type (e.g. a solid-sawn
        // species/grade, or a glulam combination symbol from a different
        // species group) is very likely invalid for the new one — clear it
        // rather than silently keep a stale, mismatched grade active. The
        // picker dialog already handles a null currentGrade gracefully.
        if (selectedMaterial == MaterialType.WOOD) {
            val currentWoodGrade = activeMaterialGrade as? MaterialGrade.Wood
            val stillValid = currentWoodGrade != null &&
                currentWoodGrade.grade in WoodGrade.validGradesFor(currentWoodGrade.species) &&
                when (shapeType) {
                    ShapeType.SOLID_RECTANGULAR -> !currentWoodGrade.species.isGlulam
                    ShapeType.GLULAM_WESTERN -> currentWoodGrade.species.isGlulam && currentWoodGrade.species != WoodSpecies.GLULAM_SP_SP
                    ShapeType.GLULAM_SOUTHERN_PINE -> currentWoodGrade.species == WoodSpecies.GLULAM_SP_SP
                    else -> true
                }
            if (!stillValid) {
                activeMaterialGrade = null
            }
        }
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

                // The methodology prompt is only meaningful for Quick Calc,
                // which has no project to inherit a methodology from. A
                // calculation launched from within a project always reverts
                // to that project's own default methodology (set just above)
                // and is never prompted.
                showMethodologyPrompt = activeProjectProvider.isQuickCalcMode.value

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
                val inputsAtResult = currentRecalculationInputs()
                withContext(Dispatchers.Main) {
                    calculationResult = result
                    calculationError = null
                    hasUnsavedChanges = inputsAtResult != savedInputsSnapshot
                }
            } catch (e: CodeDataBlockedException) {
                // Hard block: show no result at all rather than a stale one.
                withContext(Dispatchers.Main) {
                    calculationResult = null
                    calculationError = e.message
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
                val inputsAtSave = currentRecalculationInputs()
                beamRepository.saveBeamCalculation(calculation)
                withContext(Dispatchers.Main) {
                    calculationResult = calculation
                    calculationError = null
                    savedInputsSnapshot = inputsAtSave
                    hasUnsavedChanges = false
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

    /**
     * Per-span deflection criteria override. Absent entry (null lookup) means
     * "use the active building code's serviceabilityCriteria for this span",
     * matching ServiceabilityEvaluationService's fallback.
     */
    var spanDeflectionOverrides by mutableStateOf<Map<UUID, List<ServiceabilityCriterion>>>(emptyMap())

    fun updateSpanDeflectionCriteria(id: UUID, criteria: List<ServiceabilityCriterion>?) {
        spanDeflectionOverrides = if (criteria == null) {
            spanDeflectionOverrides - id
        } else {
            spanDeflectionOverrides + (id to criteria)
        }
    }

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
                    savedInputsSnapshot = currentRecalculationInputs()
                    hasUnsavedChanges = false
                    showMethodologyPrompt = false
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

        return analysisOptions(result).mapNotNull { (combinationName, analysis) ->
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
            },
            spanDeflectionOverrides = spanDeflectionOverrides.map { (spanId, criteria) ->
                SpanDeflectionOverride(spanId, criteria)
            },
            ndsAdjustmentFactors = ndsAdjustmentFactors
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
                // Analysis-element stiffness for the two bending planes. This
                // solver builds a real 6-DOF frame element (i and iy both feed
                // the stiffness matrix — see MemberAnalysisSolver), but only
                // ever applies loads in the vertical (gravity) plane, so "X" is
                // really "whichever axis is actually being bent about" rather
                // than always-strong-axis. Swapped here based on the Geometry
                // tab's orientation toggle so a weak-axis-oriented beam's
                // deflection and (for continuous/multi-span beams) moment
                // distribution are computed with the correct EI — previously
                // this was hardcoded to strong-axis regardless of orientation.
                // The orthogonal ("Y") slot isn't loaded by anything this
                // solver models, so swapping it too is safe/inert either way.
                momentOfInertiaX = (
                    (if (isStrongAxis) selectedSection?.propertiesStrongAxis else selectedSection?.propertiesWeakAxis)?.i
                        ?: MomentOfInertia(100.0)
                    ),
                momentOfInertiaY = (
                    (if (isStrongAxis) selectedSection?.propertiesWeakAxis else selectedSection?.propertiesStrongAxis)?.i
                        ?: MomentOfInertia(10.0)
                    ),
                braceState = getGlobalBraceState(),
                designMethodology = methodology,
                sectionProfile = selectedSection,
                material = activeMaterialGrade,
                buildingCode = code,
                isStrongAxis = isStrongAxis,
                ndsAdjustmentFactors = ndsAdjustmentFactors
            )
        )

        val serviceResults = ServiceabilityEvaluationService.evaluate(
            member = memberSnapshot,
            analysisResult = analysisResult,
            buildingCode = code,
            spanOverrides = spanDeflectionOverrides
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
        spanDeflectionOverrides = inputs.spanDeflectionOverrides
            .filter { override -> member.spans.any { it.id == override.spanId } }
            .associate { it.spanId to it.criteria }
        ndsAdjustmentFactors = inputs.ndsAdjustmentFactors

        selectedMaterial = inputs.selectedMaterial
        availableGrades = materialRepository.getMaterialsByType(selectedMaterial)
        // A wood material saved before a WoodPropertyService table
        // correction (e.g. the E / shear-modulus values that were off by
        // 1000x for every species) carries the old numbers verbatim —
        // this isn't re-derived from species+grade at load time otherwise.
        // Re-resolve against the current tables so restoring an old
        // calculation picks up the fix rather than silently keeping the
        // wrong stiffness.
        activeMaterialGrade = (inputs.activeMaterialGrade as? MaterialGrade.Wood)
            ?.let { WoodPropertyService.refreshed(it) }
            ?: inputs.activeMaterialGrade
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
            snapshotFlow { currentRecalculationInputs() }
                .debounce(400)
                .distinctUntilChanged()
                .collect { calculate() }
        }
    }

    /**
     * Snapshot of every input calculate() reads, taken right now. Shared by
     * the auto-recalculation observer above and by hasUnsavedChanges
     * tracking (calculate/loadCalculation/saveCalculation), which compares
     * this against savedInputsSnapshot to know whether the current on-screen
     * state actually matches what's persisted.
     */
    private fun currentRecalculationInputs() = RecalculationInputs(
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
        activeBuildingCode = activeBuildingCode,
        ndsAdjustmentFactors = ndsAdjustmentFactors
    )

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
        val activeBuildingCode: BuildingCode?,
        val ndsAdjustmentFactors: NdsAdjustmentFactors
    )
}
