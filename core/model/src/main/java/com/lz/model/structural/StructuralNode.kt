package com.lz.model.structural

import com.lz.domain.structural.BoundaryConditionFactory
import com.lz.domain.structural.BoundaryConditionPreset
import com.lz.domain.structural.NodeBoundaryCondition
import com.lz.model.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class StructuralNode(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,

    val boundaryCondition: NodeBoundaryCondition =
        BoundaryConditionFactory.fromPreset(
            BoundaryConditionPreset.FREE
        )
)