package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.*
import com.lz.vectos.domain.units.Length
import org.junit.Assert.*
import org.junit.Test

class SteelInteractionVerificationTest {

    private val strategy = SteelDesignStrategy()

    private val sampleSection = SectionProfile(
        id = "w14x90",
        designation = "W14X90",
        materialType = MaterialType.STEEL,
        shapeType = ShapeType.WIDE_FLANGE,
        area = 0.0171,
        depth = Length(0.3556),
        webThickness = 0.0112,
        webDepth = 0.311,
        torsionalConstantJ = 1.69e-6,
        warpingConstantCw = 1.91e-6,
        propertiesStrongAxis = SectionAxisProperties(i = 4.16e-4, s = 2.34e-3, z = 2.57e-3, r = 0.156),
        propertiesWeakAxis = SectionAxisProperties(i = 1.51e-4, s = 8.18e-4, z = 1.25e-3, r = 0.094)
    )

    private class TestBuildingCode(override val designMethodology: DesignMethodology) : BuildingCode() {
        override val name = "Test Code"
        override val referenceMap = CodeReferenceMap("2021", "7-16", "360-16", "2018")
        override val defaultLoadCases = emptyList<LoadCase>()
        override val defaultLoadCombinations = emptyList<LoadCombination>()
        override val phiMoment = 0.9
        override val phiShear = 1.0
        override val omegaMoment = 1.67
        override val omegaShear = 1.50
        override val serviceabilityCriteria = emptyList<ServiceabilityCriterion>()
    }

    @Test
    fun `test interaction logic - H1-1a calculation`() {
        val inputs = MaterialDesignInputs.Steel(fyPa = 345e6, unbracedLengthM = 1.0)
        val code = TestBuildingCode(DesignMethodology.LRFD)
        
        // 1. Resolve capacity
        val capacity = strategy.computeCapacity(sampleSection, code, SectionOrientation.STRONG_AXIS, inputs)
        assertNotNull(capacity.interactionResult)
        
        // 2. Perform design evaluation with mocked demands
        val momentDemand = 500000.0 // N-m
        val shearDemand = 200000.0 // N
        
        val envelope = LimitStateEnvelope(
            maxMoment = CombinationResult("1.2D+1.6L", com.lz.vectos.domain.units.Moment(momentDemand)),
            maxShear = CombinationResult("1.2D+1.6L", com.lz.vectos.domain.units.Force(shearDemand)),
            maxDeflection = com.lz.vectos.domain.units.Length(0.0)
        )
        
        val result = StrengthDesignService.evaluate(envelope, capacity, code.designMethodology)
        
        assertNotNull(result.interactionResult)
        val interaction = result.interactionResult!!
        
        val expectedRatio = (momentDemand / capacity.designMomentCapacity.newtonMeters) + (shearDemand / capacity.designShearCapacity.newtons)
        assertEquals(expectedRatio, interaction.interactionValue, 0.001)
        
        // Governing check should be interaction as it's the highest ratio
        assertEquals(GoverningMechanism.INTERACTION, result.governingMechanism)
    }

    @Test
    fun `test interaction safety - disabled for weak axis`() {
        val inputs = MaterialDesignInputs.Steel(fyPa = 345e6, unbracedLengthM = 1.0)
        val code = TestBuildingCode(DesignMethodology.LRFD)
        
        val capacity = strategy.computeCapacity(sampleSection, code, SectionOrientation.WEAK_AXIS, inputs)
        
        // Weak axis shear check is NotApplicable in current strategy
        assertNull(capacity.interactionResult)
    }
}
