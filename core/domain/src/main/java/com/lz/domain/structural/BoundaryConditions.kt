package com.lz.domain.structural

import com.lz.model.structural.ConstraintType
import com.lz.model.structural.DegreeOfFreedom
import com.lz.model.structural.DofConstraint
import com.lz.model.structural.NodeBoundaryCondition

object BoundaryConditions {

    val Free =
        BoundaryConditionDefinition(
            preset = BoundaryConditionPreset.FREE,
            name = "Free",
            description = "No translational or rotational restraints.",
            condition = NodeBoundaryCondition()
        )

    val Fixed =
        BoundaryConditionDefinition(
            preset = BoundaryConditionPreset.FIXED,
            name = "Fixed",
            description = "Restrains all translational and rotational degrees of freedom.",
            condition = NodeBoundaryCondition(
                ux = fixed(),
                uy = fixed(),
                uz = fixed(),
                rx = fixed(),
                ry = fixed(),
                rz = fixed()
            )
        )

    val Pinned =
        BoundaryConditionDefinition(
            preset = BoundaryConditionPreset.PINNED,
            name = "Pinned",
            description = "Restrains translations while allowing rotations.",
            condition = NodeBoundaryCondition(
                ux = fixed(),
                uy = fixed(),
                uz = fixed(),
                rx = free(),
                ry = free(),
                rz = free()
            )
        )

    val RollerX =
        BoundaryConditionDefinition(
            preset = BoundaryConditionPreset.ROLLER_X,
            name = "Roller X",
            description = "Restrains translation in X only.",
            condition = NodeBoundaryCondition(
                ux = fixed()
            )
        )

    val RollerY =
        BoundaryConditionDefinition(
            preset = BoundaryConditionPreset.ROLLER_Y,
            name = "Roller Y",
            description = "Restrains translation in Y only.",
            condition = NodeBoundaryCondition(
                uy = fixed()
            )
        )

    val RollerZ =
        BoundaryConditionDefinition(
            preset = BoundaryConditionPreset.ROLLER_Z,
            name = "Roller Z",
            description = "Restrains translation in Z only.",
            condition = NodeBoundaryCondition(
                uz = fixed()
            )
        )

    /**
     * Allows translation only in X.
     * Everything else restrained.
     */
    val GuideX =
        BoundaryConditionDefinition(
            preset = BoundaryConditionPreset.GUIDE_X,
            name = "Guide X",
            description = "Allows translation only in the X direction.",
            condition = NodeBoundaryCondition(
                ux = free(),
                uy = fixed(),
                uz = fixed(),
                rx = fixed(),
                ry = fixed(),
                rz = fixed()
            )
        )

    /**
     * Allows translation only in Y.
     */
    val GuideY =
        BoundaryConditionDefinition(
            preset = BoundaryConditionPreset.GUIDE_Y,
            name = "Guide Y",
            description = "Allows translation only in the Y direction.",
            condition = NodeBoundaryCondition(
                ux = fixed(),
                uy = free(),
                uz = fixed(),
                rx = fixed(),
                ry = fixed(),
                rz = fixed()
            )
        )

    /**
     * Allows translation only in Z.
     */
    val GuideZ =
        BoundaryConditionDefinition(
            preset = BoundaryConditionPreset.GUIDE_Z,
            name = "Guide Z",
            description = "Allows translation only in the Z direction.",
            condition = NodeBoundaryCondition(
                ux = fixed(),
                uy = fixed(),
                uz = free(),
                rx = fixed(),
                ry = fixed(),
                rz = fixed()
            )
        )

    val All: List<BoundaryConditionDefinition> =
        listOf(
            Free,
            Fixed,
            Pinned,
            RollerX,
            RollerY,
            RollerZ,
            GuideX,
            GuideY,
            GuideZ
        )

    fun fromPreset(
        preset: BoundaryConditionPreset
    ): NodeBoundaryCondition =
        definitionFor(preset).condition

    fun definitionFor(
        preset: BoundaryConditionPreset
    ): BoundaryConditionDefinition =
        All.firstOrNull {
            it.preset == preset
        } ?: Free

    fun spring(
        dof: DegreeOfFreedom,
        stiffness: Double
    ): NodeBoundaryCondition =
        NodeBoundaryCondition(
            uy = DofConstraint(
                type = ConstraintType.SPRING,
                stiffness = stiffness
            )
        )

    private fun fixed() =
        DofConstraint(
            type = ConstraintType.FIXED
        )

    private fun free() =
        DofConstraint(
            type = ConstraintType.FREE
        )
}