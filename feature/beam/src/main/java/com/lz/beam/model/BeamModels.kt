package com.lz.beam.model

import com.lz.domain.calculation.CalculationMetadata
import com.lz.domain.project.Project
import com.lz.model.structural.BracingInput
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.LoadCase
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.MaterialType
import com.lz.model.structural.PointCapacityResult
import com.lz.model.structural.ServiceabilityResult
import com.lz.model.structural.ShapeType
import com.lz.model.structural.StructuralMember
import com.lz.model.util.UUIDSerializer
import com.lz.solver.analysis.AnalysisResult
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Encapsulates the complete set of results for a beam calculation.
 */
@Serializable
data class BeamCalculationResults(
    val analysisResult: AnalysisResult,
    val strengthDesignResults: List<PointCapacityResult> = emptyList(),
    val serviceabilityResults: List<ServiceabilityResult> = emptyList()
)

/**
 * States the engineering assumptions used by the [BeamCalculator].
 */
@Serializable
data class Assumptions(
    val linearElastic: Boolean = true,
    val smallDeflection: Boolean = true,
    val discreteStations: Int = 101
)

/**
 * User-editable beam inputs that are not represented by analysis results.
 */
@Serializable
data class BeamCalculationInputs(
    val loadCases: List<LoadCase> = emptyList(),
    val selectedMaterial: MaterialType = MaterialType.STEEL,
    val activeMaterialGrade: MaterialGrade? = null,
    val selectedShapeType: ShapeType? = null,
    val selectedSectionId: String? = null,
    val selectedCombinationSetId: String? = null,
    val enabledCombinationNames: Set<String> = emptySet(),
    val selectedAnalysisCombinationName: String? = null,
    val includeSelfWeight: Boolean = true,
    val methodology: DesignMethodology = DesignMethodology.LRFD,
    val isStrongAxis: Boolean = true,
    val spanBracingInputs: List<SpanBracingInput> = emptyList()
)

@Serializable
data class SpanBracingInput(
    @Serializable(with = UUIDSerializer::class)
    val spanId: UUID,
    val input: BracingInput
)

/**
 * The complete record of a beam calculation.
 */
data class BeamCalculation(
    val metadata: CalculationMetadata,
    val project: Project,
    val member: StructuralMember,
    val results: BeamCalculationResults,
    val assumptions: Assumptions = Assumptions(),
    val inputs: BeamCalculationInputs = BeamCalculationInputs()
)
