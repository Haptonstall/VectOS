package com.lz.solver.material

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NDS 2024 Supplement Table 4A source data. The structural checks guard the extraction; the spot checks are rows
 * read by eye from the printed table (not from the extraction code).
 */
class Nds2024Table4ATest {

    private val rows = Nds2024Table4A.rows

    @Test
    fun `the table has the printed shape`() {
        assertEquals(277, rows.size)
        assertEquals(35, Nds2024Table4A.species.size)
        // 8 grades per species, 7 where No. 1 and No. 2 are one grade, 9 where No. 1 & Btr is also listed
        val perSpecies = rows.groupBy { it.species }.mapValues { it.value.size }
        assertEquals(28, perSpecies.values.count { it == 8 })
        assertEquals(5, perSpecies.values.count { it == 7 })
        assertEquals(2, perSpecies.values.count { it == 9 })
        assertEquals(setOf("DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH", "HEM-FIR"), perSpecies.filterValues { it == 9 }.keys)
    }

    @Test
    fun `every row is internally consistent`() {
        for (r in rows) {
            val id = "${r.species} / ${r.grade}"
            assertTrue(id, r.eMin in 100_000 until r.e)
            assertTrue(id, r.fb > 0 && r.ft > 0 && r.fv > 0 && r.fcPerp > 0 && r.fc > 0)
            assertTrue(id, r.specificGravity in 0.3..0.8)
            assertTrue(id, r.gradingRulesAgencies.isNotEmpty())
            assertEquals(id, if (r.grade in setOf("Construction", "Standard", "Utility")) "2\" - 4\" wide" else "2\" & wider", r.sizeClassification)
        }
        assertEquals(rows.size, rows.map { it.species to it.grade }.toSet().size)
        val grades = rows.map { it.grade }.toSet()
        assertEquals(
            setOf("Select Structural", "No. 1 & Btr", "No. 1", "No. 1/ No. 2", "No. 2", "No. 3", "Stud", "Construction", "Standard", "Utility"),
            grades
        )
    }

    private fun check(species: String, grade: String, fb: Int, ft: Int, fv: Int, fcPerp: Int, fc: Int, e: Int, eMin: Int, sg: Double) {
        val r = Nds2024Table4A.find(species, grade)
        assertNotNull("$species / $grade", r)
        assertEquals("$species $grade", listOf(fb, ft, fv, fcPerp, fc, e, eMin), listOf(r!!.fb, r.ft, r.fv, r.fcPerp, r.fc, r.e, r.eMin))
        assertEquals(sg, r.specificGravity, 0.0)
    }

    @Test
    fun `rows read from the printed pages`() {
        val dfl = "DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH"
        check(dfl, "Select Structural", 1500, 1000, 180, 625, 1700, 1900000, 690000, 0.50)
        check(dfl, "No. 1 & Btr", 1200, 800, 180, 625, 1550, 1800000, 660000, 0.50)
        check(dfl, "No. 2", 900, 575, 180, 625, 1350, 1600000, 580000, 0.50)
        check(dfl, "No. 3", 525, 325, 180, 625, 775, 1400000, 510000, 0.50)
        check(dfl, "Utility", 275, 175, 180, 625, 900, 1300000, 470000, 0.50)
        check("BEECH-BIRCH-HICKORY", "Select Structural", 1450, 850, 195, 715, 1200, 1700000, 620000, 0.71)
        check("COAST SITKA SPRUCE", "No. 1/ No. 2", 925, 550, 125, 455, 1100, 1500000, 550000, 0.43)
        check("COTTONWOOD", "Utility", 175, 100, 125, 320, 325, 900000, 330000, 0.41)
        check("DOUGLAS FIR-SOUTH", "No. 2", 850, 525, 180, 520, 1350, 1200000, 440000, 0.46)
        check("SPRUCE-PINE-FIR (SOUTH)", "Select Structural", 1300, 575, 135, 335, 1200, 1300000, 470000, 0.36)
    }
}
