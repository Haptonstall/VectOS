package com.lz.vectos.domain.structural.analysis

import com.lz.vectos.domain.structural.LoadCase
import com.lz.vectos.domain.structural.LoadCombination
import com.lz.vectos.domain.structural.Load
import com.lz.model.structural.StructuralMember
import com.lz.vectos.domain.structural.NormalizedBraceState
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.SectionProfile

import com.lz.model.structural.MaterialGrade

/**
 * Configuration for a beam analysis run.
 */
data class BeamAnalysisConfig(
    val member: StructuralMember,
    val loadCases: List<LoadCase>,
    val combinations: List<LoadCombination> = emptyList(),
    val modulusOfElasticityPsi: Double,
    val momentOfInertiaIn4: Double,
    val momentOfInertiaYIn4: Double? = null,
    val areaIn2: Double? = null,
    val shearModulusPsi: Double? = null,
    val torsionalConstantIn4: Double? = null,
    val braceState: List<NormalizedBraceState> = emptyList(),
    val designMethodology: DesignMethodology = DesignMethodology.LRFD,
    val sectionProfile: SectionProfile? = null,
    val material: MaterialGrade? = null,
    val liveLoadDeflectionLimitRatio: Double = 360.0,
    val totalLoadDeflectionLimitRatio: Double = 240.0
) {
    /**
     * All loads across all load cases.
     */
    val allLoads: List<Load> get() = loadCases.flatMap { it.loads }
}
