package com.lz.model.structural

import kotlinx.serialization.Serializable

@Serializable
data class NodeBoundaryCondition(

    val ux: DofConstraint = DofConstraint(),
    val uy: DofConstraint = DofConstraint(),
    val uz: DofConstraint = DofConstraint(),

    val rx: DofConstraint = DofConstraint(),
    val ry: DofConstraint = DofConstraint(),
    val rz: DofConstraint = DofConstraint()
)