package com.lz.vectos.domain.structural

/**
 * Single source of truth for code-mandated load combinations.
 */
object LoadCombinationRepository {

    fun getCombinations(
        methodology: DesignMethodology,
        asceEdition: AsceEdition
    ): List<LoadCombination> {
        return when (methodology) {
            DesignMethodology.LRFD -> getLrfdCombinations(asceEdition)
            DesignMethodology.ASD -> getAsdCombinations(asceEdition)
        }
    }

    private fun getLrfdCombinations(asceEdition: AsceEdition): List<LoadCombination> {
        val ref = asceEdition.label
        return listOf(
            LoadCombination(
                name = "LRFD-1",
                methodology = DesignMethodology.LRFD,
                equation = "1.4D",
                factors = mapOf(LoadCategory.DEAD to 1.4),
                codeReference = "$ref 2.3.1-1"
            ),
            LoadCombination(
                name = "LRFD-2",
                methodology = DesignMethodology.LRFD,
                equation = "1.2D + 1.6L + 0.5(Lr or S)",
                factors = mapOf(LoadCategory.DEAD to 1.2, LoadCategory.LIVE to 1.6, LoadCategory.SNOW to 0.5),
                codeReference = "$ref 2.3.1-2"
            ),
            LoadCombination(
                name = "LRFD-3",
                methodology = DesignMethodology.LRFD,
                equation = "1.2D + 1.6(Lr or S) + (L or 0.5W)",
                factors = mapOf(LoadCategory.DEAD to 1.2, LoadCategory.SNOW to 1.6, LoadCategory.LIVE to 1.0),
                codeReference = "$ref 2.3.1-3"
            ),
            LoadCombination(
                name = "LRFD-4",
                methodology = DesignMethodology.LRFD,
                equation = "1.2D + 1.0W + L + 0.5(Lr or S)",
                factors = mapOf(LoadCategory.DEAD to 1.2, LoadCategory.WIND to 1.0, LoadCategory.LIVE to 1.0, LoadCategory.SNOW to 0.5),
                codeReference = "$ref 2.3.1-4"
            )
        )
    }

    private fun getAsdCombinations(asceEdition: AsceEdition): List<LoadCombination> {
        val ref = asceEdition.label
        return listOf(
            LoadCombination(
                name = "ASD-1",
                methodology = DesignMethodology.ASD,
                equation = "D",
                factors = mapOf(LoadCategory.DEAD to 1.0),
                codeReference = "$ref 2.4.1-1"
            ),
            LoadCombination(
                name = "ASD-2",
                methodology = DesignMethodology.ASD,
                equation = "D + L",
                factors = mapOf(LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 1.0),
                codeReference = "$ref 2.4.1-2"
            ),
            LoadCombination(
                name = "ASD-3",
                methodology = DesignMethodology.ASD,
                equation = "D + (Lr or S)",
                factors = mapOf(LoadCategory.DEAD to 1.0, LoadCategory.SNOW to 1.0),
                codeReference = "$ref 2.4.1-3"
            ),
            LoadCombination(
                name = "ASD-4",
                methodology = DesignMethodology.ASD,
                equation = "D + 0.75L + 0.75(Lr or S)",
                factors = mapOf(LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 0.75, LoadCategory.SNOW to 0.75),
                codeReference = "$ref 2.4.1-4"
            )
        )
    }
}
