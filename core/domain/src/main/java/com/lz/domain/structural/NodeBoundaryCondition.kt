package com.lz.domain.structural

data class NodeBoundaryCondition(
    val constraints:
    Map<DegreeOfFreedom, DofConstraint> =
        DegreeOfFreedom.entries.associateWith {
            DofConstraint(ConstraintType.FREE)
        }
) {
    operator fun get(
        dof: DegreeOfFreedom
    ): DofConstraint =
        constraints[dof]
            ?: DofConstraint(ConstraintType.FREE)

    fun withConstraint(
        dof: DegreeOfFreedom,
        constraint: DofConstraint
    ): NodeBoundaryCondition =
        copy(
            constraints =
                constraints + (dof to constraint)
        )
}