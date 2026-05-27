package com.lz.vectos.domain.structural.analysis

import com.lz.vectos.domain.structural.LoadCase
import com.lz.vectos.domain.structural.StructuralMember

/**
 * Configuration for a beam analysis run.
 */
data class BeamAnalysisConfig(
    val member: StructuralMember,
    val loadCases: List<LoadCase>,
    val modulusOfElasticityPsi: Double,
    val momentOfInertiaIn4: Double,
    val momentOfInertiaYIn4: Double? = null,
    val areaIn2: Double? = null,
    val shearModulusPsi: Double? = null,
    val torsionalConstantIn4: Double? = null
)
