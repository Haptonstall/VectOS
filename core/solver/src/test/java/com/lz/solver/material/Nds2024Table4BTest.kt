package com.lz.solver.material

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NDS 2024 Supplement Table 4B source data. The structural checks guard the extraction; the spot checks are rows
 * read by eye from the printed table (not from the extraction code).
 */
class Nds2024Table4BTest {

    private val rows = Nds2024Table4B.rows
    private val sp = "SOUTHERN PINE"
    private val mixed = "MIXED SOUTHERN PINE"

    @Test
    fun `the table has the printed shape`() {
        assertEquals(76, rows.size)
        assertEquals(listOf(sp, mixed), Nds2024Table4B.species)
        assertEquals(
            listOf("2\" - 4\" wide", "4\" wide", "5\" - 6\" wide", "8\" wide", "10\" wide", "12\" wide"),
            Nds2024Table4B.widthClassifications
        )
        // Southern Pine: 10 grades in each of 5 width groups plus Construction/Standard/Utility at 4" wide
        val spRows = rows.filter { it.species == sp }
        assertEquals(53, spRows.size)
        assertEquals(10, spRows.count { it.widthClassification == "2\" - 4\" wide" })
        assertEquals(3, spRows.count { it.widthClassification == "4\" wide" })
        for (w in listOf("5\" - 6\" wide", "8\" wide", "10\" wide", "12\" wide")) assertEquals(w, 10, spRows.count { it.widthClassification == w })
        // Mixed Southern Pine: 4 grades in each of 5 width groups plus the same three at 4" wide
        val mixedRows = rows.filter { it.species == mixed }
        assertEquals(23, mixedRows.size)
        assertEquals(3, mixedRows.count { it.widthClassification == "4\" wide" })
        for (w in listOf("2\" - 4\" wide", "5\" - 6\" wide", "8\" wide", "10\" wide", "12\" wide")) assertEquals(w, 4, mixedRows.count { it.widthClassification == w })
        assertEquals(setOf("Construction", "Standard", "Utility"), rows.filter { it.widthClassification == "4\" wide" }.map { it.grade }.toSet())
    }

    @Test
    fun `every row is internally consistent`() {
        for (r in rows) {
            val id = "${r.species} / ${r.grade} / ${r.widthClassification}"
            assertTrue(id, r.eMin in 100_000 until r.e)
            assertTrue(id, r.fb > 0 && r.ft > 0 && r.fc > 0 && r.fcPerp > 0)
            assertEquals(id, 175, r.fv)
            assertEquals(id, if (r.species == sp) 0.55 else 0.51, r.specificGravity, 0.0)
            assertEquals(id, "SPIB", r.gradingRulesAgency)
        }
        assertEquals(rows.size, rows.map { Triple(it.species, it.grade, it.widthClassification) }.toSet().size)
    }

    private fun check(species: String, grade: String, width: String, fb: Int, ft: Int, fv: Int, fcPerp: Int, fc: Int, e: Int, eMin: Int) {
        val r = Nds2024Table4B.find(species, grade, width)
        assertNotNull("$species / $grade / $width", r)
        assertEquals("$species $grade $width", listOf(fb, ft, fv, fcPerp, fc, e, eMin), listOf(r!!.fb, r.ft, r.fv, r.fcPerp, r.fc, r.e, r.eMin))
    }

    @Test
    fun `rows read from the printed pages`() {
        check(sp, "Dense Select Structural", "2\" - 4\" wide", 2700, 1900, 175, 660, 2050, 1900000, 690000)
        check(sp, "No.2", "2\" - 4\" wide", 1100, 675, 175, 565, 1450, 1400000, 510000)
        check(sp, "Standard", "4\" wide", 475, 275, 175, 565, 1300, 1200000, 440000)
        check(sp, "No.3 and Stud", "5\" - 6\" wide", 575, 350, 175, 565, 800, 1300000, 470000)
        check(sp, "Non-Dense Select Structural", "8\" wide", 1700, 1200, 175, 480, 1650, 1600000, 580000)
        check(sp, "No.1 Dense", "10\" wide", 1200, 800, 175, 660, 1550, 1800000, 660000)
        check(sp, "No.2", "12\" wide", 750, 450, 175, 565, 1250, 1400000, 510000)
        check(sp, "No.3 and Stud", "12\" wide", 450, 250, 175, 565, 725, 1300000, 470000)
        check(mixed, "Select Structural", "2\" - 4\" wide", 2050, 1200, 175, 565, 1800, 1600000, 580000)
        check(mixed, "Utility", "4\" wide", 225, 125, 175, 565, 850, 1100000, 400000)
        check(mixed, "No.1", "8\" wide", 1200, 700, 175, 565, 1450, 1500000, 550000)
        check(mixed, "No.3 and Stud", "12\" wide", 450, 250, 175, 565, 725, 1200000, 440000)
    }

    @Test
    fun `spruce pine conversion factors match footnote 3`() {
        val c = Nds2024Table4B.sprucePineConversion
        assertEquals(listOf(0.78, 0.78, 0.98, 0.73, 0.78, 0.82), listOf(c.fb, c.ft, c.fv, c.fcPerp, c.fc, c.eAndEMin))
    }
}
