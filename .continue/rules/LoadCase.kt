package com.lz.core.domain.structural

/**
 * Represents a specific instance of a basic load case,
 * grouping individual loads that fall under a single load category.
 */
data class LoadCase(
    val id: String,
    val name: String,
    val loads: List<Load> = emptyList()
)
