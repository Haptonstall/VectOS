package com.lz.vectos.export

import java.util.UUID

/**
 * Interface for exporting project calculations.
 * Pure Kotlin, no Android dependencies.
 */
interface CalculationExporter {
    suspend fun exportBeamCalculations(projectId: UUID): ExportResult
}

sealed class ExportResult {
    data class Success(val content: String, val fileName: String) : ExportResult()
    data class BinarySuccess(val content: ByteArray, val fileName: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}
