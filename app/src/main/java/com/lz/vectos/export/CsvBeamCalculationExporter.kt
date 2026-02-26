package com.lz.vectos.export

import com.lz.vectos.application.repository.CalculationRepository
import com.lz.vectos.application.repository.ProjectRepository
import com.lz.vectos.domain.beam.BeamCalculation
import com.lz.vectos.domain.units.UnitSystem
import java.util.UUID

/**
 * CSV implementation of [CalculationExporter] for Beam calculations.
 * Outputs a human-readable CSV string using shared formatting logic.
 */
class CsvBeamCalculationExporter(
    private val projectRepository: ProjectRepository,
    private val calculationRepository: CalculationRepository
) : CalculationExporter {

    override suspend fun exportBeamCalculations(projectId: UUID): ExportResult {
        val project = projectRepository.getProject(projectId) 
            ?: return ExportResult.Error("Project not found")

        val calculations = calculationRepository.getCalculationsForProject(projectId)
        if (calculations.isEmpty()) {
            return ExportResult.Error("No calculations found for project")
        }

        val csvBuilder = StringBuilder()
        
        // Header
        csvBuilder.append("Project Name,Calculation Name,ID,Date,Span,Load Value,Material,Max Moment,Max Shear,Max Deflection\n")

        calculations.forEach { metadata ->
            val calc = calculationRepository.getBeamCalculation(metadata.id)
            if (calc != null) {
                val row = formatRow(project.name, calc)
                csvBuilder.append(row).append("\n")
            }
        }

        return ExportResult.Success(
            content = csvBuilder.toString(),
            fileName = "VectOS_Export_${project.name.replace(" ", "_")}.csv"
        )
    }

    private fun formatRow(projectName: String, calculation: BeamCalculation): String {
        val inputs = calculation.inputs
        // Use the system specified in inputs to maintain consistency with user's context
        val formatted = CalculationFormatter.format(calculation, inputs.unitSystem)
        
        return listOf(
            projectName,
            calculation.metadata.name,
            calculation.metadata.id.toString(),
            formatted.timestamp,
            "${inputs.span.meters} m", // Base units for audit
            inputs.loadValue.toString(),
            inputs.material.name,
            formatted.moment,
            formatted.shear,
            formatted.deflection
        ).joinToString(",") { "\"$it\"" }
    }
}
