package com.lz.solver.analysis

import com.lz.model.structural.ConstraintType
import com.lz.model.structural.EffectiveLengthCase
import com.lz.model.structural.Load
import com.lz.model.structural.LoadDirection
import com.lz.model.structural.NodeBoundaryCondition
import com.lz.model.structural.SpanGeometry
import com.lz.model.structural.StructuralMember
import com.lz.model.structural.NormalizedBraceState
import java.util.UUID
import kotlin.math.abs

/**
 * Matches the loads acting in one combination to a row of NDS Table 3.3.3 (effective length),
 * span by span. A row is used only when EVERY transverse load on the span fits that one pattern
 * and the span meets the row's conditions; otherwise the span is UNSPECIFIED (footnote 1).
 *
 *  - more than one span                         -> MULTI_SPAN (footnote 2: no table row applies)
 *  - simple span (pin / roller), unbraced:
 *      only full-length uniform loads           -> SINGLE_SPAN_UNIFORM
 *      exactly one point load at midspan        -> SINGLE_SPAN_CENTER_POINT
 *  - cantilever (fixed root, free tip), unbraced:
 *      only full-length uniform loads           -> CANTILEVER_UNIFORM
 *      exactly one point load at the free end   -> CANTILEVER_TIP_POINT
 *
 * "Unbraced" means no brace point inside the span and no unbraced segment shorter than the span
 * (the table rows assume no intermediate lateral support). Only downward vertical loads match;
 * axial loads do not change the transverse pattern and are ignored; anything else (moments,
 * torque, lateral or upward loads, partial or varying loads) leaves the span UNSPECIFIED.
 */
internal object EffectiveLengthClassifier {

    private const val REL_TOL = 1e-6

    fun classify(
        member: StructuralMember,
        loadsInCombination: List<Load>,
        braceState: List<NormalizedBraceState>
    ): Map<UUID, EffectiveLengthCase> {
        val spans = member.spans
        if (spans.size > 1) return spans.associate { it.id to EffectiveLengthCase.MULTI_SPAN }
        val span = spans.firstOrNull() ?: return emptyMap()
        return mapOf(span.id to classifySpan(member, span, loadsInCombination, braceState))
    }

    private fun restrained(bc: NodeBoundaryCondition?, dof: (NodeBoundaryCondition) -> com.lz.model.structural.DofConstraint): Boolean =
        bc != null && dof(bc).type != ConstraintType.FREE

    private fun classifySpan(
        member: StructuralMember,
        span: SpanGeometry,
        loads: List<Load>,
        braceState: List<NormalizedBraceState>
    ): EffectiveLengthCase {
        val len = span.length.inches
        if (len <= 0.0) return EffectiveLengthCase.UNSPECIFIED
        val tol = REL_TOL * len

        // Support condition
        val start = member.nodes.firstOrNull { it.id == span.startNodeId }?.boundaryCondition
        val end = member.nodes.firstOrNull { it.id == span.endNodeId }?.boundaryCondition
        val startUy = restrained(start) { it.uy }; val endUy = restrained(end) { it.uy }
        val startRz = restrained(start) { it.rz }; val endRz = restrained(end) { it.rz }
        val simple = startUy && endUy && !startRz && !endRz
        val cantileverRootAtStart = startUy && startRz && !endUy && !endRz
        val cantileverRootAtEnd = endUy && endRz && !startUy && !startRz
        if (!simple && !cantileverRootAtStart && !cantileverRootAtEnd) return EffectiveLengthCase.UNSPECIFIED

        // The table rows assume no intermediate lateral support on the compression edge.
        if (braceState.any { it.x.inches > tol && it.x.inches < len - tol }) return EffectiveLengthCase.UNSPECIFIED
        if (span.unbracedSegments.any { it.lbTop.inches < len - tol || it.lbBottom.inches < len - tol }) {
            return EffectiveLengthCase.UNSPECIFIED
        }

        // Transverse loads only; every one must fit a single pattern.
        val transverse = loads.filter {
            it.spanId == span.id && it.direction != LoadDirection.AXIAL_COMPRESSION &&
                it.direction != LoadDirection.AXIAL_TENSION
        }
        if (transverse.isEmpty()) return EffectiveLengthCase.UNSPECIFIED
        if (transverse.any { it.direction != LoadDirection.VERTICAL_DOWN }) return EffectiveLengthCase.UNSPECIFIED

        val allUniformFullSpan = transverse.all {
            it is Load.UniformDistributedLoad &&
                abs(it.locationStart.inches) <= tol && abs(it.locationEnd.inches - len) <= tol
        }
        if (allUniformFullSpan) {
            return if (simple) EffectiveLengthCase.SINGLE_SPAN_UNIFORM else EffectiveLengthCase.CANTILEVER_UNIFORM
        }
        val single = transverse.singleOrNull() as? Load.PointLoad ?: return EffectiveLengthCase.UNSPECIFIED
        val x = single.locationStart.inches
        return when {
            simple && abs(x - len / 2.0) <= tol -> EffectiveLengthCase.SINGLE_SPAN_CENTER_POINT
            cantileverRootAtStart && abs(x - len) <= tol -> EffectiveLengthCase.CANTILEVER_TIP_POINT
            cantileverRootAtEnd && abs(x) <= tol -> EffectiveLengthCase.CANTILEVER_TIP_POINT
            else -> EffectiveLengthCase.UNSPECIFIED
        }
    }
}
