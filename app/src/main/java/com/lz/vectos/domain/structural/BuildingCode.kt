package com.lz.vectos.domain.structural

/**
 * Encapsulates regional building codes and their default configurations.
 */
sealed class BuildingCode {
    abstract val name: String
    abstract val defaultLoadCases: List<LoadCase>
    abstract val defaultLoadCombinations: List<LoadCombination>

    object IBC_2021 : BuildingCode() {
        override val name = "IBC 2021 / ASCE 7-16"
        override val defaultLoadCases = listOf(
            LoadCase(StandardLoadCases.DEAD, "Dead Load"),
            LoadCase(StandardLoadCases.LIVE, "Live Load"),
            LoadCase(StandardLoadCases.SNOW, "Snow Load"),
            LoadCase(StandardLoadCases.WIND, "Wind Load"),
            LoadCase(StandardLoadCases.SEISMIC, "Seismic Load")
        )
        override val defaultLoadCombinations = listOf(
            LoadCombination("1.4D", mapOf(StandardLoadCases.DEAD to 1.4)),
            LoadCombination("1.2D + 1.6L", mapOf(StandardLoadCases.DEAD to 1.2, StandardLoadCases.LIVE to 1.6)),
            LoadCombination("1.2D + 1.0L + 0.5S", mapOf(StandardLoadCases.DEAD to 1.2, StandardLoadCases.LIVE to 1.0, StandardLoadCases.SNOW to 0.5))
        )
    }

    object EUROCODE : BuildingCode() {
        override val name = "Eurocode"
        override val defaultLoadCases = listOf(
            LoadCase(StandardLoadCases.DEAD, "Permanent Load (G)"),
            LoadCase(StandardLoadCases.LIVE, "Variable Load (Q)")
        )
        override val defaultLoadCombinations = listOf(
            LoadCombination("1.35G + 1.5Q", mapOf(StandardLoadCases.DEAD to 1.35, StandardLoadCases.LIVE to 1.5))
        )
    }
}
