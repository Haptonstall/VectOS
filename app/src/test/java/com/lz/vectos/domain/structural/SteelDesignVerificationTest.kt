package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.*
import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.UnitSystem
import org.junit.Assert.*
import org.junit.Test

class SteelDesignVerificationTest {

    private val strategy = SteelDesignStrategy()
    private val code = BuildingCode.IBC_2021 // LRFD by default

    private val sampleSection = SectionProfile(
        id = "w14x90",
        designation = "W14X90",
        materialType = MaterialType.STEEL,
        shapeType = ShapeType.WIDE_FLANGE,
        area = 0.0171,
        depth = Length(0.3556),
        torsionalConstantJ = 1.69e-6,
        warpingConstantCw = 1.91e-6,
        propertiesStrongAxis = SectionAxisProperties(i = 4.16e-4, s = 2.34e-3, z = 2.57e-3, r = 0.156),
        propertiesWeakAxis = SectionAxisProperties(i = 1.51e-4, s = 8.18e-4, z = 1.25e-3, r = 0.094)
    )

    @Test
    fun `test methodology parity - LRFD vs ASD`() {
        val inputs = MaterialDesignInputs.Steel(fyPa = 345e6, unbracedLengthM = 2.0)
        
        // LRFD
        val lrfdCode = BuildingCode.IBC_2021 
        val lrfdResult = strategy.computeCapacity(sampleSection, lrfdCode, SectionOrientation.STRONG_AXIS, inputs)
        
        // ASD (Need to mock or change methodology)
        // Since IBC_2021 has designMethodology = LRFD, let's create a simple wrapper or use a code that returns ASD
        val asdCode = object : BuildingCode() {
            override val name = "ASD Mock"
            override val referenceMap = lrfdCode.referenceMap
            override val defaultLoadCases = lrfdCode.defaultLoadCases
            override val defaultLoadCombinations = lrfdCode.defaultLoadCombinations
            override val designMethodology = DesignMethodology.ASD
            override val phiMoment = 0.9; override val phiShear = 0.9
            override val omegaMoment = 1.67; override val omegaShear = 1.67
            override val serviceabilityCriteria = lrfdCode.serviceabilityCriteria
        }
        
        val asdResult = strategy.computeCapacity(sampleSection, asdCode, SectionOrientation.STRONG_AXIS, inputs)
        
        assertEquals(lrfdResult.governingMode, asdResult.governingMode)
        assertTrue(lrfdResult.designMomentCapacity.newtonMeters > asdResult.designMomentCapacity.newtonMeters)
        
        // Trace check
        assertTrue(lrfdResult.traces.any { it.equation.contains("phi") })
        assertTrue(asdResult.traces.any { it.equation.contains("Omega") })
    }

    @Test
    fun `test orientation sensitivity - Strong vs Weak`() {
        val inputs = MaterialDesignInputs.Steel(fyPa = 345e6, unbracedLengthM = 2.0)
        
        val strongResult = strategy.computeCapacity(sampleSection, code, SectionOrientation.STRONG_AXIS, inputs)
        val weakResult = strategy.computeCapacity(sampleSection, code, SectionOrientation.WEAK_AXIS, inputs)
        
        assertNotEquals(strongResult.governingMode, weakResult.governingMode)
        assertEquals("Weak Axis Yielding", weakResult.governingMode)
        assertTrue(strongResult.nominalMomentCapacity.newtonMeters > weakResult.nominalMomentCapacity.newtonMeters)
    }

    @Test
    fun `test LTB transitions - Yielding to Inelastic to Elastic`() {
        val fy = 345e6
        // Lp ~ 1.76 * ry * sqrt(E/Fy) = 1.76 * 0.094 * sqrt(200e9/345e6) ~= 4.0 m
        // Wait, ry=0.094, sqrt(200e9/345e6) ~= 24. 1.76 * 0.094 * 24 ~= 3.97 m
        
        val plasticInputs = MaterialDesignInputs.Steel(fyPa = fy, unbracedLengthM = 1.0)
        val inelasticInputs = MaterialDesignInputs.Steel(fyPa = fy, unbracedLengthM = 6.0)
        val elasticInputs = MaterialDesignInputs.Steel(fyPa = fy, unbracedLengthM = 15.0)
        
        val plasticRes = strategy.computeCapacity(sampleSection, code, SectionOrientation.STRONG_AXIS, plasticInputs)
        val inelasticRes = strategy.computeCapacity(sampleSection, code, SectionOrientation.STRONG_AXIS, inelasticInputs)
        val elasticRes = strategy.computeCapacity(sampleSection, code, SectionOrientation.STRONG_AXIS, elasticInputs)
        
        assertEquals("Yielding (Plastic Moment)", plasticRes.governingMode)
        assertEquals("Lateral-Torsional Buckling (LTB)", inelasticRes.governingMode)
        assertEquals("Lateral-Torsional Buckling (LTB)", elasticRes.governingMode)
        
        assertTrue(plasticRes.nominalMomentCapacity.newtonMeters > inelasticRes.nominalMomentCapacity.newtonMeters)
        assertTrue(inelasticRes.nominalMomentCapacity.newtonMeters > elasticRes.nominalMomentCapacity.newtonMeters)
        
        // Verify clause in trace for LTB
        assertTrue(inelasticRes.evaluationSummary["Lateral-Torsional Buckling"].toString().contains("Inelastic"))
        assertTrue(elasticRes.evaluationSummary["Lateral-Torsional Buckling"].toString().contains("Elastic"))
    }

    @Test
    fun `test safety guard - missing unbraced length`() {
        val inputs = MaterialDesignInputs.Steel(fyPa = 345e6, unbracedLengthM = null)
        val result = strategy.computeCapacity(sampleSection, code, SectionOrientation.STRONG_AXIS, inputs)
        
        val ltbStatus = result.evaluationSummary["Lateral-Torsional Buckling"]
        assertTrue(ltbStatus is CapacityEvaluationStatus.NotEvaluated)
        assertEquals("Unbraced length (Lb) required for stability check", (ltbStatus as CapacityEvaluationStatus.NotEvaluated).reason)
    }
}
