package com.lz.solver.material

import com.lz.model.regulatory.nds.MoistureCondition
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
 * Wet service factors CM. Expected CM values come from the Supplement CM tables and footnotes
 * (Tables 4A / 4B / 5A / 5C; identical in NDS 2015 and 2018); the conditions are applied by hand
 * to the tabulated Fb, Fc and size factors CF.
 */
class NdsWetServiceTest {
    private val wet = NdsAdjustmentFactors(moisture = MoistureCondition.WET)
    private val dry = NdsAdjustmentFactors()

    private fun wood(sp: WoodSpecies, g: WoodGrade, width: Double = 4.0): MaterialGrade.Wood {
        val p = if (sp == WoodSpecies.SOUTHERN_PINE) WoodPropertyService.getSouthernPineDimensionValues(g, width)!!
        else WoodPropertyService.getReferenceProperties(sp, g)!!
        return MaterialGrade.Wood(
            id = "w", name = "w", species = sp, grade = g,
            referenceBending = p.bending, referenceShear = p.shear, referenceCompressionParallel = p.compressionParallel,
            referenceCompressionPerp = p.compressionPerp, referenceTensionParallel = p.tensionParallel,
            modulusOfElasticity = p.modulusOfElasticity, shearModulus = p.shearModulus, densityPcf = p.densityPcf
        )
    }
    private fun lumber(nominalW: Int, nominalD: Int, w: Double, d: Double) = WoodProfile(
        id = "l", designation = "${nominalW}x$nominalD",
        nominalWidth = nominalW.toDouble().inches, nominalDepth = nominalD.toDouble().inches,
        dressedWidth = w.inches, dressedDepth = d.inches
    )
    private fun cm(m: MaterialGrade.Wood, p: SectionProfile, f: NdsAdjustmentFactors = wet) =
        NdsWetService.resolve(f, resolveDesignMaterial(m, p), p, m.species.isGlulam)

    @Test
    fun `dry service has every CM at 1_0`() {
        val r = cm(wood(WoodSpecies.DF_L, WoodGrade.NO_2), lumber(2, 10, 1.5, 9.25), dry)
        listOf(r.cmFb, r.cmFt, r.cmFv, r.cmFcPerp, r.cmFc, r.cmE).forEach { assertEquals(1.0, it, 0.0) }
        assertTrue(!r.isWet)
    }

    @Test
    fun `glulam wet service factors`() {
        val p = WoodProfile("g", "5.125x12", 5.125.inches, 12.0.inches, 5.125.inches, 12.0.inches)
        for (sp in listOf(WoodSpecies.GLULAM_DF_DF, WoodSpecies.GLULAM_HARDWOODS)) {
            val g = if (sp == WoodSpecies.GLULAM_DF_DF) WoodGrade.G_16F_V3 else WoodGrade.G_24F_E5_RO
            val r = cm(wood(sp, g), p)
            assertEquals(0.8, r.cmFb, 0.0); assertEquals(0.8, r.cmFt, 0.0); assertEquals(0.875, r.cmFv, 0.0)
            assertEquals(0.53, r.cmFcPerp, 0.0); assertEquals(0.73, r.cmFc, 0.0); assertEquals(0.833, r.cmE, 0.0)
        }
    }

    @Test
    fun `sawn lumber CM depends on the property and the Fb and Fc limits`() {
        val p210 = lumber(2, 10, 1.5, 9.25)
        // DF-L Select Structural 2x10: Fb 1500 x CF 1.1 = 1650 > 1150 -> 0.85; Fc 1700 x 1.0 > 750 -> 0.8
        val ss = cm(wood(WoodSpecies.DF_L, WoodGrade.SELECT_STRUCTURAL), p210)
        assertEquals(0.85, ss.cmFb, 0.0); assertEquals(0.8, ss.cmFc, 0.0)
        assertEquals(1.0, ss.cmFt, 0.0); assertEquals(0.97, ss.cmFv, 0.0)
        assertEquals(0.67, ss.cmFcPerp, 0.0); assertEquals(0.9, ss.cmE, 0.0)
        // DF-L No.2 2x10: Fb 900 x 1.1 = 990 <= 1150 -> Fb CM = 1.0; Fc 1350 > 750 -> 0.8
        val n2 = cm(wood(WoodSpecies.DF_L, WoodGrade.NO_2), p210)
        assertEquals(1.0, n2.cmFb, 0.0); assertEquals(0.8, n2.cmFc, 0.0)
        // SPF Utility 2x4: Fb 275, Fc 750 x 1.0 = 750 <= 750 -> both 1.0 (limit is inclusive)
        val ut = cm(wood(WoodSpecies.SPF, WoodGrade.UTILITY), lumber(2, 4, 1.5, 3.5))
        assertEquals(1.0, ut.cmFb, 0.0); assertEquals(1.0, ut.cmFc, 0.0); assertEquals(0.97, ut.cmFv, 0.0)
    }

    @Test
    fun `Southern Pine uses the tabulated Fc with no CF`() {
        // SP No.2 2x4 (Table 4B, 2"-4" wide): Fb 1100 x CF 1.0 = 1100 <= 1150 -> 1.0; Fc 1450 > 750 -> 0.8
        val a = cm(wood(WoodSpecies.SOUTHERN_PINE, WoodGrade.NO_2, 4.0), lumber(2, 4, 1.5, 3.5))
        assertEquals(1.0, a.cmFb, 0.0); assertEquals(0.8, a.cmFc, 0.0)
        // SP No.3 2x12: Fb 450, Fc 725 <= 750 -> 1.0 both
        val b = cm(wood(WoodSpecies.SOUTHERN_PINE, WoodGrade.NO_3, 12.0), lumber(2, 12, 1.5, 11.25))
        assertEquals(1.0, b.cmFb, 0.0); assertEquals(1.0, b.cmFc, 0.0)
    }

    @Test
    fun `wet service is blocked for Dense Structural Southern Pine`() {
        val m = wood(WoodSpecies.SOUTHERN_PINE, WoodGrade.DENSE_STRUCTURAL_86, 4.0)
        assertNotNull(NdsCodeData.blockReason(NdsEdition.NDS_2018, m, true))
        assertNull(NdsCodeData.blockReason(NdsEdition.NDS_2018, m, false))
        var thrown = false
        try { cm(m, lumber(2, 4, 1.5, 3.5)) } catch (e: CodeDataBlockedException) { thrown = true }
        assertTrue(thrown)
    }

    @Test
    fun `the old single CM value is read as wet service and not applied to every property`() {
        val legacy = NdsAdjustmentFactors(cm = 0.85)
        assertTrue(legacy.isWet)
        val r = cm(wood(WoodSpecies.DF_L, WoodGrade.NO_2), lumber(2, 10, 1.5, 9.25), legacy)
        assertEquals(1.0, r.cm, 0.0)
        assertEquals(0.97, r.cmFv, 0.0)      // not 0.85
        assertEquals(1.0, r.cmFb, 0.0)       // Fb x CF = 990 <= 1150
    }

    // ---------------- through the calculator ----------------

    private val spanId: UUID = UUID.randomUUID()
    private val glProfile = WoodProfile("g", "5.125x12", 5.125.inches, 12.0.inches, 5.125.inches, 12.0.inches)
    private fun demand(spanFt: Double? = null) = StationDemand(
        spanId = spanId, x = 180.0.inches, moment = Moment(1000.0), shear = Force(1000.0),
        lbTop = 0.0.inches, lbBottom = 0.0.inches, compressionFlange = Flange.TOP,
        momentSpanLength = spanFt?.let { (it * 12.0).inches }
    )

    @Test
    fun `wet glulam bending and shear capacity use their own CM`() {
        val m = wood(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_16F_V3)
        // L = 30 ft: CV = 0.964961, Sx = 123.0 in^3; Fb' = 1600 x 0.8 x CV
        val bend = NdsWoodCapacityCalculator(glProfile, m, wet).evaluateDetailed(demand(30.0), DesignMethodology.ASD)
        assertEquals(1600.0 * 0.8 * 0.9649610951198176 * 123.0, bend.momentCheck.capacity.lbIn, 1e-3)
        val dryBend = NdsWoodCapacityCalculator(glProfile, m, dry).evaluateDetailed(demand(30.0), DesignMethodology.ASD)
        assertEquals(1600.0 * 0.9649610951198176 * 123.0, dryBend.momentCheck.capacity.lbIn, 1e-3)
        // shear: Fv' = 265 x 0.875 ; Vn = 2/3 Fv' b d = 2/3 x 231.875 x 5.125 x 12
        val shear = NdsWoodCapacityCalculator(glProfile, m, wet).evaluateDetailed(demand(30.0), DesignMethodology.ASD)
        assertEquals(2.0 / 3.0 * 265.0 * 0.875 * 5.125 * 12.0, shear.shearCheck.capacity.pounds, 1e-6)
    }

    @Test
    fun `Emin for stability and E for deflection carry CM`() {
        val m = wood(WoodSpecies.GLULAM_DF_DF, WoodGrade.G_16F_V3)
        // Emin 0.79 million x 0.833
        assertEquals(790_000.0 * 0.833, woodAdjustedEmin(m, NdsWetService.resolve(wet, m, glProfile, true), NdsEdition.NDS_2018), 1e-6)
        // E (Ex app 1.5 million) x 0.833 for deflection; dry stays 1.5 million
        assertEquals(1_500_000.0 * 0.833, ndsDeflectionModulusPsi(m, glProfile, wet, NdsEdition.NDS_2018), 1e-6)
        assertEquals(1_500_000.0, ndsDeflectionModulusPsi(m, glProfile, dry, NdsEdition.NDS_2018), 1e-6)
        // sawn: DF-L No.2 E 1.6 million x 0.9, with Ct 0.9 as well
        val s = wood(WoodSpecies.DF_L, WoodGrade.NO_2)
        assertEquals(1_600_000.0 * 0.9 * 0.9,
            ndsDeflectionModulusPsi(s, lumber(2, 10, 1.5, 9.25), wet.copy(ct = 0.9), NdsEdition.NDS_2018), 1e-6)
        // under NDS 2015 the 2015 table E is used
        assertEquals(1_500_000.0 * 0.833, ndsDeflectionModulusPsi(m, glProfile, wet, NdsEdition.NDS_2015), 1e-6)
    }
}
