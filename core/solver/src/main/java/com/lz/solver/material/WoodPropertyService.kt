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
        val densityPcf: Double,
        /**
         * Tabulated NDS Emin (the adjusted-for-stability modulus used in
         * CL / CP), or null where it hasn't been sourced for this entry —
         * callers then fall back to an approximation from E. Sawn-lumber
         * Emin is only ~0.37 E, well below the old E/1.76 approximation.
         */
        val eMin: Pressure? = null
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
                    // NDS 2018 Table 4A DF-L Select Structural Fc = 1,700 psi
                    // (was 1,550 — that is the "No. 1 & Btr" value).
                    compressionParallel = 1700.0.psi,
                    compressionPerp = 625.0.psi,
                    tensionParallel = 1000.0.psi,
                    modulusOfElasticity = (1.9 * 1_000_000.0).psi,
                    shearModulus = (1.9 * 1_000_000.0 / 16.0).psi,
                    eMin = 690000.0.psi,
                    densityPcf = 35.0
                )
                WoodGrade.NO_1 -> WoodReferenceProperties(
                    bending = 1000.0.psi,
                    shear = 180.0.psi,
                    compressionParallel = 1500.0.psi,
                    compressionPerp = 625.0.psi,
                    tensionParallel = 675.0.psi,
                    modulusOfElasticity = (1.7 * 1_000_000.0).psi,
                    shearModulus = (1.7 * 1_000_000.0 / 16.0).psi,
                    eMin = 620000.0.psi,
                    densityPcf = 35.0
                )
                WoodGrade.NO_2 -> WoodReferenceProperties(
                    bending = 900.0.psi,
                    shear = 180.0.psi,
                    compressionParallel = 1350.0.psi,
                    compressionPerp = 625.0.psi,
                    tensionParallel = 575.0.psi,
                    modulusOfElasticity = (1.6 * 1_000_000.0).psi,
                    shearModulus = (1.6 * 1_000_000.0 / 16.0).psi,
                    eMin = 580000.0.psi,
                    densityPcf = 35.0
                )
                // NDS Supplement 2018 Table 4A, Douglas Fir-Larch, No.3.
                WoodGrade.NO_3 -> WoodReferenceProperties(
                    bending = 525.0.psi,
                    shear = 180.0.psi,
                    compressionParallel = 775.0.psi,
                    compressionPerp = 625.0.psi,
                    tensionParallel = 325.0.psi,
                    modulusOfElasticity = (1.4 * 1_000_000.0).psi,
                    shearModulus = (1.4 * 1_000_000.0 / 16.0).psi,
                    eMin = 510000.0.psi,
                    densityPcf = 35.0
                )
                else -> null
            }
            WoodSpecies.HEM_FIR -> when (grade) {
                WoodGrade.SELECT_STRUCTURAL -> WoodReferenceProperties(
                    bending = 1400.0.psi,
                    shear = 150.0.psi,
                    // NDS 2018 Table 4A Hem-Fir Select Structural Fc = 1,500
                    // psi (was 1,300 — that is the No. 2 value).
                    compressionParallel = 1500.0.psi,
                    compressionPerp = 405.0.psi,
                    tensionParallel = 925.0.psi,
                    modulusOfElasticity = (1.6 * 1_000_000.0).psi,
                    shearModulus = (1.6 * 1_000_000.0 / 16.0).psi,
                    eMin = 580000.0.psi,
                    densityPcf = 30.0
                )
                // NDS Supplement 2018 Table 4A, Hem-Fir, No.1.
                WoodGrade.NO_1 -> WoodReferenceProperties(
                    bending = 975.0.psi,
                    shear = 150.0.psi,
                    compressionParallel = 1350.0.psi,
                    compressionPerp = 405.0.psi,
                    tensionParallel = 625.0.psi,
                    modulusOfElasticity = (1.5 * 1_000_000.0).psi,
                    shearModulus = (1.5 * 1_000_000.0 / 16.0).psi,
                    eMin = 550000.0.psi,
                    densityPcf = 30.0
                )
                // NDS Supplement 2018 Table 4A, Hem-Fir, No.2.
                WoodGrade.NO_2 -> WoodReferenceProperties(
                    bending = 850.0.psi,
                    shear = 150.0.psi,
                    compressionParallel = 1300.0.psi,
                    compressionPerp = 405.0.psi,
                    tensionParallel = 525.0.psi,
                    modulusOfElasticity = (1.3 * 1_000_000.0).psi,
                    shearModulus = (1.3 * 1_000_000.0 / 16.0).psi,
                    eMin = 470000.0.psi,
                    densityPcf = 30.0
                )
                // No.3 not yet verified against a reliable source — left
                // unmapped rather than guessed. See WoodPropertyService doc.
                else -> null
            }
            // NDS 2018 Supplement Table 4A (Spruce-Pine-Fir), 2" & wider,
            // G = 0.42. The table's combined "No.1 / No.2" row applies to
            // both grades. Density follows this file's existing 28 pcf
            // convention for SPF.
            WoodSpecies.SPF -> when (grade) {
                //                                 Fb    Ft   Fv  Fc-perp  Fc     E        Emin
                WoodGrade.SELECT_STRUCTURAL -> sawnLumber(1250, 700, 135, 425, 1400, 1_500_000, 550_000, 28.0)
                WoodGrade.NO_1, WoodGrade.NO_2 ->
                                               sawnLumber( 875, 450, 135, 425, 1150, 1_400_000, 510_000, 28.0)
                WoodGrade.NO_3              -> sawnLumber( 500, 250, 135, 425,  650, 1_200_000, 440_000, 28.0)
                WoodGrade.STUD              -> sawnLumber( 675, 350, 135, 425,  725, 1_200_000, 440_000, 28.0)
                WoodGrade.CONSTRUCTION      -> sawnLumber(1000, 500, 135, 425, 1400, 1_300_000, 470_000, 28.0)
                WoodGrade.STANDARD          -> sawnLumber( 550, 275, 135, 425, 1150, 1_200_000, 440_000, 28.0)
                WoodGrade.UTILITY           -> sawnLumber( 275, 125, 135, 425,  750, 1_100_000, 400_000, 28.0)
                else -> null
            }
            // Southern Pine's Table 4B values depend on member width. The
            // (species, grade) signature can't carry a width, so this
            // returns the 2"-4" wide band (the widest-value band, used for
            // picker display / E / density); the capacity calculators call
            // getSouthernPineDimensionValues() with the real member width.
            WoodSpecies.SOUTHERN_PINE -> getSouthernPineDimensionValues(grade, 4.0)
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
                    modulusOfElasticity = (1.6 * 1_000_000.0).psi,
                    shearModulus = (1.6 * 1_000_000.0 / 16.0).psi,
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
                    modulusOfElasticity = (1.7 * 1_000_000.0).psi,
                    shearModulus = (1.7 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                // ESR-1940 Table 1, 20F-V4 DF/DF.
                WoodGrade.G_20F_V4 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1550.0.psi,
                    compressionPerp = 590.0.psi,
                    tensionParallel = 975.0.psi,
                    modulusOfElasticity = (1.7 * 1_000_000.0).psi,
                    shearModulus = (1.7 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                // ESR-1940 Table 1, 20F-V8 DF/DF.
                WoodGrade.G_20F_V8 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1600.0.psi,
                    compressionPerp = 590.0.psi,
                    tensionParallel = 975.0.psi,
                    modulusOfElasticity = (1.7 * 1_000_000.0).psi,
                    shearModulus = (1.7 * 1_000_000.0 / 16.0).psi,
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
                    modulusOfElasticity = (1.7 * 1_000_000.0).psi,
                    shearModulus = (1.7 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                // 2024 NDS Supplement Table 5A, 16F-G6 DF/DF.
                WoodGrade.G_16F_G6 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1600.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 1000.0.psi,
                    modulusOfElasticity = (1.7 * 1_000_000.0).psi,
                    shearModulus = (1.7 * 1_000_000.0 / 16.0).psi,
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
                    modulusOfElasticity = (1.7 * 1_000_000.0).psi,
                    shearModulus = (1.7 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 31.2 // G = 0.5
                )
                // 2024 NDS Supplement Table 5A, 20F-V7 DF/DF.
                WoodGrade.G_20F_V7 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1600.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 1000.0.psi,
                    modulusOfElasticity = (1.7 * 1_000_000.0).psi,
                    shearModulus = (1.7 * 1_000_000.0 / 16.0).psi,
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
                    modulusOfElasticity = (1.8 * 1_000_000.0).psi,
                    shearModulus = (1.8 * 1_000_000.0 / 16.0).psi,
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
                    modulusOfElasticity = (1.5 * 1_000_000.0).psi,
                    shearModulus = (1.5 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 26.9 // G = 0.43
                )
                WoodGrade.G_16F_G7 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 215.0.psi,
                    compressionParallel = 1250.0.psi,
                    compressionPerp = 375.0.psi,
                    tensionParallel = 875.0.psi,
                    modulusOfElasticity = (1.5 * 1_000_000.0).psi,
                    shearModulus = (1.5 * 1_000_000.0 / 16.0).psi,
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
                    modulusOfElasticity = (1.6 * 1_000_000.0).psi,
                    shearModulus = (1.6 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 28.7 // G = 0.46
                )
                WoodGrade.G_20F_V13 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1550.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 950.0.psi,
                    modulusOfElasticity = (1.6 * 1_000_000.0).psi,
                    shearModulus = (1.6 * 1_000_000.0 / 16.0).psi,
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
                    modulusOfElasticity = (1.9 * 1_000_000.0).psi,
                    shearModulus = (1.9 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 25.6 // G = 0.41
                )
                WoodGrade.G_20F_E8 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 200.0.psi,
                    compressionParallel = 1100.0.psi,
                    compressionPerp = 450.0.psi,
                    tensionParallel = 825.0.psi,
                    modulusOfElasticity = (1.6 * 1_000_000.0).psi,
                    shearModulus = (1.6 * 1_000_000.0 / 16.0).psi,
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
                    modulusOfElasticity = (1.9 * 1_000_000.0).psi,
                    shearModulus = (1.9 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 28.1 // G = 0.45
                )
                WoodGrade.G_22F_V_POC2 -> WoodReferenceProperties(
                    bending = 2200.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1900.0.psi,
                    compressionPerp = 560.0.psi,
                    tensionParallel = 1150.0.psi,
                    modulusOfElasticity = (1.8 * 1_000_000.0).psi,
                    shearModulus = (1.8 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 28.1 // G = 0.45
                )
                // 2024 NDS Supplement Table 5A, 20F-V14 & 20F-V15 POC/POC.
                WoodGrade.G_20F_V14 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1300.0.psi,
                    compressionPerp = 470.0.psi,
                    tensionParallel = 900.0.psi,
                    modulusOfElasticity = (1.6 * 1_000_000.0).psi,
                    shearModulus = (1.6 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 28.1 // G = 0.45 (footnote default; not
                    // independently confirmed for this specific symbol)
                )
                WoodGrade.G_20F_V15 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 265.0.psi,
                    compressionParallel = 1600.0.psi,
                    compressionPerp = 470.0.psi,
                    tensionParallel = 900.0.psi,
                    modulusOfElasticity = (1.6 * 1_000_000.0).psi,
                    shearModulus = (1.6 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 28.1
                )
                else -> null
            }
            // NDS Table 5C, structural glued laminated hardwood timber —
            // 2018 NDS Supplement (Cody-supplied PDF, p. 69). NOT yet
            // reconciled against the 2024 Supplement, which is treated as
            // authoritative for the softwood species above; if hardwood
            // values changed between editions, branch on NdsEdition.
            // Same positive-bending, x-axis convention as the other glulam
            // species: Fbx+, Fvx, Fc, Fc-perp-x, Ft, Ex. Density = 62.4 * G
            // (G = the table's specific gravity for fastener design).
            WoodSpecies.GLULAM_HARDWOODS -> when (grade) {
                //                        Fbx+  Fvx   Fc    Fc-perp Ft   Ex    G     Ex,min
                WoodGrade.G_12F_V1    -> hardwoodGlulam(1200, 125,  800,  285,   600, 1.2, 0.39, 0.63)
                WoodGrade.G_12F_V2    -> hardwoodGlulam(1200, 125,  860,  285,   625, 1.2, 0.39, 0.63)
                WoodGrade.G_14F_V1    -> hardwoodGlulam(1400, 155,  950,  405,   700, 1.3, 0.45, 0.69)
                WoodGrade.G_14F_V2    -> hardwoodGlulam(1400, 180, 1200,  590,   750, 1.3, 0.53, 0.69)
                WoodGrade.G_14F_V3    -> hardwoodGlulam(1400, 155,  950,  405,   725, 1.3, 0.45, 0.69)
                WoodGrade.G_14F_V4    -> hardwoodGlulam(1400, 180, 1200,  590,   775, 1.3, 0.53, 0.69)
                WoodGrade.G_16F_V1    -> hardwoodGlulam(1600, 180, 1200,  590,   800, 1.4, 0.53, 0.74)
                WoodGrade.G_16F_V2    -> hardwoodGlulam(1600, 200, 1250,  835,   875, 1.5, 0.63, 0.79)
                WoodGrade.G_16F_V3    -> hardwoodGlulam(1600, 180, 1200,  590,   850, 1.4, 0.53, 0.74)
                WoodGrade.G_16F_V4    -> hardwoodGlulam(1600, 200, 1300,  835,   900, 1.6, 0.63, 0.85)
                WoodGrade.G_20F_V1    -> hardwoodGlulam(2000, 200, 1400,  835,   975, 1.7, 0.63, 0.90)
                WoodGrade.G_20F_V2    -> hardwoodGlulam(2000, 200, 1400,  835,  1000, 1.7, 0.63, 0.90)
                WoodGrade.G_16F_E1    -> hardwoodGlulam(1600, 125,  975,  440,   825, 1.4, 0.39, 0.74)
                WoodGrade.G_16F_E2    -> hardwoodGlulam(1600, 125, 1000,  440,   900, 1.4, 0.39, 0.74)
                WoodGrade.G_20F_E1    -> hardwoodGlulam(2000, 155, 1050,  590,   950, 1.6, 0.45, 0.85)
                WoodGrade.G_20F_E2    -> hardwoodGlulam(2000, 155, 1100,  590,  1050, 1.6, 0.45, 0.85)
                WoodGrade.G_24F_E1    -> hardwoodGlulam(2400, 180, 1400,  770,  1050, 1.8, 0.53, 0.95)
                WoodGrade.G_24F_E2    -> hardwoodGlulam(2400, 180, 1400,  770,  1050, 1.8, 0.53, 0.95)
                WoodGrade.G_24F_E3_YP -> hardwoodGlulam(2400, 155, 1200,  590,   975, 1.8, 0.45, 0.95)
                WoodGrade.G_24F_E4_RM -> hardwoodGlulam(2400, 220, 1350,  895,  1050, 1.8, 0.53, 0.95)
                WoodGrade.G_24F_E5_RO -> hardwoodGlulam(2400, 235, 1450, 1075,  1100, 1.8, 0.63, 0.95)
                else -> null
            }
            // ESR-1940 Table 1, 20F-E/SPF1 SPF/SPF.
            WoodSpecies.GLULAM_SPF_SPF -> when (grade) {
                WoodGrade.G_20F_E_SPF1 -> WoodReferenceProperties(
                    bending = 2000.0.psi,
                    shear = 215.0.psi,
                    compressionParallel = 1100.0.psi,
                    compressionPerp = 425.0.psi,
                    tensionParallel = 425.0.psi,
                    modulusOfElasticity = (1.6 * 1_000_000.0).psi,
                    shearModulus = (1.6 * 1_000_000.0 / 16.0).psi,
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
                    modulusOfElasticity = (1.9 * 1_000_000.0).psi,
                    shearModulus = (1.9 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 26.2 // G = 0.42
                )
                // ESR-1940 Table 1, 16F-V5M1 SP/SP.
                WoodGrade.G_16F_V5M1 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 300.0.psi,
                    compressionParallel = 1500.0.psi,
                    compressionPerp = 650.0.psi,
                    tensionParallel = 1000.0.psi,
                    modulusOfElasticity = (1.5 * 1_000_000.0).psi,
                    shearModulus = (1.5 * 1_000_000.0 / 16.0).psi,
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
                    modulusOfElasticity = (1.6 * 1_000_000.0).psi,
                    shearModulus = (1.6 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 34.3 // G = 0.55
                )
                // 2024 NDS Supplement Table 5A, 16F-G1 SP/SP.
                WoodGrade.G_16F_G1 -> WoodReferenceProperties(
                    bending = 1600.0.psi,
                    shear = 300.0.psi,
                    compressionParallel = 1400.0.psi,
                    compressionPerp = 650.0.psi,
                    tensionParallel = 1050.0.psi,
                    modulusOfElasticity = (1.7 * 1_000_000.0).psi,
                    shearModulus = (1.7 * 1_000_000.0 / 16.0).psi,
                    densityPcf = 34.3 // G = 0.55
                )
                else -> null
            }
        }
    }

    /**
     * One NDS Table 5C row. Kept as a helper (unlike the softwood entries,
     * which spell each field out) so all 21 hardwood rows stay one line
     * each and can be checked directly against the printed table.
     * Shear modulus follows the same E/16 convention as every other entry.
     */
    private fun hardwoodGlulam(
        fbxPositive: Int, fvx: Int, fc: Int, fcPerpX: Int, ft: Int,
        exMillionPsi: Double, specificGravity: Double, exMinMillionPsi: Double
    ) = WoodReferenceProperties(
        bending = fbxPositive.toDouble().psi,
        shear = fvx.toDouble().psi,
        compressionParallel = fc.toDouble().psi,
        compressionPerp = fcPerpX.toDouble().psi,
        tensionParallel = ft.toDouble().psi,
        modulusOfElasticity = (exMillionPsi * 1_000_000.0).psi,
        shearModulus = (exMillionPsi * 1_000_000.0 / 16.0).psi,
        densityPcf = Math.round(62.4 * specificGravity * 10.0) / 10.0,
        eMin = (exMinMillionPsi * 1_000_000.0).psi
    )

    /** One solid-sawn dimension-lumber row (psi). Shear modulus = E/16. */
    private fun sawnLumber(
        fb: Int, ft: Int, fv: Int, fcPerp: Int, fc: Int, e: Int, eMin: Int, densityPcf: Double
    ) = WoodReferenceProperties(
        bending = fb.toDouble().psi,
        shear = fv.toDouble().psi,
        compressionParallel = fc.toDouble().psi,
        compressionPerp = fcPerp.toDouble().psi,
        tensionParallel = ft.toDouble().psi,
        modulusOfElasticity = e.toDouble().psi,
        shearModulus = (e / 16.0).psi,
        densityPcf = densityPcf,
        eMin = eMin.toDouble().psi
    )

    /** One Table 4B row: Fb, Ft, Fv, Fc-perp, Fc, E, Emin (all psi). */
    private class SouthernPineRow(
        val fb: Int, val ft: Int, val fv: Int, val fcPerp: Int,
        val fc: Int, val e: Int, val eMin: Int
    )

    private fun sp(fb: Int, ft: Int, fv: Int, fcPerp: Int, fc: Int, e: Int, eMin: Int) =
        SouthernPineRow(fb, ft, fv, fcPerp, fc, e, eMin)

    // NDS 2018 Supplement Table 4B, one entry per grade; list index = width band
    // (2"-4", 5"-6", 8", 10", 12" wide). Columns: Fb, Ft, Fv, Fc-perp, Fc, E, Emin.
    private val southernPineTable4B: Map<WoodGrade, List<SouthernPineRow>> = mapOf(
        WoodGrade.DENSE_SELECT_STRUCTURAL to listOf(
            sp(2700, 1900, 175, 660, 2050, 1900000, 690000),
            sp(2400, 1650, 175, 660, 1900, 1900000, 690000),
            sp(2200, 1550, 175, 660, 1850, 1900000, 690000),
            sp(1950, 1300, 175, 660, 1800, 1900000, 690000),
            sp(1800, 1250, 175, 660, 1750, 1900000, 690000)
        ),
        WoodGrade.SELECT_STRUCTURAL to listOf(
            sp(2350, 1650, 175, 565, 1900, 1800000, 660000),
            sp(2100, 1450, 175, 565, 1800, 1800000, 660000),
            sp(1950, 1350, 175, 565, 1700, 1800000, 660000),
            sp(1700, 1150, 175, 565, 1650, 1800000, 660000),
            sp(1600, 1100, 175, 565, 1650, 1800000, 660000)
        ),
        WoodGrade.NON_DENSE_SELECT_STRUCTURAL to listOf(
            sp(2050, 1450, 175, 480, 1800, 1600000, 580000),
            sp(1850, 1300, 175, 480, 1700, 1600000, 580000),
            sp(1700, 1200, 175, 480, 1650, 1600000, 580000),
            sp(1500, 1050, 175, 480, 1600, 1600000, 580000),
            sp(1400, 975, 175, 480, 1550, 1600000, 580000)
        ),
        WoodGrade.NO_1_DENSE to listOf(
            sp(1650, 1100, 175, 660, 1750, 1800000, 660000),
            sp(1500, 1000, 175, 660, 1650, 1800000, 660000),
            sp(1350, 900, 175, 660, 1600, 1800000, 660000),
            sp(1200, 800, 175, 660, 1550, 1800000, 660000),
            sp(1100, 750, 175, 660, 1500, 1800000, 660000)
        ),
        WoodGrade.NO_1 to listOf(
            sp(1500, 1000, 175, 565, 1650, 1600000, 580000),
            sp(1350, 875, 175, 565, 1550, 1600000, 580000),
            sp(1250, 800, 175, 565, 1500, 1600000, 580000),
            sp(1050, 700, 175, 565, 1450, 1600000, 580000),
            sp(1000, 650, 175, 565, 1400, 1600000, 580000)
        ),
        WoodGrade.NO_1_NON_DENSE to listOf(
            sp(1300, 875, 175, 480, 1550, 1400000, 510000),
            sp(1200, 775, 175, 480, 1450, 1400000, 510000),
            sp(1100, 700, 175, 480, 1400, 1400000, 510000),
            sp(950, 625, 175, 480, 1400, 1400000, 510000),
            sp(900, 575, 175, 480, 1350, 1400000, 510000)
        ),
        WoodGrade.NO_2_DENSE to listOf(
            sp(1200, 750, 175, 660, 1500, 1600000, 580000),
            sp(1050, 650, 175, 660, 1450, 1600000, 580000),
            sp(975, 600, 175, 660, 1400, 1600000, 580000),
            sp(850, 525, 175, 660, 1350, 1600000, 580000),
            sp(800, 500, 175, 660, 1300, 1600000, 580000)
        ),
        WoodGrade.NO_2 to listOf(
            sp(1100, 675, 175, 565, 1450, 1400000, 510000),
            sp(1000, 600, 175, 565, 1400, 1400000, 510000),
            sp(925, 550, 175, 565, 1350, 1400000, 510000),
            sp(800, 475, 175, 565, 1300, 1400000, 510000),
            sp(750, 450, 175, 565, 1250, 1400000, 510000)
        ),
        WoodGrade.NO_2_NON_DENSE to listOf(
            sp(1050, 600, 175, 480, 1450, 1300000, 470000),
            sp(950, 525, 175, 480, 1350, 1300000, 470000),
            sp(875, 500, 175, 480, 1300, 1300000, 470000),
            sp(750, 425, 175, 480, 1250, 1300000, 470000),
            sp(700, 400, 175, 480, 1250, 1300000, 470000)
        ),
        WoodGrade.NO_3 to listOf(
            sp(650, 400, 175, 565, 850, 1300000, 470000),
            sp(575, 350, 175, 565, 800, 1300000, 470000),
            sp(525, 325, 175, 565, 775, 1300000, 470000),
            sp(475, 275, 175, 565, 750, 1300000, 470000),
            sp(450, 250, 175, 565, 725, 1300000, 470000)
        ),
        WoodGrade.STUD to listOf(
            sp(650, 400, 175, 565, 850, 1300000, 470000),
            sp(575, 350, 175, 565, 800, 1300000, 470000),
            sp(525, 325, 175, 565, 775, 1300000, 470000),
            sp(475, 275, 175, 565, 750, 1300000, 470000),
            sp(450, 250, 175, 565, 725, 1300000, 470000)
        ),
        WoodGrade.CONSTRUCTION to listOf(
            sp(875, 500, 175, 565, 1600, 1400000, 510000)
        ),
        WoodGrade.STANDARD to listOf(
            sp(475, 275, 175, 565, 1300, 1200000, 440000)
        ),
        WoodGrade.UTILITY to listOf(
            sp(225, 125, 175, 565, 850, 1200000, 440000)
        ),
        WoodGrade.DENSE_STRUCTURAL_86 to listOf(
            sp(2600, 1750, 175, 660, 2000, 1800000, 660000)
        ),
        WoodGrade.DENSE_STRUCTURAL_72 to listOf(
            sp(2200, 1450, 175, 660, 1650, 1800000, 660000)
        ),
        WoodGrade.DENSE_STRUCTURAL_65 to listOf(
            sp(2000, 1300, 175, 660, 1500, 1800000, 660000)
        )
    )

    /**
     * NDS 2018 Table 4B Southern Pine values for a member of the given
     * nominal width (the larger nominal dimension — "depth" when used
     * edgewise), or null when [grade] isn't a Southern Pine grade. Only
     * Fb, Ft and Fc vary with width; Fv, Fc-perp, E and Emin don't.
     * Widths above 12" use the 12" band (Table 4B footnote 4 — the >12"
     * size factor is applied separately, see resolveSawnCf). Grades tabulated
     * for a single size class (Construction/Standard/Utility, and the Dense
     * Structural grades) return that one row for every width.
     *
     * Not included: Mixed Southern Pine, and the "surfaced green" Dense
     * Structural values.
     */
    fun getSouthernPineDimensionValues(
        grade: WoodGrade,
        nominalWidthIn: Double
    ): WoodReferenceProperties? {
        val rows = southernPineTable4B[grade] ?: return null
        val band = when {
            nominalWidthIn <= 4.0 -> 0
            nominalWidthIn <= 6.0 -> 1
            nominalWidthIn <= 8.0 -> 2
            nominalWidthIn <= 10.0 -> 3
            else -> 4
        }.coerceAtMost(rows.lastIndex)
        val r = rows[band]
        return sawnLumber(r.fb, r.ft, r.fv, r.fcPerp, r.fc, r.e, r.eMin, densityPcf = 37.0)
    }

    /**
     * Re-derives a wood material's tabulated values from the current
     * tables. Materials picked or saved before a table correction carry the
     * old numbers with them (this includes the earlier ~1000x-too-small E),
     * so this lets restored materials pick up fixes. Returns the material
     * unchanged when there's no table entry for its species/grade. For
     * Southern Pine this yields the 2"-4" band; the capacity calculators
     * apply the real member width on top.
     */
    fun refreshed(material: com.lz.model.structural.MaterialGrade.Wood):
        com.lz.model.structural.MaterialGrade.Wood {
        val p = getReferenceProperties(material.species, material.grade) ?: return material
        return material.copy(
            referenceBending = p.bending,
            referenceShear = p.shear,
            referenceCompressionParallel = p.compressionParallel,
            referenceCompressionPerp = p.compressionPerp,
            referenceTensionParallel = p.tensionParallel,
            modulusOfElasticity = p.modulusOfElasticity,
            shearModulus = p.shearModulus,
            densityPcf = p.densityPcf
        )
    }
}