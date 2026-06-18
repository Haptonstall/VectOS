package com.lz.domain.structural

object BoundaryConditionFactory {

    private val FREE =
        DofConstraint(ConstraintType.FREE)

    private val FIXED =
        DofConstraint(ConstraintType.FIXED)

    fun fromPreset(
        preset: BoundaryConditionPreset
    ): NodeBoundaryCondition {

        return when (preset) {

            BoundaryConditionPreset.FIXED ->
                NodeBoundaryCondition(
                    ux = FIXED,
                    uy = FIXED,
                    uz = FIXED,
                    rx = FIXED,
                    ry = FIXED,
                    rz = FIXED
                )

            BoundaryConditionPreset.PINNED ->
                NodeBoundaryCondition(
                    ux = FIXED,
                    uy = FIXED,
                    uz = FIXED
                )

            BoundaryConditionPreset.ROLLER_X ->
                NodeBoundaryCondition(
                    uy = FIXED,
                    uz = FIXED
                )

            BoundaryConditionPreset.ROLLER_Y ->
                NodeBoundaryCondition(
                    ux = FIXED,
                    uz = FIXED
                )

            BoundaryConditionPreset.ROLLER_Z ->
                NodeBoundaryCondition(
                    ux = FIXED,
                    uy = FIXED
                )

            BoundaryConditionPreset.FREE ->
                NodeBoundaryCondition()

            BoundaryConditionPreset.CUSTOM ->
                NodeBoundaryCondition()
        }
    }
}