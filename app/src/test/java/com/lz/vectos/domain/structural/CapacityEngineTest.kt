package com.lz.vectos.domain.structural

import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.Flange
import com.lz.model.units.Area
import com.lz.model.units.Force
import com.lz.model.units.Length
import com.lz.model.units.Moment
import com.lz.model.units.MomentOfInertia
import com.lz.model.units.Pressure
import com.lz.model.units.SectionModulus
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.SectionAxisProperties
import com.lz.model.structural.ShapeType
import com.lz.model.structural.StationDemand
import com.lz.model.structural.SteelProfile
import com.lz.vectos.domain.structural.aisc.AiscSteelCapacityCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class CapacityEngineTest {

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
        // Mny = Zy * Fy = 1.64 * 50 = 82 kip-in = 82,000 lb-in
        
        // Let's create a demand such that Pr/Pc = 0.5 (which is >= 0.2)
        // LRFD: Pc = 0.9 * 148000 = 133,200 lbs
        // Pr = 66,600 lbs
        // Mrx/Mcx = 0.45
        // LRFD: Mcx = 0.9 * 443500 = 399,150 lb-in
        // Mrx = 0.45 * 399150 = 179,617.5 lb-in
        // Mry = 0

        val demand = StationDemand(
            x = Length(0.0),
            moment = Moment(179617.5), // in lb-in
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
            capacityCalculator = { calculator.evaluate(it) }
        )

        val result = results.first()
        println("ratioAxial: ${result.axialCheck.ratio}")
        println("ratioFlexureX: ${result.flexureCheckX.ratio}")
        println("ratioFlexureY: ${result.flexureCheckY.ratio}")
        println("interactionRatio: ${result.interactionCheck.ratio}")
        
        // Expected Interaction: Pr/Pc + (8/9)(Mrx/Mcx + Mry/Mcy)
        // = 0.5 + (8/9)*(0.45 + 0) = 0.5 + 0.4 = 0.9
        
        assertEquals(0.9, result.interactionCheck.ratio, 0.01)
        assertEquals("Interaction Eq H1-1", result.governingLimitState)
    }
}
