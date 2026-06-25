package com.lz.ui.boundary

import com.lz.model.structural.ConstraintType
import com.lz.model.structural.DegreeOfFreedom

data class DofEditorConfig(
    val editableDofs: Set<DegreeOfFreedom>,
    val allowedConstraintTypes: Set<ConstraintType> =
        setOf(
            ConstraintType.FREE,
            ConstraintType.FIXED
        )
)