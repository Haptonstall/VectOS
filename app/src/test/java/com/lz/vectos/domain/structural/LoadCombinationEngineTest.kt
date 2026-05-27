package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class LoadCombinationEngineTest {

    @Test
    fun `governing combo selection - LRFD`() {
        val spanId = UUID.randomUUID()
        val member = StructuralMember.createSimple(Length(5.0))
        val loads = listOf(
            Load.PointLoad(value = 1000.0, spanId = spanId, category = LoadCategory.DEAD),
            Load.PointLoad(value = 2000.0, spanId = spanId, category = LoadCategory.LIVE)
        )
        
        val combinations = listOf(
            LoadCombination("1.4D", DesignMethodology.LRFD, "1.4D", mapOf(LoadCategory.DEAD to 1.4), "Ref1"),
            LoadCombination("1.2D + 1.6L", DesignMethodology.LRFD, "1.2D + 1.6L", mapOf(LoadCategory.DEAD to 1.2, LoadCategory.LIVE to 1.6), "Ref2")
        )

        val result = LoadCombinationEngine.resolveGoverningDemands(member, loads, combinations)
        
        // 1.4 * 1000 = 1400
        // 1.2 * 1000 + 1.6 * 2000 = 1200 + 3200 = 4400 (Governing)
        assertEquals("1.2D + 1.6L", result.governingCombination.equation)
    }
}
