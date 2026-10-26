package com.lz.model.structural

import com.lz.model.regulatory.LoadCategory
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment
import com.lz.model.units.inches
import com.lz.model.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class Flange {
    TOP, BOTTOM, NONE
}

/**
 * Represents the 6-DOF internal forces (demands) and localized structural state
 * at a specific cross-sectional station along a structural member.
 */

@Serializable
data class StationDemand(
    @Serializable(with = UUIDSerializer::class)
    val spanId: UUID,

    val x: Length, // Distance from start of the current span

    // --- Major Axis (Strong Axis) Demands ---
    val moment: Moment,
    val shear: Force,
    val deflection: Length = 0.0.inches,

    // --- Minor Axis (Weak Axis) Demands ---
    val momentY: Moment = Moment(0.0),
    val shearY: Force = Force(0.0),
    val deflectionY: Length = 0.0.inches,

    // --- Axial & Torsional Demands ---
    val axial: Force = Force(0.0),
    val torque: Moment = Moment(0.0),

    // --- Serviceability Verification Benchmarks ---
    val allowableDeflection: Length = Double.POSITIVE_INFINITY.inches,
    val allowableDeflectionY: Length = Double.POSITIVE_INFINITY.inches,

    // --- Localized Stability State at Point X ---
    val cb: Double = 1.0,
    val lbTop: Length = 0.0.inches,
    val lbBottom: Length = 0.0.inches,
    val compressionFlange: Flange = Flange.NONE,

    // --- Per-axis unbraced lengths for column buckling / flatwise bending ---
    // lbTop / lbBottom above are the lateral bracing of the two edges. A
    // member is usually braced differently about its two axes, so wood checks
    // want each axis's own length: luStrongAxis is the unbraced length for
    // buckling in the plane of strong-axis bending (about x-x); luWeakAxis for
    // buckling about the weak axis (y-y) and flatwise bending. Null means
    // "no separate input" — the wood calculator then falls back to the
    // larger of lbTop / lbBottom, the conservative choice.
    val luStrongAxis: Length? = null,
    val luWeakAxis: Length? = null,

    // Length of the member between the points of zero moment that bracket this
    // station (NDS 5.3.6, glulam volume factor CV). Where the moment diagram
    // doesn't have a zero on both sides (a cantilever, a fixed end) it is the
    // whole member length - larger L only lowers CV, so that is conservative.
    // Null = not supplied by the solver.
    val momentSpanLength: Length? = null,

    // --- Which load combination produced this demand ---
    // Some adjustment factors depend on WHICH loads are acting, not just how
    // big they are: the NDS load duration factor CD (ASD) and time effect
    // factor lambda (LRFD) follow the shortest-duration load in the
    // combination. The solver fills these in per combination. Empty / null
    // means the demand isn't tied to a combination (a legacy single-case
    // solve), and capacity code falls back to its manual setting.
    val combinationName: String? = null,
    // The loads that are BOTH in the combination (non-zero factor) AND actually
    // applied to the member, with their factors — a combination that lists snow
    // doesn't involve snow's duration if no snow load was entered. The factors
    // matter, not just the categories: NDS Table N3 gives lambda 0.8 to
    // "1.2D + 1.6S + 0.5W" but 1.0 to "1.2D + 1.0W + L + 0.5S".
    val combinationLoadFactors: Map<LoadCategory, Double> = emptyMap(),
    val combinationMethodology: DesignMethodology? = null
) {
    /** The categories of [combinationLoadFactors]. */
    val combinationLoadCategories: Set<LoadCategory> get() = combinationLoadFactors.keys
}