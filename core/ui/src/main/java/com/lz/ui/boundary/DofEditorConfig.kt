package com.lz.ui.boundary

import com.lz.domain.structural.ConstraintType
import com.lz.domain.structural.DegreeOfFreedom

data class DofEditorConfig(
    val editableDofs: Set<DegreeOfFreedom>,
    val allowedConstraintTypes: Set<ConstraintType> =
        setOf(
            ConstraintType.FREE,
            ConstraintType.FIXED
        )
)