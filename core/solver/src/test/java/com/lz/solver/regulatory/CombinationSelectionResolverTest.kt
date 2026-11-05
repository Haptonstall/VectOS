package com.lz.solver.regulatory

import com.lz.model.regulatory.CombinationSource
import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.LoadCombinationSet
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.structural.DesignMethodology
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CombinationSelectionResolverTest {

    private fun combo(name: String) = LoadCombination(
        id = "id_$name", name = name, methodology = DesignMethodology.LRFD, type = CombinationType.STRENGTH,
        equationText = name, factors = mapOf(LoadCategory.DEAD to 1.0), codeReference = "ref"
    )
    private fun set(id: String, vararg names: String) = LoadCombinationSet(
        id = id, source = CombinationSource("src", "Source", ""), methodology = DesignMethodology.LRFD,
        description = "", combinations = names.map(::combo)
    )

    private val cur = set("CUR", "A", "B", "C")
    private val old = set("OLD", "X", "Y")
    private fun lookup(id: String) = mapOf("CUR" to cur, "OLD" to old)[id]

    private fun resolve(setId: String?, enabled: Set<String>, analysis: String? = null, current: LoadCombinationSet? = cur) =
        CombinationSelectionResolver.resolve(setId, enabled, analysis, ::lookup, current)

    @Test
    fun `an intact selection resolves with no notices`() {
        val r = resolve("CUR", setOf("A", "B", "C"), "B")
        assertEquals("CUR", r.set!!.id); assertEquals(setOf("A", "B", "C"), r.enabledNames)
        assertEquals("B", r.analysisCombinationName); assertTrue(r.notices.isEmpty())
    }

    @Test
    fun `a saved set id that exists is used even when it is not the current set`() {
        val r = resolve("OLD", setOf("X"))
        assertEquals("OLD", r.set!!.id); assertEquals(setOf("X"), r.enabledNames)
    }

    @Test
    fun `nothing saved enables every combination of the set`() {
        val r = resolve(null, emptySet())
        assertEquals("CUR", r.set!!.id); assertEquals(setOf("A", "B", "C"), r.enabledNames); assertTrue(r.notices.isEmpty())
    }

    @Test
    fun `a saved set id that is gone falls back to the current set with a notice`() {
        val r = resolve("GONE", setOf("A"))
        assertEquals("CUR", r.set!!.id)
        assertTrue(r.notices.first(), r.notices.first().contains("\"GONE\"") && r.notices.first().contains("\"CUR\""))
    }

    @Test
    fun `stale names are dropped and listed, and never leave zero enabled`() {
        val partial = resolve("CUR", setOf("A", "Z1", "Z2"))
        assertEquals(setOf("A"), partial.enabledNames)
        assertTrue(partial.notices.any { it.contains("2 saved") && it.contains("Z1") && it.contains("Z2") })
        val none = resolve("CUR", setOf("Z1", "Z2"))
        assertEquals(setOf("A", "B", "C"), none.enabledNames)
        assertTrue(none.notices.any { it.contains("None of the saved combinations matched") })
    }

    @Test
    fun `combinations missing from the saved list are kept off but listed`() {
        val r = resolve("CUR", setOf("A", "B"))
        assertEquals(setOf("A", "B"), r.enabledNames)
        assertTrue(r.notices.single(), r.notices.single().contains("1 combination(s)") && r.notices.single().contains("C"))
    }

    @Test
    fun `a missing analysis combination falls back to the envelope with a notice`() {
        val r = resolve("CUR", setOf("A", "B", "C"), "Gone")
        assertNull(r.analysisCombinationName)
        assertTrue(r.notices.single().contains("\"Gone\""))
    }

    @Test
    fun `no set at all returns an empty selection with a notice`() {
        val r = resolve("GONE", setOf("A"), current = null)
        assertNull(r.set); assertTrue(r.enabledNames.isEmpty()); assertTrue(r.notices.isNotEmpty())
    }
}
