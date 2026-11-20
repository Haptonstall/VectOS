package com.lz.solver.material

/**
 * NDS 2024 Supplement, Table 4B: Reference Design Values for Visually Graded Southern Pine Dimension Lumber
 * (2" - 4" thick), Southern Pine and Mixed Southern Pine, graded to SPIB rules. Values are for normal load duration
 * and dry service conditions, in psi, exactly as printed in the Supplement (checked by two independent extractions
 * of the PDF: a text-layout parse and a coordinate-based word parse agree on all 76 rows, including the width
 * classification of every row).
 *
 * Unlike Table 4A, the values depend on the member width ([widthClassification] is the printed size
 * classification). Specific gravity G is 0.55 for Southern Pine and 0.51 for Mixed Southern Pine (printed once per
 * species block). Grade names are as printed (for example "No.1 Dense"). The Table 4B Adjustment Factors page
 * (size factor CF, flat use Cfu, repetitive member Cr, wet service CM) is NOT part of this data and is applied by
 * the design code.
 *
 * Spruce Pine is not tabulated: footnote 3 derives it from Mixed Southern Pine with [sprucePineConversion] and
 * prescribed rounding, so no Spruce Pine rows are included here.
 *
 * Connected to design for Southern Pine through [Nds2024SawnLumber], by member width. Dense Structural 86 / 72 / 65
 * have no row in this table and are blocked for NDS 2024; Construction, Standard and Utility are served for members up
 * to 4" nominal width only. Mixed Southern Pine is data only (see [NdsCodeData]).
 */
object Nds2024Table4B {

    data class Row(
        val species: String,
        val grade: String,
        val widthClassification: String,
        val fb: Int,
        val ft: Int,
        val fv: Int,
        val fcPerp: Int,
        val fc: Int,
        val e: Int,
        val eMin: Int,
        val specificGravity: Double,
        val gradingRulesAgency: String = "SPIB"
    )

    /** Footnote 3 conversion factors (Mixed Southern Pine to Spruce Pine). E and Emin share one factor. */
    data class SprucePineConversion(
        val fb: Double, val ft: Double, val fv: Double, val fcPerp: Double, val fc: Double, val eAndEMin: Double
    )

    val sprucePineConversion = SprucePineConversion(fb = 0.78, ft = 0.78, fv = 0.98, fcPerp = 0.73, fc = 0.78, eAndEMin = 0.82)

    val rows: List<Row> = listOf(
        Row("SOUTHERN PINE", "Dense Select Structural", "2\" - 4\" wide", 2700, 1900, 175, 660, 2050, 1900000, 690000, 0.55),
        Row("SOUTHERN PINE", "Select Structural", "2\" - 4\" wide", 2350, 1650, 175, 565, 1900, 1800000, 660000, 0.55),
        Row("SOUTHERN PINE", "Non-Dense Select Structural", "2\" - 4\" wide", 2050, 1450, 175, 480, 1800, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.1 Dense", "2\" - 4\" wide", 1650, 1100, 175, 660, 1750, 1800000, 660000, 0.55),
        Row("SOUTHERN PINE", "No.1", "2\" - 4\" wide", 1500, 1000, 175, 565, 1650, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.1 Non-Dense", "2\" - 4\" wide", 1300, 875, 175, 480, 1550, 1400000, 510000, 0.55),
        Row("SOUTHERN PINE", "No.2 Dense", "2\" - 4\" wide", 1200, 750, 175, 660, 1500, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.2", "2\" - 4\" wide", 1100, 675, 175, 565, 1450, 1400000, 510000, 0.55),
        Row("SOUTHERN PINE", "No.2 Non-Dense", "2\" - 4\" wide", 1050, 600, 175, 480, 1450, 1300000, 470000, 0.55),
        Row("SOUTHERN PINE", "No.3 and Stud", "2\" - 4\" wide", 650, 400, 175, 565, 850, 1300000, 470000, 0.55),
        Row("SOUTHERN PINE", "Construction", "4\" wide", 875, 500, 175, 565, 1600, 1400000, 510000, 0.55),
        Row("SOUTHERN PINE", "Standard", "4\" wide", 475, 275, 175, 565, 1300, 1200000, 440000, 0.55),
        Row("SOUTHERN PINE", "Utility", "4\" wide", 225, 125, 175, 565, 850, 1200000, 440000, 0.55),
        Row("SOUTHERN PINE", "Dense Select Structural", "5\" - 6\" wide", 2400, 1650, 175, 660, 1900, 1900000, 690000, 0.55),
        Row("SOUTHERN PINE", "Select Structural", "5\" - 6\" wide", 2100, 1450, 175, 565, 1800, 1800000, 660000, 0.55),
        Row("SOUTHERN PINE", "Non-Dense Select Structural", "5\" - 6\" wide", 1850, 1300, 175, 480, 1700, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.1 Dense", "5\" - 6\" wide", 1500, 1000, 175, 660, 1650, 1800000, 660000, 0.55),
        Row("SOUTHERN PINE", "No.1", "5\" - 6\" wide", 1350, 875, 175, 565, 1550, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.1 Non-Dense", "5\" - 6\" wide", 1200, 775, 175, 480, 1450, 1400000, 510000, 0.55),
        Row("SOUTHERN PINE", "No.2 Dense", "5\" - 6\" wide", 1050, 650, 175, 660, 1450, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.2", "5\" - 6\" wide", 1000, 600, 175, 565, 1400, 1400000, 510000, 0.55),
        Row("SOUTHERN PINE", "No.2 Non-Dense", "5\" - 6\" wide", 950, 525, 175, 480, 1350, 1300000, 470000, 0.55),
        Row("SOUTHERN PINE", "No.3 and Stud", "5\" - 6\" wide", 575, 350, 175, 565, 800, 1300000, 470000, 0.55),
        Row("SOUTHERN PINE", "Dense Select Structural", "8\" wide", 2200, 1550, 175, 660, 1850, 1900000, 690000, 0.55),
        Row("SOUTHERN PINE", "Select Structural", "8\" wide", 1950, 1350, 175, 565, 1700, 1800000, 660000, 0.55),
        Row("SOUTHERN PINE", "Non-Dense Select Structural", "8\" wide", 1700, 1200, 175, 480, 1650, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.1 Dense", "8\" wide", 1350, 900, 175, 660, 1600, 1800000, 660000, 0.55),
        Row("SOUTHERN PINE", "No.1", "8\" wide", 1250, 800, 175, 565, 1500, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.1 Non-Dense", "8\" wide", 1100, 700, 175, 480, 1400, 1400000, 510000, 0.55),
        Row("SOUTHERN PINE", "No.2 Dense", "8\" wide", 975, 600, 175, 660, 1400, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.2", "8\" wide", 925, 550, 175, 565, 1350, 1400000, 510000, 0.55),
        Row("SOUTHERN PINE", "No.2 Non-Dense", "8\" wide", 875, 500, 175, 480, 1300, 1300000, 470000, 0.55),
        Row("SOUTHERN PINE", "No.3 and Stud", "8\" wide", 525, 325, 175, 565, 775, 1300000, 470000, 0.55),
        Row("SOUTHERN PINE", "Dense Select Structural", "10\" wide", 1950, 1300, 175, 660, 1800, 1900000, 690000, 0.55),
        Row("SOUTHERN PINE", "Select Structural", "10\" wide", 1700, 1150, 175, 565, 1650, 1800000, 660000, 0.55),
        Row("SOUTHERN PINE", "Non-Dense Select Structural", "10\" wide", 1500, 1050, 175, 480, 1600, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.1 Dense", "10\" wide", 1200, 800, 175, 660, 1550, 1800000, 660000, 0.55),
        Row("SOUTHERN PINE", "No.1", "10\" wide", 1050, 700, 175, 565, 1450, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.1 Non-Dense", "10\" wide", 950, 625, 175, 480, 1400, 1400000, 510000, 0.55),
        Row("SOUTHERN PINE", "No.2 Dense", "10\" wide", 850, 525, 175, 660, 1350, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.2", "10\" wide", 800, 475, 175, 565, 1300, 1400000, 510000, 0.55),
        Row("SOUTHERN PINE", "No.2 Non-Dense", "10\" wide", 750, 425, 175, 480, 1250, 1300000, 470000, 0.55),
        Row("SOUTHERN PINE", "No.3 and Stud", "10\" wide", 475, 275, 175, 565, 750, 1300000, 470000, 0.55),
        Row("SOUTHERN PINE", "Dense Select Structural", "12\" wide", 1800, 1250, 175, 660, 1750, 1900000, 690000, 0.55),
        Row("SOUTHERN PINE", "Select Structural", "12\" wide", 1600, 1100, 175, 565, 1650, 1800000, 660000, 0.55),
        Row("SOUTHERN PINE", "Non-Dense Select Structural", "12\" wide", 1400, 975, 175, 480, 1550, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.1 Dense", "12\" wide", 1100, 750, 175, 660, 1500, 1800000, 660000, 0.55),
        Row("SOUTHERN PINE", "No.1", "12\" wide", 1000, 650, 175, 565, 1400, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.1 Non-Dense", "12\" wide", 900, 575, 175, 480, 1350, 1400000, 510000, 0.55),
        Row("SOUTHERN PINE", "No.2 Dense", "12\" wide", 800, 500, 175, 660, 1300, 1600000, 580000, 0.55),
        Row("SOUTHERN PINE", "No.2", "12\" wide", 750, 450, 175, 565, 1250, 1400000, 510000, 0.55),
        Row("SOUTHERN PINE", "No.2 Non-Dense", "12\" wide", 700, 400, 175, 480, 1250, 1300000, 470000, 0.55),
        Row("SOUTHERN PINE", "No.3 and Stud", "12\" wide", 450, 250, 175, 565, 725, 1300000, 470000, 0.55),
        Row("MIXED SOUTHERN PINE", "Select Structural", "2\" - 4\" wide", 2050, 1200, 175, 565, 1800, 1600000, 580000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.1", "2\" - 4\" wide", 1450, 875, 175, 565, 1650, 1500000, 550000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.2", "2\" - 4\" wide", 1100, 675, 175, 565, 1450, 1400000, 510000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.3 and Stud", "2\" - 4\" wide", 650, 400, 175, 565, 850, 1200000, 440000, 0.51),
        Row("MIXED SOUTHERN PINE", "Construction", "4\" wide", 875, 500, 175, 565, 1600, 1300000, 470000, 0.51),
        Row("MIXED SOUTHERN PINE", "Standard", "4\" wide", 475, 275, 175, 565, 1300, 1200000, 440000, 0.51),
        Row("MIXED SOUTHERN PINE", "Utility", "4\" wide", 225, 125, 175, 565, 850, 1100000, 400000, 0.51),
        Row("MIXED SOUTHERN PINE", "Select Structural", "5\" - 6\" wide", 1850, 1100, 175, 565, 1700, 1600000, 580000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.1", "5\" - 6\" wide", 1300, 750, 175, 565, 1550, 1500000, 550000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.2", "5\" - 6\" wide", 1000, 600, 175, 565, 1400, 1400000, 510000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.3 and Stud", "5\" - 6\" wide", 575, 350, 175, 565, 800, 1200000, 440000, 0.51),
        Row("MIXED SOUTHERN PINE", "Select Structural", "8\" wide", 1750, 1000, 175, 565, 1600, 1600000, 580000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.1", "8\" wide", 1200, 700, 175, 565, 1450, 1500000, 550000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.2", "8\" wide", 925, 550, 175, 565, 1350, 1400000, 510000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.3 and Stud", "8\" wide", 525, 325, 175, 565, 775, 1200000, 440000, 0.51),
        Row("MIXED SOUTHERN PINE", "Select Structural", "10\" wide", 1500, 875, 175, 565, 1600, 1600000, 580000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.1", "10\" wide", 1050, 600, 175, 565, 1450, 1500000, 550000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.2", "10\" wide", 800, 475, 175, 565, 1300, 1400000, 510000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.3 and Stud", "10\" wide", 475, 275, 175, 565, 750, 1200000, 440000, 0.51),
        Row("MIXED SOUTHERN PINE", "Select Structural", "12\" wide", 1400, 825, 175, 565, 1550, 1600000, 580000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.1", "12\" wide", 975, 575, 175, 565, 1400, 1500000, 550000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.2", "12\" wide", 750, 450, 175, 565, 1250, 1400000, 510000, 0.51),
        Row("MIXED SOUTHERN PINE", "No.3 and Stud", "12\" wide", 450, 250, 175, 565, 725, 1200000, 440000, 0.51),
    )

    /** Species names exactly as printed in the table, in table order. */
    val species: List<String> = rows.map { it.species }.distinct()

    /** Printed width classifications in table order. */
    val widthClassifications: List<String> = rows.map { it.widthClassification }.distinct()

    fun find(species: String, grade: String, widthClassification: String): Row? =
        rows.firstOrNull { it.species == species && it.grade == grade && it.widthClassification == widthClassification }
}
