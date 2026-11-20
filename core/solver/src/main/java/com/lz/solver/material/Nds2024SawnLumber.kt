package com.lz.solver.material

import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodSpecies

/**
 * Maps the solid-sawn species and grades the app offers onto the NDS 2024 Supplement rows (Tables 4A and 4B, see
 * [Nds2024Table4A] and [Nds2024Table4B]). Only what the printed tables contain is served; a species, grade or width
 * with no printed row returns null so the design is blocked rather than filled from another edition.
 *
 * Assumptions (flagged in the change notes):
 *  - DF_L is the Table 4A block "DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH", HEM_FIR is "HEM-FIR" and SPF is
 *    "SPRUCE-PINE-FIR" (not the "(NORTH)" or "(SOUTH)" blocks).
 *  - Spruce-Pine-Fir is printed with one combined grade "No. 1/ No. 2"; both app grades NO_1 and NO_2 map to it.
 *  - The grades "No. 1 & Btr" (DF-L, Hem-Fir) are not offered by the app and are not mapped.
 *  - Southern Pine Construction, Standard and Utility are printed only for 4" wide members, so they are served for
 *    a nominal width of 4" or less and not above it (the 2015 / 2018 lookup returns that row for every width).
 *  - Dense Structural 86 / 72 / 65 have no row in the 2024 Table 4B (the 2024 Supplement lists "Dense Select
 *    Structural 86 / 72 / 65" only in Table 4D, timbers), so they return null for 2024.
 */
internal object Nds2024SawnLumber {

    private const val DF_L_BLOCK = "DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH"

    private fun blockName(species: WoodSpecies): String? = when (species) {
        WoodSpecies.DF_L -> DF_L_BLOCK
        WoodSpecies.HEM_FIR -> "HEM-FIR"
        WoodSpecies.SPF -> "SPRUCE-PINE-FIR"
        else -> null
    }

    private fun table4AGrades(grade: WoodGrade): List<String> = when (grade) {
        WoodGrade.SELECT_STRUCTURAL -> listOf("Select Structural")
        WoodGrade.NO_1 -> listOf("No. 1", "No. 1/ No. 2")
        WoodGrade.NO_2 -> listOf("No. 2", "No. 1/ No. 2")
        WoodGrade.NO_3 -> listOf("No. 3")
        WoodGrade.STUD -> listOf("Stud")
        WoodGrade.CONSTRUCTION -> listOf("Construction")
        WoodGrade.STANDARD -> listOf("Standard")
        WoodGrade.UTILITY -> listOf("Utility")
        else -> emptyList()
    }

    /** The Table 4A row for [species] / [grade], or null when the table has none (or the species is not Table 4A). */
    fun table4A(species: WoodSpecies, grade: WoodGrade): Nds2024Table4A.Row? {
        val block = blockName(species) ?: return null
        for (label in table4AGrades(grade)) {
            Nds2024Table4A.rows.firstOrNull { it.species == block && it.grade == label }?.let { return it }
        }
        return null
    }

    private const val CSU_WIDTH = "4\" wide"

    private fun table4BGrade(grade: WoodGrade): String? = when (grade) {
        WoodGrade.SELECT_STRUCTURAL -> "Select Structural"
        WoodGrade.DENSE_SELECT_STRUCTURAL -> "Dense Select Structural"
        WoodGrade.NON_DENSE_SELECT_STRUCTURAL -> "Non-Dense Select Structural"
        WoodGrade.NO_1 -> "No.1"
        WoodGrade.NO_1_DENSE -> "No.1 Dense"
        WoodGrade.NO_1_NON_DENSE -> "No.1 Non-Dense"
        WoodGrade.NO_2 -> "No.2"
        WoodGrade.NO_2_DENSE -> "No.2 Dense"
        WoodGrade.NO_2_NON_DENSE -> "No.2 Non-Dense"
        WoodGrade.NO_3, WoodGrade.STUD -> "No.3 and Stud"
        WoodGrade.CONSTRUCTION -> "Construction"
        WoodGrade.STANDARD -> "Standard"
        WoodGrade.UTILITY -> "Utility"
        else -> null   // Dense Structural 86 / 72 / 65: no 2024 Table 4B row
    }

    /**
     * The Table 4B Southern Pine row for [grade] and a member of nominal width [nominalWidthIn] (the larger
     * nominal dimension), or null when the table has none. Widths above 12" use the 12" row (the size factor for
     * wider lumber is applied separately by the design code).
     */
    fun southernPine(grade: WoodGrade, nominalWidthIn: Double): Nds2024Table4B.Row? {
        val label = table4BGrade(grade) ?: return null
        val width = when (grade) {
            WoodGrade.CONSTRUCTION, WoodGrade.STANDARD, WoodGrade.UTILITY ->
                if (nominalWidthIn <= 4.0) CSU_WIDTH else return null
            else -> when {
                nominalWidthIn <= 4.0 -> "2\" - 4\" wide"
                nominalWidthIn <= 6.0 -> "5\" - 6\" wide"
                nominalWidthIn <= 8.0 -> "8\" wide"
                nominalWidthIn <= 10.0 -> "10\" wide"
                else -> "12\" wide"
            }
        }
        return Nds2024Table4B.find("SOUTHERN PINE", label, width)
    }
}
