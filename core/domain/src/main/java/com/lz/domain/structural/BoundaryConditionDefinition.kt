package com.lz.domain.structural

import com.lz.model.structural.NodeBoundaryCondition

data class BoundaryConditionDefinition(
    val preset: BoundaryConditionPreset,
    val name: String,
    val description: String,
    val condition: NodeBoundaryCondition
)