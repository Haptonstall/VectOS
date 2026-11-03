package com.lz.solver.analysis

import com.lz.model.structural.*
import com.lz.model.units.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID
import kotlin.math.abs

/**
 * Explicit shear deflection (NDS 3.5.1, App. F.3.2) added to the Euler-Bernoulli FEM.
 *
 * Expected values are closed-form hand results, not output of the code under test:
 *   simple span, uniform w:       5wL^4/(384EI)  +  wL^2/(8S)
 *   simple span, midspan point P: PL^3/(48EI)    +  PL/(4S)
 *   cantilever,  tip point P:     PL^3/(3EI)     +  PL/S
 *   cantilever,  uniform w:       wL^4/(8EI)     +  wL^2/(2S)
 *   fixed-fixed, uniform w:       wL^4/(384EI)   +  wL^2/(8S)   (shear part by V = w(L/2 - x))
 *   overhang tip load P at a:     shear part P*a/S, back span (moment linear between supports) none
 * with S = G * Av.
 *
 * Common data: E = 1,000,000 psi, I = 100 in^4, G = 100,000 psi, Av = 10 in^2 (S = 1,000,000 lb), L = 100 in.
 */
class ShearDeflectionTest {

    private val e = 1_000_000.0
    private val inertia = 100.0
    private val s = 1_000_000.0
    private val shear = ShearDeflectionInput(Pressure(100_000.0), Area(10.0))

    private fun member(lengths: List<Double>, bcs: List<NodeBoundaryCondition>): StructuralMember {
        val ids = bcs.map { UUID.randomUUID() }
        val spans = lengths.mapIndexed { i, l -> SpanGeometry(length = l.inches, startNodeId = ids[i], endNodeId = ids[i + 1]) }
        return StructuralMember(nodes = ids.mapIndexed { i, id -> StructuralNode(id, bcs[i]) }, spans = spans)
    }

    private fun solve(m: StructuralMember, loads: List<Load>, withShear: Boolean): AnalysisResult =
        MemberAnalysisSolver.solve(AnalysisConfig(
            member = m,
            loadCases = listOf(LoadCase("DL", "Dead", loads)),
            modulusOfElasticity = ElasticModulus(e),
            momentOfInertiaX = MomentOfInertia(inertia),
            crossSectionalArea = Area(10.0),
            shearDeflection = if (withShear) shear else null
        ))

    /** Deflection (downward positive) at global x, from the span diagrams. */
    private fun defl(r: AnalysisResult, x: Double): Double {
        r.spanResults.forEach { sr ->
            sr.deflectionDiagram.forEach { p -> if (abs(p.x.inches - x) < 1e-6) return -p.value }   // diagram x is global
        }
        throw AssertionError("no station at x=$x")
    }

    private val pin = NodeBoundaryCondition.pinned()
    private val roller = NodeBoundaryCondition.roller()
    private val free = NodeBoundaryCondition()

    private fun udl(span: SpanGeometry, w: Double, from: Double = 0.0, to: Double = span.length.inches) =
        Load.UniformDistributedLoad(value = ForcePerLength(w), spanId = span.id, locationStart = from.inches, locationEnd = to.inches)

    @Test
    fun `simple span uniform load adds wL2 over 8S at midspan`() {
        val m = member(listOf(100.0), listOf(pin, roller))
        val loads = listOf<Load>(udl(m.spans[0], 10.0))
        assertEquals(5 * 10.0 * 1e8 / (384 * e * inertia), defl(solve(m, loads, false), 50.0), 1e-9)   // 0.130208333
        assertEquals(0.130208333333 + 0.0125, defl(solve(m, loads, true), 50.0), 1e-9)
    }

    @Test
    fun `simple span midspan point load adds PL over 4S`() {
        val m = member(listOf(100.0), listOf(pin, roller))
        val loads = listOf<Load>(Load.PointLoad(value = Force(1000.0), spanId = m.spans[0].id, locationStart = 50.0.inches))
        assertEquals(1000.0 * 1e6 / (48 * e * inertia) + 1000.0 * 100.0 / (4 * s), defl(solve(m, loads, true), 50.0), 1e-9)  // 0.208333 + 0.025
    }

    @Test
    fun `cantilever tip load adds PL over S at the tip`() {
        val m = member(listOf(100.0), listOf(NodeBoundaryCondition.fixed(), free))
        val loads = listOf<Load>(Load.PointLoad(value = Force(1000.0), spanId = m.spans[0].id, locationStart = 100.0.inches))
        assertEquals(1000.0 * 1e6 / (3 * e * inertia) + 1000.0 * 100.0 / s, defl(solve(m, loads, true), 100.0), 1e-9)  // 3.33333 + 0.1
    }

    @Test
    fun `cantilever uniform load adds wL2 over 2S at the tip`() {
        val m = member(listOf(100.0), listOf(NodeBoundaryCondition.fixed(), free))
        val loads = listOf<Load>(udl(m.spans[0], 10.0))
        assertEquals(10.0 * 1e8 / (8 * e * inertia) + 10.0 * 1e4 / (2 * s), defl(solve(m, loads, true), 100.0), 1e-9)   // 1.25 + 0.05
    }

    @Test
    fun `fixed-fixed uniform load adds wL2 over 8S at midspan`() {
        val m = member(listOf(100.0), listOf(NodeBoundaryCondition.fixed(), NodeBoundaryCondition.fixed()))
        val loads = listOf<Load>(udl(m.spans[0], 10.0))
        assertEquals(10.0 * 1e8 / (384 * e * inertia) + 0.0125, defl(solve(m, loads, true), 50.0), 1e-9)
    }

    @Test
    fun `overhang tip load adds P a over S at the tip and nothing in the back span`() {
        val m = member(listOf(100.0, 40.0), listOf(pin, roller, free))
        val loads = listOf<Load>(Load.PointLoad(value = Force(500.0), spanId = m.spans[1].id, locationStart = 40.0.inches))
        val with = solve(m, loads, true); val without = solve(m, loads, false)
        assertEquals(500.0 * 40.0 / s, defl(with, 140.0) - defl(without, 140.0), 1e-9)       // 0.02
        // back span: moment is linear between the supports, so shear adds no deflection there
        listOf(14.0, 42.0, 70.0, 98.0).forEach { x -> assertEquals(0.0, defl(with, x) - defl(without, x), 1e-9) }   // grid stations (1.4 in)
    }

    @Test
    fun `a point moment adds no shear deflection on a simple span`() {
        val m = member(listOf(100.0), listOf(pin, roller))
        val loads = listOf<Load>(Load.PointMoment(value = Moment(20_000.0), spanId = m.spans[0].id, locationStart = 50.0.inches))
        val with = solve(m, loads, true); val without = solve(m, loads, false)
        listOf(10.0, 40.0, 49.0, 51.0, 60.0, 90.0).forEach { x -> assertEquals(0.0, defl(with, x) - defl(without, x), 1e-9) }
    }

    @Test
    fun `shear input absent leaves the deflection exactly as before`() {
        val m = member(listOf(100.0), listOf(pin, roller))
        val loads = listOf<Load>(udl(m.spans[0], 10.0))
        assertEquals(solve(m, loads, false).maxDeflection.inInches, 5 * 10.0 * 1e8 / (384 * e * inertia), 1e-9)
    }

    @Test
    fun `shear deflection increases the downward deflection and scales with 1 over S`() {
        val m = member(listOf(100.0), listOf(pin, roller))
        val loads = listOf<Load>(udl(m.spans[0], 10.0))
        val soft = MemberAnalysisSolver.solve(AnalysisConfig(
            member = m, loadCases = listOf(LoadCase("DL", "Dead", loads)), modulusOfElasticity = ElasticModulus(e),
            momentOfInertiaX = MomentOfInertia(inertia), crossSectionalArea = Area(10.0),
            shearDeflection = ShearDeflectionInput(Pressure(50_000.0), Area(10.0))))          // S halved
        assertEquals(0.130208333333 + 0.025, defl(soft, 50.0), 1e-9)
        assertTrue(defl(soft, 50.0) > defl(solve(m, loads, true), 50.0))
    }
}
