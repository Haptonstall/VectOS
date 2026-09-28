package com.lz.solver.material

import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodSpecies
import com.lz.model.units.inPsi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WoodPropertyServiceHardwoodGlulamTest {

    private fun props(grade: WoodGrade) =
        WoodPropertyService.getReferenceProperties(WoodSpecies.GLULAM_HARDWOODS, grade)!!

    @Test
    fun `every hardwood symbol the picker offers has verified data`() {
        val offered = WoodGrade.validGradesFor(WoodSpecies.GLULAM_HARDWOODS)
        assertEquals(21, offered.size)
        offered.forEach { grade ->
            assertNotNull("No Table 5C data for $grade", 
                WoodPropertyService.getReferenceProperties(WoodSpecies.GLULAM_HARDWOODS, grade))
        }
    }

    @Test
    fun `12F-V1 matches Table 5C`() {
        val p = props(WoodGrade.G_12F_V1)
        assertEquals(1200.0, p.bending.inPsi, 1e-9)
        assertEquals(125.0, p.shear.inPsi, 1e-9)
        assertEquals(800.0, p.compressionParallel.inPsi, 1e-9)
        assertEquals(285.0, p.compressionPerp.inPsi, 1e-9)
        assertEquals(600.0, p.tensionParallel.inPsi, 1e-9)
        assertEquals(1200000.0, p.modulusOfElasticity.inPsi, 1e-6)
        assertEquals(24.3, p.densityPcf, 1e-9)
    }

    @Test
    fun `24F-E5 RO matches Table 5C`() {
        val p = props(WoodGrade.G_24F_E5_RO)
        assertEquals(2400.0, p.bending.inPsi, 1e-9)
        assertEquals(235.0, p.shear.inPsi, 1e-9)
        assertEquals(1450.0, p.compressionParallel.inPsi, 1e-9)
        assertEquals(1075.0, p.compressionPerp.inPsi, 1e-9)
        assertEquals(1100.0, p.tensionParallel.inPsi, 1e-9)
        assertEquals(1800000.0, p.modulusOfElasticity.inPsi, 1e-6)
        assertEquals(39.3, p.densityPcf, 1e-9)
    }

    @Test
    fun `reused symbol names resolve per species, not globally`() {
        // 16F-V2 exists for SP/SP and hardwoods with different values.
        val sp = WoodPropertyService.getReferenceProperties(
            WoodSpecies.GLULAM_SP_SP, WoodGrade.G_16F_V2)!!
        val hw = props(WoodGrade.G_16F_V2)
        assertEquals(1300.0, sp.compressionParallel.inPsi, 1e-9)
        assertEquals(1250.0, hw.compressionParallel.inPsi, 1e-9)
    }
}
