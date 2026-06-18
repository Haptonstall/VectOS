package com.lz.solver.analysis

import com.lz.model.regulatory.LoadCombination
import com.lz.model.structural.LoadCase
import com.lz.model.structural.StructuralMember
import com.lz.model.units.Area
import com.lz.model.units.MomentOfInertia
import com.lz.model.units.Pressure
import com.lz.model.units.TorsionalConstant
import com.lz.model.units.ElasticModulus
import com.lz.model.structural.NormalizedBraceState



/**
 * Public API for generic structural member load envelope resolution.
 * Assembles an [AnalysisConfig] and delegates to [MemberAnalysisSolver].
 *
 * Callers that need beam-specific output (utilization diagrams, capacity
 * checks) should use BeamAnalysisSolver in feature/beam instead.
 */
object LoadResolutionService {

    /**
     * Evaluates all load combinations and returns the governing analysis envelope.
     *
     * @param member              The structural member geometry and support conditions.
     * @param loadCases           Applied load cases grouped by [LoadCategory].
     * @param combinations        Load combinations to evaluate (LRFD or ASD).
     * @param modulusOfElasticity Elastic modulus E (e.g. 29,000 ksi for steel).
     * @param momentOfInertiaX    Strong-axis moment of inertia Ix.
     * @param momentOfInertiaY    Weak-axis moment of inertia Iy. Null if not required.
     * @param crossSectionalArea  Cross-sectional area A. Null if shear deformation ignored.
     * @param shearModulus        Shear modulus G. Required for torsion; null otherwise.
     * @param torsionalConstant   Torsional constant J. Required for torsion; null otherwise.
     * @param braceState          Normalized lateral brace points along the member.
     */
    fun resolveEnvelope(
        member: StructuralMember,
        loadCases: List<LoadCase>,
        combinations: List<LoadCombination>,
        modulusOfElasticity: ElasticModulus,
        momentOfInertiaX: MomentOfInertia,
        momentOfInertiaY: MomentOfInertia,
        crossSectionalArea: Area? = null,
        shearModulus: Pressure? = null,
        torsionalConstant: TorsionalConstant? = null,
        braceState: List<NormalizedBraceState> = emptyList()
    ): AnalysisResult {
        val config = AnalysisConfig(
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

        return MemberAnalysisSolver.solve(config)
    }
}