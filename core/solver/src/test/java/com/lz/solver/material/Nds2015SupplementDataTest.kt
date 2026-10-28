package com.lz.solver.material

import com.lz.model.regulatory.nds.NdsAdjustmentFactors
import com.lz.model.regulatory.nds.NdsEdition
import com.lz.model.structural.*
import com.lz.model.units.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Expected numbers were read from the NDS 2015 Supplement (Tables 4A, 4B, 5A, 5C) -
 * not from the code. Table 5A columns: Fbx+, Fbx-, Fc-perp-x (tension face / compression
 * face), Fvx, Ex, Ex-min, Fby, Fc-perp-y, Fvy, Ey, Ey-min, Ft, Fc, G.
 */
class Nds2015SupplementDataTest {
    private val y15 = NdsEdition.NDS_2015
    private val y18 = NdsEdition.NDS_2018
    private fun props(sp: WoodSpecies, g: WoodGrade, e: NdsEdition) = WoodPropertyService.getReferenceProperties(sp, g, e)!!

    @Test
    fun `2015 Table 5A 16F-V3 DF-DF`() {
        // 16F-V3 DF/DF: 1600 1250 560 560 265 1.5 0.79 1450 560 230 1.5 0.79 975 1500 0.50 0.50
        val p = props(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_16F_V3, y15)
        assertEquals(1600.0, p.bending.inPsi, 0.0); assertEquals(265.0, p.shear.inPsi, 0.0)
        assertEquals(1500.0, p.compressionParallel.inPsi, 0.0); assertEquals(560.0, p.compressionPerp.inPsi, 0.0)
        assertEquals(975.0, p.tensionParallel.inPsi, 0.0)
        assertEquals(1_500_000.0, p.modulusOfElasticity.inPsi, 0.0)   // 2018 table has 1.6 million
        assertEquals(790_000.0, p.eMin!!.inPsi, 0.0)
        assertEquals(1450.0, p.bendingWeakAxis!!.inPsi, 0.0)
        assertEquals(31.2, p.densityPcf, 1e-9)                         // 62.4 x 0.50
        // 2018 Table 5A, same row: Ex true 1.6, Ex app 1.5, Ex min 0.79. The app has no separate
        // shear deflection, so it carries the apparent E (1.5), which equals the 2015 value.
        assertEquals(1_500_000.0, props(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_16F_V3, y18).modulusOfElasticity.inPsi, 0.0)
        assertEquals(790_000.0, props(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_16F_V3, y18).eMin!!.inPsi, 0.0)
    }

    @Test
    fun `Table 5A gives both Fc-perp values, tension face and compression face`() {
        // 2015 20F-V3 DF/DF: 650 tension face / 560 compression face
        val v3 = props(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_20F_V3, y15)
        assertEquals(650.0, v3.compressionPerpTensionFace!!.inPsi, 0.0)
        assertEquals(560.0, v3.compressionPerpCompressionFace!!.inPsi, 0.0)
        // 2018 16F-V2 SP/SP: 740 / 650
        val sp = props(WoodSpecies.GLULAM_SP_SP, WoodGrade.G_16F_V2, y18)
        assertEquals(740.0, sp.compressionPerpTensionFace!!.inPsi, 0.0)
        assertEquals(650.0, sp.compressionPerpCompressionFace!!.inPsi, 0.0)
        assertEquals(650.0, sp.compressionPerp.inPsi, 0.0)     // conservative single value
        // sawn lumber and hardwood have one tabulated value
        assertNull(props(WoodSpecies.DF_L, WoodGrade.NO_2, y18).compressionPerpTensionFace)
        assertNull(props(WoodSpecies.GLULAM_HARDWOODS, WoodGrade.G_24F_E5_RO, y18).compressionPerpCompressionFace)
    }

    @Test
    fun `2018 Table 5A rows that the older entries had wrong`() {
        // 20F-V3 DF/DF: 2000 1450 650 560 265 Ex true 1.7 app 1.6 min 0.85 Fby 1450 ... Ft 1000 Fc 1550 G 0.50
        val v3 = props(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_20F_V3, y18)
        assertEquals(1550.0, v3.compressionParallel.inPsi, 0.0)
        assertEquals(1000.0, v3.tensionParallel.inPsi, 0.0)
        assertEquals(1_600_000.0, v3.modulusOfElasticity.inPsi, 0.0)
        assertEquals(850_000.0, v3.eMin!!.inPsi, 0.0)
        assertEquals(1450.0, v3.bendingWeakAxis!!.inPsi, 0.0)
        // 20F-V7 DF/DF: ... 650 650 265 1.7 1.6 0.85 1450 ... Ft 1050 Fc 1600
        val v7 = props(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_20F_V7, y18)
        assertEquals(1050.0, v7.tensionParallel.inPsi, 0.0)
        assertEquals(1600.0, v7.compressionParallel.inPsi, 0.0)
        assertEquals(650.0, v7.compressionPerp.inPsi, 0.0)
        // 20F-V14 / V15 POC/POC: 2000 .. 560 560 265 1.6 1.5 0.79 Fby 1300 .. Ft 900 Fc 1600 G 0.46
        for (g in listOf(WoodGrade.G_20F_V14, WoodGrade.G_20F_V15)) {
            val poc = props(WoodSpecies.GLULAM_POC_POC, g, y18)
            assertEquals(1600.0, poc.compressionParallel.inPsi, 0.0)
            assertEquals(560.0, poc.compressionPerp.inPsi, 0.0)
            assertEquals(1300.0, poc.bendingWeakAxis!!.inPsi, 0.0)
            assertEquals(28.7, poc.densityPcf, 1e-9)
        }
    }

    @Test
    fun `2015 Table 5A rows that differ from the 2018 values the app held`() {
        // 20F-V3 DF/DF: 2000 1450 650 560 265 1.6 0.85 1450 560 230 1.5 0.79 1000 1550 0.50 0.50
        val v3 = props(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_20F_V3, y15)
        assertEquals(1550.0, v3.compressionParallel.inPsi, 0.0)       // app/2018 data: 1450
        assertEquals(560.0, v3.compressionPerp.inPsi, 0.0)            // lower of the 650 / 560 faces
        assertEquals(1_600_000.0, v3.modulusOfElasticity.inPsi, 0.0)
        // 20F-V7 DF/DF: 2000 2000 650 650 265 1.6 0.85 1450 560 230 1.6 0.85 1050 1600 0.50 0.50
        val v7 = props(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_20F_V7, y15)
        assertEquals(1050.0, v7.tensionParallel.inPsi, 0.0)           // app/2018 data: 1000
        assertEquals(650.0, v7.compressionPerp.inPsi, 0.0)
        // 20F-V14 POC/POC: 2000 1450 560 560 265 1.5 0.79 1300 470 230 1.4 0.74 900 1600 0.46 0.46
        val poc = props(WoodSpecies.GLULAM_POC_POC, WoodGrade.G_20F_V14, y15)
        assertEquals(1600.0, poc.compressionParallel.inPsi, 0.0)      // app/2018 data: 1300
        assertEquals(1300.0, poc.bendingWeakAxis!!.inPsi, 0.0)
        // 16F-V2 SP/SP: 1600 1400 740 650 300 1.5 0.79 1450 650 260 1.4 0.74 1000 1300 0.55 0.55
        val sp = props(WoodSpecies.GLULAM_SP_SP, WoodGrade.G_16F_V2, y15)
        assertEquals(650.0, sp.compressionPerp.inPsi, 0.0)
        assertEquals(34.3, sp.densityPcf, 1e-9)                        // 62.4 x 0.55
    }

    @Test
    fun `combinations that are not in the 2015 Table 5A are not served for 2015`() {
        for ((sp, g) in listOf(
            WoodSpecies.GLULAM_DF_DF to WoodGrade.G_20F_V4, WoodSpecies.GLULAM_DF_DF to WoodGrade.G_20F_V8,
            WoodSpecies.GLULAM_DF_DF to WoodGrade.G_16F_G3, WoodSpecies.GLULAM_HF_HF to WoodGrade.G_16F_G2,
            WoodSpecies.GLULAM_SP_SP to WoodGrade.G_24F_1_8E, WoodSpecies.GLULAM_SPF_SPF to WoodGrade.G_20F_E_SPF1,
            WoodSpecies.GLULAM_ES_ES to WoodGrade.G_20F_E_ES1, WoodSpecies.GLULAM_POC_POC to WoodGrade.G_22F_V_POC1
        )) {
            assertNull("$sp $g", WoodPropertyService.getReferenceProperties(sp, g, y15))
            assertNotNull("2018 still has $sp $g", WoodPropertyService.getReferenceProperties(sp, g, y18))
            assertNotNull(NdsCodeData.blockReason(y15, sp, g))
            assertNull(NdsCodeData.blockReason(y18, sp, g))
        }
        assertNull(NdsCodeData.blockReason(y15, WoodSpecies.GLULAM_DF_DF, WoodGrade.G_16F_V3))
        assertNull(WoodPropertyService.getReferenceProperties(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_16F_V3, NdsEdition.NDS_2024))
    }

    @Test
    fun `2015 sawn Southern Pine and hardwood glulam values equal the 2018 ones`() {
        // Table 4A DF-L Select Structural: 1500 1000 180 625 1700 1.9 million 690000
        val df = props(WoodSpecies.DF_L, WoodGrade.SELECT_STRUCTURAL, y15)
        assertEquals(1500.0, df.bending.inPsi, 0.0); assertEquals(1000.0, df.tensionParallel.inPsi, 0.0)
        assertEquals(1700.0, df.compressionParallel.inPsi, 0.0); assertEquals(690_000.0, df.eMin!!.inPsi, 0.0)
        // Table 4A Hem-Fir No. 1: 975 625 150 405 1350 1.5 million 550000
        val hf = props(WoodSpecies.HEM_FIR, WoodGrade.NO_1, y15)
        assertEquals(975.0, hf.bending.inPsi, 0.0); assertEquals(625.0, hf.tensionParallel.inPsi, 0.0)
        // Table 4B Southern Pine No.2, 12" wide: 750 450 175 565 1250 1.4 million 510000
        val sp12 = WoodPropertyService.getSouthernPineDimensionValues(WoodGrade.NO_2, 12.0, y15)!!
        assertEquals(750.0, sp12.bending.inPsi, 0.0); assertEquals(450.0, sp12.tensionParallel.inPsi, 0.0)
        assertEquals(1250.0, sp12.compressionParallel.inPsi, 0.0)
        // Table 4B Southern Pine Select Structural, 2"-4" wide: 2350 1650 175 565 1900 1.8 million 660000
        val sp4 = WoodPropertyService.getSouthernPineDimensionValues(WoodGrade.SELECT_STRUCTURAL, 4.0, y15)!!
        assertEquals(2350.0, sp4.bending.inPsi, 0.0); assertEquals(660_000.0, sp4.eMin!!.inPsi, 0.0)
        // Table 5C 24F-E5 RO: 2400 1200 1075 235 1.8 0.95 1700 900 205 1.5 0.79 1100 1450 0.63
        val hw = props(WoodSpecies.GLULAM_HARDWOODS, WoodGrade.G_24F_E5_RO, y15)
        assertEquals(2400.0, hw.bending.inPsi, 0.0); assertEquals(235.0, hw.shear.inPsi, 0.0)
        assertEquals(1075.0, hw.compressionPerp.inPsi, 0.0); assertEquals(1100.0, hw.tensionParallel.inPsi, 0.0)
        assertEquals(1450.0, hw.compressionParallel.inPsi, 0.0); assertEquals(1_800_000.0, hw.modulusOfElasticity.inPsi, 0.0)
        assertEquals(950_000.0, hw.eMin!!.inPsi, 0.0); assertEquals(1700.0, hw.bendingWeakAxis!!.inPsi, 0.0)
    }

    // ---------------- the calculator reads the edition's table ----------------

    private val glProfile = WoodProfile(
        id = "gl", designation = "5.125x12",
        nominalWidth = 5.125.inches, nominalDepth = 12.0.inches,
        dressedWidth = 5.125.inches, dressedDepth = 12.0.inches
    )
    // Deliberately wrong numbers carried on the material (as a picker under another edition could leave).
    private val stale = MaterialGrade.Wood(
        id = "gl", name = "Glulam 16F-V3", species = WoodSpecies.GLULAM_DF_DF, grade = WoodGrade.G_16F_V3,
        referenceBending = 9999.0.psi, referenceShear = 265.0.psi, referenceCompressionParallel = 1500.0.psi,
        referenceCompressionPerp = 560.0.psi, referenceTensionParallel = 975.0.psi,
        modulusOfElasticity = 9_999_999.0.psi, shearModulus = 100000.0.psi, densityPcf = 31.2
    )

    @Test
    fun `under 2015 the calculator takes every value from the 2015 table, not from the material`() {
        // Sx = 5.125 x 12^2 / 6 = 123.0 in^3; CV = 1.0 for L = 15 ft; so Mn = Fb x Sx = 1600 x 123.
        val d = StationDemand(
            spanId = UUID.randomUUID(), x = 180.0.inches, moment = Moment(1000.0), shear = Force(0.0),
            lbTop = 0.0.inches, lbBottom = 0.0.inches, compressionFlange = Flange.TOP,
            momentSpanLength = (15.0 * 12.0).inches
        )
        val cap = NdsWoodCapacityCalculator(glProfile, stale, edition = y15)
            .evaluateDetailed(d, DesignMethodology.ASD).momentCheck.capacity.lbIn
        assertEquals(1600.0 * 123.0, cap, 1e-6)
    }

    @Test
    fun `Emin for stability comes from the edition table`() {
        val m = stale
        assertEquals(790_000.0, woodEmin(m, y15), 0.0)                  // Table 5A 2015: 0.79 million
        assertEquals(790_000.0, woodEmin(m, y18), 0.0)                  // Table 5A 2018: 0.79 million
        // a legacy (ESR-1940) combination has no tabulated Emin: falls back to E / 1.76
        assertEquals(1_700_000.0 / 1.76, woodEmin(m.copy(grade = WoodGrade.G_16F_G3), y18), 1e-6)   // table E of that legacy row, 1.7 million
    }

    @Test
    fun `a combination missing from the 2015 table makes the calculator refuse`() {
        val m = stale.copy(grade = WoodGrade.G_20F_V4)
        var blocked = false
        try { NdsWoodCapacityCalculator(glProfile, m, edition = y15) } catch (e: CodeDataBlockedException) { blocked = true }
        assertTrue(blocked)
    }
}
