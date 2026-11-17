package com.lz.solver.material

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NDS 2024 Supplement Table 4C source data. The structural checks guard the extraction; the spot checks are rows
 * read by eye from the printed table (not from the extraction code).
 */
class Nds2024Table4CTest {

    private val t = Nds2024Table4C

    @Test
    fun `the tables have the printed shape`() {
        assertEquals(76, t.rows.size)
        assertEquals(53, t.msrRows.size)
        assertEquals(23, t.melRows.size)
        assertEquals(36, t.speciesRows.size)
        assertEquals(
            listOf(
                "Douglas Fir-Larch", "Douglas Fir-Larch (N)", "Douglas Fir-South", "Englemann Spruce- Lodgepole Pine", "Hem-Fir",
                "Hem-Fir (N)", "Southern Pine", "Spruce-Pine-Fir", "Spruce-Pine-Fir (S)", "Western Cedars", "Western Woods"
            ),
            t.speciesRows.map { it.species }.distinct()
        )
        assertEquals(
            listOf(6, 3, 1, 2, 10, 1, 3, 3, 5, 1, 1),
            t.speciesRows.groupBy { it.species }.values.map { it.size }
        )
        assertEquals("2\" and less in thickness, 2\" and wider", Nds2024Table4C.SIZE_CLASSIFICATION)
    }

    @Test
    fun `msr rows agree with their own designation and are internally consistent`() {
        for (r in t.rows) {
            assertTrue(r.designation, r.eMin in 100_000 until r.e)
            assertTrue(r.designation, r.fb > 0 && r.ft > 0 && r.fc > 0)
            assertTrue(r.designation, r.gradingRulesAgencies.isNotEmpty())
            assertEquals(r.designation, r.gradingRulesAgencies.sorted(), r.gradingRulesAgencies)
        }
        for (r in t.msrRows) {
            val m = Regex("""(\d+)f-(\d\.\d)E""").matchEntire(r.designation)
            assertNotNull(r.designation, m)
            assertEquals(r.designation, m!!.groupValues[1].toInt(), r.fb)
            assertEquals(r.designation, Math.round(m.groupValues[2].toDouble() * 1_000_000.0).toInt(), r.e)
        }
        // five MSR designations are printed twice with different values
        val dup = t.rows.groupBy { it.designation }.filterValues { it.size > 1 }
        assertEquals(setOf("1450f-1.3E", "1650f-1.6E", "1950f-1.9E", "2250f-2.0E", "2500f-2.2E"), dup.keys)
        assertEquals(76 - 5, t.rows.map { it.designation }.toSet().size)
    }

    private fun check(designation: String, agency: String, fb: Int, ft: Int, fc: Int, e: Int, eMin: Int, agencies: List<String>) {
        val r = t.find(designation, agency)
        assertNotNull("$designation $agency", r)
        assertEquals("$designation $agency", listOf(fb, ft, fc, e, eMin), listOf(r!!.fb, r.ft, r.fc, r.e, r.eMin))
        assertEquals("$designation $agency", agencies, r.gradingRulesAgencies)
    }

    @Test
    fun `grade rows read from the printed pages`() {
        check("900f-1.0E", "SPIB", 900, 350, 1050, 1_000_000, 510_000, listOf("NELMA", "PLIB", "SPIB", "WWPA"))
        check("1450f-1.3E", "SPIB", 1450, 825, 1600, 1_300_000, 660_000, listOf("SPIB"))
        check("1450f-1.3E", "NLGA", 1450, 800, 1625, 1_300_000, 660_000, listOf("NELMA", "NLGA", "PLIB", "WWPA"))
        check("1700f-1.6E", "PLIB", 1700, 1175, 1725, 1_600_000, 810_000, listOf("PLIB"))
        check("2700f-2.2E", "WWPA", 2700, 2150, 2100, 2_200_000, 1_120_000, listOf("NELMA", "NLGA", "PLIB", "SPIB", "WWPA"))
        check("2850f-1.8E", "SPIB", 2850, 1600, 2100, 1_800_000, 910_000, listOf("SPIB"))
        check("3000f-2.4E", "SPIB", 3000, 2400, 2200, 2_400_000, 1_220_000, listOf("NLGA", "SPIB"))
        check("M-5", "SPIB", 900, 500, 1050, 1_100_000, 510_000, listOf("SPIB"))
        check("M-10", "NLGA", 1400, 800, 1600, 1_200_000, 560_000, listOf("NLGA"))
        check("M-25", "SPIB", 2750, 2000, 2100, 2_200_000, 1_030_000, listOf("NLGA", "SPIB"))
        check("M-31", "SPIB", 2850, 1600, 2150, 1_900_000, 890_000, listOf("SPIB"))
        // 1950f-1.9E is printed twice for PLIB with different Ft
        assertEquals(listOf(900, 1375), t.findAll("1950f-1.9E").map { it.ft })
    }

    @Test
    fun `species rows read from the printed page`() {
        fun row(species: String, e: String) = t.speciesRows.first { it.species == species && it.eRange == e }
        val dfl24 = row("Douglas Fir-Larch", "2.4")
        assertEquals(listOf(0.55, 185, 760), listOf(dfl24.specificGravity, dfl24.fv, dfl24.fcPerp))
        assertEquals(listOf("PLIB", "WWPA"), dfl24.gradingRulesAgencies)
        val hf16 = row("Hem-Fir", "1.6")
        assertEquals(listOf(0.44, 155, 510), listOf(hf16.specificGravity, hf16.fv, hf16.fcPerp))
        val sp18 = row("Southern Pine", "1.8")
        assertEquals(listOf(0.57, 190, 805), listOf(sp18.specificGravity, sp18.fv, sp18.fcPerp))
        assertTrue(sp18.footnoteMarked)
        assertEquals(1, t.speciesRows.count { it.footnoteMarked })
        assertEquals(listOf(0.55, 175, 565), listOf(Nds2024Table4C.SOUTHERN_PINE_1_8E_G055.SPECIFIC_GRAVITY, Nds2024Table4C.SOUTHERN_PINE_1_8E_G055.FV, Nds2024Table4C.SOUTHERN_PINE_1_8E_G055.FC_PERP))
        val spfs = row("Spruce-Pine-Fir (S)", "1.2 to 1.9")
        assertEquals(listOf(0.42, 150, 465), listOf(spfs.specificGravity, spfs.fv, spfs.fcPerp))
        assertEquals(listOf("NELMA"), spfs.gradingRulesAgencies)
        val ww = row("Western Woods", "1.0 and higher")
        assertEquals(listOf(0.36, 135, 335), listOf(ww.specificGravity, ww.fv, ww.fcPerp))
        val dflN = row("Douglas Fir-Larch (N)", "2.3 & higher")
        assertEquals(listOf(0.57, 190, 715), listOf(dflN.specificGravity, dflN.fv, dflN.fcPerp))
    }
}
