package com.lz.solver.material

import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodSpecies
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every glulam combination symbol [WoodGrade.validGradesFor] offers for a
 * species must resolve to real data — otherwise the picker shows it as a
 * selectable option, the user picks it, and Confirm silently stays
 * disabled ("Reference design values for this combination aren't
 * available yet") with no indication of which combination is at fault.
 * That's exactly what happened with GLULAM_DF_DF's 24F-V8 (and 10 other
 * DF/DF symbols) before this test existed — validGradesFor listed 19
 * combinations, WoodPropertyService only had 8 of them.
 */
class WoodPropertyServiceGlulamCoverageTest {

    @Test
    fun `every offered glulam combination symbol has data`() {
        val missing = mutableListOf<String>()
        WoodSpecies.entries.filter { it.isGlulam }.forEach { species ->
            WoodGrade.validGradesFor(species).forEach { grade ->
                val props = WoodPropertyService.getReferenceProperties(species, grade)
                if (props == null) missing += "$species / $grade"
            }
        }
        assertTrue(
            "These combinations are offered by validGradesFor() but have no " +
                "WoodPropertyService data (Confirm would silently stay disabled " +
                "for them): $missing",
            missing.isEmpty()
        )
    }
}
