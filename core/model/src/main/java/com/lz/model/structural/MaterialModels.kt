package com.lz.model.structural

import com.lz.model.units.Pressure
import com.lz.model.units.inPsi
import com.lz.model.units.ksi
import com.lz.model.units.psi
import kotlinx.serialization.Serializable

@Serializable
enum class WoodSpecies(val isGlulam: Boolean = false) {
    DF_L,
    HEM_FIR,
    SPF,
    SOUTHERN_PINE,

    // Glulam species combinations — NDS Supplement Table 5A / ICC-ES
    // ESR-1940 organizes glulam reference values by "outer/core" species
    // pairing, not by a single species. These 9 match the full pairing set
    // (matches the reference tree Cody supplied). GLULAM_DF_DF and
    // GLULAM_SP_SP replace the old GLULAM_WS/GLULAM_SP names (same real
    // data — GLULAM_WS was always really DF/DF-specific data in practice;
    // GLULAM_SP was always SP/SP) now that the fuller set makes a single
    // umbrella "Western Species" name misleading. Ordered to match Cody's
    // reference tree.
    GLULAM_AC_AC(isGlulam = true),
    GLULAM_DF_DF(isGlulam = true),
    GLULAM_DF_HF(isGlulam = true),
    GLULAM_ES_ES(isGlulam = true),
    // NDS Table 5C hardwood glulam combinations (12F-V1 .. 24F-E5 RO),
    // sourced from the 2018 NDS Supplement PDF Cody supplied. Note this is
    // the 2018 edition, not the 2024 one treated as authoritative elsewhere
    // in this file — see WoodPropertyService.
    GLULAM_HARDWOODS(isGlulam = true),
    GLULAM_HF_HF(isGlulam = true),
    GLULAM_POC_POC(isGlulam = true),
    GLULAM_SP_SP(isGlulam = true),
    GLULAM_SPF_SPF(isGlulam = true);

    companion object {
        fun fromString(value: String): WoodSpecies = entries.find { it.name == value } ?: DF_L
    }
}

@Serializable
enum class WoodGrade {
    SELECT_STRUCTURAL,
    NO_1,
    NO_2,
    NO_3,
    STUD,
    CONSTRUCTION,
    STANDARD,
    UTILITY,

    // Southern Pine-only grades (NDS Table 4B splits Select Structural,
    // No.1 and No.2 into Dense / standard / Non-Dense, and adds the three
    // Dense Structural grades). The plain SELECT_STRUCTURAL / NO_1 / NO_2 /
    // NO_3 / STUD / CONSTRUCTION / STANDARD / UTILITY entries above are
    // reused for the un-suffixed Southern Pine rows. Appended as new enum
    // names, so previously saved materials (persisted by name) are
    // unaffected.
    DENSE_SELECT_STRUCTURAL,
    NON_DENSE_SELECT_STRUCTURAL,
    NO_1_DENSE,
    NO_1_NON_DENSE,
    NO_2_DENSE,
    NO_2_NON_DENSE,
    DENSE_STRUCTURAL_86,
    DENSE_STRUCTURAL_72,
    DENSE_STRUCTURAL_65,

    // Legacy glulam combination-symbol naming ("stress class"-"MOE in
    // millions psi"), predating the NDS Table 5A combination-symbol set
    // below. Corrected this pass: ICC-ES ESR-1940 lists "24F-1.8E Glulam
    // Header" as its own distinct row (species "WS,SP/WS,SP" — valid across
    // multiple species groupings, not DF/DF-specific as originally assumed)
    // with its own real values, different from the DF/DF-specific "24F-V4"
    // symbol below. G_24F_1_7E / G_20F_1_5E still unmapped — not confirmed
    // against an authoritative source.
    G_24F_1_8E,
    G_24F_1_7E,
    G_20F_1_5E,

    // NDS Table 5A / AITC / ICC-ES ESR-1940 combination symbols, cross-
    // checked against the official 2024 NDS Supplement Table 5A itself
    // (Cody-supplied screenshot) where the two disagreed — the 2024 NDS
    // Supplement governs. That resolved an earlier discrepancy: ESR-1940
    // (which draws on the 2018-era NDS) used "20F-V4"/"20F-V8" for DF/DF's
    // 20F combinations, but the current 2024 NDS Supplement confirms
    // "20F-V3"/"20F-V7" are the real DF/DF symbols — Cody's originally-
    // supplied list was right. A symbol name can be reused across different
    // species pairings that happen to share a stress-class label with
    // genuinely different underlying values (e.g. "24F-V5" exists for both
    // DF/HF and SP/SP) — safe, since WoodPropertyService branches on species
    // before grade, so the pairing is never ambiguous. See validGradesFor()
    // for which symbols are valid for which species, and
    // WoodPropertyService's doc comment for exactly which have verified
    // values vs. are name-only placeholders.
    G_16F_V3,
    G_16F_V6,
    G_16F_E3,
    G_16F_E6,
    G_16F_G2,
    G_16F_G3,
    G_16F_G6,
    G_16F_G7,
    G_16F_V2,
    G_16F_V5M1,
    G_16F_G1,
    G_20F_V3,
    G_20F_V7,
    G_20F_V4,
    G_20F_V8,
    G_20F_V12,
    G_20F_V13,
    G_20F_V14,
    G_20F_V15,
    G_20F_E3,
    G_20F_E6,
    G_20F_E_ES1,
    G_20F_E8,
    G_20F_E_SPF1,
    G_22F_V_POC1,
    G_22F_V_POC2,
    G_24F_V4,
    G_24F_V5,
    G_24F_V8,
    G_24F_E4,
    G_24F_E13,
    G_24F_E18,
    G_24F_E15M1,
    G_26F_V1,
    G_26F_V2,

    // NDS Table 5C hardwood glulam combination symbols. G_16F_V2 / G_16F_V3
    // (also used by SP/SP and DF/DF) are reused for the hardwood 16F-V2 /
    // 16F-V3 — safe, since WoodPropertyService branches on species first.
    // The three species-suffixed 24F-E symbols get distinct names so they
    // can't collide with DF/DF's G_24F_E4.
    G_12F_V1,
    G_12F_V2,
    G_14F_V1,
    G_14F_V2,
    G_14F_V3,
    G_14F_V4,
    G_16F_V1,
    G_16F_V4,
    G_20F_V1,
    G_20F_V2,
    G_16F_E1,
    G_16F_E2,
    G_20F_E1,
    G_20F_E2,
    G_24F_E1,
    G_24F_E2,
    G_24F_E3_YP,
    G_24F_E4_RM,
    G_24F_E5_RO;

    companion object {
        fun fromString(value: String): WoodGrade = entries.find { it.name == value } ?: NO_2

        /**
         * Which [WoodGrade] symbols are valid options for a given
         * [WoodSpecies]. Replaces the old flat "any 'G_' grade is valid for
         * any glulam species" filter — real glulam combination symbols are
         * specific to a species pairing (e.g. "16F-V3" is DF/DF only), so
         * showing all of them regardless of the selected species was
         * misleading even though [com.lz.solver.material.WoodPropertyService]
         * already safely blocked an invalid combination from being confirmed.
         * Solid-sawn grades apply uniformly to any non-glulam species.
         */
        fun validGradesFor(species: WoodSpecies): List<WoodGrade> {
            if (species == WoodSpecies.SOUTHERN_PINE) {
                return listOf(
                    DENSE_SELECT_STRUCTURAL, SELECT_STRUCTURAL, NON_DENSE_SELECT_STRUCTURAL,
                    NO_1_DENSE, NO_1, NO_1_NON_DENSE,
                    NO_2_DENSE, NO_2, NO_2_NON_DENSE,
                    NO_3, STUD, CONSTRUCTION, STANDARD, UTILITY,
                    DENSE_STRUCTURAL_86, DENSE_STRUCTURAL_72, DENSE_STRUCTURAL_65
                )
            }
            if (!species.isGlulam) {
                return listOf(SELECT_STRUCTURAL, NO_1, NO_2, NO_3, STUD, CONSTRUCTION, STANDARD, UTILITY)
            }
            return when (species) {
                WoodSpecies.GLULAM_DF_DF -> listOf(
                    G_16F_V3, G_16F_V6, G_16F_E3, G_16F_E6, G_16F_G3, G_16F_G6,
                    G_20F_V3, G_20F_V7, G_20F_V4, G_20F_V8, G_20F_E3, G_20F_E6,
                    G_24F_V4, G_24F_V8, G_24F_E4, G_24F_E13, G_24F_E18,
                    G_26F_V1, G_26F_V2
                )
                WoodSpecies.GLULAM_DF_HF -> listOf(G_24F_V5)
                WoodSpecies.GLULAM_HF_HF -> listOf(G_24F_E15M1, G_16F_G2, G_16F_G7)
                WoodSpecies.GLULAM_AC_AC -> listOf(G_20F_V12, G_20F_V13)
                WoodSpecies.GLULAM_ES_ES -> listOf(G_20F_E_ES1, G_20F_E8)
                WoodSpecies.GLULAM_POC_POC -> listOf(G_22F_V_POC1, G_22F_V_POC2, G_20F_V14, G_20F_V15)
                WoodSpecies.GLULAM_SPF_SPF -> listOf(G_20F_E_SPF1)
                WoodSpecies.GLULAM_SP_SP -> listOf(G_24F_1_8E, G_16F_V5M1, G_16F_V2, G_16F_G1)
                WoodSpecies.GLULAM_HARDWOODS -> listOf(
                    G_12F_V1, G_12F_V2, G_14F_V1, G_14F_V2, G_14F_V3, G_14F_V4,
                    G_16F_V1, G_16F_V2, G_16F_V3, G_16F_V4, G_20F_V1, G_20F_V2,
                    G_16F_E1, G_16F_E2, G_20F_E1, G_20F_E2,
                    G_24F_E1, G_24F_E2, G_24F_E3_YP, G_24F_E4_RM, G_24F_E5_RO
                )
                else -> emptyList()
            }
        }
    }
}

/**
 * Polymorphic material grade model supporting material-specific engineering properties.
 */
@Serializable
sealed class MaterialGrade {
    abstract val id: String
    abstract val name: String
    abstract val type: MaterialType
    abstract val modulusOfElasticity: Pressure
    abstract val shearModulus: Pressure
    abstract val densityPcf: Double

    @Serializable
    data class Steel(
        override val id: String,
        override val name: String,
        val yieldStrength: Pressure,
        val ultimateStrength: Pressure,
        override val modulusOfElasticity: Pressure = 29000.0.ksi,
        override val shearModulus: Pressure = 11200.0.ksi,
        override val densityPcf: Double = 490.0
    ) : MaterialGrade() {
        override val type = MaterialType.STEEL
    }

    @Serializable
    data class Aluminum (
        override val id: String,
        override val name: String,
        val yieldStrength: Pressure,
        val ultimateStrength: Pressure,
        override val modulusOfElasticity: Pressure = 10000.0.ksi,
        override val shearModulus: Pressure = 3600.0.ksi,
        override val densityPcf: Double = 169.0
    ) : MaterialGrade() {
        override val type = MaterialType.ALUMINUM
    }

    @Serializable
    data class Wood(
        override val id: String,
        override val name: String,
        val species: WoodSpecies,
        val grade: WoodGrade,
        val referenceBending: Pressure,
        val referenceShear: Pressure,
        val referenceCompressionParallel: Pressure,
        val referenceCompressionPerp: Pressure,
        val referenceTensionParallel: Pressure,
        override val modulusOfElasticity: Pressure,
        override val shearModulus: Pressure,
        override val densityPcf: Double
    ) : MaterialGrade() {
        override val type = MaterialType.WOOD
    }

    @Serializable
    data class Generic(
        override val id: String,
        override val name: String,
        override val type: MaterialType,
        override val modulusOfElasticity: Pressure,
        override val shearModulus: Pressure,
        override val densityPcf: Double
    ) : MaterialGrade()
}

/**
 * Standardized service for retrieving material data.
 */
interface MaterialDatabaseService {
    fun getMaterial(id: String): MaterialGrade?
    fun getAllMaterials(type: MaterialType): List<MaterialGrade>
}

/**
 * Mock implementation of the material database.
 */
class MockMaterialDatabaseService : MaterialDatabaseService {
    private val materials = mutableMapOf<String, MaterialGrade>()

    init {
        // Steel Grades
        registerSteel("A992", "ASTM A992", 50.0.ksi)
        registerSteel("A36", "ASTM A36", 36.0.ksi)

        // Wood Species/Grades (Simplified placeholders for initialization)
        registerWoodPlaceholder("DF_L_NO2", "Douglas Fir-Larch No. 2", WoodSpecies.DF_L, WoodGrade.NO_2, 1600.0.ksi, 35.0)
    }

    private fun registerSteel(idSuffix: String, name: String, yield: Pressure) {
        val id = "STEEL_$idSuffix"
        materials[id] = MaterialGrade.Steel(
            id = id,
            name = name,
            yieldStrength = yield,
            ultimateStrength = (yield.inPsi * 1.3).psi
        )
    }

    private fun registerWoodPlaceholder(idSuffix: String, name: String, species: WoodSpecies, grade: WoodGrade, e: Pressure, density: Double) {
        val id = "WOOD_$idSuffix"
        materials[id] = MaterialGrade.Wood(
            id = id,
            name = name,
            species = species,
            grade = grade,
            referenceBending = 1000.0.psi,
            referenceShear = 180.0.psi,
            referenceCompressionParallel = 1200.0.psi,
            referenceCompressionPerp = 625.0.psi,
            referenceTensionParallel = 675.0.psi,
            modulusOfElasticity = e,
            shearModulus = (e.inPsi / 16.0).psi,
            densityPcf = density
        )
    }

    override fun getMaterial(id: String): MaterialGrade? = materials[id]

    override fun getAllMaterials(type: MaterialType): List<MaterialGrade> {
        return materials.values.filter { it.type == type }
    }
}