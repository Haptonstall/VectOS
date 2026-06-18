package com.lz.model.structural

import kotlinx.serialization.Serializable

/**
 * Details of a single code check.
 */
@Serializable
data class CodeCheck(
    val limitState: String = "N/A",
    val demand: Double = 0.0,
    val capacity: Double = 0.0,
    val ratio: Double = 0.0,
    val isServiceability: Boolean = false
)

/**
 * Result of a point-by-point capacity evaluation across all limit states.
 */
@Serializable
data class PointCapacityResult(
    val demand: StationDemand,

    // Detailed Checks
    val flexureCheckX: CodeCheck = CodeCheck(),
    val flexureCheckY: CodeCheck = CodeCheck(),
    val shearCheckX: CodeCheck = CodeCheck(),
    val shearCheckY: CodeCheck = CodeCheck(),
    val axialCheck: CodeCheck = CodeCheck(),
    val torsionCheck: CodeCheck = CodeCheck(),
    val deflectionCheck: CodeCheck = CodeCheck(),
    val interactionCheck: CodeCheck = CodeCheck(),

    // Top-level governing values for easy consumption
    val designCapacity: Double = 0.0,
    val utilizationRatio: Double = 0.0,
    val governingLimitState: String = "N/A",
    val compressionFlange: Flange = Flange.TOP,
    val Lb: Double = 0.0
)
