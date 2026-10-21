package com.lz.solver.material

import com.lz.model.regulatory.aisc.DesignFactor
import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.structural.CodeCheck
import com.lz.model.structural.CodeDataNotice
import com.lz.model.structural.CodeDataStatus
import com.lz.model.structural.DesignPropertyGroup
import com.lz.model.structural.DesignPropertyRow
import com.lz.model.structural.CombinedStressCheck
import com.lz.model.structural.DesignEquationTrace
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.Flange
import com.lz.model.structural.InteractionEquationResult
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.ShapeType
import com.lz.model.structural.StationDemand
import com.lz.model.structural.StrengthCheckResult
import com.lz.model.structural.StrengthDesignResult
import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodProfile
import com.lz.model.structural.WoodSpecies
import com.lz.model.units.Force
import com.lz.model.units.Moment
import com.lz.model.units.inIn2
import com.lz.model.units.inIn3
import com.lz.model.units.inIn4
import com.lz.model.units.inInches
import com.lz.model.units.inLbIn
import com.lz.model.units.inPoundsForce
import com.lz.model.units.inPsi
import com.lz.solver.capacity.CapacityCalculator
import com.lz.solver.capacity.DesignFactorSet
import com.lz.solver.capacity.RawCapacityResult
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * NDS 2018 capacity calculator for wood structural members.
 *
 * Covers:
 *   Bending     — NDS 3.3: F'b = Fb * CD * CM * Ct * CL * CF * Cfu * Ci * Cr
 *                 Glulam:  CV replaces CF; lesser of CV and CL governs (NDS 5.3.6)
 *   Shear       — NDS 3.4: F'v = Fv * CD * CM * Ct * Ci
 *   Compression — NDS 3.7: F'c = Fc * CD * CM * Ct * CF * Ci * CP
 *                 CP computed from slenderness ratio (NDS 3.7.1)
 *   Tension     — NDS 3.8: F't = Ft * CD * CM * Ct * CF * Ci
 *   Weak axis   — flatwise bending about y-y (Sy, Cfu; CL = 1.0; glulam Fby)
 *   Combined    — NDS 3.9.1 bending + axial tension (Eq. 3.9-1, 3.9-2);
 *                 NDS 3.9.2 bending + axial compression and biaxial
 *                 bending (Eq. 3.9-3, 3.9-4) — [evaluateDetailed] only
 *   Deflection  — serviceability limit from demand
 *
 * phi/omega factors are NOT applied here for the [evaluate] path —
 * [CapacityEngine] owns that step. [evaluateDetailed] applies NDS
 * ASD factors directly for report output (NDS is inherently ASD;
 * LRFD format factor lambda is applied when methodology is LRFD).
 *
 * Member type is inferred from [WoodSpecies.isGlulam]:
 *   isGlulam = true  → glulam provisions (CV, NDS Chapter 5)
 *   isGlulam = false → sawn lumber / SCL provisions (CF, NDS Chapter 4)
 */
class NdsWoodCapacityCalculator(
    private val profile: SectionProfile,
    private val material: MaterialGrade.Wood,
    private val adjustmentFactors: NdsAdjustmentFactors = NdsAdjustmentFactors(),
    /**
     * Resolved NDS edition (from the active project's BuildingCode ->
     * Standard -> StandardEdition.Nds chain: IBC 2015 -> NDS 2015, IBC 2018 /
     * 2021 -> NDS 2018, IBC 2024 -> NDS 2024).
     *
     * The values this calculator uses have been sourced for NDS 2018 only; it
     * has NOT been established that 2015 / 2024 share them. So the edition is
     * not assumed inert: [NdsCodeData] reports, per edition, what is
     * verified, unverified, standing in from another edition, or not
     * implemented, and every [StrengthDesignResult] carries those notices so
     * a result for an edition whose data is missing says so instead of
     * looking authoritative. Where an edition genuinely differs, branch on
     * [edition] here AND update [NdsCodeData].
     */
    private val edition: NdsEdition = NdsEdition.NDS_2018,
    /**
     * Which principal axis the member is oriented to bend about — the
     * Design tab's strong/weak toggle. The analysis already swaps the
     * stiffness (Ix <-> Iy) for the toggle, so [StationDemand.moment] is the
     * moment about *this* axis; capacity must come from the same axis.
     * Previously this calculator always used Sx, so a member flipped to
     * weak-axis bending was analysed with Iy but checked against the
     * strong-axis section modulus (the same gap the steel calculator had).
     * True = edgewise / strong axis (x-x, NDS fb1); false = flatwise / weak
     * axis (y-y, NDS fb2).
     */
    private val memberIsStrongAxis: Boolean = true,
    /**
     * The load duration factor / time effect factor already resolved for the
     * one load combination being evaluated. Set only on the per-combination
     * copy this class makes of itself (see [forDemand]); leave null otherwise.
     */
    private val loadDuration: ResolvedLoadDuration? = null
) : CapacityCalculator {

    private val isGlulam: Boolean = material.species.isGlulam

    /**
     * [material] with any member-width-dependent values (Southern Pine,
     * NDS Table 4B) resolved for this profile. All capacity math below
     * reads reference values from here, not from [material] directly.
     */
    private val designMaterial: MaterialGrade.Wood = resolveDesignMaterial(material, profile)

    /**
     * Mirrors [AiscSteelCapacityCalculator]'s own private result wrapper —
     * same shape, same reasoning: a nominal/limit-state pair for the
     * [evaluate] path (CapacityEngine's point-by-point diagram, which
     * doesn't need traces) that also carries optional [DesignEquationTrace]s
     * for [evaluateDetailed]'s Design tab card. Destructuring
     * (`val (nominal, limitState) = ...`) still works unchanged against
     * this three-component class, since Kotlin destructuring only binds as
     * many components as the left side asks for.
     */
    private data class LimitStateResult(
        val nominal: Double,
        val limitState: String,
        val traces: List<DesignEquationTrace> = emptyList()
    )

    private fun fmt(v: Double, d: Int = 2): String = String.format(Locale.US, "%.${d}f", v)
    private fun kipFt(lbIn: Double): String = fmt(lbIn / 12000.0, 2)
    private fun kips(lb: Double): String = fmt(lb / 1000.0, 2)

    // ------------------------------------------------------------------
    // Selected properties — everything the checks above actually used
    // ------------------------------------------------------------------

    /**
     * The inputs behind this evaluation, grouped for the Design tab: the
     * material's reference design values (the resolved values the checks
     * used, not just the picker's label), the section, every adjustment
     * factor with its source, and the unbraced / effective lengths for each
     * axis — including the Table 3.3.3 expression and whether a length came
     * from the bracing input or from the conservative default.
     */
    private fun codeDataNotices(methodology: DesignMethodology, demand: StationDemand): List<CodeDataNotice> {
        val notices = NdsCodeData.noticesFor(edition, methodology, isGlulam).toMutableList()
        if (loadDuration == null) {
            notices += CodeDataNotice(
                dataset = if (methodology == DesignMethodology.LRFD) "Time effect factor \u03bb" else "Load duration factor CD",
                status = CodeDataStatus.UNVERIFIED,
                message = when {
                    !adjustmentFactors.usesAutoLoadDuration ->
                        "Set by hand (CD = ${fmt(adjustmentFactors.cd, 2)}), not derived from the load combination."
                    else ->
                        "This demand isn't tied to a load combination, so the manual value is used instead of one derived from the loads acting."
                }
            )
        }
        return notices
    }

    private fun buildSelectedProperties(u: Unbraced, methodology: DesignMethodology, demand: StationDemand): List<DesignPropertyGroup> {
        fun row(sym: String, desc: String, value: String) = DesignPropertyRow(sym, desc, value)
        val m = designMaterial
        val f = adjustmentFactors
        val groups = mutableListOf<DesignPropertyGroup>()

        groups += DesignPropertyGroup("Code and standard", buildList {
            add(row("Standard", "Edition the check was run against", NdsCodeData.standardLabel(edition)))
            add(row("Methodology", "Design method", methodology.name))
            demand.combinationName?.let { add(row("Combination", "Load combination this demand belongs to", it)) }
            if (demand.combinationLoadCategories.isNotEmpty())
                add(row("Loads acting", "In the combination and applied to the member",
                    demand.combinationLoadCategories.joinToString(", ") { it.shortLabel }))
        })

        groups += DesignPropertyGroup("Material", listOf(
            row("Product", "Wood product", if (isGlulam) "Structural glued laminated timber" else "Sawn lumber / solid"),
            row("Species", "Species / combination", material.species.name),
            row("Grade", "Grade / combination symbol", material.grade.name)
        ))

        val fby = if (isGlulam)
            WoodPropertyService.getReferenceProperties(m.species, m.grade)?.bendingWeakAxis?.inPsi
        else null
        groups += DesignPropertyGroup("Reference design values", listOf(
            row("Fb", if (isGlulam) "Bending, strong axis (Fbx)" else "Bending", "${fmt(m.referenceBending.inPsi, 0)} psi"),
            row("Fby", "Bending, weak axis",
                when {
                    !isGlulam -> "= Fb (one tabulated value for sawn lumber)"
                    fby != null -> "${fmt(fby, 0)} psi"
                    else -> "not sourced for this combination"
                }),
            row("Ft", "Tension parallel to grain", "${fmt(m.referenceTensionParallel.inPsi, 0)} psi"),
            row("Fv", "Shear parallel to grain", "${fmt(m.referenceShear.inPsi, 0)} psi"),
            row("Fc", "Compression parallel to grain", "${fmt(m.referenceCompressionParallel.inPsi, 0)} psi"),
            row("Fc\u22a5", "Compression perpendicular to grain", "${fmt(m.referenceCompressionPerp.inPsi, 0)} psi"),
            row("E", "Modulus of elasticity", "${fmt(m.modulusOfElasticity.inPsi, 0)} psi"),
            row("Emin", "Modulus for stability calculations", "${fmt(woodEmin(m), 0)} psi")
        ))

        val b = if (profile is WoodProfile) profile.dressedWidth.inInches else sqrt(profile.area.inIn2)
        val d = profile.depth.inInches
        groups += DesignPropertyGroup("Section", listOf(
            row("Size", "Designation", profile.designation),
            row("b", "Width (dressed)", "${fmt(b, 3)} in"),
            row("d", "Depth (dressed)", "${fmt(d, 3)} in"),
            row("A", "Area", "${fmt(profile.area.inIn2, 3)} in\u00b2"),
            row("Sx", "Section modulus, strong axis", "${fmt(profile.propertiesStrongAxis.s.inIn3, 3)} in\u00b3"),
            row("Sy", "Section modulus, weak axis", "${fmt(profile.propertiesWeakAxis.s.inIn3, 3)} in\u00b3"),
            row("Ix", "Moment of inertia, strong axis", "${fmt(profile.propertiesStrongAxis.i.inIn4, 3)} in\u2074"),
            row("Iy", "Moment of inertia, weak axis", "${fmt(profile.propertiesWeakAxis.i.inIn4, 3)} in\u2074")
        ))

        val strongAx = bendingAxis(true, u)
        val weakAx = bendingAxis(false, u)
        val cpParts = computeCPTraced(u.colStrong, u.colWeak)
        val factorRows = mutableListOf(
            if (methodology == DesignMethodology.LRFD)
                row("\u03bb", loadDuration?.explanation ?: "Time effect factor \u2014 default (combination not known)",
                    fmt(loadDuration?.factor ?: LAMBDA_DEFAULT, 2))
            else
                row("CD", loadDuration?.explanation ?: "Load duration \u2014 set by hand or combination not known", fmt(f.cd, 2)),
            row("CM", "Wet service", fmt(f.cm, 2)),
            row("Ct", "Temperature", fmt(f.ct, 2)),
            row("Ci", "Incising", fmt(f.ci, 2)),
            row("Cr", "Repetitive member", fmt(f.cr, 2))
        )
        if (!isGlulam) {
            factorRows += row("CF (bending)", "Size factor, Fb (Table 4A/4B)", fmt(computeSawnCF(NdsCfProperty.BENDING), 3))
            factorRows += row("CF (tension)", "Size factor, Ft", fmt(computeSawnCF(NdsCfProperty.TENSION), 3))
            factorRows += row("CF (compression)", "Size factor, Fc", fmt(computeSawnCF(NdsCfProperty.COMPRESSION), 3))
        }
        factorRows += row("Cfu", "Flat use factor (flatwise bending only)", fmt(resolveFlatUseCfu(f, m, profile), 3))
        if (isGlulam) factorRows += row("CV", "Volume factor (strong axis)", fmt(computeCV(u.strongBend), 3))
        factorRows += row("CL (strong axis)", "Beam stability, bending about x-x", fmt(strongAx.cl, 3))
        factorRows += row("CL (weak axis)", "Beam stability, bending about y-y", fmt(weakAx.cl, 3))
        factorRows += row("CP", "Column stability", fmt(cpParts.cp, 3))
        groups += DesignPropertyGroup("Adjustment factors", factorRows)

        val bendStrong = computeNdsBeamStability(u.strongBend, profile, m, f, isGlulam, aboutStrongAxis = true)
        val bendWeak = computeNdsBeamStability(u.weakBend, profile, m, f, isGlulam, aboutStrongAxis = false)
        val lenRows = mutableListOf<DesignPropertyRow>()
        lenRows += row("lu (strong-axis bending)", "Unbraced length, compression edge", "${fmt(u.strongBend, 1)} in")
        bendStrong?.let {
            lenRows += row("le (strong-axis bending)", "${it.effectiveLength.formula}  (${it.effectiveLength.tableReference})",
                "${fmt(it.le, 1)} in  [lu/d = ${fmt(it.effectiveLength.luOverD, 2)}]")
            lenRows += row("RB (strong axis)", if (it.exemptByShape) "d \u2264 b: CL = 1.0 (NDS 3.3.3.1)" else "Slenderness, \u221a(le\u00b7d/b\u00b2)",
                fmt(it.rb, 2))
        }
        lenRows += row("lu (weak-axis bending)", "Unbraced length, flatwise bending" + if (u.weakIsDefault) " \u2014 default: larger of top/bottom bracing" else "",
            "${fmt(u.weakBend, 1)} in")
        bendWeak?.let {
            lenRows += row("le (weak-axis bending)", "${it.effectiveLength.formula}  (${it.effectiveLength.tableReference})",
                "${fmt(it.le, 1)} in  [lu/d = ${fmt(it.effectiveLength.luOverD, 2)}]")
            lenRows += row("RB (weak axis)", if (it.exemptByShape) "d \u2264 b: CL = 1.0 (NDS 3.3.3.1)" else "Slenderness, \u221a(le\u00b7d/b\u00b2)",
                fmt(it.rb, 2))
        }
        lenRows += row("le1 (column, strong plane)", "Ke = 1.0" + if (u.strongIsDefault) " \u2014 default: larger of top/bottom bracing" else "",
            "${fmt(u.colStrong, 1)} in  [le1/d1 = ${fmt(if (d > 0) u.colStrong / d else 0.0, 2)}]")
        lenRows += row("le2 (column, weak plane)", "Ke = 1.0" + if (u.weakIsDefault) " \u2014 default: larger of top/bottom bracing" else "",
            "${fmt(u.colWeak, 1)} in  [le2/d2 = ${fmt(if (b > 0) u.colWeak / b else 0.0, 2)}]")
        groups += DesignPropertyGroup("Unbraced and effective lengths", lenRows)

        return groups
    }

    // ------------------------------------------------------------------
    // CapacityCalculator contract
    // ------------------------------------------------------------------

    /**
     * The unbraced lengths that apply to each axis, resolved separately —
     * members are usually braced differently about their two axes, and
     * using one length for everything under-checks whichever axis is
     * braced less.
     *
     *  - [strongBend]: compression edge for edgewise (x-x) bending — the
     *    lateral-torsional buckling length, from the flange that is in
     *    compression (this is what lbTop / lbBottom describe).
     *  - [weakBend]: flatwise (y-y) bending.
     *  - [colStrong] / [colWeak]: column buckling in the plane of strong-axis
     *    bending and about the weak axis.
     *
     * Where the demand carries no per-axis length ([StationDemand.luStrongAxis]
     * / [StationDemand.luWeakAxis] null) the larger of lbTop and lbBottom is
     * used — the conservative envelope, and the only information available.
     */
    private data class Unbraced(
        val strongBend: Double,
        val weakBend: Double,
        val colStrong: Double,
        val colWeak: Double,
        val weakIsDefault: Boolean,
        val strongIsDefault: Boolean
    )

    private fun unbraced(demand: StationDemand): Unbraced {
        val flangeLb = if (demand.compressionFlange == Flange.TOP)
            demand.lbTop.inInches else demand.lbBottom.inInches
        val envelope = maxOf(demand.lbTop.inInches, demand.lbBottom.inInches)
        val strongPlane = demand.luStrongAxis?.inInches ?: envelope
        val weakPlane = demand.luWeakAxis?.inInches ?: envelope
        return Unbraced(
            strongBend = flangeLb, weakBend = weakPlane,
            colStrong = strongPlane, colWeak = weakPlane,
            weakIsDefault = demand.luWeakAxis == null, strongIsDefault = demand.luStrongAxis == null
        )
    }

    /**
     * A copy of this calculator carrying the load duration factor for the
     * combination that produced [demand], or null when none is needed (the
     * user set CD by hand, the demand isn't tied to a combination, or this
     * already IS the per-combination copy).
     *
     * CD (NDS 2.3.2) is a property of the loads in a combination — the
     * shortest-duration load acting sets it — so it can't be one number for
     * the whole member. ASD: CD multiplies Fb, Ft, Fv, Fc (never E, Emin or
     * Fc-perp) through the usual adjustment chain. LRFD: CD does not apply;
     * the time effect factor lambda does instead, applied with phi
     * ([designFactorsFor]) — so the nominal capacities carry no duration factor.
     */
    private fun forDemand(demand: StationDemand, methodology: DesignMethodology): NdsWoodCapacityCalculator? {
        if (loadDuration != null || !adjustmentFactors.usesAutoLoadDuration) return null
        val rd = NdsLoadDuration.resolve(demand.combinationLoadCategories, methodology, edition) ?: return null
        val cd = if (methodology == DesignMethodology.LRFD) 1.0 else rd.factor
        return NdsWoodCapacityCalculator(
            profile, material,
            adjustmentFactors.copy(cd = cd, loadDurationFromCombination = false),
            edition, memberIsStrongAxis, rd
        )
    }

    override fun evaluate(demand: StationDemand): RawCapacityResult {
        forDemand(demand, demand.combinationMethodology ?: DesignMethodology.ASD)?.let { return it.evaluate(demand) }
        val u = unbraced(demand)
        val lb = u.strongBend

        val isAxialTension = demand.axial.pounds >= 0.0

        val (nomMn, lsMn) = calculateBending(u)
        val (nomVn, lsVn) = calculateShear()
        val (nomPn, lsPn) = if (isAxialTension)
            calculateTension()
        else
            calculateCompression(u.colStrong, u.colWeak)

        // The capacity about the *other* principal axis, checked against
        // StationDemand.momentY. (It used to be a flat "N/A — weak axis not
        // checked for wood", so out-of-plane moment never reached any check.)
        val secondaryAxis = calculateBendingAbout(!memberIsStrongAxis, u)

        return RawCapacityResult(
            nominalFlexureX      = nomMn,
            limitStateFlexureX   = lsMn,
            nominalFlexureY      = secondaryAxis.nominal,
            limitStateFlexureY   = secondaryAxis.limitState,
            nominalShearX        = nomVn,
            limitStateShearX     = lsVn,
            nominalShearY        = 0.0,
            limitStateShearY     = "N/A",
            nominalAxial         = nomPn,
            limitStateAxial      = lsPn,
            nominalTorsion       = 0.0,
            limitStateTorsion    = "N/A — Torsion not codified for wood members",
            allowableDeflection  = demand.allowableDeflection.inInches,
            limitStateDeflection = "Deflection Limit"
        )
    }

    override fun evaluateDetailed(
        demand: StationDemand,
        methodology: DesignMethodology
    ): StrengthDesignResult {
        forDemand(demand, methodology)?.let { return it.evaluateDetailed(demand, methodology) }
        val u = unbraced(demand)
        val lb = u.strongBend
        val isAxialTension = demand.axial.pounds >= 0.0

        val (nomMn, lsMn, tracesMn) = calculateBending(u)
        val (nomVn, lsVn, tracesVn) = calculateShear()
        val (nomPn, lsPn, tracesPn) = if (isAxialTension)
            calculateTension()
        else
            calculateCompression(u.colStrong, u.colWeak)

        // NDS is inherently ASD. For LRFD, apply the time effect factor
        // lambda per NDS Appendix N, Table N3 — 0.6 dead load only, 0.8
        // occupancy live / roof live / snow / rain, 1.0 with wind or
        // earthquake — resolved for THIS combination. When the combination
        // isn't known (or CD was set by hand) the old default of 0.8 stands.
        val lambda = if (methodology == DesignMethodology.LRFD) (loadDuration?.factor ?: LAMBDA_DEFAULT) else 1.0

        // ASD: design value = adjusted F' * S (already in nominals)
        // LRFD: design value = lambda * phi * F'n * S
        val phiBending     = 0.85
        val phiShear       = 0.75
        val phiCompression = 0.90
        val phiTension     = 0.80

        val designMn = if (methodology == DesignMethodology.LRFD)
            lambda * phiBending * nomMn else nomMn
        val designVn = if (methodology == DesignMethodology.LRFD)
            lambda * phiShear * nomVn else nomVn
        val designPn = if (methodology == DesignMethodology.LRFD)
            lambda * (if (isAxialTension) phiTension else phiCompression) * nomPn
        else nomPn

        // Utilization is always demand vs. capacity as magnitudes — the
        // signed value only matters upstream (e.g. isAxialTension, or
        // which diagram side a moment plots on), never for the check
        // itself. This was the bug behind negative utilization ratios.
        //
        // Zero capacity with real demand is a FAILURE, not a pass: it is what
        // the NDS slenderness limits produce (RB > 50 in 3.3.3.7, le/d > 50 in
        // 3.7.1.4 both zero the capacity), and a plain `demand / 0` guard
        // returning 0.0 showed such a member as 0.00 utilized.
        val ratioMn = utilizationRatio(abs(demand.moment.lbIn), designMn)
        val ratioVn = utilizationRatio(abs(demand.shear.pounds), designVn)
        val ratioPn = utilizationRatio(abs(demand.axial.pounds), designPn)

        val kBend = if (methodology == DesignMethodology.LRFD) lambda * phiBending else 1.0
        val kTens = if (methodology == DesignMethodology.LRFD) lambda * phiTension else 1.0
        val kComp = if (methodology == DesignMethodology.LRFD) lambda * phiCompression else 1.0

        // Moment about the other principal axis (StationDemand.momentY), when present.
        val secondaryDemand = abs(demand.momentY.lbIn)
        val secondaryCheck = if (secondaryDemand > 0.0) {
            val sec = calculateBendingAbout(!memberIsStrongAxis, u)
            val designMnSec = if (methodology == DesignMethodology.LRFD)
                lambda * phiBending * sec.nominal else sec.nominal
            StrengthCheckResult(
                demand               = Moment(secondaryDemand),
                capacity             = Moment(designMnSec),
                utilization          = utilizationRatio(secondaryDemand, designMnSec),
                governingCombination = "Current",
                governingMode        = sec.limitState,
                traces               = sec.traces
            )
        } else null

        val combinedChecks = buildCombinedChecks(
            demand = demand, u = u,
            kBend = kBend, kTens = kTens, kComp = kComp,
            isLrfd = methodology == DesignMethodology.LRFD
        )

        val cl = computeCL(lb)
        val cv = if (isGlulam) computeCV(lb) else 1.0

        return StrengthDesignResult(
            momentCheck = StrengthCheckResult(
                // Stored as a magnitude, not the signed envelope value — a
                // demand/capacity check always compares |demand| to
                // capacity, so a negative Demand on the Design tab reads as
                // wrong regardless of which diagram side it came from.
                demand               = Moment(abs(demand.moment.lbIn)),
                capacity             = Moment(designMn),
                utilization          = ratioMn,
                governingCombination = "Current",
                governingMode        = lsMn,
                traces               = tracesMn
            ),
            shearCheck = StrengthCheckResult(
                demand               = Force(abs(demand.shear.pounds)),
                capacity             = Force(designVn),
                utilization          = ratioVn,
                governingCombination = "Current",
                governingMode        = lsVn,
                traces               = tracesVn
            ),
            axialCheck = StrengthCheckResult(
                demand               = Force(abs(demand.axial.pounds)),
                capacity             = Force(designPn),
                utilization          = ratioPn,
                governingCombination = "Current",
                governingMode        = lsPn,
                traces               = tracesPn
            ),
            torsionCheck = StrengthCheckResult(
                demand               = Moment(abs(demand.torque.lbIn)),
                capacity             = Moment(0.0),
                utilization          = 0.0,
                governingCombination = "N/A",
                governingMode        = "Torsion not codified for wood"
            ),
            methodology      = methodology,
            secondaryMomentCheck = secondaryCheck,
            combinedChecks   = combinedChecks,
            selectedProperties = buildSelectedProperties(u, methodology, demand),
            codeDataNotices  = codeDataNotices(methodology, demand),
            standardLabel    = NdsCodeData.standardLabel(edition),
            designParameters = buildMap {
                put("Member Type",  if (isGlulam) "Glulam" else "Sawn Lumber / SCL")
                put("Species",      material.species.name)
                put("Grade",        material.grade.name)
                put("Lu (bending)", String.format(Locale.US, "%.1f in", lb))
                put("CL",           String.format(Locale.US, "%.3f", cl))
                if (isGlulam) put("CV", String.format(Locale.US, "%.3f", cv))
                if (methodology != DesignMethodology.LRFD)
                    put("CD",       String.format(Locale.US, "%.2f", adjustmentFactors.cd))
                put("CM",           String.format(Locale.US, "%.2f", adjustmentFactors.cm))
                put("Ct",           String.format(Locale.US, "%.2f", adjustmentFactors.ct))
                put("Cr",           String.format(Locale.US, "%.2f", adjustmentFactors.cr))
                if (methodology == DesignMethodology.LRFD)
                    put("Lambda", String.format(Locale.US, "%.2f", lambda))
            }
        )
    }

    /**
     * NDS is inherently ASD: [evaluate]'s nominal values already ARE the
     * fully code-adjusted allowable capacity (F' * S, with CD/CM/Ct/CL/CF/
     * etc. baked in). So for ASD, `value = 1.0` here — CapacityEngine will
     * divide by 1.0, i.e. use the nominal as-is. Dividing by anything else
     * would double-count NDS's own adjustment factors.
     *
     * For LRFD, reproduces exactly the same lambda*phi logic already used in
     * [evaluateDetailed] (NDS Appendix N format conversion). Folding lambda
     * into the returned factor's `value` lets [DesignFactorSet.apply] use
     * the same `nominal * value` shape as the AISC LRFD case.
     */
    override fun designFactors(methodology: DesignMethodology): DesignFactorSet {
        if (methodology == DesignMethodology.ASD) {
            val one = DesignFactor(1.0, "NDS ASD — nominal is already the adjusted allowable capacity")
            return DesignFactorSet(
                methodology       = methodology,
                flexure           = one,
                shear             = one,
                axialTension      = one,
                axialCompression  = one,
                torsion           = one
            )
        }

        return lrfdFactors(LAMBDA_DEFAULT)
    }

    /**
     * The factors for the combination that produced [demand]. Same as
     * [designFactors] except LRFD uses that combination's own time effect
     * factor lambda (Table N3) instead of the 0.8 default — so a dead-load-only
     * combination gets 0.6 and a wind combination 1.0.
     */
    fun designFactorsFor(demand: StationDemand, methodology: DesignMethodology): DesignFactorSet {
        if (methodology != DesignMethodology.LRFD || !adjustmentFactors.usesAutoLoadDuration) {
            return designFactors(methodology)
        }
        val rd = NdsLoadDuration.resolve(demand.combinationLoadCategories, methodology, edition)
        return lrfdFactors(rd?.factor ?: LAMBDA_DEFAULT)
    }

    private fun lrfdFactors(lambda: Double): DesignFactorSet {
        val methodology = DesignMethodology.LRFD
        val phiBending     = 0.85
        val phiShear       = 0.75
        val phiCompression = 0.90
        val phiTension     = 0.80

        return DesignFactorSet(
            methodology       = methodology,
            flexure           = DesignFactor(lambda * phiBending,     "NDS Appendix N — lambda x phi (bending); KF not applied"),
            shear             = DesignFactor(lambda * phiShear,       "NDS Appendix N — lambda x phi (shear); KF not applied"),
            axialTension      = DesignFactor(lambda * phiTension,     "NDS Appendix N — lambda x phi (tension); KF not applied"),
            axialCompression  = DesignFactor(lambda * phiCompression, "NDS Appendix N — lambda x phi (compression); KF not applied"),
            torsion           = DesignFactor(1.0, "Torsion not codified for wood")
        )
    }

    // ------------------------------------------------------------------
    // NDS 3.3 — Bending
    // ------------------------------------------------------------------

    /**
     * Everything one axis's bending check needs, built once so the capacity
     * check (F'b x S), the NDS 3.9.1 tension interaction (which needs F*b
     * and F**b) and the NDS 3.9.2 compression / biaxial interaction (which
     * needs F'b1 and F'b2) all read a single development of the adjustment
     * factors instead of each rebuilding it.
     *
     * @property s section modulus about this axis (in^3).
     * @property fbAdj F'b — every applicable factor (the lesser of CL and CV
     *   for glulam strong axis; CL = 1.0 about the weak axis).
     * @property fStar NDS 3.9.1's F*b: all factors except CL (so CV stays in).
     * @property fStarStar NDS 3.9.1's F**b: all factors except CV (so CL stays in).
     * @property unavailableReason non-null when this axis can't be checked
     *   (glulam weak axis with no sourced Fby) — capacity is then reported
     *   as zero with this reason as the limit state, never a guessed value.
     */
    private data class BendingAxis(
        val strong: Boolean,
        val s: Double,
        val fbAdj: Double,
        val fStar: Double,
        val fStarStar: Double,
        val cl: Double,
        val rbExceedsLimit: Boolean,
        val limitState: String,
        val traces: List<DesignEquationTrace>,
        val fStarTrace: DesignEquationTrace? = null,
        val fStarStarTrace: DesignEquationTrace? = null,
        val unavailableReason: String? = null
    ) {
        val available: Boolean get() = unavailableReason == null
    }

    private fun bendingAxis(strong: Boolean, u: Unbraced): BendingAxis =
        if (strong) strongBendingAxis(u.strongBend) else weakBendingAxis(u.weakBend)

    /**
     * The NDS 3.3.3 development for one axis, as traces: the effective-length
     * expression actually used (Table 3.3.3, with which row and which
     * lu/d), RB, FbE, F*b and CL — or, when d <= b, the statement that
     * CL = 1.0 and why.
     */
    private fun stabilityTraces(stab: NdsBeamStabilityDetail, fb: Double): List<DesignEquationTrace> {
        val f = adjustmentFactors
        val traces = mutableListOf<DesignEquationTrace>()
        val el = stab.effectiveLength
        val axisName = if (stab.aboutStrongAxis) "strong axis (x-x)" else "weak axis (y-y)"
        traces += DesignEquationTrace(
            symbolicEquation = el.formula,
            substitutedEquation = "lu/d = ${fmt(stab.lb)}/${fmt(stab.d)} = ${fmt(el.luOverD, 2)} -> " +
                (if (el.dTerm > 0.0) "le = ${el.coefficient} x ${fmt(stab.lb)} + ${el.dTerm.toInt()} x ${fmt(stab.d)}"
                else "le = ${el.coefficient} x ${fmt(stab.lb)}") + "  [$axisName]",
            result = fmt(stab.le, 2),
            units = "in",
            codeReference = el.tableReference,
            variables = mapOf("lu" to stab.lb, "d" to stab.d, "lu/d" to el.luOverD, "le" to stab.le)
        )
        if (stab.exemptByShape) {
            traces += DesignEquationTrace(
                symbolicEquation = "d <= b  ->  CL = 1.0",
                substitutedEquation = "d = ${fmt(stab.d)} <= b = ${fmt(stab.b)}  [$axisName]",
                result = "1.000",
                units = "",
                codeReference = "NDS 3.3.3.1",
                variables = mapOf("d" to stab.d, "b" to stab.b)
            )
            return traces
        }
        val cfStab = computeSawnCF(NdsCfProperty.BENDING)
        val ratio = stab.fbE / stab.fStarB
        traces += DesignEquationTrace(
            symbolicEquation = "RB = sqrt(le x d / b^2)",
            substitutedEquation = "RB = sqrt(${fmt(stab.le)} x ${fmt(stab.d)} / ${fmt(stab.b)}^2)",
            result = fmt(stab.rb, 2) + if (stab.rbExceedsLimit) " (exceeds 50 — NDS 3.3.3.7)" else "",
            units = "",
            codeReference = "NDS 3.3.3.6, Eq. 3.3-5",
            variables = mapOf("le" to stab.le, "d" to stab.d, "b" to stab.b)
        )
        traces += DesignEquationTrace(
            symbolicEquation = "FbE = 1.20 E'min / RB^2",
            substitutedEquation = "FbE = 1.20 x ${fmt(stab.eMinAdj, 0)} / ${fmt(stab.rb, 2)}^2",
            result = fmt(stab.fbE, 0),
            units = "psi",
            codeReference = "NDS 3.3.3.8",
            variables = mapOf("E'min" to stab.eMinAdj, "RB" to stab.rb)
        )
        traces += DesignEquationTrace(
            symbolicEquation = "F*b = Fb x CD x CM x Ct x CF x Ci x Cr   (all factors except Cfu, CV, CL)",
            substitutedEquation = "F*b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x ${fmt(f.ct)} x " +
                "${fmt(cfStab, 3)} x ${fmt(f.ci)} x ${fmt(f.cr)}",
            result = fmt(stab.fStarB, 0),
            units = "psi",
            codeReference = "NDS 3.3.3.8",
            variables = mapOf(
                "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct,
                "CF" to cfStab, "Ci" to f.ci, "Cr" to f.cr
            )
        )
        traces += DesignEquationTrace(
            symbolicEquation = "CL = (1+(FbE/F*b))/1.9 - sqrt([(1+(FbE/F*b))/1.9]^2 - (FbE/F*b)/0.95)",
            substitutedEquation = "CL = (1+${fmt(ratio, 3)})/1.9 - sqrt([(1+${fmt(ratio, 3)})/1.9]^2 - " +
                "${fmt(ratio, 3)}/0.95)",
            result = fmt(stab.cl, 3),
            units = "",
            codeReference = "NDS 3.3.3.8, Eq. 3.3-6",
            variables = mapOf("FbE" to stab.fbE, "F*b" to stab.fStarB, "FbE/F*b" to ratio)
        )
        return traces
    }

    /**
     * Strong-axis (edgewise, x-x) bending — NDS 3.3.
     *
     * Sawn lumber: F'b = Fb * CD * CM * Ct * CL * CF * Cfu * Ci * Cr
     * Glulam:      F'b = Fb * CD * CM * Ct * min(CV, CL) * Cfu * Ci
     *              NDS 5.3.6: CV and CL are not applied simultaneously.
     */
    private fun strongBendingAxis(lb: Double): BendingAxis {
        val fb = designMaterial.referenceBending.inPsi
        val sx = profile.propertiesStrongAxis.s.inIn3
        val f = adjustmentFactors

        val stab = computeNdsBeamStability(lb, profile, designMaterial, f, isGlulam)
        val cl = stab?.cl ?: 1.0

        val traces = if (stab != null) stabilityTraces(stab, fb).toMutableList() else mutableListOf<DesignEquationTrace>()

        val fbAdj: Double
        val fStar: Double
        val fStarStar: Double
        val limitState: String
        val adjTrace: DesignEquationTrace
        val fStarTrace: DesignEquationTrace
        val fStarStarTrace: DesignEquationTrace

        if (isGlulam) {
            val cv = computeCV(lb)
            // NDS 5.3.6: use lesser of CV and CL
            val stabilityFactor = min(cl, cv)
            fbAdj = fb * f.cd * f.cm * f.ct * stabilityFactor * f.cfu * f.ci
            fStar = fb * f.cd * f.cm * f.ct * cv * f.cfu * f.ci
            fStarStar = fb * f.cd * f.cm * f.ct * cl * f.cfu * f.ci
            limitState = if (cv <= cl)
                "Bending — Volume Factor CV (NDS 5.3.6)"
            else
                "Bending — Beam Stability CL (NDS 3.3.3)"
            adjTrace = DesignEquationTrace(
                symbolicEquation = "F'b = Fb x CD x CM x Ct x min(CL, CV) x Cfu x Ci",
                substitutedEquation = "F'b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x " +
                    "${fmt(f.ct)} x min(${fmt(cl, 3)}, ${fmt(cv, 3)}) x ${fmt(f.cfu)} x ${fmt(f.ci)}",
                result = fmt(fbAdj, 0),
                units = "psi",
                codeReference = "NDS 5.3.2, 5.3.6",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct,
                    "CL" to cl, "CV" to cv, "Cfu" to f.cfu, "Ci" to f.ci
                )
            )
            fStarTrace = DesignEquationTrace(
                symbolicEquation = "F*b = Fb x CD x CM x Ct x CV x Cfu x Ci   (all factors except CL)",
                substitutedEquation = "F*b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x ${fmt(f.ct)} x " +
                    "${fmt(cv, 3)} x ${fmt(f.cfu)} x ${fmt(f.ci)}",
                result = fmt(fStar, 0),
                units = "psi",
                codeReference = "NDS 3.9.1",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct,
                    "CV" to cv, "Cfu" to f.cfu, "Ci" to f.ci
                )
            )
            fStarStarTrace = DesignEquationTrace(
                symbolicEquation = "F**b = Fb x CD x CM x Ct x CL x Cfu x Ci   (all factors except CV)",
                substitutedEquation = "F**b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x ${fmt(f.ct)} x " +
                    "${fmt(cl, 3)} x ${fmt(f.cfu)} x ${fmt(f.ci)}",
                result = fmt(fStarStar, 0),
                units = "psi",
                codeReference = "NDS 3.9.1",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct,
                    "CL" to cl, "Cfu" to f.cfu, "Ci" to f.ci
                )
            )
        } else {
            // Sawn lumber / SCL: CF from adjustmentFactors (caller must set it)
            val cfEffective = computeSawnCF(NdsCfProperty.BENDING)
            fbAdj = fb * f.cd * f.cm * f.ct * cl * cfEffective * f.cfu * f.ci * f.cr
            fStar = fb * f.cd * f.cm * f.ct * cfEffective * f.cfu * f.ci * f.cr
            fStarStar = fbAdj
            limitState = if (cl < 1.0)
                "Bending — Beam Stability CL (NDS 3.3.3)"
            else
                "Bending — Size Factor CF (NDS Table 4A)"
            adjTrace = DesignEquationTrace(
                symbolicEquation = "F'b = Fb x CD x CM x Ct x CL x CF x Cfu x Ci x Cr",
                substitutedEquation = "F'b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x " +
                    "${fmt(f.ct)} x ${fmt(cl, 3)} x ${fmt(cfEffective, 3)} x ${fmt(f.cfu)} x " +
                    "${fmt(f.ci)} x ${fmt(f.cr)}",
                result = fmt(fbAdj, 0),
                units = "psi",
                codeReference = "NDS 3.3.2, 3.3.3, Table 4A",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct, "CL" to cl,
                    "CF" to cfEffective, "Cfu" to f.cfu, "Ci" to f.ci, "Cr" to f.cr
                )
            )
            fStarTrace = DesignEquationTrace(
                symbolicEquation = "F*b = Fb x CD x CM x Ct x CF x Cfu x Ci x Cr   (all factors except CL)",
                substitutedEquation = "F*b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x ${fmt(f.ct)} x " +
                    "${fmt(cfEffective, 3)} x ${fmt(f.cfu)} x ${fmt(f.ci)} x ${fmt(f.cr)}",
                result = fmt(fStar, 0),
                units = "psi",
                codeReference = "NDS 3.9.1",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct,
                    "CF" to cfEffective, "Cfu" to f.cfu, "Ci" to f.ci, "Cr" to f.cr
                )
            )
            // Sawn lumber has no CV, so "all factors except CV" is F'b itself.
            fStarStarTrace = adjTrace.copy(
                symbolicEquation = "F**b = F'b   (no CV for sawn lumber — all factors apply)",
                codeReference = "NDS 3.9.1"
            )
        }

        traces += adjTrace
        return BendingAxis(
            strong = true, s = sx, fbAdj = fbAdj, fStar = fStar, fStarStar = fStarStar,
            cl = cl, rbExceedsLimit = stab?.rbExceedsLimit == true,
            limitState = limitState, traces = traces,
            fStarTrace = fStarTrace, fStarStarTrace = fStarStarTrace
        )
    }

    /**
     * Weak-axis (flatwise, y-y) bending — the load is on the member's wide
     * face. CL comes from the same NDS 3.3.3 development as the strong axis
     * but with d and b swapped (d = the member's width, b = its depth). For
     * any rectangular member bent flatwise d <= b, so NDS 3.3.3.1 sets
     * CL = 1.0 — but that is now the result of the check, shown in the
     * traces, rather than an assumption; a member whose width exceeds its
     * depth would go through the full RB / FbE / CL calculation with its own
     * [lbWeak]. The flat use factor Cfu applies (NDS 4.3.7; Table 4A / 4B
     * for sawn lumber, Table 5A / 5C for glulam).
     *
     * Sawn lumber has one tabulated Fb for both axes: F'b = Fb * CD * CM *
     * Ct * CL * CF * Cfu * Ci * Cr (CF as tabulated by the member's nominal
     * width, with Cfu on top — Table 4A's note for dimension lumber used
     * flatwise).
     *
     * Glulam's Fby is tabulated separately from Fbx, and isn't sourced for
     * every combination: where it's missing this reports the axis as
     * unavailable rather than substituting Fbx.
     */
    private fun weakBendingAxis(lbWeak: Double): BendingAxis {
        val f = adjustmentFactors
        val sy = profile.propertiesWeakAxis.s.inIn3
        val cfu = resolveFlatUseCfu(f, designMaterial, profile)

        val stab = computeNdsBeamStability(lbWeak, profile, designMaterial, f, isGlulam, aboutStrongAxis = false)
        val cl = stab?.cl ?: 1.0
        val stabTraces = mutableListOf<DesignEquationTrace>()
        if (stab != null) {
            stabTraces += stabilityTraces(stab, designMaterial.referenceBending.inPsi)
        } else {
            val dW = if (profile is WoodProfile) profile.dressedWidth.inInches else sqrt(profile.area.inIn2)
            val bW = profile.depth.inInches
            if (dW <= bW) stabTraces += DesignEquationTrace(
                symbolicEquation = "d <= b  ->  CL = 1.0",
                substitutedEquation = "d = ${fmt(dW)} <= b = ${fmt(bW)}  [weak axis (y-y)]",
                result = "1.000", units = "", codeReference = "NDS 3.3.3.1",
                variables = mapOf("d" to dW, "b" to bW)
            )
        }
        val clLabel = fmt(cl, 3)

        val fbAdj: Double
        val fStarNoCl: Double
        val adjTrace: DesignEquationTrace
        if (isGlulam) {
            val fby = WoodPropertyService
                .getReferenceProperties(designMaterial.species, designMaterial.grade)
                ?.bendingWeakAxis?.inPsi
            if (fby == null) {
                val reason = "Weak-axis bending — Fby is not sourced for this glulam combination"
                return BendingAxis(
                    strong = false, s = sy, fbAdj = 0.0, fStar = 0.0, fStarStar = 0.0, cl = 1.0,
                    rbExceedsLimit = false, limitState = reason, traces = emptyList(),
                    unavailableReason = reason
                )
            }
            fStarNoCl = fby * f.cd * f.cm * f.ct * cfu * f.ci
            fbAdj = fStarNoCl * cl
            adjTrace = DesignEquationTrace(
                symbolicEquation = "F'by = Fby x CD x CM x Ct x CL x Cfu x Ci",
                substitutedEquation = "F'by = ${fmt(fby, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x ${fmt(f.ct)} x " +
                    "$clLabel x ${fmt(cfu, 3)} x ${fmt(f.ci)}",
                result = fmt(fbAdj, 0),
                units = "psi",
                codeReference = "NDS 3.3.3.1, Table 5A (Fby, Cfu)",
                variables = mapOf(
                    "Fby" to fby, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct,
                    "CL" to cl, "Cfu" to cfu, "Ci" to f.ci
                )
            )
        } else {
            val fb = designMaterial.referenceBending.inPsi
            val cf = computeSawnCF(NdsCfProperty.BENDING)
            fStarNoCl = fb * f.cd * f.cm * f.ct * cf * cfu * f.ci * f.cr
            fbAdj = fStarNoCl * cl
            adjTrace = DesignEquationTrace(
                symbolicEquation = "F'b = Fb x CD x CM x Ct x CL x CF x Cfu x Ci x Cr",
                substitutedEquation = "F'b = ${fmt(fb, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x ${fmt(f.ct)} x " +
                    "$clLabel x ${fmt(cf, 3)} x ${fmt(cfu, 3)} x ${fmt(f.ci)} x ${fmt(f.cr)}",
                result = fmt(fbAdj, 0),
                units = "psi",
                codeReference = "NDS 3.3.3.1, 4.3.7, Table 4A (CF, Cfu)",
                variables = mapOf(
                    "Fb" to fb, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct, "CL" to cl,
                    "CF" to cf, "Cfu" to cfu, "Ci" to f.ci, "Cr" to f.cr
                )
            )
        }

        return BendingAxis(
            strong = false, s = sy, fbAdj = fbAdj, fStar = fStarNoCl, fStarStar = fbAdj, cl = cl,
            rbExceedsLimit = stab?.rbExceedsLimit ?: false,
            limitState = "Bending — Weak axis / flatwise, Cfu = ${fmt(cfu, 2)} (NDS 4.3.7)",
            traces = stabTraces + adjTrace,
            fStarTrace = adjTrace.copy(
                symbolicEquation = "F*b = F'b / CL   (flatwise: all factors except CL; no CV)", codeReference = "NDS 3.9.1",
                result = fmt(fStarNoCl, 0)
            ),
            fStarStarTrace = adjTrace.copy(
                symbolicEquation = "F**b = F'b   (flatwise: CL in, no CV)", codeReference = "NDS 3.9.1"
            )
        )
    }

    /** Bending capacity about the given principal axis: F'b x S, with its traces. */
    private fun calculateBendingAbout(strong: Boolean, u: Unbraced): LimitStateResult {
        val ax = bendingAxis(strong, u)
        if (!ax.available) return LimitStateResult(0.0, ax.limitState, ax.traces)
        if (ax.rbExceedsLimit) {
            return LimitStateResult(
                0.0, "Bending — RB exceeds 50, not permitted (NDS 3.3.3.7)", ax.traces
            )
        }

        val mn = ax.fbAdj * ax.s
        val sName = if (strong) "Sx" else "Sy"
        val mnTrace = DesignEquationTrace(
            symbolicEquation = "Mn = F'b x $sName",
            substitutedEquation = "Mn = ${fmt(ax.fbAdj, 0)} x ${fmt(ax.s, 2)}",
            result = "${fmt(mn, 0)} lb-in (${kipFt(mn)} k-ft)",
            units = "lb-in",
            codeReference = "NDS 3.3.2",
            variables = mapOf("F'b" to ax.fbAdj, sName to ax.s)
        )
        return LimitStateResult(mn, ax.limitState, ax.traces + mnTrace)
    }

    /** Bending capacity about the axis the member is oriented to bend about. */
    private fun calculateBending(u: Unbraced): LimitStateResult =
        calculateBendingAbout(memberIsStrongAxis, u)

    /**
     * NDS 3.3.3 Beam Stability Factor CL.
     * Delegates to [computeNdsCL] — see that function for the formula and
     * why it's shared with [NdsClCalculator].
     */
    private fun computeCL(lb: Double): Double =
        computeNdsCL(lb, profile, designMaterial, adjustmentFactors, isGlulam)

    /**
     * NDS 5.3.6 Volume Factor CV for glulam.
     * CV = (21/L)^(1/x) * (12/d)^(1/x) * (5.125/b)^(1/x) <= 1.0
     * x = 10 for western species, 20 for southern pine glulam.
     */
    private fun computeCV(spanLength: Double): Double {
        if (!isGlulam || spanLength <= 0.0) return 1.0

        val d = profile.depth.inInches
        val b = if (profile is WoodProfile) profile.dressedWidth.inInches else 5.125
        val l = spanLength / 12.0  // Convert inches to feet for NDS formula

        if (l <= 0.0 || d <= 0.0 || b <= 0.0) return 1.0

        val x = if (material.species == WoodSpecies.GLULAM_SP_SP) 20.0 else 10.0

        val cv = (21.0 / l).pow(1.0 / x) *
                (12.0 / d).pow(1.0 / x) *
                (5.125 / b).pow(1.0 / x)

        return min(cv, 1.0)
    }

    /**
     * Size factor CF for sawn dimension lumber, looked up by nominal width,
     * thickness and grade — NOT the continuous `(12/d)^(1/9)` formula, which
     * is Table 4D's timber (5"x5" and larger) formula. Table 4A gives Fb,
     * Ft and Fc *separate* schedules (Fc's is much smaller than Fb's, and
     * Fb differs again for 4"-thick members), so each capacity path asks for
     * its own [property]. Southern Pine (Table 4B) has size built into its
     * per-width tabulated values instead. Delegates to [resolveSawnCf].
     */
    private fun computeSawnCF(property: NdsCfProperty): Double =
        resolveSawnCf(adjustmentFactors, designMaterial, profile, property)

    companion object {
        /** Time effect factor used when the combination isn't known (NDS Table N3, occupancy live load). */
        const val LAMBDA_DEFAULT = 0.8

        /** Cap on a point-path combined ratio — see [pointInteractionCheck]. */
        const val POINT_RATIO_CAP = 99.0

        /**
         * NDS 2018 Supplement Table 4A "Size Factors, CF" for dimension
         * lumber 2"-4" thick (all species except Southern Pine, whose
         * Table 4B builds size into the tabulated values).
         *
         * The table has separate columns: Fb for 2"-3" thick, Fb for 4"
         * thick, Ft, and Fc. Ft matches Fb(2"-3"); Fc has its own, smaller
         * schedule. [nominalThicknessIn] only matters for Fb (>= 4 selects
         * the 4"-thick column).
         *
         * Grade groups per the table: Select Structural / No.1 & Btr /
         * No.1 / No.2 / No.3 share one schedule; Stud has its own (and
         * defers to the No.3 schedule at 8" and wider); Construction and
         * Standard are 1.0; Utility is 1.0 at 4" wide and 0.4 (Fb, Ft) /
         * 0.6 (Fc) at 2"-3".
         */
        fun ndsTable4ASizeFactor(
            grade: WoodGrade,
            nominalWidthIn: Double,
            property: NdsCfProperty = NdsCfProperty.BENDING,
            nominalThicknessIn: Double = 2.0
        ): Double {
            fun selectStructuralGroupCf(width: Double): Double = when (property) {
                NdsCfProperty.COMPRESSION -> when {
                    width <= 4.0 -> 1.15
                    width <= 6.0 -> 1.1
                    width <= 8.0 -> 1.05
                    width <= 12.0 -> 1.0
                    else -> 0.9
                }
                NdsCfProperty.BENDING ->
                    if (nominalThicknessIn >= 4.0) when {
                        width <= 4.0 -> 1.5
                        width <= 5.0 -> 1.4
                        width <= 6.0 -> 1.3
                        width <= 8.0 -> 1.3
                        width <= 10.0 -> 1.2
                        width <= 12.0 -> 1.1
                        else -> 1.0
                    } else when {
                        width <= 4.0 -> 1.5
                        width <= 5.0 -> 1.4
                        width <= 6.0 -> 1.3
                        width <= 8.0 -> 1.2
                        width <= 10.0 -> 1.1
                        width <= 12.0 -> 1.0
                        else -> 0.9
                    }
                NdsCfProperty.TENSION -> when {
                    width <= 4.0 -> 1.5
                    width <= 5.0 -> 1.4
                    width <= 6.0 -> 1.3
                    width <= 8.0 -> 1.2
                    width <= 10.0 -> 1.1
                    width <= 12.0 -> 1.0
                    else -> 0.9
                }
            }
            return when (grade) {
                WoodGrade.SELECT_STRUCTURAL, WoodGrade.NO_1, WoodGrade.NO_2, WoodGrade.NO_3 ->
                    selectStructuralGroupCf(nominalWidthIn)
                WoodGrade.STUD -> when {
                    nominalWidthIn <= 4.0 ->
                        if (property == NdsCfProperty.COMPRESSION) 1.05 else 1.1
                    nominalWidthIn <= 6.0 -> 1.0
                    // "8\" & wider: Use No.3 Grade tabulated design values
                    // and size factors" — NDS Table 4A.
                    else -> selectStructuralGroupCf(nominalWidthIn)
                }
                WoodGrade.CONSTRUCTION, WoodGrade.STANDARD -> 1.0
                WoodGrade.UTILITY ->
                    if (nominalWidthIn <= 3.0) {
                        if (property == NdsCfProperty.COMPRESSION) 0.6 else 0.4
                    } else 1.0
                // Glulam grades (both the legacy MOE-style names and the
                // newer NDS Table 5A combination symbols) never reach this
                // path — glulam has no CF — but return 1.0 rather than
                // throw if ever called with one.
                else -> 1.0
            }
        }
    }

    // ------------------------------------------------------------------
    // NDS 3.4 — Shear
    // ------------------------------------------------------------------

    /**
     * NDS 3.4 horizontal shear capacity.
     * V'n = F'v * A * (2/3) for rectangular sections.
     * The 2/3 factor accounts for the parabolic shear distribution
     * in rectangular cross-sections (NDS 3.4.2).
     */
    private fun calculateShear(): LimitStateResult {
        val fv = designMaterial.referenceShear.inPsi
        val f = adjustmentFactors
        val fvAdj = f.adjustedShear(fv)
        val area  = profile.area.inIn2

        val fvTrace = DesignEquationTrace(
            symbolicEquation = "F'v = Fv x CD x CM x Ct x Ci",
            substitutedEquation = "F'v = ${fmt(fv, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x " +
                "${fmt(f.ct)} x ${fmt(f.ci)}",
            result = fmt(fvAdj, 0),
            units = "psi",
            codeReference = "NDS 3.4.1",
            variables = mapOf("Fv" to fv, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct, "Ci" to f.ci)
        )

        val vn = (2.0 / 3.0) * fvAdj * area
        val vnTrace = DesignEquationTrace(
            symbolicEquation = "Vn = (2/3) x F'v x A",
            substitutedEquation = "Vn = (2/3) x ${fmt(fvAdj, 0)} x ${fmt(area, 2)}",
            result = "${fmt(vn, 0)} lb (${kips(vn)} kips)",
            units = "lb",
            codeReference = "NDS 3.4.2",
            variables = mapOf("F'v" to fvAdj, "A" to area)
        )

        return LimitStateResult(vn, "Shear \u2014 Vn = (2/3) F'v A (NDS 3.4.2)", listOf(fvTrace, vnTrace))
    }

    // ------------------------------------------------------------------
    // NDS 3.7 — Compression Parallel to Grain
    // ------------------------------------------------------------------

    /**
     * F*c, CP and F'c — the compression development shared by the axial
     * capacity check and the NDS 3.9.2 combined-bending interaction (whose
     * first term is (fc / F'c)^2 and so needs the same F'c).
     */
    private data class CompressionParts(
        val fStarC: Double,
        val cp: Double,
        val fcAdj: Double,
        val traces: List<DesignEquationTrace>
    )

    private fun compressionParts(luStrong: Double, luWeak: Double): CompressionParts {
        val fc = designMaterial.referenceCompressionParallel.inPsi
        val f = adjustmentFactors

        val cpResult = computeCPTraced(luStrong, luWeak)
        val fStarC = fc * f.cd * f.cm * f.ct * computeSawnCF(NdsCfProperty.COMPRESSION) * f.ci
        val fcAdj = fStarC * cpResult.cp

        val fcAdjTrace = DesignEquationTrace(
            symbolicEquation = "F'c = F*c x CP",
            substitutedEquation = "F'c = ${fmt(fStarC, 0)} x ${fmt(cpResult.cp, 3)}",
            result = fmt(fcAdj, 0),
            units = "psi",
            codeReference = "NDS 3.7.1",
            variables = mapOf("F*c" to fStarC, "CP" to cpResult.cp)
        )
        return CompressionParts(fStarC, cpResult.cp, fcAdj, cpResult.traces + fcAdjTrace)
    }

    /**
     * NDS 3.7 compression capacity including column stability factor CP.
     * F'c = Fc * CD * CM * Ct * CF * Ci * CP
     * CP computed from NDS 3.7.1 using slenderness ratio le/d.
     */
    private fun calculateCompression(luStrong: Double, luWeak: Double): LimitStateResult {
        val area = profile.area.inIn2
        val parts = compressionParts(luStrong, luWeak)

        val pn = parts.fcAdj * area
        val pnTrace = DesignEquationTrace(
            symbolicEquation = "Pn = F'c x A",
            substitutedEquation = "Pn = ${fmt(parts.fcAdj, 0)} x ${fmt(area, 2)}",
            result = "${fmt(pn, 0)} lb (${kips(pn)} kips)",
            units = "lb",
            codeReference = "NDS 3.7",
            variables = mapOf("F'c" to parts.fcAdj, "A" to area)
        )

        return LimitStateResult(
            pn,
            "Compression (NDS 3.7, CP=${fmt(parts.cp, 3)})",
            parts.traces + pnTrace
        )
    }

    /** NDS 3.7.1 Column Stability Factor CP, with its derivation traced. */
    private data class CpResult(val cp: Double, val traces: List<DesignEquationTrace>)

    /**
     * NDS 3.7.1 Column Stability Factor CP.
     * CP = (1 + ratio) / (2c) - sqrt(((1 + ratio) / (2c))^2 - ratio/c)
     * where ratio = FcE / F*c, c = 0.8 (sawn) or 0.9 (glulam/SCL)
     */
    private fun computeCPTraced(luStrong: Double, luWeak: Double): CpResult {
        if (luStrong <= 0.0 && luWeak <= 0.0) return CpResult(1.0, emptyList())

        val d = profile.depth.inInches
        val b = if (profile is WoodProfile) profile.dressedWidth.inInches
        else sqrt(profile.area.inIn2)

        // NDS 3.7.1.2 / 3.7.1.4: le/d is evaluated for BOTH buckling planes —
        // le1/d1 in the plane of strong-axis bending, le2/d2 about the weak
        // axis — and the larger governs (each plane has its own unbraced
        // length; Ke = 1.0). It must not exceed 50.
        val slenderStrong = if (d > 0.0) luStrong / d else 0.0
        val slenderWeak = if (b > 0.0) luWeak / b else 0.0
        val weakGoverns = slenderWeak >= slenderStrong
        val slenderness = maxOf(slenderStrong, slenderWeak)
        if (slenderness > 50.0) return CpResult(0.0, emptyList())  // Exceeds NDS max — inadequate

        // Critical buckling stress FcE (NDS 3.7.1)
        val eMin      = woodAdjustedEmin(designMaterial, adjustmentFactors)
        val fce       = 0.822 * eMin / slenderness.pow(2)
        val fceTrace = DesignEquationTrace(
            symbolicEquation = "FcE = 0.822 x E'min / (le/d)^2",
            substitutedEquation = "FcE = 0.822 x ${fmt(eMin, 0)} / ${fmt(slenderness, 2)}^2",
            result = fmt(fce, 0),
            units = "psi",
            codeReference = "NDS 3.7.1",
            variables = mapOf("E'min" to eMin, "le/d" to slenderness)
        )
        val planeTrace = DesignEquationTrace(
            symbolicEquation = "le/d = max(le1/d1, le2/d2)   (Ke = 1.0)",
            substitutedEquation = "le1/d1 = ${fmt(luStrong)}/${fmt(d)} = ${fmt(slenderStrong, 2)};  " +
                "le2/d2 = ${fmt(luWeak)}/${fmt(b)} = ${fmt(slenderWeak, 2)}  ->  " +
                (if (weakGoverns) "weak axis governs" else "strong axis governs"),
            result = fmt(slenderness, 2),
            units = "",
            codeReference = "NDS 3.7.1.2, 3.7.1.4",
            variables = mapOf("le1" to luStrong, "d1" to d, "le2" to luWeak, "d2" to b)
        )

        // F*c = Fc with all adjustments except CP
        val f = adjustmentFactors
        val fcStar    = designMaterial.referenceCompressionParallel.inPsi *
                f.cd * f.cm * f.ct * computeSawnCF(NdsCfProperty.COMPRESSION) * f.ci

        if (fcStar <= 0.0) return CpResult(1.0, listOf(planeTrace, fceTrace))

        val ratio = fce / fcStar
        val c     = if (isGlulam) 0.90 else 0.80

        val term = (1.0 + ratio) / (2.0 * c)
        val cp = term - sqrt(term.pow(2) - ratio / c)
        val cpTrace = DesignEquationTrace(
            symbolicEquation = "CP = (1+(FcE/F*c))/2c - sqrt([(1+(FcE/F*c))/2c]^2 - (FcE/F*c)/c)",
            substitutedEquation = "CP = (1+${fmt(ratio, 3)})/${fmt(2 * c, 2)} - sqrt([(1+${fmt(ratio, 3)})/" +
                "${fmt(2 * c, 2)}]^2 - ${fmt(ratio, 3)}/${fmt(c, 2)})",
            result = fmt(cp, 3),
            units = "",
            codeReference = "NDS 3.7.1",
            variables = mapOf("FcE" to fce, "F*c" to fcStar, "FcE/F*c" to ratio, "c" to c)
        )

        return CpResult(cp, listOf(planeTrace, fceTrace, cpTrace))
    }

    // ------------------------------------------------------------------
    // NDS 3.8 — Tension Parallel to Grain
    // ------------------------------------------------------------------

    /**
     * NDS 3.8 tension capacity.
     * F't = Ft * CD * CM * Ct * CF * Ci
     * Net section area reduction for connections is a connection-level
     * check and not applied here. Computed inline (not via
     * [NdsAdjustmentFactors.adjustedTension]) because that helper uses
     * the raw, un-resolved CF field — see [resolveSawnCf].
     */
    private fun calculateTension(): LimitStateResult {
        val ft = designMaterial.referenceTensionParallel.inPsi
        val f = adjustmentFactors
        val cf = computeSawnCF(NdsCfProperty.TENSION)
        val ftAdj = adjustedTensionStress()
        val area = profile.area.inIn2

        val ftTrace = DesignEquationTrace(
            symbolicEquation = "F't = Ft x CD x CM x Ct x CF x Ci",
            substitutedEquation = "F't = ${fmt(ft, 0)} x ${fmt(f.cd)} x ${fmt(f.cm)} x " +
                "${fmt(f.ct)} x ${fmt(cf, 3)} x ${fmt(f.ci)}",
            result = fmt(ftAdj, 0),
            units = "psi",
            codeReference = "NDS 3.8.1, Table 4A",
            variables = mapOf(
                "Ft" to ft, "CD" to f.cd, "CM" to f.cm, "Ct" to f.ct, "CF" to cf, "Ci" to f.ci
            )
        )
        val pn = ftAdj * area
        val pnTrace = DesignEquationTrace(
            symbolicEquation = "Pn = F't x A",
            substitutedEquation = "Pn = ${fmt(ftAdj, 0)} x ${fmt(area, 2)}",
            result = "${fmt(pn, 0)} lb (${kips(pn)} kips)",
            units = "lb",
            codeReference = "NDS 3.8",
            variables = mapOf("F't" to ftAdj, "A" to area)
        )

        return LimitStateResult(pn, "Tension (NDS 3.8)", listOf(ftTrace, pnTrace))
    }

    // ------------------------------------------------------------------
    // NDS 3.9 — Combined bending and axial loading
    // ------------------------------------------------------------------

    /** Demand / capacity as magnitudes; zero capacity with real demand is a failure, not a pass. */
    private fun utilizationRatio(demandAbs: Double, capacity: Double): Double =
        if (capacity > 0.0) demandAbs / capacity
        else if (demandAbs > 0.0) Double.POSITIVE_INFINITY
        else 0.0

    /** F't = Ft x CD x CM x Ct x CF x Ci (psi) — shared by the tension capacity and NDS 3.9.1. */
    private fun adjustedTensionStress(): Double {
        val f = adjustmentFactors
        return designMaterial.referenceTensionParallel.inPsi * f.cd * f.cm * f.ct *
            computeSawnCF(NdsCfProperty.TENSION) * f.ci
    }

    private fun fmtOrNg(v: Double, d: Int = 3): String =
        if (v.isNaN() || v.isInfinite()) "N.G." else fmt(v, d)

    /**
     * Combined-stress checks for the actual stresses at this station:
     *
     *  - axial tension + bending -> NDS 3.9.1 (Eq. 3.9-1, 3.9-2)
     *  - axial compression + bending, or biaxial bending alone (NDS
     *    3.3.3.10 sends biaxial bending to 3.9.2) -> NDS 3.9.2 (Eq. 3.9-3, 3.9-4)
     *
     * fb1 is the edgewise (strong axis, x-x) bending stress and fb2 the
     * flatwise (weak axis, y-y) bending stress, taken from the member's
     * primary moment (per [memberIsStrongAxis]) and the moment about the
     * other axis ([StationDemand.momentY]). Stresses use the gross section.
     * Returns nothing when there's no bending, or no axial load and
     * bending about only one axis (the ordinary flexure check covers that).
     */
    private fun buildCombinedChecks(
        demand: StationDemand,
        u: Unbraced,
        kBend: Double,
        kTens: Double,
        kComp: Double,
        isLrfd: Boolean
    ): List<CombinedStressCheck> {
        val area = profile.area.inIn2
        if (area <= 0.0) return emptyList()

        val axial = demand.axial.pounds
        val primaryM = abs(demand.moment.lbIn)
        val otherM = abs(demand.momentY.lbIn)

        val strongAx = bendingAxis(true, u)
        val weakAx = bendingAxis(false, u)

        val mStrong = if (memberIsStrongAxis) primaryM else otherM
        val mWeak = if (memberIsStrongAxis) otherM else primaryM
        val fb1 = if (strongAx.s > 0.0) mStrong / strongAx.s else 0.0
        val fb2 = if (weakAx.s > 0.0) mWeak / weakAx.s else 0.0
        if (fb1 <= 0.0 && fb2 <= 0.0) return emptyList()

        val notes = mutableListOf<String>()
        if (isLrfd) {
            notes += "LRFD: adjusted design values are scaled by lambda x phi exactly as in the " +
                "individual member checks (lambda = 0.8; phi = 0.85 bending, 0.80 tension, " +
                "0.90 compression). The NDS Appendix N format conversion factor KF is not applied."
        }

        return when {
            axial > 0.0 -> listOf(
                combinedTension(axial, area, mStrong, mWeak, fb1, fb2, strongAx, weakAx, kBend, kTens, notes)
            )
            axial < 0.0 || (fb1 > 0.0 && fb2 > 0.0) -> listOf(
                combinedCompression(
                    axial, area, u, mStrong, mWeak, fb1, fb2, strongAx, weakAx, kBend, kComp, notes
                )
            )
            else -> emptyList()
        }
    }

    private fun stressTrace(name: String, moment: Double, axis: BendingAxis, stress: Double) =
        DesignEquationTrace(
            symbolicEquation = "$name = M / ${if (axis.strong) "Sx" else "Sy"}",
            substitutedEquation = "$name = ${fmt(moment, 0)} / ${fmt(axis.s, 2)}",
            result = fmt(stress, 1),
            units = "psi",
            codeReference = "NDS 3.9",
            variables = mapOf("M" to moment, if (axis.strong) "Sx" to axis.s else "Sy" to axis.s)
        )

    /** NDS 3.9.1 — bending and axial tension. */
    private fun combinedTension(
        axialLb: Double, area: Double, mStrong: Double, mWeak: Double,
        fb1: Double, fb2: Double, strongAx: BendingAxis, weakAx: BendingAxis,
        kBend: Double, kTens: Double, notes: List<String>
    ): CombinedStressCheck {
        val inf = Double.POSITIVE_INFINITY
        val ft = abs(axialLb) / area
        val ftAdj = adjustedTensionStress() * kTens
        val allNotes = notes.toMutableList()
        val traces = mutableListOf<DesignEquationTrace>()
        val equations = mutableListOf<InteractionEquationResult>()

        traces += DesignEquationTrace(
            symbolicEquation = "ft = T / A",
            substitutedEquation = "ft = ${fmt(abs(axialLb), 0)} / ${fmt(area, 2)}",
            result = fmt(ft, 1),
            units = "psi",
            codeReference = "NDS 3.9.1",
            variables = mapOf("T" to abs(axialLb), "A" to area)
        )
        calculateTension().traces.firstOrNull()?.let { traces += it }

        if (fb1 > 0.0) {
            traces += stressTrace("fb1", mStrong, strongAx, fb1)
            strongAx.fStarTrace?.let { traces += it }
            strongAx.fStarStarTrace?.let { traces += it }
        }
        if (fb2 > 0.0) {
            traces += stressTrace("fb2", mWeak, weakAx, fb2)
            if (!weakAx.available) allNotes += weakAx.limitState
            weakAx.fStarTrace?.let { traces += it }
        }

        // Eq. 3.9-1:  ft/F't + fb/F*b <= 1.0
        val fStar1 = strongAx.fStar * kBend
        val fStar2 = weakAx.fStar * kBend
        var v1 = if (ftAdj > 0.0) ft / ftAdj else if (ft > 0.0) inf else 0.0
        val symbolic = StringBuilder("ft/F't")
        val substituted = StringBuilder("${fmt(ft, 1)}/${fmt(ftAdj, 0)}")
        val vars = linkedMapOf("ft" to ft, "F't" to ftAdj)
        if (fb1 > 0.0) {
            v1 += if (fStar1 > 0.0) fb1 / fStar1 else inf
            symbolic.append(" + fb1/F*b1")
            substituted.append(" + ${fmt(fb1, 1)}/${fmt(fStar1, 0)}")
            vars["fb1"] = fb1; vars["F*b1"] = fStar1
        }
        if (fb2 > 0.0) {
            v1 += if (weakAx.available && fStar2 > 0.0) fb2 / fStar2 else inf
            symbolic.append(" + fb2/F*b2")
            substituted.append(" + ${fmt(fb2, 1)}/${fmt(fStar2, 0)}")
            vars["fb2"] = fb2; vars["F*b2"] = fStar2
        }
        equations += InteractionEquationResult("Eq. 3.9-1", v1)
        traces += DesignEquationTrace(
            symbolicEquation = "$symbolic <= 1.0",
            substitutedEquation = "$substituted = ${fmtOrNg(v1)}",
            result = fmtOrNg(v1),
            units = "",
            codeReference = "NDS 3.9.1, Eq. 3.9-1",
            variables = vars
        )

        // Eq. 3.9-2:  (fb - ft)/F**b <= 1.0   (net stress on the compression face)
        if (fb1 > 0.0) {
            val fss1 = strongAx.fStarStar * kBend
            val v = if (fss1 > 0.0) (fb1 - ft) / fss1 else inf
            equations += InteractionEquationResult("Eq. 3.9-2 (strong axis)", v)
            traces += DesignEquationTrace(
                symbolicEquation = "(fb1 - ft)/F**b1 <= 1.0",
                substitutedEquation = "(${fmt(fb1, 1)} - ${fmt(ft, 1)})/${fmt(fss1, 0)} = ${fmtOrNg(v)}",
                result = fmtOrNg(v),
                units = "",
                codeReference = "NDS 3.9.1, Eq. 3.9-2",
                variables = mapOf("fb1" to fb1, "ft" to ft, "F**b1" to fss1)
            )
        }
        if (fb2 > 0.0 && weakAx.available) {
            val fss2 = weakAx.fStarStar * kBend
            val v = if (fss2 > 0.0) (fb2 - ft) / fss2 else inf
            equations += InteractionEquationResult("Eq. 3.9-2 (weak axis)", v)
            traces += DesignEquationTrace(
                symbolicEquation = "(fb2 - ft)/F**b2 <= 1.0",
                substitutedEquation = "(${fmt(fb2, 1)} - ${fmt(ft, 1)})/${fmt(fss2, 0)} = ${fmtOrNg(v)}",
                result = fmtOrNg(v),
                units = "",
                codeReference = "NDS 3.9.1, Eq. 3.9-2",
                variables = mapOf("fb2" to fb2, "ft" to ft, "F**b2" to fss2)
            )
        }
        if (fb1 > 0.0 && fb2 > 0.0) {
            allNotes += "Bending about both axes with axial tension: NDS 3.9.1 is written for a single " +
                "bending stress fb. The weak-axis (fb2) term is added to Eq. 3.9-1 and checked in " +
                "Eq. 3.9-2 as a conservative extension."
        }

        val governing = equations.maxByOrNull { it.value } ?: equations.first()
        return CombinedStressCheck(
            label = "Bending + Axial Tension",
            clause = "NDS 3.9.1",
            utilization = governing.value.coerceAtLeast(0.0),
            governingEquation = governing.equation,
            equations = equations,
            traces = traces,
            notes = allNotes
        )
    }

    /** NDS 3.9.2 — bending and axial compression (and biaxial bending, per 3.3.3.10). */
    private fun combinedCompression(
        axialLb: Double, area: Double, u: Unbraced,
        mStrong: Double, mWeak: Double, fb1: Double, fb2: Double,
        strongAx: BendingAxis, weakAx: BendingAxis,
        kBend: Double, kComp: Double, notes: List<String>
    ): CombinedStressCheck {
        val inf = Double.POSITIVE_INFINITY
        val fc = if (axialLb < 0.0) abs(axialLb) / area else 0.0
        val allNotes = notes.toMutableList()
        val traces = mutableListOf<DesignEquationTrace>()
        val equations = mutableListOf<InteractionEquationResult>()
        val problems = mutableListOf<String>()

        val parts = compressionParts(u.colStrong, u.colWeak)
        val fcAdj = parts.fcAdj * kComp

        // d1 = wide face dimension (edgewise bending), d2 = narrow face (flatwise) — NDS Fig. 3H.
        val d1 = profile.depth.inInches
        val d2 = if (profile is WoodProfile) profile.dressedWidth.inInches else sqrt(area)
        // Each buckling plane has its own effective length (Ke = 1.0 on the
        // unbraced length for that axis).
        val le1 = u.colStrong
        val le2 = u.colWeak
        val eMinAdj = woodAdjustedEmin(designMaterial, adjustmentFactors)
        val fcE1 = if (le1 > 0.0 && d1 > 0.0) 0.822 * eMinAdj / (le1 / d1).pow(2) else inf
        val fcE2 = if (le2 > 0.0 && d2 > 0.0) 0.822 * eMinAdj / (le2 / d2).pow(2) else inf

        val stab = computeNdsBeamStability(u.strongBend, profile, designMaterial, adjustmentFactors, isGlulam, aboutStrongAxis = true)
        val fbE = stab?.fbE ?: inf

        val fb1Adj = strongAx.fbAdj * kBend
        val fb2Adj = weakAx.fbAdj * kBend

        // ---- stresses and their development
        if (fc > 0.0) {
            traces += DesignEquationTrace(
                symbolicEquation = "fc = P / A",
                substitutedEquation = "fc = ${fmt(abs(axialLb), 0)} / ${fmt(area, 2)}",
                result = fmt(fc, 1),
                units = "psi",
                codeReference = "NDS 3.9.2",
                variables = mapOf("P" to abs(axialLb), "A" to area)
            )
            traces += parts.traces
        }
        if (fb1 > 0.0) {
            traces += stressTrace("fb1", mStrong, strongAx, fb1)
            strongAx.traces.lastOrNull()?.let { traces += it }
        }
        if (fb2 > 0.0) {
            traces += stressTrace("fb2", mWeak, weakAx, fb2)
            if (weakAx.available) weakAx.traces.lastOrNull()?.let { traces += it }
            else { allNotes += weakAx.limitState; problems += "weak-axis bending value (Fby) is not available" }
        }

        if (fcE1.isFinite() && (fb1 > 0.0 || fb2 > 0.0)) {
            traces += DesignEquationTrace(
                symbolicEquation = "FcE1 = 0.822 E'min / (le1/d1)^2",
                substitutedEquation = "FcE1 = 0.822 x ${fmt(eMinAdj, 0)} / (${fmt(le1)}/${fmt(d1)})^2",
                result = fmt(fcE1, 0),
                units = "psi",
                codeReference = "NDS 3.9.2",
                variables = mapOf("E'min" to eMinAdj, "le1" to le1, "d1" to d1)
            )
        }
        if (fcE2.isFinite() && (fb2 > 0.0)) {
            traces += DesignEquationTrace(
                symbolicEquation = "FcE2 = 0.822 E'min / (le2/d2)^2",
                substitutedEquation = "FcE2 = 0.822 x ${fmt(eMinAdj, 0)} / (${fmt(le2)}/${fmt(d2)})^2",
                result = fmt(fcE2, 0),
                units = "psi",
                codeReference = "NDS 3.9.2",
                variables = mapOf("E'min" to eMinAdj, "le2" to le2, "d2" to d2)
            )
        }
        if (fb1 > 0.0 && fb2 > 0.0 && stab != null) {
            traces += DesignEquationTrace(
                symbolicEquation = "FbE = 1.20 E'min / RB^2",
                substitutedEquation = "FbE = 1.20 x ${fmt(stab.eMinAdj, 0)} / ${fmt(stab.rb, 2)}^2",
                result = fmt(stab.fbE, 0),
                units = "psi",
                codeReference = "NDS 3.9.2, 3.3.3.8",
                variables = mapOf("E'min" to stab.eMinAdj, "RB" to stab.rb)
            )
        }

        // ---- applicability limits stated with Eq. 3.9-3 / 3.9-4
        if (fb1 > 0.0 && fc >= fcE1) problems += "fc >= FcE1 (member is unstable about the strong axis)"
        if (fb2 > 0.0 && fc >= fcE2) problems += "fc >= FcE2 (member is unstable about the weak axis)"
        if (fb1 > 0.0 && fb2 > 0.0 && fb1 >= fbE) problems += "fb1 >= FbE (biaxial bending limit)"
        if (fc > 0.0 && fcAdj <= 0.0) problems += "F'c is zero (slenderness exceeds the NDS limit of 50)"
        if (fb1 > 0.0 && fb1Adj <= 0.0) problems += "F'b1 is zero"
        if (fb2 > 0.0 && weakAx.available && fb2Adj <= 0.0) problems += "F'b2 is zero"
        if (strongAx.rbExceedsLimit && fb1 > 0.0) problems += "RB exceeds 50 (NDS 3.3.3.7)"

        // ---- Eq. 3.9-4:  fc/FcE2 + (fb1/FbE)^2 < 1.0   (biaxial bending)
        if (fb1 > 0.0 && fb2 > 0.0) {
            val v4 = fc / fcE2 + (fb1 / fbE).pow(2)
            equations += InteractionEquationResult("Eq. 3.9-4", v4, isStrictLimit = true)
            traces += DesignEquationTrace(
                symbolicEquation = "fc/FcE2 + (fb1/FbE)^2 < 1.0",
                substitutedEquation = "${fmt(fc, 1)}/${fmtOrNg(fcE2, 0)} + (${fmt(fb1, 1)}/${fmtOrNg(fbE, 0)})^2 = ${fmtOrNg(v4)}",
                result = fmtOrNg(v4),
                units = "",
                codeReference = "NDS 3.9.2, Eq. 3.9-4",
                variables = buildMap {
                    put("fc", fc); put("fb1", fb1)
                    if (fcE2.isFinite()) put("FcE2", fcE2)
                    if (fbE.isFinite()) put("FbE", fbE)
                }
            )
        }

        // ---- Eq. 3.9-3
        val v3: Double
        if (problems.isNotEmpty()) {
            v3 = inf
            allNotes += "Eq. 3.9-3 cannot be satisfied: " + problems.joinToString("; ") + "."
        } else {
            val t1 = if (fc > 0.0) (fc / fcAdj).pow(2) else 0.0
            val t2 = if (fb1 > 0.0) fb1 / (fb1Adj * (1.0 - fc / fcE1)) else 0.0
            val t3 = if (fb2 > 0.0) fb2 / (fb2Adj * (1.0 - fc / fcE2 - (fb1 / fbE).pow(2))) else 0.0
            v3 = t1 + t2 + t3

            val symbolic = StringBuilder()
            val substituted = StringBuilder()
            val vars = linkedMapOf<String, Double>()
            fun addTerm(sym: String, sub: String) {
                if (symbolic.isNotEmpty()) { symbolic.append(" + "); substituted.append(" + ") }
                symbolic.append(sym); substituted.append(sub)
            }
            if (fc > 0.0) {
                addTerm("[fc/F'c]^2", "[${fmt(fc, 1)}/${fmt(fcAdj, 0)}]^2")
                vars["fc"] = fc; vars["F'c"] = fcAdj
            }
            if (fb1 > 0.0) {
                addTerm("fb1/(F'b1[1-(fc/FcE1)])",
                    "${fmt(fb1, 1)}/(${fmt(fb1Adj, 0)}[1-(${fmt(fc, 1)}/${fmtOrNg(fcE1, 0)})])")
                vars["fb1"] = fb1; vars["F'b1"] = fb1Adj
                if (fcE1.isFinite()) vars["FcE1"] = fcE1
            }
            if (fb2 > 0.0) {
                addTerm("fb2/(F'b2[1-(fc/FcE2)-(fb1/FbE)^2])",
                    "${fmt(fb2, 1)}/(${fmt(fb2Adj, 0)}[1-(${fmt(fc, 1)}/${fmtOrNg(fcE2, 0)})-" +
                        "(${fmt(fb1, 1)}/${fmtOrNg(fbE, 0)})^2])")
                vars["fb2"] = fb2; vars["F'b2"] = fb2Adj
                if (fcE2.isFinite()) vars["FcE2"] = fcE2
                if (fbE.isFinite()) vars["FbE"] = fbE
            }
            traces += DesignEquationTrace(
                symbolicEquation = "$symbolic <= 1.0",
                substitutedEquation = "$substituted = ${fmtOrNg(v3)}",
                result = fmtOrNg(v3),
                units = "",
                codeReference = "NDS 3.9.2, Eq. 3.9-3",
                variables = vars
            )
        }
        equations.add(0, InteractionEquationResult("Eq. 3.9-3", v3))

        val governing = equations.maxByOrNull { it.value } ?: equations.first()
        return CombinedStressCheck(
            label = if (fc > 0.0) "Bending + Axial Compression" else "Biaxial Bending",
            clause = "NDS 3.9.2",
            utilization = governing.value,
            governingEquation = governing.equation,
            equations = equations,
            traces = traces,
            notes = allNotes
        )
    }
    /**
     * NDS 3.9 combined-stress ratio for the point-by-point capacity path
     * (the header / governing-utilization diagram), as a [CodeCheck] to plug
     * into [CapacityEngine.evaluate]'s `interactionCheckProvider`. Without it
     * that path judged wood by AISC Eq. H1-1.
     *
     * Combined checks only exist where a station has bending with axial load,
     * or bending about both axes; elsewhere the individual checks already
     * cover the member and this returns 0. Where one applies, the worst of
     * the 3.9 checks is returned. An infinite ratio (a precondition such as
     * fc < FcE violated) is capped at [POINT_RATIO_CAP] so diagrams stay
     * finite; the Design tab's detailed card still shows it as N.G.
     */
    fun pointInteractionCheck(demand: StationDemand, methodology: DesignMethodology): CodeCheck {
        val label = "NDS 3.9 Combined"
        val mx = abs(demand.moment.inLbIn)
        val my = abs(demand.momentY.inLbIn)
        val axial = abs(demand.axial.inPoundsForce)
        val applies = ((mx > 0.0 || my > 0.0) && axial > 0.0) || (mx > 0.0 && my > 0.0)
        if (!applies) return CodeCheck(label, 0.0, 1.0, 0.0)
        val worst = evaluateDetailed(demand, methodology).combinedChecks.maxByOrNull { it.utilization }
            ?: return CodeCheck(label, 0.0, 1.0, 0.0)
        val ratio = worst.utilization.coerceAtMost(POINT_RATIO_CAP)
        return CodeCheck("${worst.clause} ${worst.governingEquation}", ratio, 1.0, ratio)
    }
}
