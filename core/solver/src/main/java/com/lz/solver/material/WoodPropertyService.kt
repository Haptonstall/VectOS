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
            // Glulam combination symbols — NDS Supplement Table 5A / ICC-ES
            // ESR-1940 (joint APA/ICC-ES evaluation report), organized by
            // species pairing. Real glulam properties are asymmetric
            // (different values for positive vs. negative-moment bending,
            // and for the x-axis vs. y-axis) in a way this single-value
            // shape can't fully capture; the values below are each
            // combination's positive-bending, x-axis set (Fbx+, Fvx,
            // Fc-perp-x, Ex-true), which is what a simple-span beam in
            // positive bending needs. See WoodGrade.validGradesFor() for
            // which symbols the picker offers for each species; not every
            // symbol offered there has verified values here yet — each
            // branch below documents exactly which do.
            WoodSpecies.GLULAM_DF_DF -> when (grade) {
                // NDS Table 5A / APA PR-L313 & ICC-ES ESR-1940, 16F-V3 DF/DF
                // (unbalanced layup, for simple-span use).
                WoodGrade.G_16F_V3 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1500.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 975.0.psi,
                    modulusOfElasticity = (1.6 * 1000.0).psi,
                    shearModulus = (1.6 * 1000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                // ESR-1940 Table 1, 16F-V6 DF/DF (balanced layup, for
                // continuous/cantilever use).
                WoodGrade.G_16F_V6 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1600.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 1000.0.psi,
                    modulusOfElasticity = (1.7 * 1000.0).psi,
                    shearModulus = (1.7 * 1000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                // ESR-1940 Table 1, 20F-V4 DF/DF.
                WoodGrade.G_20F_V4 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1550.0.psi,
                    compressionPerp = 590.0.psi,
                    tensionParallel = 975.0.psi,
                    modulusOfElasticity = (1.7 * 1000.0).psi,
                    shearModulus = (1.7 * 1000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                // ESR-1940 Table 1, 20F-V8 DF/DF.
                WoodGrade.G_20F_V8 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1600.0.psi,
                    compressionPerp = 590.0.psi,
                    tensionParallel = 975.0.psi,
                    modulusOfElasticity = (1.7 * 1000.0).psi,
                    shearModulus = (1.7 * 1000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                // 2024 NDS Supplement Table 5A (Cody-supplied screenshot),
                // 16F-G3 DF/DF.
                WoodGrade.G_16F_G3 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1600.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 975.0.psi,
                    modulusOfElasticity = (1.7 * 1000.0).psi,
                    shearModulus = (1.7 * 1000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                // 2024 NDS Supplement Table 5A, 16F-G6 DF/DF.
                WoodGrade.G_16F_G6 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1600.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 1000.0.psi,
                    modulusOfElasticity = (1.7 * 1000.0).psi,
                    shearModulus = (1.7 * 1000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                // 2024 NDS Supplement Table 5A, 20F-V3 DF/DF. Resolves an
                // earlier discrepancy: ESR-1940 (2018-era NDS) didn't have a
                // symbol by this name for DF/DF, only "20F-V4"/"20F-V8" —
                // the current 2024 NDS Supplement confirms 20F-V3/V7 are the
                // real DF/DF symbols after all, matching what Cody originally
                // asked for.
                WoodGrade.G_20F_V3 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1450.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 1000.0.psi,
                    modulusOfElasticity = (1.7 * 1000.0).psi,
                    shearModulus = (1.7 * 1000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                // 2024 NDS Supplement Table 5A, 20F-V7 DF/DF.
                WoodGrade.G_20F_V7 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1600.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 1000.0.psi,
                    modulusOfElasticity = (1.7 * 1000.0).psi,
                    shearModulus = (1.7 * 1000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                // G_16F_E3/E6, G_20F_E3/E6, G_24F_V4/V8, G_24F_E4/E13/E18,
                // G_26F_V1/V2 — not yet confidently extracted (sit in wider,
                // denser blocks of the source tables than the rows above).
                else -> null
            }
            // 2024 NDS Supplement Table 5A / ESR-1940 Table 1, 24F-V5 DF/HF.
            WoodSpecies.GLULAM_DF_HF -> when (grade) {
                WoodGrade.G_24F_V5 -> WoodReferenceProperties(
                    bending = 2400.0.psi,
                    shear = 215.0.psi,
                    compressionParallel = 1450.0.psi,
                    compressionPerp = 650.0.psi,
                    tensionParallel = 1100.0.psi,
                    modulusOfElasticity = (1.8 * 1000.0).psi,
                    shearModulus = (1.8 * 1000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                else -> null
            }
            // 2024 NDS Supplement Table 5A, 16F-G2 & 16F-G7 HF/HF.
            WoodSpecies.GLULAM_HF_HF -> when (grade) {
                WoodGrade.G_16F_G2 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 215.0.psi,
                    compressionParallel = 1150.0.psi,
                    compressionPerp = 375.0.psi,
                    tensionParallel = 825.0.psi,
                    modulusOfElasticity = (1.5 * 1000.0).psi,
                    shearModulus = (1.5 * 1000.0 / 16.0).psi,
                    densityPcf = 26.9 // G = 0.43
                )
                WoodGrade.G_16F_G7 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 215.0.psi,
                    compressionParallel = 1250.0.psi,
                    compressionPerp = 375.0.psi,
                    tensionParallel = 875.0.psi,
                    modulusOfElasticity = (1.5 * 1000.0).psi,
                    shearModulus = (1.5 * 1000.0 / 16.0).psi,
                    densityPcf = 26.9 // G = 0.43
                )
                // 24F-E15M1 not yet confidently extracted.
                else -> null
            }
            // ESR-1940 Table 1, 20F-V12/V13 AC/AC.
            WoodSpecies.GLULAM_AC_AC -> when (grade) {
                WoodGrade.G_20F_V12 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1500.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 925.0.psi,
                    modulusOfElasticity = (1.6 * 1000.0).psi,
                    shearModulus = (1.6 * 1000.0 / 16.0).psi,
                    densityPcf = 28.7 // G = 0.46
                )
                WoodGrade.G_20F_V13 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1550.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 950.0.psi,
                    modulusOfElasticity = (1.6 * 1000.0).psi,
                    shearModulus = (1.6 * 1000.0 / 16.0).psi,
                    densityPcf = 28.7 // G = 0.46
                )
                else -> null
            }
            // ESR-1940 Table 1, 20F-E/ES1 & 20F-E8 ES/ES.
            WoodSpecies.GLULAM_ES_ES -> when (grade) {
                WoodGrade.G_20F_E_ES1 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 200.0.psi,
                    compressionParallel = 1150.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 1050.0.psi,
                    modulusOfElasticity = (1.9 * 1000.0).psi,
                    shearModulus = (1.9 * 1000.0 / 16.0).psi,
                    densityPcf = 25.6 // G = 0.41
                )
                WoodGrade.G_20F_E8 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 200.0.psi,
                    compressionParallel = 1100.0.psi,
                    compressionPerp = 450.0.psi,
                    tensionParallel = 825.0.psi,
                    modulusOfElasticity = (1.6 * 1000.0).psi,
                    shearModulus = (1.6 * 1000.0 / 16.0).psi,
                    densityPcf = 25.6 // G = 0.41
                )
                else -> null
            }
            // ESR-1940 Table 1, 22F-V/POC1 & 22F-V/POC2 POC/POC.
            WoodSpecies.GLULAM_POC_POC -> when (grade) {
                WoodGrade.G_22F_V_POC1 -> WoodReferenceProperties(
                    bending = 2200.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1950.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 1150.0.psi,
                    modulusOfElasticity = (1.9 * 1000.0).psi,
                    shearModulus = (1.9 * 1000.0 / 16.0).psi,
                    densityPcf = 28.1 // G = 0.45
                )
                WoodGrade.G_22F_V_POC2 -> WoodReferenceProperties(
                    bending = 2200.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1900.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 1150.0.psi,
                    modulusOfElasticity = (1.8 * 1000.0).psi,
                    shearModulus = (1.8 * 1000.0 / 16.0).psi,
                    densityPcf = 28.1 // G = 0.45
                )
                // 2024 NDS Supplement Table 5A, 20F-V14 & 20F-V15 POC/POC.
                WoodGrade.G_20F_V14 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1300.0.psi,
                    compressionPerp = 470.0.psi,
                    tensionParallel = 900.0.psi,
                    modulusOfElasticity = (1.6 * 1000.0).psi,
                    shearModulus = (1.6 * 1000.0 / 16.0).psi,
                    densityPcf = 28.1 // G = 0.45 (footnote default; not
                    // independently confirmed for this specific symbol)
                )
                WoodGrade.G_20F_V15 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1600.0.psi,
                    compressionPerp = 470.0.psi,
                    tensionParallel = 900.0.psi,
                    modulusOfElasticity = (1.6 * 1000.0).psi,
                    shearModulus = (1.6 * 1000.0 / 16.0).psi,
                    densityPcf = 28.1
                )
                else -> null
            }
            // No NDS Table 5C hardwood data sourced yet.
            WoodSpecies.GLULAM_HARDWOODS -> null
            // ESR-1940 Table 1, 20F-E/SPF1 SPF/SPF.
            WoodSpecies.GLULAM_SPF_SPF -> when (grade) {
                WoodGrade.G_20F_E_SPF1 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 215.0.psi,
                    compressionParallel = 1100.0.psi,
                    compressionPerp = 425.0.psi,
                    tensionParallel = 425.0.psi,
                    modulusOfElasticity = (1.6 * 1000.0).psi,
                    shearModulus = (1.6 * 1000.0 / 16.0).psi,
                    densityPcf = 26.2 // G = 0.42
                )
                else -> null
            }
            WoodSpecies.GLULAM_SP_SP -> when (grade) {
                // ESR-1940 Table 1, "24F-1.8E Glulam Header" (species
                // "WS,SP/WS,SP" — valid across multiple species groupings per
                // the source, filed here since it's most often specified for
                // SP headers; not DF/DF-specific as this entry's values were
                // wrongly assumed to be before this pass).
                WoodGrade.G_24F_1_8E -> WoodReferenceProperties(
                    bending = 2400.0.psi,
                    shear = 215.0.psi,
                    compressionParallel = 1200.0.psi,
                    compressionPerp = 500.0.psi,
                    tensionParallel = 950.0.psi,
                    modulusOfElasticity = (1.9 * 1000.0).psi,
                    shearModulus = (1.9 * 1000.0 / 16.0).psi,
                    densityPcf = 26.2 // G = 0.42
                )
                // ESR-1940 Table 1, 16F-V5M1 SP/SP.
                WoodGrade.G_16F_V5M1 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 300.0.psi,
                    compressionParallel = 1500.0.psi,
                    compressionPerp = 650.0.psi,
                    tensionParallel = 1000.0.psi,
                    modulusOfElasticity = (1.5 * 1000.0).psi,
                    shearModulus = (1.5 * 1000.0 / 16.0).psi,
                    densityPcf = 34.3 // G = 0.55
                )
                // 2024 NDS Supplement Table 5A (Cody-supplied screenshot),
                // 16F-V2 SP/SP.
                WoodGrade.G_16F_V2 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 300.0.psi,
                    compressionParallel = 1300.0.psi,
                    compressionPerp = 740.0.psi,
                    tensionParallel = 1000.0.psi,
                    modulusOfElasticity = (1.6 * 1000.0).psi,
                    shearModulus = (1.6 * 1000.0 / 16.0).psi,
                    densityPcf = 34.3 // G = 0.55
                )
                // 2024 NDS Supplement Table 5A, 16F-G1 SP/SP.
                WoodGrade.G_16F_G1 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 300.0.psi,
                    compressionParallel = 1400.0.psi,
                    compressionPerp = 650.0.psi,
                    tensionParallel = 1050.0.psi,
                    modulusOfElasticity = (1.7 * 1000.0).psi,
                    shearModulus = (1.7 * 1000.0 / 16.0).psi,
                    densityPcf = 34.3 // G = 0.55
                )
                else -> null
            }
        }
    }
}