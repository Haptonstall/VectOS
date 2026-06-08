package com.lz.vectos.domain.structural

import com.lz.model.structural.StructuralMember
import com.lz.vectos.domain.structural.analysis.BeamAnalysisConfig
import com.lz.vectos.domain.structural.analysis.BeamAnalysisResult
import com.lz.vectos.domain.structural.analysis.BeamAnalysisSolver
import com.lz.model.units.*

/**
 * Service to resolve governing load effects using matrix analysis.
 * Now a thin wrapper around BeamAnalysisSolver.
 */
object LoadResolutionService {

    /**
     * Evaluates all combinations and determines the governing analysis envelope.
     */
    fun resolveEnvelope(
        member: StructuralMember,
        loadCases: List<LoadCase>,
        combinations: List<LoadCombination>,
        modulusOfElasticityPsi: Double,
        momentOfInertiaIn4: Double,
        momentOfInertiaYIn4: Double? = null,
        areaIn2: Double? = null,
        shearModulusPsi: Double? = null,
        torsionalConstantIn4: Double? = null,
        braceState: List<NormalizedBraceState> = emptyList()
    ): BeamAnalysisResult {
        val config = BeamAnalysisConfig(
            member = member,
            loadCases = loadCases,
            combinations = combinations,
            modulusOfElasticityPsi = modulusOfElasticityPsi,
            momentOfInertiaIn4 = momentOfInertiaIn4,
            momentOfInertiaYIn4 = momentOfInertiaYIn4,
            areaIn2 = areaIn2,
            shearModulusPsi = shearModulusPsi,
            torsionalConstantIn4 = torsionalConstantIn4,
            braceState = braceState
        )
        
        return BeamAnalysisSolver.solve(config)
    }
}
