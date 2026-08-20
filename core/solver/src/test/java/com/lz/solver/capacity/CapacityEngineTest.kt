package com.lz.solver.capacity

import com.lz.model.structural.*
import com.lz.model.units.*
import com.lz.solver.material.AiscSteelCapacityCalculator
import com.lz.solver.material.NdsWoodCapacityCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

/**
 * Regression coverage for the CapacityEngine fix: it previously applied a
 * hardcoded placeholder (phi=0.9 LRFD / omega=1.67 ASD, flat across every
 * limit state and every material) instead of each material's real code
 * factors. For wood specifically this silently double-divided ASD capacity
 * (NDS's nominal already IS the adjusted allowable value) and used the
 * wrong phi for LRFD (flat 0.9 instead of NDS's per-limit-state 0.85/0.75/
 * 0.80/0.90).
 */
class CapacityEngineTest {

    private val woodProfile = WoodProfile(
        id = "2x10",
        designation = "2x10",
        nominalWidth = 2.0.inches,
        nominalDepth = 10.0.inches,
        dressedWidth = 1.5.inches,
        dressedDepth = 9.25.inches
    )

    private val woodMaterial = MaterialGrade.Wood(
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

    private fun woodDemand() = StationDemand(
        spanId = UUID.randomUUID(),
        x = 60.0.inches,
        moment = Moment(-10000.0),
        shear = Force(500.0),
        lbTop = 0.0.inches,
        lbBottom = 0.0.inches,
        compressionFlange = Flange.TOP
    )

    @Test
    fun `wood ASD is not double-divided by an extra Omega`() {
        val calculator = NdsWoodCapacityCalculator(woodProfile, woodMaterial)
        val demand = woodDemand()

        val nominal = calculator.evaluate(demand).nominalFlexureX
        val factors = calculator.designFactors(DesignMethodology.ASD)

        val result = CapacityEngine.evaluate(
            demands = listOf(demand),
            section = woodProfile,
            methodology = DesignMethodology.ASD,
            factors = factors
        ) { calculator.evaluate(it) }.first()

        // ASD design capacity must equal the nominal (already-adjusted NDS
        // allowable value) exactly — NOT nominal / 1.67, which was the
        // previous behavior.
        assertEquals(nominal, result.flexureCheckX.capacity, 1e-6)
    }

    @Test
    fun `wood LRFD uses real NDS phi, not the old flat 0_9`() {
        val calculator = NdsWoodCapacityCalculator(woodProfile, woodMaterial)
        val demand = woodDemand()

        val nominal = calculator.evaluate(demand).nominalFlexureX
        val factors = calculator.designFactors(DesignMethodology.LRFD)

        val result = CapacityEngine.evaluate(
            demands = listOf(demand),
            section = woodProfile,
            methodology = DesignMethodology.LRFD,
            factors = factors
        ) { calculator.evaluate(it) }.first()

        // Expected: lambda (0.8) * phiBending (0.85) * nominal — matches
        // NdsWoodCapacityCalculator.evaluateDetailed()'s own logic, NOT the
        // old flat 0.9 placeholder.
        val expected = 0.8 * 0.85 * nominal
        assertEquals(expected, result.flexureCheckX.capacity, 1e-6)
    }

    @Test
    fun `steel LRFD and ASD flexure factors match AiscDesignFactors, not a placeholder`() {
        val profile = SteelProfile(
            id = "W8x10",
            designation = "W8x10",
            shapeType = ShapeType.WIDE_FLANGE,
            area = Area(2.96),
            depth = Length(7.89),
            webThickness = Length(0.170),
            flangeWidth = Length(3.94),
            flangeThickness = Length(0.205),
            propertiesStrongAxis = SectionAxisProperties(
                i = MomentOfInertia(30.8), s = SectionModulus(7.81),
                r = Length(3.22), z = SectionModulus(8.87)
            ),
            propertiesWeakAxis = SectionAxisProperties(
                i = MomentOfInertia(2.09), s = SectionModulus(1.06),
                r = Length(0.841), z = SectionModulus(1.64)
            ),
            torsionalConstantJ = 0.0436,
            warpingConstantCw = 31.7
        )
        val material = MaterialGrade.Steel(
            id = "A992", name = "A992",
            yieldStrength = Pressure(50000.0),
            ultimateStrength = Pressure(65000.0),
            modulusOfElasticity = Pressure(29000000.0),
            shearModulus = Pressure(11200000.0),
            densityPcf = 490.0
        )
        val calculator = AiscSteelCapacityCalculator(profile, material)
        val demand = StationDemand(
            spanId = UUID.randomUUID(), x = 0.0.inches,
            moment = Moment(100000.0), shear = Force(0.0),
            lbTop = 0.0.inches, lbBottom = 0.0.inches,
            compressionFlange = Flange.TOP
        )

        val nominal = calculator.evaluate(demand).nominalFlexureX

        val lrfdResult = CapacityEngine.evaluate(
            listOf(demand), profile, DesignMethodology.LRFD,
            calculator.designFactors(DesignMethodology.LRFD)
        ) { calculator.evaluate(it) }.first()
        assertEquals(nominal * 0.90, lrfdResult.flexureCheckX.capacity, 1e-6)

        val asdResult = CapacityEngine.evaluate(
            listOf(demand), profile, DesignMethodology.ASD,
            calculator.designFactors(DesignMethodology.ASD)
        ) { calculator.evaluate(it) }.first()
        assertEquals(nominal / 1.67, asdResult.flexureCheckX.capacity, 1e-6)
    }
}
