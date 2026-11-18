package com.lz.solver.material

/**
 * NDS 2024 Supplement, Table 4F: Reference Design Values for Non-North American Visually Graded Dimension Lumber
 * (2" - 4" thick). Values are in psi, exactly as printed, for normal load duration and dry service conditions.
 * Checked by two independent extractions of the PDF (a coordinate-based parse and a text-layout parse) that agree on
 * all 144 rows (18 species/country blocks of 8 grades): grade name and all seven values, and the species/country
 * headers.
 *
 * Column order is as printed: Fb, Ft, Fv, Fc-perp, Fc, E, Emin. [Row.species] is the printed block heading, including
 * the country or region (the same species appears for several countries with different values), kept as printed
 * (for example "SILVER FIR (Abies alba) - Germany. NE France, & Switzerland").
 *
 * [Row.footnotes] holds the footnote numbers printed on the block heading:
 *  - 2 (Douglas Fir/European Larch): the values apply only to 2x4 dimension lumber and shall not be multiplied by the
 *    size factor CF (footnote 2 of the table). Design code must enforce this; the data does not.
 *  - 4 (Scots Pine - Germany): "Does not include states of Baden-Wurttemburg and Saarland".
 * Footnote 3 (combining species uses the lowest value of each property) is a design rule, not data.
 *
 * The size classification column is merged per group of rows in the printed table: Select Structural, No. 1, No. 2
 * and No. 3 share one cell, Stud has its own, and Construction, Standard and Utility share one. It is stored per
 * row as printed ("2" & wider" or "2" - 4" wide"). Specific gravity and agency are one printed cell per block.
 * The Table 4F Adjustment Factors page (size factor CF, repetitive member Cr, flat use Cfu, wet service CM) is NOT
 * part of this data and is applied by the design code (page 57 repeats the dimension-lumber factors).
 *
 * This is source data only: NDS 2024 stays blocked for design (see [NdsCodeData]) until the design-value lookup,
 * the 2024 adjustment-factor pages and the species mapping are connected and verified.
 */
object Nds2024Table4F {

    data class Row(
        /** Printed block heading: species and country/region. */
        val species: String,
        /** Footnote numbers printed on the heading (see the class documentation). */
        val footnotes: List<Int>,
        val grade: String,
        val sizeClassification: String,
        val fb: Int,
        val ft: Int,
        val fv: Int,
        val fcPerp: Int,
        val fc: Int,
        val e: Int,
        val eMin: Int,
        val specificGravity: Double,
        val gradingRulesAgencies: List<String>
    )

    val rows: List<Row> = listOf(
        Row("AUSTRIAN SPRUCE - Austria & The Czech Republic", emptyList(), "Select Structural", "2\" & wider", 1500, 675, 175, 260, 1250, 1700000, 620000, 0.43, listOf("PLIB")),
        Row("AUSTRIAN SPRUCE - Austria & The Czech Republic", emptyList(), "No. 1", "2\" & wider", 1000, 450, 175, 260, 1100, 1600000, 580000, 0.43, listOf("PLIB")),
        Row("AUSTRIAN SPRUCE - Austria & The Czech Republic", emptyList(), "No. 2", "2\" & wider", 925, 400, 175, 260, 1050, 1500000, 550000, 0.43, listOf("PLIB")),
        Row("AUSTRIAN SPRUCE - Austria & The Czech Republic", emptyList(), "No. 3", "2\" & wider", 525, 225, 175, 260, 625, 1300000, 470000, 0.43, listOf("PLIB")),
        Row("AUSTRIAN SPRUCE - Austria & The Czech Republic", emptyList(), "Stud", "2\" & wider", 725, 325, 175, 260, 675, 1300000, 470000, 0.43, listOf("PLIB")),
        Row("AUSTRIAN SPRUCE - Austria & The Czech Republic", emptyList(), "Construction", "2\" - 4\" wide", 1050, 475, 175, 260, 1300, 1400000, 510000, 0.43, listOf("PLIB")),
        Row("AUSTRIAN SPRUCE - Austria & The Czech Republic", emptyList(), "Standard", "2\" - 4\" wide", 575, 250, 175, 260, 1100, 1300000, 470000, 0.43, listOf("PLIB")),
        Row("AUSTRIAN SPRUCE - Austria & The Czech Republic", emptyList(), "Utility", "2\" - 4\" wide", 275, 125, 175, 260, 725, 1200000, 440000, 0.43, listOf("PLIB")),
        Row("DOUGLAS FIR - France & Germany", emptyList(), "Select Structural", "2\" & wider", 1500, 675, 205, 540, 1250, 1900000, 690000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR - France & Germany", emptyList(), "No. 1", "2\" & wider", 975, 450, 205, 540, 1100, 1700000, 620000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR - France & Germany", emptyList(), "No. 2", "2\" & wider", 825, 375, 205, 540, 1000, 1500000, 550000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR - France & Germany", emptyList(), "No. 3", "2\" & wider", 475, 225, 205, 540, 600, 1300000, 470000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR - France & Germany", emptyList(), "Stud", "2\" & wider", 650, 300, 205, 540, 650, 1300000, 470000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR - France & Germany", emptyList(), "Construction", "2\" - 4\" wide", 925, 425, 205, 540, 1250, 1400000, 510000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR - France & Germany", emptyList(), "Standard", "2\" - 4\" wide", 525, 225, 205, 540, 1050, 1300000, 470000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR - France & Germany", emptyList(), "Utility", "2\" - 4\" wide", 250, 100, 205, 540, 675, 1200000, 440000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR/EUROPEAN LARCH - Austria, The Czech Republic, & Bavaria", listOf(2), "Select Structural", "2\" - 4\" wide", 1900, 850, 195, 440, 1400, 1800000, 660000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR/EUROPEAN LARCH - Austria, The Czech Republic, & Bavaria", listOf(2), "No. 1", "2\" - 4\" wide", 1400, 625, 195, 440, 1250, 1700000, 620000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR/EUROPEAN LARCH - Austria, The Czech Republic, & Bavaria", listOf(2), "No. 2", "2\" - 4\" wide", 1350, 600, 195, 440, 1250, 1600000, 580000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR/EUROPEAN LARCH - Austria, The Czech Republic, & Bavaria", listOf(2), "No. 3", "2\" - 4\" wide", 775, 350, 195, 440, 700, 1400000, 510000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR/EUROPEAN LARCH - Austria, The Czech Republic, & Bavaria", listOf(2), "Stud", "2\" - 4\" wide", 800, 350, 195, 440, 700, 1400000, 510000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR/EUROPEAN LARCH - Austria, The Czech Republic, & Bavaria", listOf(2), "Construction", "2\" - 4\" wide", 1000, 450, 195, 440, 1250, 1500000, 550000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR/EUROPEAN LARCH - Austria, The Czech Republic, & Bavaria", listOf(2), "Standard", "2\" - 4\" wide", 575, 250, 195, 440, 1100, 1300000, 470000, 0.48, listOf("PLIB")),
        Row("DOUGLAS FIR/EUROPEAN LARCH - Austria, The Czech Republic, & Bavaria", listOf(2), "Utility", "2\" - 4\" wide", 275, 125, 195, 440, 700, 1300000, 470000, 0.48, listOf("PLIB")),
        Row("MONTANE PINE - South Africa", emptyList(), "Select Structural", "2\" & wider", 975, 425, 135, 325, 1100, 1300000, 470000, 0.45, listOf("PLIB")),
        Row("MONTANE PINE - South Africa", emptyList(), "No. 1", "2\" & wider", 650, 300, 135, 325, 950, 1100000, 400000, 0.45, listOf("PLIB")),
        Row("MONTANE PINE - South Africa", emptyList(), "No. 2", "2\" & wider", 600, 275, 135, 325, 850, 1000000, 370000, 0.45, listOf("PLIB")),
        Row("MONTANE PINE - South Africa", emptyList(), "No. 3", "2\" & wider", 350, 150, 135, 325, 475, 900000, 330000, 0.45, listOf("PLIB")),
        Row("MONTANE PINE - South Africa", emptyList(), "Stud", "2\" & wider", 475, 200, 135, 325, 525, 900000, 330000, 0.45, listOf("PLIB")),
        Row("MONTANE PINE - South Africa", emptyList(), "Construction", "2\" - 4\" wide", 675, 300, 135, 325, 1050, 900000, 330000, 0.45, listOf("PLIB")),
        Row("MONTANE PINE - South Africa", emptyList(), "Standard", "2\" - 4\" wide", 375, 175, 135, 325, 875, 800000, 290000, 0.45, listOf("PLIB")),
        Row("MONTANE PINE - South Africa", emptyList(), "Utility", "2\" - 4\" wide", 175, 75, 135, 325, 575, 800000, 290000, 0.45, listOf("PLIB")),
        Row("NORWAY SPRUCE - Estonia, Latvia, & Lithuania", emptyList(), "Select Structural", "2\" & wider", 1200, 550, 150, 430, 1200, 1600000, 580000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Estonia, Latvia, & Lithuania", emptyList(), "No. 1", "2\" & wider", 850, 375, 150, 430, 1050, 1400000, 510000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Estonia, Latvia, & Lithuania", emptyList(), "No. 2", "2\" & wider", 800, 350, 150, 430, 1000, 1300000, 470000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Estonia, Latvia, & Lithuania", emptyList(), "No. 3", "2\" & wider", 450, 200, 150, 430, 575, 1100000, 400000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Estonia, Latvia, & Lithuania", emptyList(), "Stud", "2\" & wider", 625, 275, 150, 430, 625, 1100000, 400000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Estonia, Latvia, & Lithuania", emptyList(), "Construction", "2\" - 4\" wide", 900, 400, 150, 430, 1200, 1200000, 440000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Estonia, Latvia, & Lithuania", emptyList(), "Standard", "2\" - 4\" wide", 500, 225, 150, 430, 1050, 1100000, 400000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Estonia, Latvia, & Lithuania", emptyList(), "Utility", "2\" - 4\" wide", 250, 100, 150, 430, 675, 1000000, 370000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Finland", emptyList(), "Select Structural", "2\" & wider", 1350, 600, 125, 220, 1200, 1500000, 550000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Finland", emptyList(), "No. 1", "2\" & wider", 850, 375, 125, 220, 1000, 1400000, 510000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Finland", emptyList(), "No. 2", "2\" & wider", 625, 275, 125, 220, 875, 1200000, 440000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Finland", emptyList(), "No. 3", "2\" & wider", 375, 175, 125, 220, 500, 1100000, 400000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Finland", emptyList(), "Stud", "2\" & wider", 575, 250, 125, 220, 600, 1100000, 400000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Finland", emptyList(), "Construction", "2\" - 4\" wide", 725, 325, 125, 220, 1100, 1100000, 400000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Finland", emptyList(), "Standard", "2\" - 4\" wide", 400, 175, 125, 220, 900, 1000000, 370000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Finland", emptyList(), "Utility", "2\" - 4\" wide", 200, 75, 125, 220, 600, 1000000, 370000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Germany, NE France, & Switzerland", emptyList(), "Select Structural", "2\" & wider", 1200, 550, 170, 355, 1200, 1600000, 580000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Germany, NE France, & Switzerland", emptyList(), "No. 1", "2\" & wider", 825, 375, 170, 355, 1050, 1400000, 510000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Germany, NE France, & Switzerland", emptyList(), "No. 2", "2\" & wider", 725, 325, 170, 355, 950, 1200000, 440000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Germany, NE France, & Switzerland", emptyList(), "No. 3", "2\" & wider", 425, 200, 170, 355, 550, 1100000, 400000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Germany, NE France, & Switzerland", emptyList(), "Stud", "2\" & wider", 575, 250, 170, 355, 600, 1100000, 400000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Germany, NE France, & Switzerland", emptyList(), "Construction", "2\" - 4\" wide", 825, 375, 170, 355, 1200, 1100000, 400000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Germany, NE France, & Switzerland", emptyList(), "Standard", "2\" - 4\" wide", 475, 200, 170, 355, 975, 1000000, 370000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Germany, NE France, & Switzerland", emptyList(), "Utility", "2\" - 4\" wide", 225, 100, 170, 355, 650, 900000, 330000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Norway", emptyList(), "Select Structural", "2\" & wider", 1550, 700, 115, 360, 1250, 1800000, 660000, 0.43, listOf("PLIB")),
        Row("NORWAY SPRUCE - Norway", emptyList(), "No. 1", "2\" & wider", 1000, 450, 115, 360, 1100, 1500000, 550000, 0.43, listOf("PLIB")),
        Row("NORWAY SPRUCE - Norway", emptyList(), "No. 2", "2\" & wider", 800, 375, 115, 360, 1000, 1300000, 470000, 0.43, listOf("PLIB")),
        Row("NORWAY SPRUCE - Norway", emptyList(), "No. 3", "2\" & wider", 475, 200, 115, 360, 575, 1200000, 440000, 0.43, listOf("PLIB")),
        Row("NORWAY SPRUCE - Norway", emptyList(), "Stud", "2\" & wider", 625, 275, 115, 360, 650, 1200000, 440000, 0.43, listOf("PLIB")),
        Row("NORWAY SPRUCE - Norway", emptyList(), "Construction", "2\" - 4\" wide", 925, 425, 115, 360, 1250, 1200000, 440000, 0.43, listOf("PLIB")),
        Row("NORWAY SPRUCE - Norway", emptyList(), "Standard", "2\" - 4\" wide", 525, 225, 115, 360, 1050, 1100000, 400000, 0.43, listOf("PLIB")),
        Row("NORWAY SPRUCE - Norway", emptyList(), "Utility", "2\" - 4\" wide", 250, 100, 115, 360, 675, 1100000, 370000, 0.43, listOf("PLIB")),
        Row("NORWAY SPRUCE - Romania & Ukraine", emptyList(), "Select Structural", "2\" & wider", 1250, 575, 100, 275, 1200, 1500000, 550000, 0.38, listOf("PLIB")),
        Row("NORWAY SPRUCE - Romania & Ukraine", emptyList(), "No. 1", "2\" & wider", 850, 375, 100, 275, 1050, 1400000, 510000, 0.38, listOf("PLIB")),
        Row("NORWAY SPRUCE - Romania & Ukraine", emptyList(), "No. 2", "2\" & wider", 750, 325, 100, 275, 950, 1200000, 440000, 0.38, listOf("PLIB")),
        Row("NORWAY SPRUCE - Romania & Ukraine", emptyList(), "No. 3", "2\" & wider", 425, 200, 100, 275, 550, 1100000, 400000, 0.38, listOf("PLIB")),
        Row("NORWAY SPRUCE - Romania & Ukraine", emptyList(), "Stud", "2\" & wider", 575, 250, 100, 275, 600, 1100000, 400000, 0.38, listOf("PLIB")),
        Row("NORWAY SPRUCE - Romania & Ukraine", emptyList(), "Construction", "2\" - 4\" wide", 850, 375, 100, 275, 1200, 1100000, 400000, 0.38, listOf("PLIB")),
        Row("NORWAY SPRUCE - Romania & Ukraine", emptyList(), "Standard", "2\" - 4\" wide", 475, 200, 100, 275, 1000, 1000000, 370000, 0.38, listOf("PLIB")),
        Row("NORWAY SPRUCE - Romania & Ukraine", emptyList(), "Utility", "2\" - 4\" wide", 225, 100, 100, 275, 650, 1000000, 370000, 0.38, listOf("PLIB")),
        Row("NORWAY SPRUCE - Sweden", emptyList(), "Select Structural", "2\" & wider", 1250, 550, 170, 285, 1200, 1600000, 580000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Sweden", emptyList(), "No. 1", "2\" & wider", 825, 375, 170, 285, 1050, 1400000, 510000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Sweden", emptyList(), "No. 2", "2\" & wider", 675, 300, 170, 285, 925, 1200000, 440000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Sweden", emptyList(), "No. 3", "2\" & wider", 400, 175, 170, 285, 525, 1100000, 400000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Sweden", emptyList(), "Stud", "2\" & wider", 550, 250, 170, 285, 575, 1100000, 400000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Sweden", emptyList(), "Construction", "2\" - 4\" wide", 775, 350, 170, 285, 1150, 1200000, 440000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Sweden", emptyList(), "Standard", "2\" - 4\" wide", 425, 200, 170, 285, 950, 1100000, 400000, 0.42, listOf("PLIB")),
        Row("NORWAY SPRUCE - Sweden", emptyList(), "Utility", "2\" - 4\" wide", 200, 100, 170, 285, 625, 1000000, 370000, 0.42, listOf("PLIB")),
        Row("SCOTS PINE - Austria & The Czech Republic, Romania, & Ukraine", emptyList(), "Select Structural", "2\" & wider", 1300, 600, 135, 270, 1200, 1700000, 620000, 0.5, listOf("PLIB")),
        Row("SCOTS PINE - Austria & The Czech Republic, Romania, & Ukraine", emptyList(), "No. 1", "2\" & wider", 900, 400, 135, 270, 1050, 1600000, 580000, 0.5, listOf("PLIB")),
        Row("SCOTS PINE - Austria & The Czech Republic, Romania, & Ukraine", emptyList(), "No. 2", "2\" & wider", 775, 350, 135, 270, 1000, 1400000, 510000, 0.5, listOf("PLIB")),
        Row("SCOTS PINE - Austria & The Czech Republic, Romania, & Ukraine", emptyList(), "No. 3", "2\" & wider", 450, 200, 135, 270, 575, 1300000, 470000, 0.5, listOf("PLIB")),
        Row("SCOTS PINE - Austria & The Czech Republic, Romania, & Ukraine", emptyList(), "Stud", "2\" & wider", 600, 275, 135, 270, 625, 1300000, 470000, 0.5, listOf("PLIB")),
        Row("SCOTS PINE - Austria & The Czech Republic, Romania, & Ukraine", emptyList(), "Construction", "2\" - 4\" wide", 875, 400, 135, 270, 1200, 1300000, 470000, 0.5, listOf("PLIB")),
        Row("SCOTS PINE - Austria & The Czech Republic, Romania, & Ukraine", emptyList(), "Standard", "2\" - 4\" wide", 500, 225, 135, 270, 1000, 1200000, 440000, 0.5, listOf("PLIB")),
        Row("SCOTS PINE - Austria & The Czech Republic, Romania, & Ukraine", emptyList(), "Utility", "2\" - 4\" wide", 225, 100, 135, 270, 675, 1100000, 400000, 0.5, listOf("PLIB")),
        Row("SCOTS PINE - Estonia, Latvia, & Lithuania", emptyList(), "Select Structural", "2\" & wider", 1150, 525, 130, 430, 1150, 1500000, 550000, 0.45, listOf("PLIB")),
        Row("SCOTS PINE - Estonia, Latvia, & Lithuania", emptyList(), "No. 1", "2\" & wider", 800, 350, 130, 430, 1050, 1400000, 510000, 0.45, listOf("PLIB")),
        Row("SCOTS PINE - Estonia, Latvia, & Lithuania", emptyList(), "No. 2", "2\" & wider", 750, 325, 130, 430, 975, 1200000, 440000, 0.45, listOf("PLIB")),
        Row("SCOTS PINE - Estonia, Latvia, & Lithuania", emptyList(), "No. 3", "2\" & wider", 425, 200, 130, 430, 550, 1100000, 400000, 0.45, listOf("PLIB")),
        Row("SCOTS PINE - Estonia, Latvia, & Lithuania", emptyList(), "Stud", "2\" & wider", 575, 275, 130, 430, 625, 1100000, 400000, 0.45, listOf("PLIB")),
        Row("SCOTS PINE - Estonia, Latvia, & Lithuania", emptyList(), "Construction", "2\" - 4\" wide", 850, 375, 130, 430, 1200, 1100000, 400000, 0.45, listOf("PLIB")),
        Row("SCOTS PINE - Estonia, Latvia, & Lithuania", emptyList(), "Standard", "2\" - 4\" wide", 475, 225, 130, 430, 1000, 1000000, 370000, 0.45, listOf("PLIB")),
        Row("SCOTS PINE - Estonia, Latvia, & Lithuania", emptyList(), "Utility", "2\" - 4\" wide", 225, 100, 130, 430, 650, 1000000, 370000, 0.45, listOf("PLIB")),
        Row("SCOTS PINE - Finland", emptyList(), "Select Structural", "2\" & wider", 1300, 600, 150, 210, 1200, 1500000, 550000, 0.48, listOf("PLIB")),
        Row("SCOTS PINE - Finland", emptyList(), "No. 1", "2\" & wider", 950, 425, 150, 210, 1100, 1400000, 510000, 0.48, listOf("PLIB")),
        Row("SCOTS PINE - Finland", emptyList(), "No. 2", "2\" & wider", 925, 425, 150, 210, 1100, 1300000, 470000, 0.48, listOf("PLIB")),
        Row("SCOTS PINE - Finland", emptyList(), "No. 3", "2\" & wider", 525, 250, 150, 210, 625, 1200000, 440000, 0.48, listOf("PLIB")),
        Row("SCOTS PINE - Finland", emptyList(), "Stud", "2\" & wider", 725, 325, 150, 210, 675, 1200000, 440000, 0.48, listOf("PLIB")),
        Row("SCOTS PINE - Finland", emptyList(), "Construction", "2\" - 4\" wide", 1050, 475, 150, 210, 1300, 1200000, 440000, 0.48, listOf("PLIB")),
        Row("SCOTS PINE - Finland", emptyList(), "Standard", "2\" - 4\" wide", 600, 275, 150, 210, 1100, 1100000, 400000, 0.48, listOf("PLIB")),
        Row("SCOTS PINE - Finland", emptyList(), "Utility", "2\" - 4\" wide", 275, 125, 150, 210, 725, 1000000, 370000, 0.48, listOf("PLIB")),
        Row("SCOTS PINE - Germany", listOf(4), "Select Structural", "2\" & wider", 1200, 550, 160, 395, 1200, 1600000, 580000, 0.53, listOf("PLIB")),
        Row("SCOTS PINE - Germany", listOf(4), "No. 1", "2\" & wider", 800, 375, 160, 395, 1050, 1400000, 510000, 0.53, listOf("PLIB")),
        Row("SCOTS PINE - Germany", listOf(4), "No. 2", "2\" & wider", 700, 325, 160, 395, 950, 1100000, 400000, 0.53, listOf("PLIB")),
        Row("SCOTS PINE - Germany", listOf(4), "No. 3", "2\" & wider", 400, 175, 160, 395, 550, 1000000, 370000, 0.53, listOf("PLIB")),
        Row("SCOTS PINE - Germany", listOf(4), "Stud", "2\" & wider", 550, 250, 160, 395, 600, 1000000, 370000, 0.53, listOf("PLIB")),
        Row("SCOTS PINE - Germany", listOf(4), "Construction", "2\" - 4\" wide", 800, 375, 160, 395, 1150, 1100000, 400000, 0.53, listOf("PLIB")),
        Row("SCOTS PINE - Germany", listOf(4), "Standard", "2\" - 4\" wide", 450, 200, 160, 395, 975, 1000000, 370000, 0.53, listOf("PLIB")),
        Row("SCOTS PINE - Germany", listOf(4), "Utility", "2\" - 4\" wide", 225, 100, 160, 395, 625, 900000, 330000, 0.53, listOf("PLIB")),
        Row("SCOTS PINE - Sweden", emptyList(), "Select Structural", "2\" & wider", 1350, 600, 120, 410, 1200, 1700000, 620000, 0.47, listOf("PLIB")),
        Row("SCOTS PINE - Sweden", emptyList(), "No. 1", "2\" & wider", 850, 375, 120, 410, 1000, 1500000, 550000, 0.47, listOf("PLIB")),
        Row("SCOTS PINE - Sweden", emptyList(), "No. 2", "2\" & wider", 625, 275, 120, 410, 875, 1300000, 470000, 0.47, listOf("PLIB")),
        Row("SCOTS PINE - Sweden", emptyList(), "No. 3", "2\" & wider", 375, 175, 120, 410, 500, 1200000, 440000, 0.47, listOf("PLIB")),
        Row("SCOTS PINE - Sweden", emptyList(), "Stud", "2\" & wider", 500, 225, 120, 410, 550, 1200000, 440000, 0.47, listOf("PLIB")),
        Row("SCOTS PINE - Sweden", emptyList(), "Construction", "2\" - 4\" wide", 725, 325, 120, 410, 1100, 1200000, 440000, 0.47, listOf("PLIB")),
        Row("SCOTS PINE - Sweden", emptyList(), "Standard", "2\" - 4\" wide", 400, 175, 120, 410, 900, 1100000, 400000, 0.47, listOf("PLIB")),
        Row("SCOTS PINE - Sweden", emptyList(), "Utility", "2\" - 4\" wide", 200, 75, 120, 410, 600, 1000000, 370000, 0.47, listOf("PLIB")),
        Row("SILVER FIR (Abies alba) - Germany. NE France, & Switzerland", emptyList(), "Select Structural", "2\" & wider", 950, 425, 125, 400, 1100, 1500000, 550000, 0.43, listOf("PLIB")),
        Row("SILVER FIR (Abies alba) - Germany. NE France, & Switzerland", emptyList(), "No. 1", "2\" & wider", 725, 325, 125, 400, 975, 1400000, 510000, 0.43, listOf("PLIB")),
        Row("SILVER FIR (Abies alba) - Germany. NE France, & Switzerland", emptyList(), "No. 2", "2\" & wider", 725, 325, 125, 400, 950, 1300000, 470000, 0.43, listOf("PLIB")),
        Row("SILVER FIR (Abies alba) - Germany. NE France, & Switzerland", emptyList(), "No. 3", "2\" & wider", 425, 200, 125, 400, 550, 1100000, 400000, 0.43, listOf("PLIB")),
        Row("SILVER FIR (Abies alba) - Germany. NE France, & Switzerland", emptyList(), "Stud", "2\" & wider", 575, 250, 125, 400, 600, 1100000, 400000, 0.43, listOf("PLIB")),
        Row("SILVER FIR (Abies alba) - Germany. NE France, & Switzerland", emptyList(), "Construction", "2\" - 4\" wide", 825, 375, 125, 400, 1150, 1200000, 440000, 0.43, listOf("PLIB")),
        Row("SILVER FIR (Abies alba) - Germany. NE France, & Switzerland", emptyList(), "Standard", "2\" - 4\" wide", 475, 200, 125, 400, 975, 1100000, 400000, 0.43, listOf("PLIB")),
        Row("SILVER FIR (Abies alba) - Germany. NE France, & Switzerland", emptyList(), "Utility", "2\" - 4\" wide", 225, 100, 125, 400, 650, 1000000, 370000, 0.43, listOf("PLIB")),
        Row("SOUTHERN PINE - Misiones Argentina", emptyList(), "Select Structural", "2\" & wider", 1100, 500, 150, 440, 1150, 1200000, 440000, 0.45, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina", emptyList(), "No. 1", "2\" & wider", 775, 350, 150, 440, 1000, 1100000, 400000, 0.45, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina", emptyList(), "No. 2", "2\" & wider", 725, 325, 150, 440, 950, 1100000, 400000, 0.45, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina", emptyList(), "No. 3", "2\" & wider", 425, 200, 150, 440, 550, 900000, 330000, 0.45, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina", emptyList(), "Stud", "2\" & wider", 575, 250, 150, 440, 600, 900000, 330000, 0.45, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina", emptyList(), "Construction", "2\" - 4\" wide", 825, 375, 150, 440, 1150, 1000000, 370000, 0.45, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina", emptyList(), "Standard", "2\" - 4\" wide", 475, 200, 150, 440, 975, 900000, 330000, 0.45, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina", emptyList(), "Utility", "2\" - 4\" wide", 225, 100, 150, 440, 650, 800000, 290000, 0.45, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina, Free of Heart Center and Medium Grain Density", emptyList(), "Select Structural", "2\" & wider", 1700, 775, 210, 710, 1250, 1500000, 550000, 0.54, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina, Free of Heart Center and Medium Grain Density", emptyList(), "No. 1", "2\" & wider", 1150, 525, 210, 710, 1150, 1500000, 550000, 0.54, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina, Free of Heart Center and Medium Grain Density", emptyList(), "No. 2", "2\" & wider", 1000, 450, 210, 710, 1100, 1500000, 550000, 0.54, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina, Free of Heart Center and Medium Grain Density", emptyList(), "No. 3", "2\" & wider", 575, 250, 210, 710, 650, 1400000, 510000, 0.54, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina, Free of Heart Center and Medium Grain Density", emptyList(), "Stud", "2\" & wider", 800, 350, 210, 710, 700, 1400000, 510000, 0.54, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina, Free of Heart Center and Medium Grain Density", emptyList(), "Construction", "2\" - 4\" wide", 1150, 525, 210, 710, 1350, 1400000, 510000, 0.54, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina, Free of Heart Center and Medium Grain Density", emptyList(), "Standard", "2\" - 4\" wide", 650, 300, 210, 710, 1150, 1300000, 470000, 0.54, listOf("SPIB")),
        Row("SOUTHERN PINE - Misiones Argentina, Free of Heart Center and Medium Grain Density", emptyList(), "Utility", "2\" - 4\" wide", 300, 125, 210, 710, 750, 1200000, 440000, 0.54, listOf("SPIB")),
    )

    /** Block headings exactly as printed, in table order. */
    val species: List<String> = rows.map { it.species }.distinct()

    fun find(species: String, grade: String): Row? = rows.firstOrNull { it.species == species && it.grade == grade }
}
