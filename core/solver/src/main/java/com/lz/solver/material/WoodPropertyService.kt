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

    /**
     * Returns the tabulated NDS Supplement reference properties for
     * [species]/[grade], or null when that combination isn't in the table
     * yet. Deliberately does NOT fall back to a generic placeholder value —
     * this is a structural design tool, and silently substituting fabricated
     * numbers for an untabulated species/grade (most notably: every glulam
     * combination, since none are tabulated below despite being selectable
     * in [com.lz.ui.material.WoodMaterialPickerDialog]) would let a real
     * calculation proceed on numbers nobody verified. Callers must handle
     * null by refusing to proceed, not by inventing a fallback of their own.
     */
    fun getReferenceProperties(species: WoodSpecies, grade: WoodGrade): WoodReferenceProperties? {
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
                else -> null
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
                else -> null
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
                else -> null
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
                else -> null
            }
            // GLULAM_WS / GLULAM_SP: no combinations tabulated yet — see the
            // doc comment above. Needs real NDS Supplement 5A/5B combination
            // symbol data (e.g. 24F-1.8E) before any glulam grade can safely
            // return a value here.
            WoodSpecies.GLULAM_WS, WoodSpecies.GLULAM_SP -> null
        }
    }
}