package com.lz.domain.structural

import com.lz.model.structural.ConstraintType
import com.lz.model.structural.DofConstraint
import com.lz.model.structural.NodeBoundaryCondition

object BoundaryConditionValidator {

    fun validate(
        condition: NodeBoundaryCondition,
    ): List<String> {

        val errors = mutableListOf<String>()

        validateConstraint(condition.ux, "UX", errors)
        validateConstraint(condition.uy, "UY", errors)
        validateConstraint(condition.uz, "UZ", errors)

        validateConstraint(condition.rx, "RX", errors)
        validateConstraint(condition.ry, "RY", errors)
        validateConstraint(condition.rz, "RZ", errors)

        return errors
    }

    private fun validateConstraint(
        constraint: DofConstraint,
        dof: String,
        errors: MutableList<String>
    ) {
        val stiffness = constraint.stiffness

        if (
            (constraint.type == ConstraintType.SPRING) &&
            (stiffness == null || stiffness <= 0.0)
        ) {
            errors +=
                "$dof spring restraint requires positive stiffness."
        }

        if (
            (constraint.type != ConstraintType.SPRING) &&
            (stiffness != null)
        ) {
            errors +=
                "$dof contains stiffness but is not a spring restraint."
        }
    }
}