package com.lz.solver.material

import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.regulatory.nds.LiveLoadKind
import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.regulatory.nds.NdsLoadDurationAssumptions
import com.lz.model.structural.*
import com.lz.model.units.*
import com.lz.solver.analysis.MemberAnalysisSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Expected values come from the NDS tables themselves (Table 2.3.2 CD,
 * Table N3 lambda, 5.3.6 CV) and from hand arithmetic done separately, not
 * from the code under test.
 */
class NdsAssumptionsBlockAndSpanTest {

    private val D = LoadCategory.DEAD
    private val L = LoadCategory.LIVE
    private val S = LoadCategory.SNOW
    private val LR = LoadCategory.ROOF_LIVE
    private val R = LoadCategory.RAIN
    private val W = LoadCategory.WIND

    private fun asd(
        loads: Set<LoadCategory>,
        a: NdsLoadDurationAssumptions = NdsLoadDurationAssumptions(),
        treated: Boolean = false
    ) = NdsLoadDuration.resolve(loads.associateWith { 1.0 }, DesignMethodology.ASD, NdsEdition.NDS_2015, a, treated)!!.factor

    private fun lrfd(
        loads: Map<LoadCategory, Double>,
        a: NdsLoadDurationAssumptions = NdsLoadDurationAssumptions(),
        treated: Boolean = false
    ) = NdsLoadDuration.resolve(loads, DesignMethodology.LRFD, NdsEdition.NDS_2015, a, treated)!!.factor

    // ---------------- CD assumptions (ASD) ----------------

    @Test
    fun `default assumptions give the Table 2_3_2 values`() {
        assertEquals(0.9, asd(setOf(D)), 0.0)
        assertEquals(1.0, asd(setOf(D, L)), 0.0)
        assertEquals(1.15, asd(setOf(D, S)), 0.0)
        assertEquals(1.25, asd(setOf(D, LR)), 0.0)
        assertEquals(1.0, asd(setOf(D, R)), 0.0)
        assertEquals(1.6, asd(setOf(D, W)), 0.0)
        assertEquals(1.25, asd(setOf(D, S, LR)), 0.0)
    }

    @Test
    fun `the user can change the snow roof live rain and live CD`() {
        val a = NdsLoadDurationAssumptions(liveCd = 0.9, roofLiveCd = 1.15, snowCd = 1.0, rainCd = 1.25)
        assertEquals(1.0, asd(setOf(D, S), a), 0.0)       // longer snow duration
        assertEquals(1.15, asd(setOf(D, LR), a), 0.0)
        assertEquals(1.25, asd(setOf(D, R), a), 0.0)
        assertEquals(0.9, asd(setOf(D, L), a), 0.0)
        assertEquals(1.15, asd(setOf(D, S, LR), a), 0.0)   // largest CD of the loads acting governs
        assertEquals(1.6, asd(setOf(D, W, S), a), 0.0)      // wind stays fixed at 1.6
    }

    @Test
    fun `a treated member is limited to CD 1_6 in resolve and in the factors`() {
        val a = NdsLoadDurationAssumptions(roofLiveCd = 2.0)
        assertEquals(2.0, asd(setOf(D, LR), a, treated = false), 0.0)
        assertEquals(1.6, asd(setOf(D, LR), a, treated = true), 0.0)
        assertEquals(1.6, NdsAdjustmentFactors(cd = 2.0, treatedMember = true).effectiveCd, 0.0)
        assertEquals(2.0, NdsAdjustmentFactors(cd = 2.0).effectiveCd, 0.0)
        assertEquals(1.25, NdsAdjustmentFactors(cd = 1.25, treatedMember = true).effectiveCd, 0.0)
    }

    @Test
    fun `assumption values outside 0 to 2 are reported invalid`() {
        assertTrue(NdsLoadDurationAssumptions().isValid)
        assertTrue(!NdsLoadDurationAssumptions(snowCd = 0.0).isValid)
        assertTrue(!NdsLoadDurationAssumptions(rainCd = 2.5).isValid)
        assertTrue(!NdsLoadDurationAssumptions(liveCd = Double.NaN).isValid)
    }

    // ---------------- lambda (Table N3, 2015) ----------------

    @Test
    fun `lambda follows Table N3 for every row`() {
        assertEquals(0.6, lrfd(mapOf(D to 1.4)), 0.0)
        assertEquals(0.8, lrfd(mapOf(D to 1.2, L to 1.6, S to 0.5)), 0.0)          // occupancy
        assertEquals(0.8, lrfd(mapOf(D to 1.2, S to 1.6, L to 1.0)), 0.0)          // 1.2D + 1.6S + L
        assertEquals(0.8, lrfd(mapOf(D to 1.2, LR to 1.6, W to 0.5)), 0.0)         // 1.2D + 1.6Lr + 0.5W
        assertEquals(1.0, lrfd(mapOf(D to 1.2, W to 1.0, L to 1.0, S to 0.5)), 0.0)
        assertEquals(1.0, lrfd(mapOf(D to 1.2, LoadCategory.SEISMIC to 1.0, L to 1.0, S to 0.2)), 0.0)
        assertEquals(1.0, lrfd(mapOf(D to 0.9, W to 1.0)), 0.0)
        assertEquals(1.0, lrfd(mapOf(D to 0.9, LoadCategory.SEISMIC to 1.0)), 0.0)
    }

    @Test
    fun `lambda uses the live load type and reversed wind keeps lambda 1_0`() {
        val loads = mapOf(D to 1.2, L to 1.6, S to 0.5)
        assertEquals(0.7, lrfd(loads, NdsLoadDurationAssumptions(liveKind = LiveLoadKind.STORAGE)), 0.0)
        assertEquals(0.8, lrfd(loads, NdsLoadDurationAssumptions(liveKind = LiveLoadKind.OCCUPANCY)), 0.0)
        assertEquals(1.25, lrfd(loads, NdsLoadDurationAssumptions(liveKind = LiveLoadKind.IMPACT)), 0.0)
        // Table N3 note 1: above 1.0 does not apply to treated members
        assertEquals(1.0, lrfd(loads, NdsLoadDurationAssumptions(liveKind = LiveLoadKind.IMPACT), treated = true), 0.0)
        // the live type only matters in the 1.6L row
        assertEquals(0.8, lrfd(mapOf(D to 1.2, S to 1.6, L to 1.0), NdsLoadDurationAssumptions(liveKind = LiveLoadKind.STORAGE)), 0.0)
        // reversed wind (negative factor) is still a strength-level wind combination
        assertEquals(1.0, lrfd(mapOf(D to 0.9, W to -1.0)), 0.0)
    }

    // ---------------- hard block ----------------

    @Test
    fun `wood design is blocked for editions without sourced design values`() {
        assertNull(NdsCodeData.blockReason(NdsEdition.NDS_2018))
        assertNotNull(NdsCodeData.blockReason(NdsEdition.NDS_2015))
        assertNotNull(NdsCodeData.blockReason(NdsEdition.NDS_2024))
        NdsCodeData.requireUsable(NdsEdition.NDS_2018)
        var thrown = false
        try { NdsCodeData.requireUsable(NdsEdition.NDS_2015) } catch (e: CodeDataBlockedException) { thrown = true }
        assertTrue(thrown)
    }

    // ---------------- zero-moment span (NDS 5.3.6) ----------------

    private val xs = (0..12).map { it * 10.0 }

    @Test
    fun `simple span stations see the support to support length`() {
        val l = 120.0
        val spans = MemberAnalysisSolver.zeroMomentSpans(xs, xs.map { it * (l - it) / 2 })
        assertNull(spans.first()); assertNull(spans.last())          // moment is zero there
        (1..11).forEach { assertEquals(120.0, spans[it]!!, 1e-9) }
    }

    @Test
    fun `an overhang station sees the whole length where only one zero brackets it`() {
        // M = 0 at x = 0 and x = 80 (inflection); negative beyond. Stations at 10-unit steps.
        val ms = xs.map { if (it <= 80.0) it * (80.0 - it) else -(it - 80.0) * 10.0 }
        val spans = MemberAnalysisSolver.zeroMomentSpans(xs, ms)
        assertEquals(80.0, spans[4]!!, 1e-9)       // x = 40 between the zeros at 0 and 80
        assertEquals(120.0, spans[10]!!, 1e-9)     // x = 100: zero on the left only -> full length
    }

    @Test
    fun `an interpolated zero is used between stations`() {
        // M(x) = x - 25 at stations 0,10,20,30,40: zero at x = 25, only one zero -> full length 40
        val spans = MemberAnalysisSolver.zeroMomentSpans(listOf(0.0, 10.0, 20.0, 30.0, 40.0),
            listOf(-25.0, -15.0, -5.0, 5.0, 15.0))
        assertEquals(40.0, spans[1]!!, 1e-9)
        // two zeros: M = 0, 10, 20 (x = 0,10,20 -> zero at x=0), then -10 at 30, -20 at 40: crossing at 20 exactly
        val s2 = MemberAnalysisSolver.zeroMomentSpans(listOf(0.0, 10.0, 20.0, 30.0), listOf(0.0, 10.0, 0.0, -10.0))
        assertEquals(20.0, s2[1]!!, 1e-9)
        // sign change between stations: 10 at x=10, -30 at x=20 -> zero at 12.5
        val s3 = MemberAnalysisSolver.zeroMomentSpans(listOf(0.0, 10.0, 20.0), listOf(0.0, 10.0, -30.0))
        assertEquals(12.5, s3[1]!!, 1e-9)
    }

    @Test
    fun `a zero moment diagram gives no span`() {
        assertTrue(MemberAnalysisSolver.zeroMomentSpans(xs, xs.map { 0.0 }).all { it == null })
    }

    // ---------------- CV uses that span, not the unbraced length ----------------

    private val spanId: UUID = UUID.randomUUID()
    private val glProfile = WoodProfile(
        id = "gl", designation = "5.125x12",
        nominalWidth = 5.125.inches, nominalDepth = 12.0.inches,
        dressedWidth = 5.125.inches, dressedDepth = 12.0.inches
    )
    private val glulam = MaterialGrade.Wood(
        id = "gl", name = "Glulam 16F-V3",
        species = WoodSpecies.GLULAM_DF_DF, grade = WoodGrade.G_16F_V3,
        referenceBending = 1600.0.psi, referenceShear = 265.0.psi,
        referenceCompressionParallel = 1500.0.psi, referenceCompressionPerp = 560.0.psi,
        referenceTensionParallel = 975.0.psi,
        modulusOfElasticity = 1600000.0.psi, shearModulus = 100000.0.psi, densityPcf = 31.2
    )

    private fun strongCapacity(spanFt: Double?): Double {
        val d = StationDemand(
            spanId = spanId, x = 180.0.inches, moment = Moment(1000.0), shear = Force(0.0),
            lbTop = 0.0.inches, lbBottom = 0.0.inches, compressionFlange = Flange.TOP,
            momentSpanLength = spanFt?.let { (it * 12.0).inches }
        )
        return NdsWoodCapacityCalculator(glProfile, glulam).evaluateDetailed(d, DesignMethodology.ASD)
            .momentCheck.capacity.lbIn
    }

    @Test
    fun `glulam CV takes L between zero-moment points`() {
        // CV = (21/30)^(1/10) (12/12)^(1/10) (5.125/5.125)^(1/10) = 0.964961; Sx = 5.125 x 12^2 / 6 = 123.0
        assertEquals(1600.0 * 0.9649610951198176 * 123.0, strongCapacity(30.0), 1e-3)
        // L = 15 ft: CV would exceed 1, limited to 1.0
        assertEquals(1600.0 * 123.0, strongCapacity(15.0), 1e-3)
    }

    @Test
    fun `Cr does not enter glulam F-star-b for CL`() {
        val withCr = computeNdsBeamStability(120.0, glProfile, glulam, NdsAdjustmentFactors(cr = 1.15), true)!!
        val noCr = computeNdsBeamStability(120.0, glProfile, glulam, NdsAdjustmentFactors(), true)!!
        assertEquals(noCr.fStarB, withCr.fStarB, 1e-9)
        assertEquals(noCr.cl, withCr.cl, 1e-12)
    }

    // ---------------- reversal and duplicates ----------------

    private fun combo(name: String, f: Map<LoadCategory, Double>) = LoadCombination(
        id = name, name = name, methodology = DesignMethodology.LRFD, type = CombinationType.STRENGTH,
        equationText = name, factors = f, codeReference = ""
    )

    @Test
    fun `wind combinations get a reversed twin`() {
        val out = MemberAnalysisSolver.expandCombinations(
            listOf(combo("0.9D+W", mapOf(D to 0.9, W to 1.0)), combo("1.4D", mapOf(D to 1.4))),
            setOf(D, W)
        )
        assertEquals(3, out.size)
        val rev = out.first { it.name.contains("reversed") }
        assertEquals(-1.0, rev.factors[W]!!, 0.0)
        assertEquals(0.9, rev.factors[D]!!, 0.0)
        assertTrue(out.none { it.name == "1.4D" && it.factors.containsKey(W) })
    }

    @Test
    fun `no reversal when wind is not applied`() {
        val out = MemberAnalysisSolver.expandCombinations(listOf(combo("0.9D+W", mapOf(D to 0.9, W to 1.0))), setOf(D))
        assertEquals(1, out.size)
    }

    @Test
    fun `combinations differing only by unapplied loads are not duplicated`() {
        val out = MemberAnalysisSolver.expandCombinations(
            listOf(
                combo("1.2D+1.6L+0.5Lr", mapOf(D to 1.2, L to 1.6, LR to 0.5)),
                combo("1.2D+1.6L+0.5S", mapOf(D to 1.2, L to 1.6, S to 0.5)),
                combo("1.2D+1.6L+0.5R", mapOf(D to 1.2, L to 1.6, R to 0.5)),
                combo("1.2D+1.6S+L", mapOf(D to 1.2, S to 1.6, L to 1.0))
            ),
            setOf(D, L)
        )
        assertEquals(listOf("1.2D+1.6L+0.5Lr", "1.2D+1.6S+L"), out.map { it.name }.let { names ->
            // the second kept combination differs (L at 1.0 rather than 1.6)
            names
        })
    }
}
