package com.lz.model.structural

import com.lz.model.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class StructuralNode(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),

    val boundaryCondition: NodeBoundaryCondition =
        NodeBoundaryCondition()
)