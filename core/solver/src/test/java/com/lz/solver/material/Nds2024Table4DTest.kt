package com.lz.solver.material

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NDS 2024 Supplement Table 4D source data. The structural checks guard the extraction; the spot checks are rows
 * read by eye from the printed table (not from the extraction code).
 */
class Nds2024Table4DTest {

    private val t = Nds2024Table4D
    private val bs = "Beams and Stringers"
    private val pt = "Posts and Timbers"

    @Test
    fun `the table has the printed shape`() {
        assertEquals(250, t.rows.size)
        assertEquals(39, t.species.size)
        // 33 species x 2 size classes x 3 grades, plus the special blocks
        val perSpecies = t.rows.groupBy { it.species }
        for ((s, rs) in perSpecies) {
            when (s) {
                "DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH" -> assertEquals(s, 22, rs.size) // PLIB 5+5, WWPA 6+6
                "SITKA SPRUCE" -> assertEquals(s, 12, rs.size)                                  // PLIB 3+3, WWPA 3+3
                "SOUTHERN PINE" -> assertEquals(s, 9, rs.size)
                "BALDCYPRESS", "MIXED SOUTHERN PINE", "REDWOOD" -> assertEquals(s, 3, rs.size)
                else -> assertEquals(s, 6, rs.size)
            }
        }
        assertEquals(setOf("Beams and Stringers", "Posts and Timbers", "5\"x5\" and Larger", "5\" x 5\" and Larger"), t.rows.map { it.sizeClassification }.toSet())
        assertEquals(3, t.rows.count { it.species == "SOUTHERN PINE" && (it.grade.endsWith("86") || it.grade.endsWith("72") || it.grade.endsWith("65")) })
    }

    @Test
    fun `every row is internally consistent`() {
        for (r in t.rows) {
            val id = "${r.species} / ${r.grade} / ${r.sizeClassification} / ${r.gradingRulesAgencies}"
            assertTrue(id, r.eMin in 100_000 until r.e)
            assertTrue(id, r.fb > 0 && r.ft > 0 && r.fv > 0 && r.fcPerp > 0 && r.fc > 0)
            assertTrue(id, r.specificGravity in 0.30..0.75)
            assertTrue(id, r.gradingRulesAgencies.isNotEmpty())
        }
        assertEquals(setOf("MIXED SOUTHERN PINE", "SOUTHERN PINE"), t.rows.filter { it.wetServiceValues }.map { it.species }.toSet())
        // every block has one G
        for ((k, rs) in t.rows.groupBy { it.species to it.gradingRulesAgencies }) assertEquals(k.toString(), 1, rs.map { it.specificGravity }.toSet().size)
    }

    private fun check(
        species: String, grade: String, size: String, agency: String,
        fb: Int, ft: Int, fv: Int, fcPerp: Int, fc: Int, e: Int, eMin: Int, g: Double
    ) {
        val r = t.find(species, grade, size, agency)
        assertNotNull("$species $grade $size $agency", r)
        assertEquals("$species $grade $size $agency", listOf(fb, ft, fv, fcPerp, fc, e, eMin), listOf(r!!.fb, r.ft, r.fv, r.fcPerp, r.fc, r.e, r.eMin))
        assertEquals("$species $grade G", g, r.specificGravity, 0.0)
    }

    @Test
    fun `rows read from the printed pages`() {
        check("ALASKA CEDAR", "Select Structural", bs, "PLIB", 1400, 675, 155, 525, 925, 1_200_000, 440_000, 0.47)
        check("BALDCYPRESS", "No.2", "5\"x5\" and Larger", "SPIB", 625, 425, 175, 615, 600, 1_000_000, 370_000, 0.43)
        check("DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH", "Dense No. 1", bs, "PLIB", 1550, 775, 170, 730, 1100, 1_700_000, 620_000, 0.50)
        check("DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH", "No. 2 Dense", bs, "WWPA", 1000, 500, 170, 730, 700, 1_400_000, 510_000, 0.50)
        check("DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH", "No. 2 Dense", pt, "WWPA", 850, 550, 170, 730, 825, 1_400_000, 510_000, 0.50)
        check("HEM-FIR", "No.1", pt, "WWPA", 975, 650, 140, 405, 850, 1_300_000, 470_000, 0.43)
        check("HEM-FIR", "No.1", pt, "PLIB", 975, 650, 140, 405, 850, 1_300_000, 470_000, 0.43)
        check("MIXED SOUTHERN PINE", "Select Structural", "5\"x5\" and Larger", "SPIB", 1500, 1000, 165, 375, 900, 1_300_000, 470_000, 0.51)
        check("REDWOOD", "No. 2", "5\" x 5\" and Larger", "RIS", 750, 400, 145, 420, 650, 900_000, 330_000, 0.37)
        check("SITKA SPRUCE", "No.2", bs, "PLIB", 650, 325, 140, 435, 450, 1_000_000, 370_000, 0.43)
        check("SITKA SPRUCE", "No.2", bs, "WWPA", 650, 325, 140, 435, 450, 1_100_000, 400_000, 0.43)
        check("SOUTHERN PINE", "Dense Select Structural 86", "5\" x 5\" and Larger", "SPIB", 2100, 1400, 165, 440, 1300, 1_600_000, 580_000, 0.55)
        check("SOUTHERN PINE", "No. 2", "5\" x 5\" and Larger", "SPIB", 850, 550, 165, 375, 525, 1_200_000, 440_000, 0.55)
        check("SPRUCE-PINE-FIR (SOUTH)", "No.2", pt, "NELMA", 475, 325, 125, 335, 425, 1_000_000, 370_000, 0.36)
        check("WESTERN JUNIPER", "Select Structural", pt, "PLIB", 1300, 675, 115, 770, 350, 500_000, 180_000, 0.42)
        check("WHITE OAK", "No.2", bs, "NELMA", 750, 375, 205, 800, 475, 800_000, 290_000, 0.73)
        assertEquals(listOf("PLIB", "WWPA"), t.find("WESTERN WOODS", "No.1", pt, "PLIB")!!.gradingRulesAgencies)
        assertEquals(listOf("NELMA", "PLIB", "WWPA"), t.find("SPRUCE-PINE-FIR (SOUTH)", "No.1", bs, "WWPA")!!.gradingRulesAgencies)
    }

    @Test
    fun `spruce pine conversion factors match footnote 2`() {
        val c = t.sprucePineConversion
        assertEquals(listOf(0.78, 0.78, 0.98, 0.73, 0.78, 0.82), listOf(c.fb, c.ft, c.fv, c.fcPerp, c.fc, c.eAndEMin))
    }
}
