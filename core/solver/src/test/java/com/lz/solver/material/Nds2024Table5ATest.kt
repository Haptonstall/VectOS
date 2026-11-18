package com.lz.solver.material

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NDS 2024 Supplement Table 5A (and Expanded) source data. The structural checks guard the extraction; the spot checks
 * are rows read by eye from the printed table (not from the extraction code).
 */
class Nds2024Table5ATest {

    private val t = Nds2024Table5A

    @Test
    fun `the tables have the printed shape`() {
        assertEquals(listOf("16F-1.3E", "20F-1.5E", "24F-1.7E", "24F-1.8E", "26F-1.9E", "28F-2.1E", "30F-2.1E"), t.stressClasses.map { it.name })
        assertEquals(t.stressClasses.map { it.name }, t.expandedStressClasses.map { it.name })
        assertEquals(listOf(false, false, false, false, false, true, true), t.stressClasses.map { it.southernPineSuffix })
        assertEquals(56, t.combinations.size)
        // number of combinations printed under each stress class heading of the expanded table (counted by hand)
        val under = t.combinations.groupBy { it.printedUnder }.mapValues { it.value.size }
        assertEquals(mapOf("16F-1.3E" to 11, "20F-1.5E" to 18, "24F-1.7E" to 7, "24F-1.8E" to 9, "26F-1.9E" to 7, "28F-2.1E" to 2, "30F-2.1E" to 2), under)
        // the same symbol is printed for different species, so (symbol, outer, core) is the key
        assertEquals(56, t.combinations.map { Triple(it.symbol, it.outer, it.core) }.toSet().size)
        assertEquals(setOf("DF", "HF", "SP", "SPF", "AC", "POC", "ES"), t.combinations.flatMap { listOf(it.outer, it.core) }.toSet())
    }

    @Test
    fun `basic and expanded stress classes agree except the printed Ex min of 28F and 30F`() {
        for ((a, b) in t.stressClasses.zip(t.expandedStressClasses)) {
            if (a.name == "28F-2.1E" || a.name == "30F-2.1E") {
                assertEquals(a.name, 1.09, a.exMin, 0.0)
                assertEquals(a.name, 1.11, b.exMin, 0.0)
                assertEquals(a.name, a.copy(exMin = 1.11, markers = emptyList()), b.copy(markers = emptyList()))
            } else {
                assertEquals(a.name, a.copy(markers = emptyList()), b.copy(markers = emptyList()))
            }
        }
    }

    @Test
    fun `rows are internally consistent`() {
        for (s in t.stressClasses + t.expandedStressClasses) {
            val id = s.name
            assertTrue(id, s.fbxNeg <= s.fbxPos || s.fbxNeg <= 2400)
            assertTrue(id, s.ex <= s.exTrue && s.exMin < s.ex && s.ey <= s.eyTrue && s.eyMin < s.ey)
            assertTrue(id, s.exMin / s.ex in 0.51..0.54)
        }
        for (c in t.combinations) {
            val id = "${c.symbol} ${c.outer}/${c.core}"
            assertTrue(id, c.fbxNeg <= c.fbxPos)
            assertTrue(id, c.ex <= c.exTrue && c.exMin < c.ex && c.ey <= c.eyTrue && c.eyMin <= c.ey)
            assertTrue(id, c.ft > 0 && c.fc > 0 && c.fvx > 0 && c.fvy > 0)
            assertTrue(id, c.gTopBottomFace in 0.40..0.56 && c.gSideFace in 0.40..0.56)
            assertTrue(id, c.fcPerpXTensionFace >= c.fcPerpXCompressionFace || c.fcPerpXTensionFace <= 810)
        }
        // the printed irregularity: only 20F-E7 HF/HF differs from the usual 0.1 between Ex true and Ex
        val odd = t.combinations.filter { Math.round((it.exTrue - it.ex) * 100) != 10L || Math.round((it.eyTrue - it.ey) * 100) != 10L }
        assertEquals(listOf("20F-E7"), odd.map { it.symbol })
        assertEquals(listOf(1.8, 1.6), listOf(odd[0].exTrue, odd[0].ex))
    }

    @Test
    fun `stress classes read from the printed page`() {
        val s16 = t.stressClass("16F-1.3E")!!
        assertEquals(listOf(1600, 925, 315, 195), listOf(s16.fbxPos, s16.fbxNeg, s16.fcPerpX, s16.fvx))
        assertEquals(listOf(1.4, 1.3, 0.69), listOf(s16.exTrue, s16.ex, s16.exMin))
        assertEquals(listOf(800, 315, 170), listOf(s16.fby, s16.fcPerpY, s16.fvy))
        assertEquals(listOf(1.2, 1.1, 0.58), listOf(s16.eyTrue, s16.ey, s16.eyMin))
        assertEquals(listOf(675, 925), listOf(s16.ft, s16.fc))
        assertEquals(0.41, s16.g, 0.0)
        val s24 = t.stressClass("24F-1.8E")!!
        assertEquals(listOf(2400, 1450, 650, 265), listOf(s24.fbxPos, s24.fbxNeg, s24.fcPerpX, s24.fvx))
        assertEquals(listOf(1450, 560, 230), listOf(s24.fby, s24.fcPerpY, s24.fvy))
        assertEquals(listOf(1100, 1600), listOf(s24.ft, s24.fc))
        assertEquals(0.50, s24.g, 0.0)
        val s30 = t.stressClass("30F-2.1E")!!
        assertEquals(listOf(3000, 2400, 805, 300), listOf(s30.fbxPos, s30.fbxNeg, s30.fcPerpX, s30.fvx))
        assertEquals(listOf(2.2, 2.1, 1.09), listOf(s30.exTrue, s30.ex, s30.exMin))
        assertEquals(listOf(1750, 650, 260), listOf(s30.fby, s30.fcPerpY, s30.fvy))
        assertEquals(listOf(1250, 1750), listOf(s30.ft, s30.fc))
    }

    private fun combo(symbol: String, outer: String, core: String): Nds2024Table5A.Combination {
        val c = t.combination(symbol, outer, core)
        assertNotNull("$symbol $outer/$core", c)
        return c!!
    }

    @Test
    fun `combinations read from the printed pages`() {
        val a = combo("16F-V3", "DF", "DF")
        assertEquals(listOf(1600, 1250, 560, 560, 265), listOf(a.fbxPos, a.fbxNeg, a.fcPerpXTensionFace, a.fcPerpXCompressionFace, a.fvx))
        assertEquals(listOf(1.6, 1.5, 0.79), listOf(a.exTrue, a.ex, a.exMin))
        assertEquals(listOf(1450, 560, 230), listOf(a.fby, a.fcPerpY, a.fvy))
        assertEquals(listOf(1.6, 1.5, 0.79), listOf(a.eyTrue, a.ey, a.eyMin))
        assertEquals(listOf(975, 1500), listOf(a.ft, a.fc))
        assertEquals(listOf(0.50, 0.50), listOf(a.gTopBottomFace, a.gSideFace))
        val b = combo("16F-V3", "SP", "SP")
        assertEquals(listOf(1600, 1450, 740, 740, 300), listOf(b.fbxPos, b.fbxNeg, b.fcPerpXTensionFace, b.fcPerpXCompressionFace, b.fvx))
        val c = combo("20F-E8", "ES", "ES")
        assertEquals(listOf(2000, 1300, 450, 450, 200), listOf(c.fbxPos, c.fbxNeg, c.fcPerpXTensionFace, c.fcPerpXCompressionFace, c.fvx))
        assertEquals(listOf(825, 1100), listOf(c.ft, c.fc))
        assertEquals(0.41, c.gTopBottomFace, 0.0)
        val d = combo("24F-E/SPF3", "SPF", "SPF")
        assertEquals(listOf(2400, 1550, 560, 650, 215), listOf(d.fbxPos, d.fbxNeg, d.fcPerpXTensionFace, d.fcPerpXCompressionFace, d.fvx))
        assertEquals("20F-1.5E", d.printedUnder)
        val e = combo("24F-V4", "SP", "SP")
        assertEquals(listOf(2400, 1650, 740, 650, 210), listOf(e.fbxPos, e.fbxNeg, e.fcPerpXTensionFace, e.fcPerpXCompressionFace, e.fvx))
        assertEquals(listOf(0.55, 0.43), listOf(e.gTopBottomFace, e.gSideFace))
        assertEquals(listOf(Nds2024Table5A.Marker("label", listOf(4))), e.markers)
        val f = combo("24F-V5", "DF", "HF")
        assertEquals(listOf(0.50, 0.43), listOf(f.gTopBottomFace, f.gSideFace))
        val g = combo("30F-E2", "SP", "SP")
        assertEquals(listOf(3000, 3000, 805, 805, 300), listOf(g.fbxPos, g.fbxNeg, g.fcPerpXTensionFace, g.fcPerpXCompressionFace, g.fvx))
        assertEquals(listOf(2.2, 2.1, 1.11), listOf(g.exTrue, g.ex, g.exMin))
        assertEquals(listOf(1350, 1750), listOf(g.ft, g.fc))
        val h = combo("20F-E7", "HF", "HF")
        assertEquals(listOf(1.8, 1.6, 0.85), listOf(h.exTrue, h.ex, h.exMin))
    }

    @Test
    fun `footnote markers are kept where printed`() {
        assertEquals(listOf(Nds2024Table5A.Marker("fvx", listOf(6))), t.stressClass("20F-1.5E")!!.markers)
        assertEquals(listOf(Nds2024Table5A.Marker("fbxNeg", listOf(2)), Nds2024Table5A.Marker("fvx", listOf(3)), Nds2024Table5A.Marker("fvy", listOf(3)), Nds2024Table5A.Marker("g", listOf(10))).toSet(),
            t.stressClass("24F-1.8E")!!.markers.toSet())
        val m30 = t.stressClass("30F-2.1E")!!.markers
        assertTrue(m30.contains(Nds2024Table5A.Marker("label", listOf(7, 8))))
        assertEquals(setOf("exTrue", "ex", "exMin"), m30.filter { it.footnotes == listOf(9) }.map { it.column }.toSet())
        assertTrue(t.combination("28F-E1", "SP", "SP")!!.markers.all { it.footnotes == listOf(7) })
    }
}
