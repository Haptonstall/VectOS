package com.lz.solver.analysis

import com.lz.model.regulatory.CombinationSource
import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.LoadCombinationSet
import com.lz.model.regulatory.loads.CombinationType
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.structural.*
import com.lz.model.units.*
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class LimitStateServiceTest {

    @Test
    fun `evaluate returns non-zero locations for peaks`() {
        val member = StructuralMember.createSimple(Length(120.0)) // 10 ft
        val spanId = member.spans.first().id
        
        // Point load at 40 inches
        val pointLoad = Load.PointLoad(
            id = UUID.randomUUID(),
            value = Force(1000.0),
            spanId = spanId,
            locationStart = Length(40.0),
            category = LoadCategory.DEAD
        )
        
        val loadCases = listOf(
            LoadCase("D", "Dead", listOf(pointLoad))
        )
        
        val combo = LoadCombination(
            id = "test_combo",
            name = "1.0D",
            methodology = DesignMethodology.LRFD,
            type = CombinationType.STRENGTH,
            equationText = "1.0D",
            factors = mapOf(LoadCategory.DEAD to 1.0),
            codeReference = "Test"
        )
        
        val comboSet = LoadCombinationSet(
            id = "test_set",
            source = CombinationSource("test", "Test", "Test"),
            methodology = DesignMethodology.LRFD,
            description = "Test Set",
            combinations = listOf(combo)
        )
        
        val buildingCode = BuildingCode(
            id = "test_code",
            shortName = "Test",
            longName = "Test Code",
            defaultLrfdSetId = "test_set",
            stateSpecificCombinations = listOf(comboSet),
            defaultMaterialStandards = emptyMap(),
            standards = emptyList()
        )
        
        val results = LimitStateService.evaluate(
            member = member,
            loadCases = loadCases,
            buildingCode = buildingCode,
            methodology = DesignMethodology.LRFD,
            e = 29000.0,
            ix = 100.0,
            iy = 10.0
        )
        
        assertTrue("Should have strength results", results.containsKey(LimitState.STRENGTH))
        val strength = results[LimitState.STRENGTH]!!

        // For a simple span with a point load at 40", max moment is AT 40"
        assertEquals("Governing combination name should match", "1.0D", strength.maxMoment.combinationName)
        assertEquals("Moment peak location should be approx 40 inches", 40.0, strength.maxMoment.location, 0.5)
        
        // Shear peak is at supports (0 or 120)
        assertTrue("Shear peak location should be at supports", strength.maxShear.location == 0.0 || strength.maxShear.location == 120.0)
    }
}