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
     * numbers for an untabulated species/grade would let a real calculation
     * proceed on numbers nobody verified. Callers must handle null by
     * refusing to proceed, not by inventing a fallback of their own.
     *
     * Coverage as of this writing is partial by design — each species'
     * `when` block below documents exactly which grades are verified and
     * which are still open, rather than silently returning null for an
     * unverified combination with no explanation at the call site.
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
                // NDS Supplement 2018 Table 4A, Douglas Fir-Larch, No.3.
                WoodGrade.NO_3 -> WoodReferenceProperties(
                    bending = 525.0.psi,
                    shear = 180.0.psi,
                    compressionParallel = 775.0.psi,
                    compressionPerp = 625.0.psi,
                    tensionParallel = 325.0.psi,
                    modulusOfElasticity = (1.4 * 1000.0).psi,
                    shearModulus = (1.4 * 1000.0 / 16.0).psi,
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
                // NDS Supplement 2018 Table 4A, Hem-Fir, No.1.
                WoodGrade.NO_1 -> WoodReferenceProperties(
                    bending = 975.0.psi,
                    shear = 150.0.psi,
                    compressionParallel = 1350.0.psi,
                    compressionPerp = 405.0.psi,
                    tensionParallel = 625.0.psi,
                    modulusOfElasticity = (1.5 * 1000.0).psi,
                    shearModulus = (1.5 * 1000.0 / 16.0).psi,
                    densityPcf = 30.0
                )
                // NDS Supplement 2018 Table 4A, Hem-Fir, No.2.
                WoodGrade.NO_2 -> WoodReferenceProperties(
                    bending = 850.0.psi,
                    shear = 150.0.psi,
                    compressionParallel = 1300.0.psi,
                    compressionPerp = 405.0.psi,
                    tensionParallel = 525.0.psi,
                    modulusOfElasticity = (1.3 * 1000.0).psi,
                    shearModulus = (1.3 * 1000.0 / 16.0).psi,
                    densityPcf = 30.0
                )
                // No.3 not yet verified against a reliable source — left
                // unmapped rather than guessed. See WoodPropertyService doc.
                else -> null
            }
            WoodSpecies.SPF -> when (grade) {
                // Only Select Structural has been verified against a
                // reliable source so far. SPF's No.1/No.2 (NDS combines
                // these into one tabulated grade) and No.3 need a follow-up
                // pass — see WoodPropertyService doc comment.
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
                // Only No.2 has been mapped so far, and even this single
                // value is an approximation: unlike the other three species,
                // Southern Pine's real NDS Table 4B design values vary by
                // member width (a 2x4's Fb differs from a 2x12's), which
                // this lookup's species+grade-only signature can't represent
                // without a wider change — see WoodPropertyService doc.
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
            // 24F-1.8E is the only glulam combination mapped so far, for
            // both species groupings — see NDS Supplement Table 5A ("24F-V4
            // 1.8E DF/DF" for GLULAM_WS, "24F-V3 1.8E SP/SP" for GLULAM_SP).
            // Real glulam properties are asymmetric (different values for
            // positive vs. negative-moment bending, and for the x-axis vs.
            // y-axis) in a way this single-value shape can't fully capture;
            // the values below are each combination's positive-bending,
            // x-axis set (Fbx+, Fvx, Fc-perp-x, Ex/Eminx), which is what a
            // simple-span beam in positive bending needs. G_24F_1_7E and
            // G_20F_1_5E are not yet mapped.
            WoodSpecies.GLULAM_WS -> when (grade) {
                WoodGrade.G_24F_1_8E -> WoodReferenceProperties(
                    bending = 2400.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1650.0.psi,
                    compressionPerp = 650.0.psi,
                    tensionParallel = 1100.0.psi,
                    modulusOfElasticity = (1.8 * 1000.0).psi,
                    shearModulus = (1.8 * 1000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                else -> null
            }
            WoodSpecies.GLULAM_SP -> when (grade) {
                WoodGrade.G_24F_1_8E -> WoodReferenceProperties(
                    bending = 2400.0.psi,
                    shear = 300.0.psi,
                    compressionParallel = 1650.0.psi,
                    compressionPerp = 740.0.psi,
                    tensionParallel = 1150.0.psi,
                    modulusOfElasticity = (1.8 * 1000.0).psi,
                    shearModulus = (1.8 * 1000.0 / 16.0).psi,
                    densityPcf = 34.3 // G = 0.55
                )
                else -> null
            }
        }
    }
}