package com.lz.beam.solver

import com.lz.model.regulatory.LoadCombination
import com.lz.model.structural.DesignMethodology
import com.lz.model.structural.Load
import com.lz.model.structural.LoadCase
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.NormalizedBraceState
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.StructuralMember
import com.lz.model.units.Area
import com.lz.model.units.ElasticModulus
import com.lz.model.units.MomentOfInertia
import com.lz.model.units.Pressure
import com.lz.model.units.TorsionalConstant
import com.lz.solver.analysis.AnalysisConfig

/**
 * Configuration for a beam analysis run.
 */
data class BeamAnalysisConfig(
    val member: StructuralMember,
    val loadCases: List<LoadCase>,
    val combinations: List<LoadCombination> = emptyList(),
    val modulusOfElasticity: ElasticModulus,
    val momentOfInertiaX: MomentOfInertia,
    val momentOfInertiaY: MomentOfInertia? = null,
    val crossSectionalArea: Area? = null,
    val shearModulus: Pressure? = null,
    val torsionalConstant: TorsionalConstant? = null,
    val braceState: List<NormalizedBraceState> = emptyList(),

    // Beam-specific additions
    val designMethodology: DesignMethodology = DesignMethodology.ASD,
    val sectionProfile: SectionProfile? = null,
    val material: MaterialGrade? = null,
    val liveLoadDeflectionLimitRatio: Double = 360.0,
    val totalLoadDeflectionLimitRatio: Double = 240.0
) {
    /**
     * All loads across all load cases.
     */
    val allLoads: List<Load> get() = loadCases.flatMap { it.loads }

    // Converts to a generic AnalysisConfig for MemberAnalysisSolver
    fun  toAnalysisConfig() = AnalysisConfig(
        member = member,
        loadCases = loadCases,
        combinations = combinations,
        modulusOfElasticity = modulusOfElasticity,
        momentOfInertiaX = momentOfInertiaX,
        momentOfInertiaY = momentOfInertiaY,
        crossSectionalArea = crossSectionalArea,
        shearModulus = shearModulus,
        torsionalConstant = torsionalConstant,
        braceState = braceState
    )
}