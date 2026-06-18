package com.lz.solver.material

import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodSpecies
import com.lz.model.units.Pressure
import com.lz.model.units.psi

/**
 * Service to provide reference engineering properties for Wood species and grades
 * as defined in the NDS Supplement.
 */
object WoodPropertyService {

    data class WoodReferenceProperties(
        val bending: Pressure,
        val shear: Pressure,
        val compressionParallel: Pressure,
        val compressionPerp: Pressure,
        val tensionParallel: Pressure,
        val modulusOfElasticity: Pressure,
        val shearModulus: Pressure,
        val densityPcf: Double
    )

    fun getReferenceProperties(species: WoodSpecies, grade: WoodGrade): WoodReferenceProperties {
        return when (species) {
            WoodSpecies.DF_L -> when (grade) {
                WoodGrade.SELECT_STRUCTURAL -> WoodReferenceProperties(
                    bending = 1500.0.psi,
                    shear = 180.0.psi,
                    compressionParallel = 1550.0.psi,
                    compressionPerp = 625.0.psi,
                    tensionParallel = 1000.0.psi,
                    modulusOfElasticity = (1.9 * 1000.0).psi,
                    shearModulus = (1.9 * 1000.0 / 16.0).psi,
                    densityPcf = 35.0
                )
                WoodGrade.NO_1 -> WoodReferenceProperties(
                    bending = 1000.0.psi,
                    shear = 180.0.psi,
                    compressionParallel = 1500.0.psi,
                    compressionPerp = 625.0.psi,
                    tensionParallel = 675.0.psi,
                    modulusOfElasticity = (1.7 * 1000.0).psi,
                    shearModulus = (1.7 * 1000.0 / 16.0).psi,
                    densityPcf = 35.0
                )
                WoodGrade.NO_2 -> WoodReferenceProperties(
                    bending = 900.0.psi,
                    shear = 180.0.psi,
                    compressionParallel = 1350.0.psi,
                    compressionPerp = 625.0.psi,
                    tensionParallel = 575.0.psi,
                    modulusOfElasticity = (1.6 * 1000.0).psi,
                    shearModulus = (1.6 * 1000.0 / 16.0).psi,
                    densityPcf = 35.0
                )
                else -> getFallbackProperties(35.0)
            }
            WoodSpecies.HEM_FIR -> when (grade) {
                WoodGrade.SELECT_STRUCTURAL -> WoodReferenceProperties(
                    bending = 1400.0.psi,
                    shear = 150.0.psi,
                    compressionParallel = 1300.0.psi,
                    compressionPerp = 405.0.psi,
                    tensionParallel = 925.0.psi,
                    modulusOfElasticity = (1.6 * 1000.0).psi,
                    shearModulus = (1.6 * 1000.0 / 16.0).psi,
                    densityPcf = 30.0
                )
                else -> getFallbackProperties(30.0)
            }
            WoodSpecies.SPF -> when (grade) {
                WoodGrade.SELECT_STRUCTURAL -> WoodReferenceProperties(
                    bending = 1250.0.psi,
                    shear = 135.0.psi,
                    compressionParallel = 1150.0.psi,
                    compressionPerp = 425.0.psi,
                    tensionParallel = 700.0.psi,
                    modulusOfElasticity = (1.5 * 1000.0).psi,
                    shearModulus = (1.5 * 1000.0 / 16.0).psi,
                    densityPcf = 28.0
                )
                else -> getFallbackProperties(28.0)
            }
            WoodSpecies.SOUTHERN_PINE -> when (grade) {
                WoodGrade.NO_2 -> WoodReferenceProperties(
                    bending = 1150.0.psi,
                    shear = 175.0.psi,
                    compressionParallel = 1450.0.psi,
                    compressionPerp = 565.0.psi,
                    tensionParallel = 750.0.psi,
                    modulusOfElasticity = (1.6 * 1000.0).psi,
                    shearModulus = (1.6 * 1000.0 / 16.0).psi,
                    densityPcf = 37.0
                )
                else -> getFallbackProperties(37.0)
            }
            else -> getFallbackProperties(35.0)
        }
    }

    private fun getFallbackProperties(density: Double) = WoodReferenceProperties(
        bending = 600.0.psi,
        shear = 150.0.psi,
        compressionParallel = 800.0.psi,
        compressionPerp = 400.0.psi,
        tensionParallel = 400.0.psi,
        modulusOfElasticity = (1.0 * 1000.0).psi,
        shearModulus = (1.0 * 1000.0 / 16.0).psi,
        densityPcf = density
    )
}