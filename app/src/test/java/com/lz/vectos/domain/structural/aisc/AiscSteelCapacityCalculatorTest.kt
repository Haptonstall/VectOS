package com.lz.vectos.domain.structural.aisc

import com.lz.vectos.domain.structural.MaterialGrade
import com.lz.vectos.domain.beam.ShapeType
import com.lz.vectos.domain.beam.SteelProfile
import com.lz.vectos.domain.structural.Flange
import com.lz.vectos.domain.structural.StationDemand
import com.lz.vectos.domain.units.*
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.abs

class AiscSteelCapacityCalculatorTest {

    @Test
    fun `verify W8x10 flexural capacity against AISC Table 3-2`() {
        // Setup W8x10 profile
        val w8x10 = SteelProfile(
            id = java.util.UUID.randomUUID().toString(),
            designation = "W8x10",
            shapeType = ShapeType.WIDE_FLANGE,
            area = Area(2.96),
            depth = Length(7.89),
            webThickness = Length(0.170),
            flangeWidth = Length(3.94),
            flangeThickness = Length(0.205),
            propertiesStrongAxis = com.lz.vectos.domain.beam.SectionAxisProperties(
                i = MomentOfInertia(30.8),
                s = SectionModulus(7.81),
                r = Length(3.22),
                z = SectionModulus(8.87)
            ),
            propertiesWeakAxis = com.lz.vectos.domain.beam.SectionAxisProperties(
                i = MomentOfInertia(2.09),
                s = SectionModulus(1.06),
                r = Length(0.841),
                z = SectionModulus(1.64)
            ),
            torsionalConstantJ = 0.0436,
            warpingConstantCw = 31.7
        )

        // Setup A992 Material
        val a992 = MaterialGrade.Steel(
            id = "A992",
            name = "A992",
            yieldStrength = Pressure(50000.0), // 50 ksi
            ultimateStrength = Pressure(65000.0),
            modulusOfElasticity = Pressure(29000000.0),
            shearModulus = Pressure(11200000.0),
            densityPcf = 490.0
        )

        val calculator = AiscSteelCapacityCalculator(w8x10, a992)

        // Fully Braced Condition (Lb = 0)
        val demandFullyBraced = StationDemand(
            x = Length(0.0),
            moment = Moment(0.0),
            shear = Force(0.0),
            axial = Force(0.0),
            momentY = Moment(0.0),
            shearY = Force(0.0),
            torque = Moment(0.0),
            deflection = Length(0.0),
            spanId = java.util.UUID.randomUUID(),
            cb = 1.0,
            lbTop = Length(0.0),
            lbBottom = Length(0.0),
            allowableDeflection = Length(1.0),
            compressionFlange = Flange.TOP
        )

        val resultBraced = calculator.evaluate(demandFullyBraced)

        // Flange Local Buckling governs
        assertEquals(438279.303, resultBraced.nominalFlexureX, 1.0)
        assertEquals("Flange Local Buckling", resultBraced.limitStateFlexureX)
    }
}
