package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Pressure
import com.lz.vectos.domain.units.psi

object WoodPropertyService {

    data class ReferenceValues(
        val fb: Double,
        val fv: Double,
        val e: Double
    )

    fun getReferenceValues(species: WoodSpecies, grade: WoodGrade): ReferenceValues {
        return when {
            species.isGlulam -> getGlulamValues(species, grade)
            else -> getSawnLumberValues(species, grade)
        }
    }

    private fun getSawnLumberValues(species: WoodSpecies, grade: WoodGrade): ReferenceValues {
        return when (species) {
            WoodSpecies.DF_L -> when (grade) {
                WoodGrade.SELECT_STRUCTURAL -> ReferenceValues(1500.0, 180.0, 1.9e6)
                WoodGrade.NO_1 -> ReferenceValues(1000.0, 180.0, 1.7e6)
                WoodGrade.NO_2 -> ReferenceValues(900.0, 180.0, 1.6e6)
                WoodGrade.STUD -> ReferenceValues(700.0, 180.0, 1.4e6)
                else -> ReferenceValues(600.0, 180.0, 1.2e6)
            }
            WoodSpecies.HEM_FIR -> when (grade) {
                WoodGrade.SELECT_STRUCTURAL -> ReferenceValues(1400.0, 150.0, 1.6e6)
                WoodGrade.NO_1 -> ReferenceValues(975.0, 150.0, 1.5e6)
                WoodGrade.NO_2 -> ReferenceValues(850.0, 150.0, 1.3e6)
                else -> ReferenceValues(500.0, 150.0, 1.1e6)
            }
            WoodSpecies.SPF -> when (grade) {
                WoodGrade.SELECT_STRUCTURAL -> ReferenceValues(1250.0, 135.0, 1.5e6)
                WoodGrade.NO_1 -> ReferenceValues(925.0, 135.0, 1.4e6)
                WoodGrade.NO_2 -> ReferenceValues(875.0, 135.0, 1.4e6)
                else -> ReferenceValues(450.0, 135.0, 1.1e6)
            }
            WoodSpecies.SOUTHERN_PINE -> when (grade) {
                WoodGrade.SELECT_STRUCTURAL -> ReferenceValues(1250.0, 175.0, 1.6e6)
                WoodGrade.NO_1 -> ReferenceValues(1050.0, 175.0, 1.5e6)
                WoodGrade.NO_2 -> ReferenceValues(1150.0, 175.0, 1.6e6) // SP has unique grading
                else -> ReferenceValues(500.0, 175.0, 1.2e6)
            }
            else -> ReferenceValues(600.0, 150.0, 1.0e6)
        }
    }

    private fun getGlulamValues(species: WoodSpecies, grade: WoodGrade): ReferenceValues {
        return when (grade) {
            WoodGrade.G_24F_1_8E -> ReferenceValues(2400.0, 265.0, 1.8e6)
            WoodGrade.G_24F_1_7E -> ReferenceValues(2400.0, 210.0, 1.7e6)
            WoodGrade.G_20F_1_5E -> ReferenceValues(2000.0, 210.0, 1.5e6)
            else -> ReferenceValues(2400.0, 265.0, 1.8e6)
        }
    }
}
