package com.lz.solver.regulatory

import com.lz.model.regulatory.LoadCombination
import com.lz.model.regulatory.codes.BuildingCode
import com.lz.model.structural.DesignMethodology

object LoadCombinationEngine {

    fun getCombinations(code: BuildingCode, methodology: DesignMethodology): List<LoadCombination> {
        val defaultSetId = when (methodology) {
            DesignMethodology.ASD -> code.defaultAsdSetId
            DesignMethodology.LRFD -> code.defaultLrfdSetId
        }

        val defaultCombinations = defaultSetId
            ?.let { code.getCombinationSet(it) }
            ?.combinations
            .orEmpty()
            .filter { it.methodology == methodology }

        if (defaultCombinations.isNotEmpty()) return defaultCombinations

        return code.stateSpecificCombinations
            .filter { it.methodology == methodology }
            .flatMap { it.combinations }
            .filter { it.methodology == methodology }
    }
}
