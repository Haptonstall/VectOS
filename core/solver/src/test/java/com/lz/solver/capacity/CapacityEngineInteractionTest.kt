package com.lz.solver.capacity

import com.lz.model.structural.*
import com.lz.model.units.*
import com.lz.solver.material.AiscSteelCapacityCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

/**
 * Ported from the pre-migration app/src/test/.../CapacityEngineTest.kt,
 * which referenced a com.lz.vectos.domain.structural package that no longer
 * exists as real source (broke `./gradlew build` — see 260820 session).
 * Reference values/assertions unchanged from the original; only the
 * `evaluate()` call is updated to pass `factors` (see DesignFactorSet —
 * evaluate() no longer hardcodes phi/omega internally).
 *
 * Distinct from CapacityEngineTest's other coverage in this file: this
 * specifically checks the Chapter H1-1 interaction equation and
 * governingLimitState labeling, which nothing else in this test class covers.
 */
class CapacityEngineInteractionTest {

    @Test
    fun `verify interaction equations Chapter H1-1`() {
        val w8x10 = SteelProfile(
            id = UUID.randomUUID().toString(),
            designation = "W8x10",
            shapeType = ShapeType.WIDE_FLANGE,
            area = Area(2.96),
            depth = Length(7.89),
            webThickness = Length(0.170),
            flangeWidth = Length(3.94),
            flangeThickness = Length(0.205),
            propertiesStrongAxis = SectionAxisProperties(
                i = MomentOfInertia(30.8),
                s = SectionModulus(7.81),
                r = Length(3.22),
                z = SectionModulus(8.87)
            ),
            propertiesWeakAxis = SectionAxisProperties(
                i = MomentOfInertia(2.09),
                s = SectionModulus(1.06),
                r = Length(0.841),
                z = SectionModulus(1.64)
            ),
            torsionalConstantJ = 0.0436,
            warpingConstantCw = 31.7
        )

        val a992 = MaterialGrade.Steel(
            id = "A992",
            name = "A992",
            yieldStrength = Pressure(50000.0),
            ultimateStrength = Pressure(65000.0),
            modulusOfElasticity = Pressure(29000000.0),
            shearModulus = Pressure(11200000.0),
            densityPcf = 490.0
        )

        val calculator = AiscSteelCapacityCalculator(w8x10, a992)

        // Pn = Ag * Fy = 2.96 * 50 = 148 kips = 148,000 lbs (assuming Lb=0)
        // Mnx = Zx * Fy = 8.87 * 50 = 443.5 kip-in = 443,500 lb-in
        //
        // Demand set so Pr/Pc = 0.5 (>= 0.2, so H1-1a governs):
        // LRFD: Pc = 0.9 * 148000 = 133,200 lbs -> Pr = 66,600 lbs
        // LRFD: Mcx = 0.9 * 443500 = 399,150 lb-in -> Mrx/Mcx = 0.45 -> Mrx = 179,617.5 lb-in

        val demand = StationDemand(
            x = Length(0.0),
            moment = Moment(179617.5),
            shear = Force(0.0),
            axial = Force(66600.0),
            momentY = Moment(0.0),
            shearY = Force(0.0),
            torque = Moment(0.0),
            deflection = Length(0.0),
            spanId = UUID.randomUUID(),
            cb = 1.0,
            lbTop = Length(0.0),
            lbBottom = Length(0.0),
            allowableDeflection = Length(1.0),
            compressionFlange = Flange.TOP
        )

        val results = CapacityEngine.evaluate(
            demands = listOf(demand),
            section = w8x10,
            methodology = DesignMethodology.LRFD,
            factors = calculator.designFactors(DesignMethodology.LRFD),
            capacityCalculator = { calculator.evaluate(it) }
        )

        val result = results.first()

        // Expected Interaction: Pr/Pc + (8/9)(Mrx/Mcx + Mry/Mcy)
        // = 0.5 + (8/9)*(0.45 + 0) = 0.5 + 0.4 = 0.9
        assertEquals(0.9, result.interactionCheck.ratio, 0.01)
        assertEquals("Interaction Eq H1-1", result.governingLimitState)
    }
}
