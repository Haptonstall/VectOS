package com.lz.beam.model

import com.lz.domain.calculation.CalculationMetadata
import com.lz.domain.project.Project
import com.lz.model.structural.PointCapacityResult
import com.lz.model.structural.ServiceabilityResult
import com.lz.model.structural.StructuralMember
import com.lz.solver.analysis.AnalysisResult
import kotlinx.serialization.Serializable

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
 * The complete record of a beam calculation.
 */
data class BeamCalculation(
    val metadata: CalculationMetadata,
    val project: Project,
    val member: StructuralMember,
    val results: BeamCalculationResults,
    val assumptions: Assumptions = Assumptions()
)