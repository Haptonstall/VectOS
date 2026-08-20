package com.lz.solver.material

import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.structural.*
import com.lz.model.units.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class NdsClCalculatorTest {

    // Nominal 2x10 DF-L No.2, dressed dimensions
    private val profile = WoodProfile(
        id = "2x10",
        designation = "2x10",
        nominalWidth = 2.0.inches,
        nominalDepth = 10.0.inches,
        dressedWidth = 1.5.inches,
        dressedDepth = 9.25.inches
    )

    private val material = MaterialGrade.Wood(
        id = "df-l-no2",
        name = "DF-L No.2",
        species = WoodSpecies.DF_L,
        grade = WoodGrade.NO_2,
        referenceBending = 875.0.psi,
        referenceShear = 180.0.psi,
        referenceCompressionParallel = 1350.0.psi,
        referenceCompressionPerp = 625.0.psi,
        referenceTensionParallel = 575.0.psi,
        modulusOfElasticity = 1600000.0.psi,
        shearModulus = 100000.0.psi,
        densityPcf = 35.0
    )

    private val spanId: UUID = UUID.randomUUID()

    private fun demandAt(x: Double) = StationDemand(
        spanId = spanId,
        x = x.inches,
        moment = Moment(0.0),
        shear = Force(0.0)
    )

    @Test
    fun `zero unbraced length returns CL of 1_0`() {
        val cl = computeNdsCL(
            lb = 0.0,
            profile = profile,
            material = material,
            adjustmentFactors = NdsAdjustmentFactors(),
            isGlulam = false
        )
        assertEquals(1.0, cl, 1e-9)
    }

    @Test
    fun `long unbraced segment produces a real reduction, not the old 1_0 stub`() {
        // 10 ft unbraced length on a shallow 2x10 — should meaningfully
        // reduce CL below 1.0. Prior to this fix, NdsClCalculator always
        // returned 1.0 regardless of Lb.
        val cl = computeNdsCL(
            lb = 120.0,
            profile = profile,
            material = material,
            adjustmentFactors = NdsAdjustmentFactors(),
            isGlulam = false
        )
        assertTrue("Expected CL < 1.0 for a long unbraced segment, was $cl", cl < 1.0)
        assertTrue("CL should never be negative or absurdly small, was $cl", cl > 0.0)
    }

    @Test
    fun `NdsClCalculator segment result matches the shared computeNdsCL formula`() {
        val calculator = NdsClCalculator(profile, material)

        // Segment spanning 0" to 120" (matches lb = 120 case above)
        val segmentDemands = listOf(demandAt(0.0), demandAt(120.0))

        val expected = computeNdsCL(
            lb = 120.0,
            profile = profile,
            material = material,
            adjustmentFactors = NdsAdjustmentFactors(),
            isGlulam = false
        )
        val actual = calculator.calculate(segmentDemands, isCantilever = false)

        assertEquals(expected, actual, 1e-9)
    }

    @Test
    fun `empty segment demands returns conservative default of 1_0`() {
        val calculator = NdsClCalculator(profile, material)
        val cl = calculator.calculate(emptyList(), isCantilever = false)
        assertEquals(1.0, cl, 1e-9)
    }

    @Test
    fun `NdsClCalculator agrees with NdsWoodCapacityCalculator's internal CL for the same Lb`() {
        // Regression guard against the two paths drifting apart again now
        // that they're split across two classes but share one formula.
        val demand = StationDemand(
            spanId = spanId,
            x = 60.0.inches,
            moment = Moment(-50000.0), // sagging per solver's negated convention
            shear = Force(500.0),
            lbTop = 120.0.inches,
            lbBottom = 120.0.inches,
            compressionFlange = Flange.TOP
        )

        val capacityCalculator = NdsWoodCapacityCalculator(profile, material)
        val detailed = capacityCalculator.evaluateDetailed(demand, DesignMethodology.ASD)
        val clFromCapacityCheck = detailed.designParameters["CL"]!!.toDouble()

        val stabilityCalculator = NdsClCalculator(profile, material)
        val clFromStabilityCalculator = stabilityCalculator.calculate(
            segmentDemands = listOf(demandAt(0.0), demandAt(120.0)),
            isCantilever = false
        )

        assertEquals(clFromCapacityCheck, clFromStabilityCalculator, 1e-3)
    }
}
