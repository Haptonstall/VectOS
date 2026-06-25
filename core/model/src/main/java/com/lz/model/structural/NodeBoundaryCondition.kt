package com.lz.model.structural

import kotlinx.serialization.Serializable

/**
 * Defines the boundary condition for a structural node across all six DOFs.
 * Each DOF is independently constrained via [DofConstraint].
 *
 * Use the companion factory functions for standard structural support presets.
 * Custom conditions are built by constructing this class directly.
 *
 * DOF convention (right-hand rule, member local x-axis along member length):
 *   UX = axial translation
 *   UY = strong-axis transverse translation
 *   UZ = weak-axis transverse translation
 *   RX = torsional rotation
 *   RY = weak-axis bending rotation
 *   RZ = strong-axis bending rotation
 */
@Serializable
data class NodeBoundaryCondition(

    val ux: DofConstraint = DofConstraint(),
    val uy: DofConstraint = DofConstraint(),
    val uz: DofConstraint = DofConstraint(),

    val rx: DofConstraint = DofConstraint(),
    val ry: DofConstraint = DofConstraint(),
    val rz: DofConstraint = DofConstraint()
) {
    companion object {

        private val FIXED = DofConstraint(ConstraintType.FIXED)
        private val FREE  = DofConstraint(ConstraintType.FREE)

        /**
         * Pinned: resists all translations, free to rotate about all axes.
         * Typical interior column base or beam end at a simple support.
         */
        fun pinned() = NodeBoundaryCondition(
            ux = FIXED, uy = FIXED, uz = FIXED,
            rx = FREE,  ry = FREE,  rz = FREE
        )

        /**
         * Roller: resists transverse translation (UY, UZ) only.
         * Free to translate axially and rotate.
         * Typical far end of a simply-supported beam.
         */
        fun roller() = NodeBoundaryCondition(
            ux = FREE,  uy = FIXED, uz = FIXED,
            rx = FREE,  ry = FREE,  rz = FREE
        )

        /**
         * Fixed (cantilever): resists all translations and all rotations.
         * Fully rigid connection to a wall or foundation.
         */
        fun fixed() = NodeBoundaryCondition(
            ux = FIXED, uy = FIXED, uz = FIXED,
            rx = FIXED, ry = FIXED, rz = FIXED
        )

        /**
         * Free: no restraint on any DOF.
         * Used for internal nodes or free ends.
         */
        fun free() = NodeBoundaryCondition(
            ux = FREE, uy = FREE, uz = FREE,
            rx = FREE, ry = FREE, rz = FREE
        )

        /**
         * Pin-roller in strong axis only (2D planar beam simplification).
         * UY fixed, UX and UZ free, all rotations free.
         * Used when modelling planar beams where out-of-plane is not analysed.
         */
        fun rollerStrongAxis() = NodeBoundaryCondition(
            ux = FREE, uy = FIXED, uz = FREE,
            rx = FREE, ry = FREE,  rz = FREE
        )

        /**
         * Spring support on UY only, with the given translational stiffness (kip/in).
         * All other DOFs are free.
         */
        fun springUY(stiffnessKipPerIn: Double) = NodeBoundaryCondition(
            uy = DofConstraint(ConstraintType.SPRING, stiffnessKipPerIn)
        )

        /**
         * Spring support on UZ only, with the given translational stiffness (kip/in).
         */
        fun springUZ(stiffnessKipPerIn: Double) = NodeBoundaryCondition(
            uz = DofConstraint(ConstraintType.SPRING, stiffnessKipPerIn)
        )

        /**
         * Builds a [NodeBoundaryCondition] from a [BoundaryConditionPreset] resolved
         * against a specific [DegreesOfFreedomProfile]. Used by the UI picker.
         */
        fun fromDofConstraints(
            ux: DofConstraint = FREE,
            uy: DofConstraint = FREE,
            uz: DofConstraint = FREE,
            rx: DofConstraint = FREE,
            ry: DofConstraint = FREE,
            rz: DofConstraint = FREE
        ) = NodeBoundaryCondition(ux, uy, uz, rx, ry, rz)
    }

    /** Returns true if this node has any restraint in any DOF. */
    fun isConstrained(): Boolean =
        listOf(ux, uy, uz, rx, ry, rz).any { it.type != ConstraintType.FREE }

    /** Returns all DOFs and their constraints as an ordered list for solver iteration. */
    fun toOrderedList(): List<Pair<DegreeOfFreedom, DofConstraint>> = listOf(
        DegreeOfFreedom.UX to ux,
        DegreeOfFreedom.UY to uy,
        DegreeOfFreedom.UZ to uz,
        DegreeOfFreedom.RX to rx,
        DegreeOfFreedom.RY to ry,
        DegreeOfFreedom.RZ to rz
    )

    /** Accesses a specific DOF constraint by [DegreeOfFreedom]. */
    operator fun get(dof: DegreeOfFreedom): DofConstraint = when (dof) {
        DegreeOfFreedom.UX -> ux
        DegreeOfFreedom.UY -> uy
        DegreeOfFreedom.UZ -> uz
        DegreeOfFreedom.RX -> rx
        DegreeOfFreedom.RY -> ry
        DegreeOfFreedom.RZ -> rz
    }

    /** Returns a copy of this condition with a single DOF constraint replaced. */
    fun withConstraint(
        dof: DegreeOfFreedom,
        constraint: DofConstraint
    ): NodeBoundaryCondition = when (dof) {
        DegreeOfFreedom.UX -> copy(ux = constraint)
        DegreeOfFreedom.UY -> copy(uy = constraint)
        DegreeOfFreedom.UZ -> copy(uz = constraint)
        DegreeOfFreedom.RX -> copy(rx = constraint)
        DegreeOfFreedom.RY -> copy(ry = constraint)
        DegreeOfFreedom.RZ -> copy(rz = constraint)
    }
}