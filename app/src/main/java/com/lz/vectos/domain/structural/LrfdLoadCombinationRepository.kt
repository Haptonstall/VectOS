package com.lz.vectos.domain.structural

/**
 * Repository for code-mandated LRFD load combinations per ASCE 7.
 */
object LrfdLoadCombinationRepository {

    fun getCombinations(asceEdition: AsceEdition): List<LoadCombination> {
        val ref = asceEdition.label
        return listOf(
            LoadCombination(
                name = "LRFD-1",
                methodology = DesignMethodology.LRFD,
                equation = "1.4D",
                factors = mapOf(LoadCategory.DEAD to 1.4),
                codeReference = "$ref §2.3.2-1"
            ),
            LoadCombination(
                name = "LRFD-2",
                methodology = DesignMethodology.LRFD,
                equation = "1.2D + 1.6L + 0.5(Lr or S or R)",
                factors = mapOf(LoadCategory.DEAD to 1.2, LoadCategory.LIVE to 1.6, LoadCategory.SNOW to 0.5),
                codeReference = "$ref §2.3.2-2"
            ),
            LoadCombination(
                name = "LRFD-3",
                methodology = DesignMethodology.LRFD,
                equation = "1.2D + 1.6(Lr or S or R) + (L or 0.5W)",
                factors = mapOf(LoadCategory.DEAD to 1.2, LoadCategory.SNOW to 1.6, LoadCategory.LIVE to 1.0),
                codeReference = "$ref §2.3.2-3"
            ),
            LoadCombination(
                name = "LRFD-4",
                methodology = DesignMethodology.LRFD,
                equation = "1.2D + 1.0W + L + 0.5(Lr or S or R)",
                factors = mapOf(LoadCategory.DEAD to 1.2, LoadCategory.WIND to 1.0, LoadCategory.LIVE to 1.0, LoadCategory.SNOW to 0.5),
                codeReference = "$ref §2.3.2-4"
            ),
            LoadCombination(
                name = "LRFD-5",
                methodology = DesignMethodology.LRFD,
                equation = "0.9D + 1.0W",
                factors = mapOf(LoadCategory.DEAD to 0.9, LoadCategory.WIND to 1.0),
                codeReference = "$ref §2.3.2-5"
            )
        )
    }
}
