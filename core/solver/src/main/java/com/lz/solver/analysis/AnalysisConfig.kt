package com.lz.solver.analysis

import com.lz.model.regulatory.LoadCombination
import com.lz.model.structural.StructuralMember
import com.lz.model.structural.LoadCase
import com.lz.model.structural.Load

import com.lz.model.units.Area
import com.lz.model.units.MomentOfInertia
import com.lz.model.units.Pressure
import com.lz.model.units.TorsionalConstant
import com.lz.model.structural.NormalizedBraceState
import com.lz.model.units.ElasticModulus

/**
 * Shear stiffness for the explicit shear-deflection term: gamma = V / (G * Av), where
 * [effectiveShearArea] is kappa * A (kappa = 5/6 for a solid rectangle).
 */
data class ShearDeflectionInput(
    val shearModulus: Pressure,
    val effectiveShearArea: Area
)

/**
 * Generic configuration for a structural member analysis run.
 * Contains only inputs required by the FEM solver — no design or
 * material-specific parameters.
 */
data class AnalysisConfig(
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
    /**
     * Explicit shear deflection (NDS 3.5.1, App. F.3.2). Null (default) adds nothing:
     * the supplied [modulusOfElasticity] is then taken to be an apparent E that already
     * includes shear (NDS F.3.1, sawn lumber and glulam tables). Set it only together with
     * a true (shear-free) E, otherwise shear is counted twice.
     */
    val shearDeflection: ShearDeflectionInput? = null
) {
    val allLoads: List<Load> get() = loadCases.flatMap { it.loads }
}