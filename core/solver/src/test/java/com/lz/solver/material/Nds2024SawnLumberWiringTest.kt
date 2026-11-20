package com.lz.solver.material

import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodSpecies
import com.lz.model.units.inPsi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NDS 2024 wiring for solid-sawn lumber (Tables 4A / 4B). Expected values are the ones printed in the 2024 Supplement
 * (read from the pages, not from the code under test).
 */
class Nds2024SawnLumberWiringTest {

    private val e24 = NdsEdition.NDS_2024

    private fun vals(species: WoodSpecies, grade: WoodGrade): List<Double> {
        val p = WoodPropertyService.getReferenceProperties(species, grade, e24)
        assertNotNull("$species $grade", p)
        p!!
        return listOf(
            p.bending.inPsi, p.tensionParallel.inPsi, p.shear.inPsi, p.compressionPerp.inPsi, p.compressionParallel.inPsi,
            p.modulusOfElasticity.inPsi, p.eMin!!.inPsi
        )
    }

    private fun d(vararg v: Int) = v.map { it.toDouble() }

    @Test
    fun `Table 4A rows are served for Douglas Fir-Larch, Hem-Fir and Spruce-Pine-Fir`() {
        assertEquals(d(1500, 1000, 180, 625, 1700, 1900000, 690000), vals(WoodSpecies.DF_L, WoodGrade.SELECT_STRUCTURAL))
        assertEquals(d(900, 575, 180, 625, 1350, 1600000, 580000), vals(WoodSpecies.DF_L, WoodGrade.NO_2))
        assertEquals(d(525, 325, 180, 625, 775, 1400000, 510000), vals(WoodSpecies.DF_L, WoodGrade.NO_3))
        assertEquals(d(850, 525, 150, 405, 1300, 1300000, 470000), vals(WoodSpecies.HEM_FIR, WoodGrade.NO_2))
        assertEquals(d(1250, 700, 135, 425, 1400, 1500000, 550000), vals(WoodSpecies.SPF, WoodGrade.SELECT_STRUCTURAL))
        assertEquals(d(675, 350, 135, 425, 725, 1200000, 440000), vals(WoodSpecies.SPF, WoodGrade.STUD))
        // Spruce-Pine-Fir is printed with one combined "No. 1/ No. 2" row, used for both grades
        assertEquals(d(875, 450, 135, 425, 1150, 1400000, 510000), vals(WoodSpecies.SPF, WoodGrade.NO_1))
        assertEquals(d(875, 450, 135, 425, 1150, 1400000, 510000), vals(WoodSpecies.SPF, WoodGrade.NO_2))
    }

    @Test
    fun `the unit weight is the species value the app already uses and the shear modulus is E over 16`() {
        val p = WoodPropertyService.getReferenceProperties(WoodSpecies.DF_L, WoodGrade.NO_2, e24)!!
        assertEquals(35.0, p.densityPcf, 0.0)
        assertEquals(1_600_000.0 / 16.0, p.shearModulus.inPsi, 1e-6)
        assertEquals(30.0, WoodPropertyService.getReferenceProperties(WoodSpecies.HEM_FIR, WoodGrade.NO_2, e24)!!.densityPcf, 0.0)
    }

    private fun sp(grade: WoodGrade, width: Double): List<Double>? =
        WoodPropertyService.getSouthernPineDimensionValues(grade, width, e24)?.let {
            listOf(it.bending.inPsi, it.tensionParallel.inPsi, it.shear.inPsi, it.compressionPerp.inPsi, it.compressionParallel.inPsi,
                it.modulusOfElasticity.inPsi, it.eMin!!.inPsi)
        }

    @Test
    fun `Table 4B Southern Pine values follow the member width`() {
        assertEquals(d(1100, 675, 175, 565, 1450, 1400000, 510000), sp(WoodGrade.NO_2, 4.0))
        assertEquals(d(1100, 675, 175, 565, 1450, 1400000, 510000), sp(WoodGrade.NO_2, 2.0))
        assertEquals(d(750, 450, 175, 565, 1250, 1400000, 510000), sp(WoodGrade.NO_2, 12.0))
        assertEquals(d(750, 450, 175, 565, 1250, 1400000, 510000), sp(WoodGrade.NO_2, 14.0))   // above 12": the 12" row
        assertEquals(d(1700, 1200, 175, 480, 1650, 1600000, 580000), sp(WoodGrade.NON_DENSE_SELECT_STRUCTURAL, 8.0))
        assertEquals(d(1200, 800, 175, 660, 1550, 1800000, 660000), sp(WoodGrade.NO_1_DENSE, 10.0))
        assertEquals(d(575, 350, 175, 565, 800, 1300000, 470000), sp(WoodGrade.NO_3, 6.0))
        assertEquals(sp(WoodGrade.NO_3, 6.0), sp(WoodGrade.STUD, 6.0))                          // printed as "No.3 and Stud"
        assertEquals(d(2700, 1900, 175, 660, 2050, 1900000, 690000), sp(WoodGrade.DENSE_SELECT_STRUCTURAL, 3.0))
        // the species lookup uses the 2"-4" band
        assertEquals(d(1100, 675, 175, 565, 1450, 1400000, 510000), vals(WoodSpecies.SOUTHERN_PINE, WoodGrade.NO_2))
    }

    @Test
    fun `Southern Pine Construction, Standard and Utility are served for 4 in or less only`() {
        assertEquals(d(475, 275, 175, 565, 1300, 1200000, 440000), sp(WoodGrade.STANDARD, 4.0))
        assertEquals(d(475, 275, 175, 565, 1300, 1200000, 440000), sp(WoodGrade.STANDARD, 3.0))
        for (g in listOf(WoodGrade.CONSTRUCTION, WoodGrade.STANDARD, WoodGrade.UTILITY)) {
            assertNull("$g 6 in", sp(g, 6.0))
            assertNull("$g 12 in", sp(g, 12.0))
        }
    }

    @Test
    fun `Dense Structural 86, 72 and 65 have no 2024 row and are blocked, as is everything not in Tables 4A and 4B`() {
        for (g in listOf(WoodGrade.DENSE_STRUCTURAL_86, WoodGrade.DENSE_STRUCTURAL_72, WoodGrade.DENSE_STRUCTURAL_65)) {
            assertNull(g.name, WoodPropertyService.getReferenceProperties(WoodSpecies.SOUTHERN_PINE, g, e24))
            assertNull(g.name, sp(g, 4.0))
            assertTrue(g.name, NdsCodeData.blockReason(e24, WoodSpecies.SOUTHERN_PINE, g)!!.contains("NDS 2024"))
            // the 2018 table still has them
            assertNotNull(g.name, WoodPropertyService.getReferenceProperties(WoodSpecies.SOUTHERN_PINE, g, NdsEdition.NDS_2018))
        }
        assertNull(WoodPropertyService.getReferenceProperties(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_16F_V3, e24))
        assertTrue(NdsCodeData.blockReason(e24, WoodSpecies.GLULAM_DF_DF, WoodGrade.G_16F_V3)!!.contains("glulam"))
    }

    @Test
    fun `sawn lumber is usable for 2024 and the 2024 values match the 2018 ones the app held for these grades`() {
        // the grades the app offers for each species (Hem-Fir has no No. 3)
        val offered = mapOf(
            WoodSpecies.DF_L to listOf(WoodGrade.SELECT_STRUCTURAL, WoodGrade.NO_1, WoodGrade.NO_2, WoodGrade.NO_3),
            WoodSpecies.HEM_FIR to listOf(WoodGrade.SELECT_STRUCTURAL, WoodGrade.NO_1, WoodGrade.NO_2),
            WoodSpecies.SPF to listOf(WoodGrade.SELECT_STRUCTURAL, WoodGrade.NO_1, WoodGrade.NO_2, WoodGrade.NO_3, WoodGrade.STUD),
            WoodSpecies.SOUTHERN_PINE to listOf(WoodGrade.SELECT_STRUCTURAL, WoodGrade.NO_1, WoodGrade.NO_2, WoodGrade.NO_3)
        )
        for ((s, grades) in offered) {
            for (g in grades) {
                assertNull("$s $g", NdsCodeData.blockReason(e24, s, g))
                val a = WoodPropertyService.getReferenceProperties(s, g, NdsEdition.NDS_2018)!!
                val b = WoodPropertyService.getReferenceProperties(s, g, e24)!!
                assertEquals("$s $g", a, b)   // the Supplement printed the same values for the offered grades
            }
        }
    }

    @Test
    fun `the edition-only gate no longer blocks 2024 and 2018 and 2015 are unchanged`() {
        assertNull(NdsCodeData.blockReason(e24))
        assertNull(NdsCodeData.blockReason(NdsEdition.NDS_2018))
        assertNull(NdsCodeData.blockReason(NdsEdition.NDS_2015))
        // glulam notices for 2024 still say the design values are not sourced; sawn lumber has none
        assertTrue(NdsCodeData.noticesFor(e24, com.lz.model.structural.DesignMethodology.ASD, isGlulam = false)
            .none { it.status == com.lz.model.structural.CodeDataStatus.NOT_SOURCED })
        assertTrue(NdsCodeData.noticesFor(e24, com.lz.model.structural.DesignMethodology.ASD, isGlulam = true)
            .any { it.status == com.lz.model.structural.CodeDataStatus.NOT_SOURCED })
    }
}
