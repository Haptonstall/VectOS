package com.lz.solver.capacity

import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.DesignPropertyGroup
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.SectionAxisProperties
import com.lz.model.structural.ShapeType
import com.lz.model.structural.StationDemand
import com.lz.model.structural.SteelProfile
import com.lz.model.units.*
import com.lz.solver.material.AiscSteelCapacityCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Selected Properties for every material. The inputs below are the test's own numbers
 * (W12x26-like shape, A992-like grade); each expected string is the same number formatted
 * by hand: 3 decimals for lengths and areas, 2 for I, S and Z, ksi for stresses and moduli.
 */
class SelectedPropertiesBuilderTest {

    private val w12 = SteelProfile(
        id = "W12X26", designation = "W12X26", shapeType = ShapeType.WIDE_FLANGE,
        area = 7.65.in2, depth = 12.2.inches, webThickness = 0.230.inches,
        flangeWidth = 6.49.inches, flangeThickness = 0.380.inches,
        torsionalConstantJ = 0.300, warpingConstantCw = 576.0,
        propertiesStrongAxis = SectionAxisProperties(204.0.in4, 33.4.in3, 37.2.in3, 5.17.inches),
        propertiesWeakAxis = SectionAxisProperties(17.3.in4, 5.34.in3, 8.17.in3, 1.51.inches)
    )
    private val a992 = MaterialGrade.Steel("A992", "A992", yieldStrength = 50000.0.psi, ultimateStrength = 65000.0.psi)

    private fun v(g: List<DesignPropertyGroup>, group: String, symbol: String) =
        g.first { it.title == group }.rows.first { it.symbol == symbol }.value

    @Test
    fun `steel material and section groups carry the shape and grade values`() {
        val g = SelectedPropertiesBuilder.forSection(w12, a992)
        assertEquals("A992", v(g, "Material", "Grade"))
        assertEquals("50.0 ksi", v(g, "Material", "Fy"))
        assertEquals("65.0 ksi", v(g, "Material", "Fu"))
        assertEquals("29000 ksi", v(g, "Material", "E"))
        assertEquals("W12X26", v(g, "Section", "Size"))
        assertEquals("12.200 in", v(g, "Section", "d"))
        assertEquals("7.650 in\u00B2", v(g, "Section", "A"))
        assertEquals("6.490 in", v(g, "Section", "bf"))
        assertEquals("204.00 in\u2074", v(g, "Section", "Ix"))
        assertEquals("33.40 in\u00B3", v(g, "Section", "Sx"))
        assertEquals("37.20 in\u00B3", v(g, "Section", "Zx"))
        assertEquals("5.170 in", v(g, "Section", "rx"))
        assertEquals("1.510 in", v(g, "Section", "ry"))
        assertEquals("0.300 in\u2074", v(g, "Section", "J"))
        assertEquals("576 in\u2076", v(g, "Section", "Cw"))
        // weight = A / 144 x 490 pcf = 26.0312 lb/ft
        assertEquals("26.03 lb/ft", v(g, "Section", "w"))
    }

    @Test
    fun `a material with no calculator still gets material and section groups`() {
        val concrete = MaterialGrade.Generic("C4000", "Concrete 4000 psi", com.lz.model.structural.MaterialType.CONCRETE,
            modulusOfElasticity = 3605.0.ksi, shearModulus = 1500.0.ksi, densityPcf = 150.0)
        val g = SelectedPropertiesBuilder.forSection(w12, concrete)
        assertEquals("CONCRETE", v(g, "Material", "Type"))
        assertEquals("3605 ksi", v(g, "Material", "E"))
        assertEquals("150.0 pcf", v(g, "Material", "\u03C1"))
        assertEquals("Concrete 4000 psi", v(g, "Material", "Grade"))
        assertEquals("W12X26", v(g, "Section", "Size"))
        // weight uses the grade's own density: 7.65 / 144 x 150 = 7.96875
        assertEquals("7.97 lb/ft", v(g, "Section", "w"))
    }

    @Test
    fun `missing material or section just omits that group`() {
        assertTrue(SelectedPropertiesBuilder.forSection(null, null).isEmpty())
        assertEquals(listOf("Section"), SelectedPropertiesBuilder.forSection(w12, null).map { it.title })
        assertEquals(listOf("Material"), SelectedPropertiesBuilder.forSection(null, a992).map { it.title })
    }

    @Test
    fun `the steel calculator's detailed result populates Selected Properties with design inputs`() {
        val calc = AiscSteelCapacityCalculator(w12, a992)
        val demand = StationDemand(
            spanId = java.util.UUID.randomUUID(), x = 60.0.inches, moment = Moment(100000.0), shear = Force(5000.0),
            momentY = Moment(0.0), axial = Force(0.0)
        )
        val g = calc.evaluateDetailed(demand, DesignMethodology.LRFD).selectedProperties
        assertEquals(listOf("Material", "Section", "Design"), g.map { it.title })
        assertEquals("33.40 in\u00B3", v(g, "Section", "Sx"))
        assertEquals("Strong (x)", v(g, "Design", "Axis"))
        assertEquals("LRFD", v(g, "Design", "Method"))
        val phis = g.first { it.title == "Design" }.rows.filter { it.symbol == "\u03C6" }
        assertEquals(5, phis.size)
        assertFalse(g.first { it.title == "Design" }.rows.any { it.symbol == "\u03A9" })
        val asd = calc.evaluateDetailed(demand, DesignMethodology.ASD).selectedProperties
        assertEquals(5, asd.first { it.title == "Design" }.rows.count { it.symbol == "\u03A9" })
    }
}
