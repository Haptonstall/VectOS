package com.lz.vectos.domain.structural.analysis

import com.lz.vectos.domain.structural.*
import com.lz.vectos.domain.units.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID
import kotlin.math.abs
import kotlin.math.pow

class BeamAnalysisSolverVerificationTest {

    private val E_STEEL = 29e6
    private val I_VAL = 100.0
    private val G_STEEL = E_STEEL / (2 * (1 + 0.3))

    @Test
    fun `verify UDL mid-span deflection matches analytical 5wL^4 over 384EI`() {
        val spanId = UUID.randomUUID()
        val length = Length(120.0) // 10 ft
        val span = SpanGeometry(id = spanId, length = length, startSupport = SupportCondition.ROLLER, endSupport = SupportCondition.ROLLER)
        val member = StructuralMember(spans = listOf(span))
        
        val w = 100.0 // lb/in
        val loads = listOf(
            Load.UniformDistributedLoad(
                id = UUID.randomUUID(),
                spanId = spanId,
                value = ForcePerLength(w),
                locationStart = Length(0.0),
                locationEnd = length,
                direction = LoadDirection.VERTICAL_DOWN
            )
        )
        
        val config = BeamAnalysisConfig(ePsi = E_STEEL, iIn4 = I_VAL, avIn2 = null) // No shear deformation
        val result = BeamAnalysisSolver.solve(member, loads, config)
        
        val expectedDeflection = (5 * w * length.inches.pow(4)) / (384 * E_STEEL * I_VAL)
        
        // Find mid-span deflection in the diagram
        val midPoint = result.spanResults[0].deflectionDiagram.find { abs(it.x.inches - 60.0) < 0.1 }
        
        assertEquals("Mid-span deflection should match analytical formula", expectedDeflection, midPoint?.value ?: 0.0, 1e-4)
    }

    @Test
    fun `verify point load FEA matches PL over 8`() {
        val spanId = UUID.randomUUID()
        val length = Length(100.0)
        // Fixed-Fixed beam
        val span = SpanGeometry(id = spanId, length = length, startSupport = SupportCondition.FIXED, endSupport = SupportCondition.FIXED)
        val member = StructuralMember(spans = listOf(span))
        
        val p = 1000.0 // lbs
        val loads = listOf(
            Load.PointLoad(
                id = UUID.randomUUID(),
                spanId = spanId,
                value = Force(p),
                locationStart = Length(50.0),
                direction = LoadDirection.VERTICAL_DOWN
            )
        )
        
        val config = BeamAnalysisConfig(ePsi = E_STEEL, iIn4 = I_VAL)
        val result = BeamAnalysisSolver.solve(member, loads, config)
        
        val expectedMoment = (p * length.inches) / 8.0
        
        // Reactions[1] is RZ at node 0 (Start Moment)
        val startMoment = abs(result.reactions.find { it.nodeIndex == 0 }?.moment?.lbIn ?: 0.0)
        
        assertEquals("Fixed-end moment should be PL/8", expectedMoment, startMoment, 1e-3)
    }

    @Test
    fun `verify Timoshenko shear deformation increases deflection`() {
        val spanId = UUID.randomUUID()
        val length = Length(24.0) // Short deep beam
        val span = SpanGeometry(id = spanId, length = length, startSupport = SupportCondition.ROLLER, endSupport = SupportCondition.ROLLER)
        val member = StructuralMember(spans = listOf(span))
        
        val w = 1000.0 // lb/in
        val loads = listOf(
            Load.UniformDistributedLoad(
                id = UUID.randomUUID(),
                spanId = spanId,
                value = ForcePerLength(w),
                locationStart = Length(0.0),
                locationEnd = length,
                direction = LoadDirection.VERTICAL_DOWN
            )
        )
        
        // 1. Solve without shear deformation
        val configNoShear = BeamAnalysisConfig(ePsi = E_STEEL, iIn4 = I_VAL, avIn2 = null)
        val resNoShear = BeamAnalysisSolver.solve(member, loads, configNoShear)
        val deltaNoShear = resNoShear.maxDeflection.inches
        
        // 2. Solve with shear deformation (Small Av to amplify effect)
        val configShear = BeamAnalysisConfig(ePsi = E_STEEL, iIn4 = I_VAL, gPsi = G_STEEL, avIn2 = 2.0)
        val resShear = BeamAnalysisSolver.solve(member, loads, configShear)
        val deltaShear = resShear.maxDeflection.inches
        
        assertTrue("Shear deformation should increase total deflection", deltaShear > deltaNoShear)
        
        // Analytical check for Timoshenko UDL: delta_total = delta_bending + delta_shear
        // delta_shear for UDL = wL^2 / 8GAv
        val expectedDeltaShear = (w * length.inches.pow(2)) / (8 * G_STEEL * 2.0)
        val expectedTotal = deltaNoShear + expectedDeltaShear
        
        assertEquals("Timoshenko deflection should match bending + shear components", expectedTotal, deltaShear, 1e-4)
    }
}
