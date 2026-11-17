package com.lz.solver.material

/**
 * NDS 2024 Supplement, Table 4E: Reference Design Values for Visually Graded Decking. Values are in psi, exactly as
 * printed, for normal load duration and dry service conditions unless [Row.surfacing] says otherwise. Checked by two
 * independent extractions of the PDF (a coordinate-based parse and a text-layout parse) that agree on all 74 rows:
 * species, grade name and all five values.
 *
 * Printed "-" entries (no value published) are null: Fb single member and Fc-perp are null for several species
 * (Balsam Fir, Eastern Hemlock-Tamarack, Eastern Spruce, Eastern White Pine, Northern Pine, Northern White Cedar,
 * and Fc-perp for Redwood Select/Commercial). A null is never to be replaced by another table's number: the design
 * code must treat it as missing data and block, or skip, that check.
 *
 * [Row.fbRepetitive] is the printed "(Fb)(Cr)" column: the repetitive-member factor is already included, so Cr
 * must not be applied again (Table 4E Adjustment Factors page). Fb values are already adjusted for flatwise use
 * (no Cfu). The size factor CF applies to Fb for 2" and 3" thick decking of all species except Redwood.
 *
 * The size classification column, G and the agency are merged cells of the printed table (the page geometry has no
 * border between the rows of one block). [Row.sizeClassification] therefore holds the printed lines of the block's
 * size cell joined with " / ", for example "2"-4" thick / 4"-12"wide" (spacing as printed); it is NOT split into a
 * per-row thickness and width. Redwood is the exception: its Select/Commercial pair and each of Deck Heart and
 * Deck Common have their own size cell. For Deck Heart and Deck Common the printed grade cell is one merged cell
 * ("Deck Heart", "and", "Deck Common"); the two names are assigned to the two rows by their printed alignment with
 * the row values.
 *
 * Douglas Fir-Larch, Hem-Fir and Western Cedars are printed as two blocks each (PLIB with "Dex" grade names and
 * 6"-8" wide, WWPA with "Selected"/"Commercial" grade names and 4"-12" wide), so rows are looked up by agency as
 * well. Spruce-Pine-Fir (South) lists NELMA and WWPA in one block. Southern Pine is printed as two blocks,
 * "(Surfaced dry - Used in dry service conditions - 19% or less moisture content)" and "(Surfaced Green - Used in any
 * service condition)" ([Row.surfacing]); the Adjustment Factors page says to use the surfaced green values for wet
 * service without further adjustment.
 *
 * Footnote 2: when species are combined, use the lowest value of each property (a design rule, not data).
 *
 * This is source data only: NDS 2024 stays blocked for design (see [NdsCodeData]) until the design-value lookup,
 * the 2024 adjustment-factor pages and the species mapping are connected and verified.
 */
object Nds2024Table4E {

    data class Row(
        val species: String,
        val grade: String,
        /** The printed size cell of the block, lines joined with " / ". */
        val sizeClassification: String,
        /** Printed surfacing/service note (Southern Pine only), else null. */
        val surfacing: String?,
        /** Printed Fb for single-member use, or null where the table prints a dash. */
        val fbSingle: Int?,
        /** Printed (Fb)(Cr): repetitive-member Fb with Cr already applied. */
        val fbRepetitive: Int,
        /** Compression perpendicular to grain, or null where the table prints a dash. */
        val fcPerp: Int?,
        val e: Int,
        val eMin: Int,
        val specificGravity: Double,
        val gradingRulesAgencies: List<String>
    )

    val rows: List<Row> = listOf(
        Row("BALSAM FIR", "Select", "2\"-4\" thick / 4\"-12\"wide", null, null, 1650, null, 1500000, 550000, 0.36, listOf("NELMA")),
        Row("BALSAM FIR", "Commercial", "2\"-4\" thick / 4\"-12\"wide", null, null, 1400, null, 1300000, 470000, 0.36, listOf("NELMA")),
        Row("COAST SITKA SPRUCE", "Select", "2\"-4\" thick / 4\"& wider", null, 1250, 1450, 455, 1700000, 620000, 0.43, listOf("NLGA")),
        Row("COAST SITKA SPRUCE", "Commercial", "2\"-4\" thick / 4\"& wider", null, 1050, 1200, 455, 1500000, 550000, 0.43, listOf("NLGA")),
        Row("COAST SPECIES", "Select", "2\"-4\" thick / 4\"& wider", null, 1250, 1450, 370, 1500000, 550000, 0.43, listOf("NLGA")),
        Row("COAST SPECIES", "Commercial", "2\"-4\" thick / 4\"& wider", null, 1050, 1200, 370, 1400000, 510000, 0.43, listOf("NLGA")),
        Row("DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH", "Select Dex", "2\"-4\" thick / 6\"-8\"wide", null, 1750, 2000, 625, 1800000, 660000, 0.5, listOf("PLIB")),
        Row("DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH", "Commercial Dex", "2\"-4\" thick / 6\"-8\"wide", null, 1450, 1650, 625, 1700000, 620000, 0.5, listOf("PLIB")),
        Row("DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH", "Selected", "2\"-4\" thick / 4\"-12\"wide", null, 1750, 2000, 625, 1800000, 660000, 0.5, listOf("WWPA")),
        Row("DOUGLAS FIR-LARCH, DOUGLAS FIR, WESTERN LARCH", "Commercial", "2\"-4\" thick / 4\"-12\"wide", null, 1450, 1650, 625, 1700000, 620000, 0.5, listOf("WWPA")),
        Row("DOUGLAS FIR-LARCH (NORTH), DOUGLAS FIR (NORTH), WESTERN LARCH (NORTH)", "Select", "2\"-4\" thick / 4\"& wider", null, 1750, 2000, 625, 1800000, 660000, 0.49, listOf("NLGA")),
        Row("DOUGLAS FIR-LARCH (NORTH), DOUGLAS FIR (NORTH), WESTERN LARCH (NORTH)", "Commercial", "2\"-4\" thick / 4\"& wider", null, 1450, 1650, 625, 1700000, 620000, 0.49, listOf("NLGA")),
        Row("DOUGLAS FIR-SOUTH", "Selected", "2\"-4\" thick / 4\"-12\"wide", null, 1650, 1900, 520, 1400000, 510000, 0.46, listOf("WWPA")),
        Row("DOUGLAS FIR-SOUTH", "Commercial", "2\"-4\" thick / 4\"-12\"wide", null, 1400, 1600, 520, 1300000, 470000, 0.46, listOf("WWPA")),
        Row("EASTERN HEMLOCK-TAMARACK", "Select", "2\"-4\" thick / 4\"-12\"wide", null, null, 1700, null, 1300000, 470000, 0.41, listOf("NELMA")),
        Row("EASTERN HEMLOCK-TAMARACK", "Commercial", "2\"-4\" thick / 4\"-12\"wide", null, null, 1450, null, 1100000, 400000, 0.41, listOf("NELMA")),
        Row("EASTERN HEMLOCK-TAMARACK (NORTH)", "Select", "2\"-4\" thick / 4\"& wider", null, 1500, 1700, 555, 1300000, 470000, 0.47, listOf("NLGA")),
        Row("EASTERN HEMLOCK-TAMARACK (NORTH)", "Commercial", "2\"-4\" thick / 4\"& wider", null, 1250, 1450, 555, 1100000, 400000, 0.47, listOf("NLGA")),
        Row("EASTERN SPRUCE", "Select", "2\"-4\" thick / 4\"-12\"wide", null, null, 1300, null, 1500000, 550000, 0.41, listOf("NELMA")),
        Row("EASTERN SPRUCE", "Commercial", "2\"-4\" thick / 4\"-12\"wide", null, null, 1100, null, 1400000, 510000, 0.41, listOf("NELMA")),
        Row("EASTERN WHITE PINE", "Select", "2\"-4\" thick / 4\"-12\"wide", null, null, 1300, null, 1200000, 440000, 0.36, listOf("NELMA")),
        Row("EASTERN WHITE PINE", "Commercial", "2\"-4\" thick / 4\"-12\"wide", null, null, 1100, null, 1100000, 400000, 0.36, listOf("NELMA")),
        Row("EASTERN WHITE PINE (NORTH)", "Select", "2\"-4\" thick / 4\"& wider", null, 900, 1050, 350, 1200000, 440000, 0.38, listOf("NLGA")),
        Row("EASTERN WHITE PINE (NORTH)", "Commercial", "2\"-4\" thick / 4\"& wider", null, 775, 875, 350, 1100000, 400000, 0.38, listOf("NLGA")),
        Row("HEM-FIR", "Select Dex", "2\"-4\" thick / 6\"-8\"wide", null, 1400, 1600, 405, 1500000, 550000, 0.43, listOf("PLIB")),
        Row("HEM-FIR", "Commercial Dex", "2\"-4\" thick / 6\"-8\"wide", null, 1150, 1350, 405, 1400000, 510000, 0.43, listOf("PLIB")),
        Row("HEM-FIR", "Selected", "2\"-4\" thick / 4\"-12\"wide", null, 1400, 1600, 405, 1500000, 550000, 0.43, listOf("WWPA")),
        Row("HEM-FIR", "Commercial", "2\"-4\" thick / 4\"-12\"wide", null, 1150, 1350, 405, 1400000, 510000, 0.43, listOf("WWPA")),
        Row("HEM-FIR (NORTH)", "Select", "2\"-4\" thick / 4\"& wider", null, 1350, 1500, 405, 1500000, 550000, 0.46, listOf("NLGA")),
        Row("HEM-FIR (NORTH)", "Commercial", "2\"-4\" thick / 4\"& wider", null, 1100, 1300, 405, 1400000, 510000, 0.46, listOf("NLGA")),
        Row("NORTHERN PINE", "Select", "2\"-4\" thick / 4\"-12\"wide", null, null, 1550, null, 1400000, 510000, 0.42, listOf("NELMA")),
        Row("NORTHERN PINE", "Commercial", "2\"-4\" thick / 4\"-12\"wide", null, null, 1300, null, 1300000, 470000, 0.42, listOf("NELMA")),
        Row("NORTHERN SPECIES", "Select", "2\"-4\" thick / 4\"& wider", null, 900, 1050, 350, 1100000, 400000, 0.35, listOf("NLGA")),
        Row("NORTHERN SPECIES", "Commercial", "2\"-4\" thick / 4\"& wider", null, 775, 875, 350, 1000000, 370000, 0.35, listOf("NLGA")),
        Row("NORTHERN WHITE CEDAR", "Select", "2\"-4\" thick / 4\"-12\"wide", null, null, 1100, null, 800000, 290000, 0.31, listOf("NELMA")),
        Row("NORTHERN WHITE CEDAR", "Commercial", "2\"-4\" thick / 4\"-12\"wide", null, null, 950, null, 700000, 260000, 0.31, listOf("NELMA")),
        Row("PONDEROSA PINE", "Select", "2\"-4\" thick / 4\"& wider", null, 1200, 1400, 535, 1300000, 470000, 0.43, listOf("NLGA")),
        Row("PONDEROSA PINE", "Commercial", "2\"-4\" thick / 4\"& wider", null, 1000, 1150, 535, 1100000, 400000, 0.43, listOf("NLGA")),
        Row("RED PINE", "Select", "2\"-4\" thick / 4\"& wider", null, 1150, 1350, 440, 1300000, 470000, 0.44, listOf("NLGA")),
        Row("RED PINE", "Commercial", "2\"-4\" thick / 4\"& wider", null, 975, 1100, 440, 1200000, 440000, 0.44, listOf("NLGA")),
        Row("REDWOOD", "Select", "2\" thick / 6\"& wider", null, 1450, 1700, null, 1100000, 400000, 0.37, listOf("RIS")),
        Row("REDWOOD", "Commercial", "2\" thick / 6\"& wider", null, 1200, 1350, null, 1000000, 370000, 0.37, listOf("RIS")),
        Row("REDWOOD", "Deck Heart", "2\" thick / 4\" wide", null, 400, 450, 420, 900000, 330000, 0.37, listOf("RIS")),
        Row("REDWOOD", "Deck Common", "2\" thick / 6\" wide", null, 700, 800, 420, 900000, 330000, 0.37, listOf("RIS")),
        Row("SITKA SPRUCE", "Select Dex", "2\"-4\" thick / 6\"-8\"wide", null, 1300, 1500, 435, 1500000, 550000, 0.43, listOf("PLIB")),
        Row("SITKA SPRUCE", "Commercial Dex", "2\"-4\" thick / 6\"-8\"wide", null, 1100, 1250, 435, 1300000, 470000, 0.43, listOf("PLIB")),
        Row("SOUTHERN PINE", "Dense Standard", "2\"-4\" thick / 2\" & wider", "(Surfaced dry – Used in dry service conditions — 19% or less moisture content)", 2000, 2300, 660, 1800000, 660000, 0.55, listOf("SPIB")),
        Row("SOUTHERN PINE", "Dense Select", "2\"-4\" thick / 2\" & wider", "(Surfaced dry – Used in dry service conditions — 19% or less moisture content)", 1650, 1900, 660, 1600000, 580000, 0.55, listOf("SPIB")),
        Row("SOUTHERN PINE", "Select", "2\"-4\" thick / 2\" & wider", "(Surfaced dry – Used in dry service conditions — 19% or less moisture content)", 1400, 1650, 565, 1600000, 580000, 0.55, listOf("SPIB")),
        Row("SOUTHERN PINE", "Dense Commercial", "2\"-4\" thick / 2\" & wider", "(Surfaced dry – Used in dry service conditions — 19% or less moisture content)", 1650, 1900, 660, 1600000, 580000, 0.55, listOf("SPIB")),
        Row("SOUTHERN PINE", "Commercial", "2\"-4\" thick / 2\" & wider", "(Surfaced dry – Used in dry service conditions — 19% or less moisture content)", 1400, 1650, 565, 1600000, 580000, 0.55, listOf("SPIB")),
        Row("SOUTHERN PINE", "Dense Standard", "2-1/2\"-4\" thick / 2\" & wider", "(Surfaced Green – Used in any service condition)", 1600, 1800, 440, 1600000, 580000, 0.55, listOf("SPIB")),
        Row("SOUTHERN PINE", "Dense Select", "2-1/2\"-4\" thick / 2\" & wider", "(Surfaced Green – Used in any service condition)", 1350, 1500, 440, 1400000, 510000, 0.55, listOf("SPIB")),
        Row("SOUTHERN PINE", "Select", "2-1/2\"-4\" thick / 2\" & wider", "(Surfaced Green – Used in any service condition)", 1150, 1300, 375, 1400000, 510000, 0.55, listOf("SPIB")),
        Row("SOUTHERN PINE", "Dense Commercial", "2-1/2\"-4\" thick / 2\" & wider", "(Surfaced Green – Used in any service condition)", 1350, 1500, 440, 1400000, 510000, 0.55, listOf("SPIB")),
        Row("SOUTHERN PINE", "Commercial", "2-1/2\"-4\" thick / 2\" & wider", "(Surfaced Green – Used in any service condition)", 1150, 1300, 375, 1400000, 510000, 0.55, listOf("SPIB")),
        Row("SPRUCE-PINE-FIR", "Select", "2\"-4\" thick / 4\"& wider", null, 1200, 1400, 425, 1500000, 550000, 0.42, listOf("NLGA")),
        Row("SPRUCE-PINE-FIR", "Commercial", "2\"-4\" thick / 4\"& wider", null, 1000, 1150, 425, 1300000, 470000, 0.42, listOf("NLGA")),
        Row("SPRUCE-PINE-FIR (SOUTH)", "Selected", "2\"-4\" thick / 4\"-12\"wide", null, 1150, 1350, 335, 1400000, 510000, 0.36, listOf("NELMA", "WWPA")),
        Row("SPRUCE-PINE-FIR (SOUTH)", "Commercial", "2\"-4\" thick / 4\"-12\"wide", null, 950, 1100, 335, 1200000, 440000, 0.36, listOf("NELMA", "WWPA")),
        Row("WESTERN CEDARS", "Select Dex", "2\"-4\" thick / 6\"-8\"wide", null, 1250, 1450, 425, 1100000, 400000, 0.36, listOf("PLIB")),
        Row("WESTERN CEDARS", "Commercial Dex", "2\"-4\" thick / 6\"-8\"wide", null, 1050, 1200, 425, 1000000, 370000, 0.36, listOf("PLIB")),
        Row("WESTERN CEDARS", "Selected", "2\"-4\" thick / 4\"-12\"wide", null, 1250, 1450, 425, 1100000, 400000, 0.36, listOf("WWPA")),
        Row("WESTERN CEDARS", "Commercial", "2\"-4\" thick / 4\"-12\"wide", null, 1050, 1200, 425, 1000000, 370000, 0.36, listOf("WWPA")),
        Row("WESTERN CEDARS (NORTH)", "Select", "2\"-4\" thick / 4\"& wider", null, 1200, 1400, 425, 1100000, 400000, 0.35, listOf("NLGA")),
        Row("WESTERN CEDARS (NORTH)", "Commercial", "2\"-4\" thick / 4\"& wider", null, 1050, 1200, 425, 1000000, 370000, 0.35, listOf("NLGA")),
        Row("WESTERN HEMLOCK", "Select Dex", "2\"-4\" thick / 6\"& wider", null, 1500, 1750, 410, 1600000, 580000, 0.47, listOf("PLIB")),
        Row("WESTERN HEMLOCK", "Commercial Dex", "2\"-4\" thick / 6\"& wider", null, 1300, 1450, 410, 1400000, 510000, 0.47, listOf("PLIB")),
        Row("WESTERN HEMLOCK (NORTH)", "Select", "2\"-4\" thick / 4\"& wider", null, 1500, 1750, 410, 1600000, 580000, 0.46, listOf("NLGA")),
        Row("WESTERN HEMLOCK (NORTH)", "Commercial", "2\"-4\" thick / 4\"& wider", null, 1300, 1450, 410, 1400000, 510000, 0.46, listOf("NLGA")),
        Row("WESTERN WHITE PINE", "Select", "2\"-4\" thick / 4\"& wider", null, 1100, 1300, 375, 1400000, 510000, 0.4, listOf("NLGA")),
        Row("WESTERN WHITE PINE", "Commercial", "2\"-4\" thick / 4\"& wider", null, 925, 1050, 375, 1300000, 470000, 0.4, listOf("NLGA")),
        Row("WESTERN WOODS", "Selected", "2\"-4\" thick / 4\"-12\"wide", null, 1150, 1300, 335, 1200000, 440000, 0.36, listOf("WWPA")),
        Row("WESTERN WOODS", "Commercial", "2\"-4\" thick / 4\"-12\"wide", null, 950, 1100, 335, 1100000, 400000, 0.36, listOf("WWPA")),
    )

    /** Species names exactly as printed, in table order. */
    val species: List<String> = rows.map { it.species }.distinct()

    fun find(species: String, grade: String, agency: String, surfacing: String? = null): Row? =
        rows.firstOrNull {
            it.species == species && it.grade == grade && agency in it.gradingRulesAgencies &&
                (surfacing == null || it.surfacing?.contains(surfacing, ignoreCase = true) == true)
        }
}
