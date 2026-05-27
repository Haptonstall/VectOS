package com.lz.vectos.domain.structural

/**
 * Single source of truth for code-mandated ASD load combinations.
 */
object AsdLoadCombinationRepository {

    fun getAsdCombinations(asceEdition: AsceEdition): List<LoadCombination> {
        val ref = asceEdition.label
        return listOf(
            LoadCombination(
                name = "ASD-1",
                methodology = DesignMethodology.ASD,
                equation = "D",
                factors = mapOf(LoadCategory.DEAD to 1.0),
                codeReference = "$ref §2.4.1-1"
            ),
            LoadCombination(
                name = "ASD-2",
                methodology = DesignMethodology.ASD,
                equation = "D + L",
                factors = mapOf(LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 1.0),
                codeReference = "$ref §2.4.1-2"
            ),
            LoadCombination(
                name = "ASD-3",
                methodology = DesignMethodology.ASD,
                equation = "D + (Lr or S or R)",
                factors = mapOf(LoadCategory.DEAD to 1.0, LoadCategory.SNOW to 1.0),
                codeReference = "$ref §2.4.1-3"
            ),
            LoadCombination(
                name = "ASD-4",
                methodology = DesignMethodology.ASD,
                equation = "D + 0.75L + 0.75(Lr or S or R)",
                factors = mapOf(LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 0.75, LoadCategory.SNOW to 0.75),
                codeReference = "$ref §2.4.1-4"
            ),
            LoadCombination(
                name = "ASD-5",
                methodology = DesignMethodology.ASD,
                equation = "D + 0.6W",
                factors = mapOf(LoadCategory.DEAD to 1.0, LoadCategory.WIND to 0.6),
                codeReference = "$ref §2.4.1-5"
            ),
            LoadCombination(
                name = "ASD-6",
                methodology = DesignMethodology.ASD,
                equation = "D + 0.75L + 0.75W + 0.75(Lr or S or R)",
                factors = mapOf(LoadCategory.DEAD to 1.0, LoadCategory.LIVE to 0.75, LoadCategory.WIND to 0.75, LoadCategory.SNOW to 0.75),
                codeReference = "$ref §2.4.1-6"
            )
        )
    }
}
