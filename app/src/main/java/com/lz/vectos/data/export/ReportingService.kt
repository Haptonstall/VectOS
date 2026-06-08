package com.lz.vectos.data.export

import java.util.UUID

/**
 * Supported report formats for engineering exports.
 */
enum class ReportFormat {
    CSV,
    PDF
}

/**
 * Unified result type for reporting operations.
 * Standardizes output across different formats.
 */
sealed class ReportResult {
    /**
     * Represents a successful export.
     * [content] will be a [String] for CSV or a [ByteArray] for PDF.
     */
    data class Success(
        val content: Any, 
        val fileName: String, 
        val format: ReportFormat
    ) : ReportResult()
    
    data class Failure(val message: String) : ReportResult()
}

/**
 * Orchestrates multi-format reporting by delegating to specific exporters.
 * Pure Kotlin, provides a single entry point for all export operations.
 */
class ReportingService(
    private val csvExporter: CalculationExporter,
    private val pdfExporter: CalculationExporter
) {
    /**
     * Coordinates the export of beam calculations for a project.
     * Returns a unified [ReportResult] containing either formatted text or binary data.
     */
    suspend fun export(projectId: UUID, format: ReportFormat): ReportResult {
        val exportResult = when (format) {
            ReportFormat.CSV -> csvExporter.exportBeamCalculations(projectId)
            ReportFormat.PDF -> pdfExporter.exportBeamCalculations(projectId)
        }

        return when (exportResult) {
            is ExportResult.Success -> ReportResult.Success(
                content = exportResult.content,
                fileName = exportResult.fileName,
                format = format
            )
            is ExportResult.BinarySuccess -> ReportResult.Success(
                content = exportResult.content,
                fileName = exportResult.fileName,
                format = format
            )
            is ExportResult.Error -> ReportResult.Failure(exportResult.message)
        }
    }
}
