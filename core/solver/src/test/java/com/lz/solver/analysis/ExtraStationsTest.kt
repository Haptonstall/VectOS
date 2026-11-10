package com.lz.solver.analysis

import com.lz.model.structural.Load
import com.lz.model.structural.LoadCase
import com.lz.model.structural.NodeBoundaryCondition
import com.lz.model.structural.SpanGeometry
import com.lz.model.structural.StructuralMember
import com.lz.model.structural.StructuralNode
import com.lz.model.units.Area
import com.lz.model.units.ElasticModulus
import com.lz.model.units.ForcePerLength
import com.lz.model.units.MomentOfInertia
import com.lz.model.units.inches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

/**
 * An extra station (the user's Design Location) is evaluated exactly, not at the nearest grid
 * station. Simple span L = 100 in, UDL w = 10 lb/in, hand calcs at x = 37.3 in:
 *   |M| = w x (L - x) / 2 = 10 * 37.3 * 62.7 / 2 = 11693.55 lb-in
 *   |V| = w (L/2 - x)     = 10 * (50 - 37.3)      = 127 lb
 * The default grid has stations every 1.0 in, so 37.3 is not on it.
 */
class ExtraStationsTest {

    private fun solve(extra: List<Double>): AnalysisResult {
        val nodes = listOf(NodeBoundaryCondition.pinned(), NodeBoundaryCondition.roller())
            .map { StructuralNode(id = UUID.randomUUID(), boundaryCondition = it) }
        val span = SpanGeometry(id = UUID.randomUUID(), length = 100.0.inches, startNodeId = nodes[0].id, endNodeId = nodes[1].id)
        val member = StructuralMember(nodes = nodes, spans = listOf(span))
        return MemberAnalysisSolver.solve(
            AnalysisConfig(
                member = member,
                loadCases = listOf(LoadCase("DL", "Dead", listOf<Load>(
                    Load.UniformDistributedLoad(ForcePerLength(10.0), span.id, 0.0.inches, 100.0.inches)))),
                modulusOfElasticity = ElasticModulus(1_000_000.0),
                momentOfInertiaX = MomentOfInertia(100.0),
                crossSectionalArea = Area(10.0),
                extraStations = extra.map { it.inches }
            )
        )
    }

    private fun station(r: AnalysisResult, x: Double) =
        r.spanResults.flatMap { it.stationDemands }.firstOrNull { Math.abs(it.x.inches - x) < 1e-9 }

    @Test
    fun `a typed location becomes an exact station with the closed-form moment and shear`() {
        val d = station(solve(listOf(37.3)), 37.3)
        assertNotNull(d)
        assertEquals(11693.55, Math.abs(d!!.moment.lbIn), 1e-6)
        assertEquals(127.0, Math.abs(d.shear.pounds), 1e-6)
    }

    @Test
    fun `without an extra station the location is not on the grid`() {
        assertNull(station(solve(emptyList()), 37.3))
    }

    @Test
    fun `locations outside the member are ignored`() {
        val r = solve(listOf(-5.0, 150.0))
        assertNull(station(r, -5.0)); assertNull(station(r, 150.0))
        assertNotNull(station(r, 50.0))
    }
}
