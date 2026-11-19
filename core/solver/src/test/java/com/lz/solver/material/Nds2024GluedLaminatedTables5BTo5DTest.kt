package com.lz.solver.material

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NDS 2024 Supplement Tables 5B, 5C and 5D source data. The structural checks guard the extraction; the spot checks
 * are rows read by eye from the printed tables (not from the extraction code).
 */
class Nds2024GluedLaminatedTables5BTo5DTest {

    // ---------------------------------------------------------------- 5B
    private val b = Nds2024Table5B

    @Test
    fun `5B has the printed shape and consistent rows`() {
        assertEquals(30, b.rows.size)
        assertEquals(17, b.rows.count { it.section == "Visually Graded Western Species" })
        assertEquals(13, b.rows.count { it.section == "Visually Graded Southern Pine" })
        assertEquals(setOf("DF", "HF", "SW", "AC", "POC", "SP"), b.rows.map { it.species }.toSet())
        assertEquals(30, b.rows.map { it.symbol }.toSet().size)
        for (r in b.rows) {
            val id = "${r.symbol} ${r.species} ${r.grade}"
            assertTrue(id, r.eMin < r.e && r.e <= r.eTrue)
            assertTrue(id, r.fbyLam4Plus >= r.fbyLam3 && r.fbyLam3 >= r.fbyLam2)
            assertTrue(id, r.fcLam4Plus >= r.fcLam2or3)
            assertTrue(id, r.specificGravity in 0.30..0.60)
        }
        assertEquals(listOf(Nds2024Table5B.Marker("label", listOf(5))), b.find("22")!!.markers)
        assertEquals(1, b.rows.count { it.markers.isNotEmpty() })
    }

    private fun checkB(symbol: String, species: String, grade: String, v: List<Number>) {
        val r = b.find(symbol)
        assertNotNull(symbol, r)
        assertEquals(symbol, listOf(species, grade), listOf(r!!.species, r.grade))
        assertEquals(
            symbol, v,
            listOf(r.eTrue, r.e, r.eMin, r.fcPerp, r.ft, r.fcLam4Plus, r.fcLam2or3, r.fbyLam4Plus, r.fbyLam3, r.fbyLam2, r.fvy, r.fbxLam2To15InDeep, r.fvx, r.specificGravity)
        )
    }

    @Test
    fun `5B rows read from the printed page`() {
        checkB("1", "DF", "L3", listOf(1.6, 1.5, 0.79, 560, 950, 1550, 1250, 1450, 1250, 1000, 230, 1250, 265, 0.50))
        checkB("22", "SW", "L3", listOf(1.1, 1.0, 0.53, 315, 525, 850, 725, 800, 700, 575, 170, 725, 195, 0.35))
        checkB("47 1:10", "SP", "N2M10", listOf(1.5, 1.4, 0.74, 650, 1150, 1700, 1150, 1750, 1550, 1300, 260, 1400, 300, 0.55))
        checkB("50 1:10", "SP", "N1D", listOf(2.0, 1.9, 1.00, 740, 1350, 2000, 1700, 2100, 2100, 1750, 260, 2100, 300, 0.55))
        checkB("75", "POC", "L1D", listOf(1.8, 1.7, 0.90, 560, 1350, 2300, 2050, 1950, 1750, 1500, 230, 1850, 265, 0.46))
    }

    // ---------------------------------------------------------------- 5C
    private val c = Nds2024Table5C

    @Test
    fun `5C has the printed shape and consistent rows`() {
        assertEquals(21, c.rows.size)
        assertEquals(12, c.rows.count { it.section == "Visually Graded Hardwoods" })
        assertEquals(9, c.rows.count { it.section == "E-Rated Hardwoods" })
        assertEquals(21, c.rows.map { it.symbol }.toSet().size)
        assertEquals(mapOf("24F-E3" to "YP", "24F-E4" to "RM", "24F-E5" to "RO"), c.rows.filter { it.speciesCode != null }.associate { it.symbol to it.speciesCode })
        for (r in c.rows) {
            val id = r.symbol
            assertTrue(id, r.exMin < r.ex && r.eyMin < r.ey)
            assertTrue(id, r.fbxNeg <= r.fbxPos || r.fbxNeg == 2000)
            assertTrue(id, r.specificGravity in 0.35..0.70)
        }
    }

    @Test
    fun `5C rows read from the printed page`() {
        val a = c.find("12F-V1")!!
        assertEquals(listOf(1200, 600, 285, 125), listOf(a.fbxPos, a.fbxNeg, a.fcx, a.fvx))
        assertEquals(listOf(1.2, 0.63), listOf(a.ex, a.exMin))
        assertEquals(listOf(1050, 285, 110), listOf(a.fby, a.fcy, a.fvy))
        assertEquals(listOf(1.0, 0.53), listOf(a.ey, a.eyMin))
        assertEquals(listOf(600, 800), listOf(a.ft, a.fc))
        assertEquals(0.39, a.specificGravity, 0.0)
        val d = c.find("20F-V2")!!
        assertEquals(listOf(2000, 2000, 835, 200), listOf(d.fbxPos, d.fbxNeg, d.fcx, d.fvx))
        val e = c.find("24F-E3")!!
        assertEquals("YP", e.speciesCode)
        assertEquals(listOf(2400, 1200, 590, 155), listOf(e.fbxPos, e.fbxNeg, e.fcx, e.fvx))
        assertEquals(listOf(1.8, 0.95), listOf(e.ex, e.exMin))
        assertEquals(listOf(1450, 405, 135), listOf(e.fby, e.fcy, e.fvy))
        assertEquals(listOf(1.5, 0.79), listOf(e.ey, e.eyMin))
        assertEquals(listOf(975, 1200), listOf(e.ft, e.fc))
        assertEquals(0.45, e.specificGravity, 0.0)
        val f = c.find("24F-E5")!!
        assertEquals(listOf(2400, 1200, 1075, 235), listOf(f.fbxPos, f.fbxNeg, f.fcx, f.fvx))
        assertEquals(listOf(1.8, 0.95, 1.5, 0.79), listOf(f.ex, f.exMin, f.ey, f.eyMin))
        assertEquals(listOf(1700, 900, 205), listOf(f.fby, f.fcy, f.fvy))
        assertEquals(listOf(1100, 1450), listOf(f.ft, f.fc))
        assertEquals(0.63, f.specificGravity, 0.0)
    }

    // ---------------------------------------------------------------- 5D
    private val d = Nds2024Table5D

    @Test
    fun `5D has the printed shape and consistent rows`() {
        assertEquals(37, d.rows.size)
        assertEquals((1..37).map { "H$it" }, d.rows.map { it.symbol })
        assertEquals(16, d.rows.count { it.section == "Visually Graded Hardwoods" })
        assertEquals(21, d.rows.count { it.section == "E-Rated Hardwoods" })
        assertEquals(setOf("A", "B", "C", "D"), d.rows.map { it.speciesGroup }.toSet())
        assertEquals(setOf("N3", "N2", "N1", "SS"), d.rows.take(16).map { it.grade }.toSet())
        assertTrue(d.rows.all { it.markers.isEmpty() })
        for (r in d.rows) {
            val id = r.symbol
            assertTrue(id, r.eMin < r.e)
            assertTrue(id, r.fbyLam4Plus >= r.fbyLam3 && r.fbyLam3 >= r.fbyLam2)
            assertTrue(id, r.fcLam4Plus >= r.fcLam2or3)
            assertTrue(id, r.specificGravity in 0.35..0.70)
        }
        // each species group has the same G in every row
        for ((g, rs) in d.rows.groupBy { it.speciesGroup }) assertEquals(g, 1, rs.map { it.specificGravity }.toSet().size)
    }

    @Test
    fun `5D rows read from the printed pages`() {
        val a = d.find("H1")!!
        assertEquals(listOf("A", "N3"), listOf(a.speciesGroup, a.grade))
        assertEquals(listOf(1.3, 0.67), listOf(a.e, a.eMin))
        assertEquals(listOf(835, 425, 900, 900, 1250, 1100, 875, 175, 925, 200), listOf(a.fcPerp, a.ft, a.fcLam4Plus, a.fcLam2or3, a.fbyLam4Plus, a.fbyLam3, a.fbyLam2, a.fvy, a.fbxLam2To15InDeep, a.fvx))
        assertEquals(0.63, a.specificGravity, 0.0)
        val b12 = d.find("H12")!!
        assertEquals(listOf("C", "SS"), listOf(b12.speciesGroup, b12.grade))
        assertEquals(listOf(1.3, 0.67), listOf(b12.e, b12.eMin))
        assertEquals(listOf(405, 825, 1100, 1100, 1450, 1350, 1150, 135, 1200, 155), listOf(b12.fcPerp, b12.ft, b12.fcLam4Plus, b12.fcLam2or3, b12.fbyLam4Plus, b12.fbyLam3, b12.fbyLam2, b12.fvy, b12.fbxLam2To15InDeep, b12.fvx))
        val c35 = d.find("H35")!!
        assertEquals(listOf("D", "1.8E6"), listOf(c35.speciesGroup, c35.grade))
        assertEquals(listOf(1.7, 0.88), listOf(c35.e, c35.eMin))
        assertEquals(listOf(440, 1450, 2000, 1900, 2300, 2200, 1950, 110, 1650, 125), listOf(c35.fcPerp, c35.ft, c35.fcLam4Plus, c35.fcLam2or3, c35.fbyLam4Plus, c35.fbyLam3, c35.fbyLam2, c35.fvy, c35.fbxLam2To15InDeep, c35.fvx))
        assertEquals(0.39, c35.specificGravity, 0.0)
        val z = d.find("H37")!!
        assertEquals(listOf(1.9, 0.98), listOf(z.e, z.eMin))
        assertEquals(listOf(440, 1700, 2400, 2300, 2400, 2400, 2300, 110, 2100, 125), listOf(z.fcPerp, z.ft, z.fcLam4Plus, z.fcLam2or3, z.fbyLam4Plus, z.fbyLam3, z.fbyLam2, z.fvy, z.fbxLam2To15InDeep, z.fvx))
    }
}
