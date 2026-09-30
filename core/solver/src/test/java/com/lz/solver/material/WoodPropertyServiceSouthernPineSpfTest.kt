package com.lz.solver.material

import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodSpecies
import com.lz.model.units.Pressure
import com.lz.model.units.inPsi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WoodPropertyServiceSouthernPineSpfTest {

    @Test
    fun `Southern Pine No2 varies by width per Table 4B`() {
        // 2"-4" wide
        val narrow = WoodPropertyService.getSouthernPineDimensionValues(WoodGrade.NO_2, 4.0)!!
        assertEquals(1100.0, narrow.bending.inPsi, 1e-9)
        assertEquals(675.0, narrow.tensionParallel.inPsi, 1e-9)
        assertEquals(1450.0, narrow.compressionParallel.inPsi, 1e-9)
        // 12" wide — meaningfully lower Fb than the 4" band
        val wide = WoodPropertyService.getSouthernPineDimensionValues(WoodGrade.NO_2, 12.0)!!
        assertEquals(750.0, wide.bending.inPsi, 1e-9)
        assertEquals(450.0, wide.tensionParallel.inPsi, 1e-9)
        assertEquals(1250.0, wide.compressionParallel.inPsi, 1e-9)
        // Fv, Fc-perp, E, Emin don't vary by width
        assertEquals(narrow.shear.inPsi, wide.shear.inPsi, 1e-9)
        assertEquals(narrow.compressionPerp.inPsi, wide.compressionPerp.inPsi, 1e-9)
    }

    @Test
    fun `Southern Pine width above 12 inches uses the 12 inch band`() {
        val at12 = WoodPropertyService.getSouthernPineDimensionValues(WoodGrade.NO_1, 12.0)!!
        val above = WoodPropertyService.getSouthernPineDimensionValues(WoodGrade.NO_1, 16.0)!!
        assertEquals(at12.bending.inPsi, above.bending.inPsi, 1e-9)
    }

    @Test
    fun `Southern Pine single-band grades return the same row at every width`() {
        val narrow = WoodPropertyService.getSouthernPineDimensionValues(
            WoodGrade.DENSE_STRUCTURAL_86, 4.0)!!
        val wide = WoodPropertyService.getSouthernPineDimensionValues(
            WoodGrade.DENSE_STRUCTURAL_86, 12.0)!!
        assertEquals(2600.0, narrow.bending.inPsi, 1e-9)
        assertEquals(narrow.bending.inPsi, wide.bending.inPsi, 1e-9)
    }

    @Test
    fun `non-Southern-Pine grade returns null from the width lookup`() {
        assertNull(WoodPropertyService.getSouthernPineDimensionValues(WoodGrade.G_16F_V3, 6.0))
    }

    @Test
    fun `SPF No1 and No2 share the NDS combined row`() {
        val no1 = WoodPropertyService.getReferenceProperties(WoodSpecies.SPF, WoodGrade.NO_1)!!
        val no2 = WoodPropertyService.getReferenceProperties(WoodSpecies.SPF, WoodGrade.NO_2)!!
        assertEquals(875.0, no1.bending.inPsi, 1e-9)
        assertEquals(no1.bending.inPsi, no2.bending.inPsi, 1e-9)
        assertEquals(1_400_000.0, no1.modulusOfElasticity.inPsi, 1e-6)
    }

    @Test
    fun `DF-L and Hem-Fir E is in the millions of psi, not thousands`() {
        val dfl = WoodPropertyService.getReferenceProperties(
            WoodSpecies.DF_L, WoodGrade.SELECT_STRUCTURAL)!!
        assertEquals(1_900_000.0, dfl.modulusOfElasticity.inPsi, 1e-6)
        assertEquals(1700.0, dfl.compressionParallel.inPsi, 1e-9)
        assertNotNull(dfl.eMin)
        assertEquals(690_000.0, dfl.eMin!!.inPsi, 1e-6)

        val hf = WoodPropertyService.getReferenceProperties(
            WoodSpecies.HEM_FIR, WoodGrade.SELECT_STRUCTURAL)!!
        assertEquals(1_600_000.0, hf.modulusOfElasticity.inPsi, 1e-6)
        assertEquals(1500.0, hf.compressionParallel.inPsi, 1e-9)
    }

    @Test
    fun `refreshed re-derives a stale wood material from the current tables`() {
        val stale = MaterialGrade.Wood(
            id = "x", name = "x",
            species = WoodSpecies.DF_L, grade = WoodGrade.SELECT_STRUCTURAL,
            referenceBending = Pressure(1500.0),
            referenceShear = Pressure(180.0),
            referenceCompressionParallel = Pressure(1550.0), // old, wrong
            referenceCompressionPerp = Pressure(625.0),
            referenceTensionParallel = Pressure(1000.0),
            modulusOfElasticity = Pressure(1900.0), // old, 1000x too small
            shearModulus = Pressure(118.75),
            densityPcf = 35.0
        )
        val fixed = WoodPropertyService.refreshed(stale)
        assertEquals(1_900_000.0, fixed.modulusOfElasticity.inPsi, 1e-6)
        assertEquals(1700.0, fixed.referenceCompressionParallel.inPsi, 1e-9)
    }
}
