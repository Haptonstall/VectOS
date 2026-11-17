package com.lz.solver.material

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NDS 2024 Supplement Table 4E source data. The structural checks guard the extraction; the spot checks are rows
 * read by eye from the printed table (not from the extraction code).
 */
class Nds2024Table4ETest {

    private val t = Nds2024Table4E

    @Test
    fun `the table has the printed shape`() {
        assertEquals(74, t.rows.size)
        assertEquals(29, t.species.size)
        val per = t.rows.groupBy { it.species }
        for ((s, rs) in per) {
            when (s) {
                "REDWOOD" -> assertEquals(s, 4, rs.size)
                "SOUTHERN PINE" -> assertEquals(s, 10, rs.size)
                "DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH", "HEM-FIR", "WESTERN CEDARS" -> assertEquals(s, 4, rs.size)
                else -> assertEquals(s, 2, rs.size)
            }
        }
        assertEquals(5, t.rows.count { it.species == "SOUTHERN PINE" && it.surfacing!!.startsWith("(Surfaced dry") })
        assertEquals(5, t.rows.count { it.species == "SOUTHERN PINE" && it.surfacing!!.startsWith("(Surfaced Green") })
        assertEquals(10, t.rows.count { it.surfacing != null })
    }

    @Test
    fun `every row is internally consistent`() {
        for (r in t.rows) {
            val id = "${r.species} / ${r.grade} / ${r.gradingRulesAgencies}"
            assertTrue(id, r.eMin in 100_000 until r.e)
            assertTrue(id, r.specificGravity in 0.30..0.60)
            assertTrue(id, r.gradingRulesAgencies.isNotEmpty())
            // (Fb)(Cr) is the single-member value times a repetitive factor of roughly 1.1 to 1.2
            val single = r.fbSingle
            if (single != null) assertTrue(id, r.fbRepetitive.toDouble() / single in 1.10..1.20)
        }
        // printed dashes: exactly these species have no single-member Fb and no Fc-perp
        val noSingle = t.rows.filter { it.fbSingle == null }
        assertEquals(
            setOf("BALSAM FIR", "EASTERN HEMLOCK-TAMARACK", "EASTERN SPRUCE", "EASTERN WHITE PINE", "NORTHERN PINE", "NORTHERN WHITE CEDAR"),
            noSingle.map { it.species }.toSet()
        )
        assertEquals(12, noSingle.size)
        assertTrue(noSingle.all { it.fcPerp == null })
        assertEquals(setOf("REDWOOD"), t.rows.filter { it.fbSingle != null && it.fcPerp == null }.map { it.species }.toSet())
        assertEquals(2, t.rows.count { it.fbSingle != null && it.fcPerp == null })
    }

    private fun check(
        species: String, grade: String, agency: String, fbSingle: Int?, fbRep: Int, fcPerp: Int?, e: Int, eMin: Int, g: Double,
        surfacing: String? = null
    ): Nds2024Table4E.Row {
        val r = t.find(species, grade, agency, surfacing)
        assertNotNull("$species $grade $agency", r)
        assertEquals("$species $grade $agency", fbSingle, r!!.fbSingle)
        assertEquals("$species $grade $agency", listOf(fbRep, e, eMin), listOf(r.fbRepetitive, r.e, r.eMin))
        assertEquals("$species $grade $agency", fcPerp, r.fcPerp)
        assertEquals("$species $grade G", g, r.specificGravity, 0.0)
        return r
    }

    @Test
    fun `rows read from the printed pages`() {
        check("BALSAM FIR", "Select", "NELMA", null, 1650, null, 1_500_000, 550_000, 0.36)
        check("BALSAM FIR", "Commercial", "NELMA", null, 1400, null, 1_300_000, 470_000, 0.36)
        check("COAST SITKA SPRUCE", "Select", "NLGA", 1250, 1450, 455, 1_700_000, 620_000, 0.43)
        val dflDex = check("DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH", "Select Dex", "PLIB", 1750, 2000, 625, 1_800_000, 660_000, 0.50)
        assertEquals("2\"-4\" thick / 6\"-8\"wide", dflDex.sizeClassification)
        val dflW = check("DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH", "Commercial", "WWPA", 1450, 1650, 625, 1_700_000, 620_000, 0.50)
        assertEquals("2\"-4\" thick / 4\"-12\"wide", dflW.sizeClassification)
        check("EASTERN WHITE PINE (NORTH)", "Commercial", "NLGA", 775, 875, 350, 1_100_000, 400_000, 0.38)
        check("NORTHERN WHITE CEDAR", "Commercial", "NELMA", null, 950, null, 700_000, 260_000, 0.31)
        val rwS = check("REDWOOD", "Select", "RIS", 1450, 1700, null, 1_100_000, 400_000, 0.37)
        assertEquals("2\" thick / 6\"& wider", rwS.sizeClassification)
        val rwH = check("REDWOOD", "Deck Heart", "RIS", 400, 450, 420, 900_000, 330_000, 0.37)
        assertEquals("2\" thick / 4\" wide", rwH.sizeClassification)
        val rwC = check("REDWOOD", "Deck Common", "RIS", 700, 800, 420, 900_000, 330_000, 0.37)
        assertEquals("2\" thick / 6\" wide", rwC.sizeClassification)
        val spDry = check("SOUTHERN PINE", "Dense Standard", "SPIB", 2000, 2300, 660, 1_800_000, 660_000, 0.55, surfacing = "Surfaced dry")
        assertEquals("2\"-4\" thick / 2\" & wider", spDry.sizeClassification)
        val spGreen = check("SOUTHERN PINE", "Commercial", "SPIB", 1150, 1300, 375, 1_400_000, 510_000, 0.55, surfacing = "Surfaced Green")
        assertEquals("2-1/2\"-4\" thick / 2\" & wider", spGreen.sizeClassification)
        check("SPRUCE-PINE-FIR (SOUTH)", "Selected", "WWPA", 1150, 1350, 335, 1_400_000, 510_000, 0.36)
        assertEquals(listOf("NELMA", "WWPA"), t.find("SPRUCE-PINE-FIR (SOUTH)", "Commercial", "NELMA")!!.gradingRulesAgencies)
        check("WESTERN HEMLOCK", "Select Dex", "PLIB", 1500, 1750, 410, 1_600_000, 580_000, 0.47)
        check("WESTERN WOODS", "Commercial", "WWPA", 950, 1100, 335, 1_100_000, 400_000, 0.36)
        assertNull(t.find("WESTERN WOODS", "Commercial", "PLIB"))
    }
}
