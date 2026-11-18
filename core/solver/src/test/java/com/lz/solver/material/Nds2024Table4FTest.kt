package com.lz.solver.material

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NDS 2024 Supplement Table 4F source data. The structural checks guard the extraction; the spot checks are rows
 * read by eye from the printed table (not from the extraction code).
 */
class Nds2024Table4FTest {

    private val t = Nds2024Table4F
    private val grades = listOf("Select Structural", "No. 1", "No. 2", "No. 3", "Stud", "Construction", "Standard", "Utility")

    @Test
    fun `the table has the printed shape`() {
        assertEquals(144, t.rows.size)
        assertEquals(18, t.species.size)
        for ((s, rs) in t.rows.groupBy { it.species }) {
            assertEquals(s, grades, rs.map { it.grade })
            assertEquals(s, 1, rs.map { it.specificGravity }.toSet().size)
            assertEquals(s, 1, rs.map { it.gradingRulesAgencies }.toSet().size)
            assertEquals(s, 1, rs.map { it.fv }.toSet().size)
            assertEquals(s, 1, rs.map { it.fcPerp }.toSet().size)
            // size cells: Select Structural..No. 3 and Stud as one cell group, Construction/Standard/Utility another
            assertEquals(s, 1, rs.take(5).map { it.sizeClassification }.toSet().size)
            assertEquals(s, 1, rs.drop(5).map { it.sizeClassification }.toSet().size)
            assertEquals(s, "2\" - 4\" wide", rs.last().sizeClassification)
        }
        assertEquals(setOf("2\" & wider", "2\" - 4\" wide"), t.rows.map { it.sizeClassification }.toSet())
        // only the Douglas Fir/European Larch block is "2" - 4" wide" throughout
        val allNarrow = t.rows.groupBy { it.species }.filterValues { rs -> rs.all { it.sizeClassification == "2\" - 4\" wide" } }.keys
        assertEquals(setOf("DOUGLAS FIR/EUROPEAN LARCH - Austria, The Czech Republic, & Bavaria"), allNarrow)
        assertEquals(setOf("DOUGLAS FIR/EUROPEAN LARCH - Austria, The Czech Republic, & Bavaria" to listOf(2), "SCOTS PINE - Germany" to listOf(4)),
            t.rows.filter { it.footnotes.isNotEmpty() }.map { it.species to it.footnotes }.toSet())
    }

    @Test
    fun `every row is internally consistent`() {
        for (r in t.rows) {
            val id = "${r.species} / ${r.grade}"
            assertTrue(id, r.eMin in 100_000 until r.e)
            assertTrue(id, r.fb > 0 && r.ft > 0 && r.fc > 0)
            assertTrue(id, r.specificGravity in 0.35..0.60)
            assertTrue(id, r.gradingRulesAgencies.isNotEmpty())
        }
        assertEquals(setOf("SOUTHERN PINE - Misiones Argentina", "SOUTHERN PINE - Misiones Argentina, Free of Heart Center and Medium Grain Density"),
            t.rows.filter { it.gradingRulesAgencies == listOf("SPIB") }.map { it.species }.toSet())
    }

    private fun check(species: String, grade: String, fb: Int, ft: Int, fv: Int, fcPerp: Int, fc: Int, e: Int, eMin: Int, g: Double) {
        val r = t.find(species, grade)
        assertNotNull("$species $grade", r)
        assertEquals("$species $grade", listOf(fb, ft, fv, fcPerp, fc, e, eMin), listOf(r!!.fb, r.ft, r.fv, r.fcPerp, r.fc, r.e, r.eMin))
        assertEquals("$species $grade G", g, r.specificGravity, 0.0)
    }

    @Test
    fun `rows read from the printed pages`() {
        check("AUSTRIAN SPRUCE - Austria & The Czech Republic", "Select Structural", 1500, 675, 175, 260, 1250, 1_700_000, 620_000, 0.43)
        check("AUSTRIAN SPRUCE - Austria & The Czech Republic", "Utility", 275, 125, 175, 260, 725, 1_200_000, 440_000, 0.43)
        check("DOUGLAS FIR - France & Germany", "Stud", 650, 300, 205, 540, 650, 1_300_000, 470_000, 0.48)
        check("DOUGLAS FIR/EUROPEAN LARCH - Austria, The Czech Republic, & Bavaria", "No. 2", 1350, 600, 195, 440, 1250, 1_600_000, 580_000, 0.48)
        check("MONTANE PINE - South Africa", "Utility", 175, 75, 135, 325, 575, 800_000, 290_000, 0.45)
        check("NORWAY SPRUCE - Norway", "Utility", 250, 100, 115, 360, 675, 1_100_000, 370_000, 0.43)
        check("NORWAY SPRUCE - Romania & Ukraine", "Select Structural", 1250, 575, 100, 275, 1200, 1_500_000, 550_000, 0.38)
        check("SCOTS PINE - Finland", "Standard", 600, 275, 150, 210, 1100, 1_100_000, 400_000, 0.48)
        check("SCOTS PINE - Germany", "No. 1", 800, 375, 160, 395, 1050, 1_400_000, 510_000, 0.53)
        check("SCOTS PINE - Sweden", "Utility", 200, 75, 120, 410, 600, 1_000_000, 370_000, 0.47)
        check("SILVER FIR (Abies alba) - Germany. NE France, & Switzerland", "Select Structural", 950, 425, 125, 400, 1100, 1_500_000, 550_000, 0.43)
        check("SOUTHERN PINE - Misiones Argentina", "No. 1", 775, 350, 150, 440, 1000, 1_100_000, 400_000, 0.45)
        check("SOUTHERN PINE - Misiones Argentina, Free of Heart Center and Medium Grain Density", "No. 2", 1000, 450, 210, 710, 1100, 1_500_000, 550_000, 0.54)
    }
}
