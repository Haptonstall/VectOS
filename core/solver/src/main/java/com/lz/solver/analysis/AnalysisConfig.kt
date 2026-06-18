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
    val braceState: List<NormalizedBraceState> = emptyList()
) {
    val allLoads: List<Load> get() = loadCases.flatMap { it.loads }
}