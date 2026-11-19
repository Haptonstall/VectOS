package com.lz.solver.material

/**
 * NDS 2024 Supplement, Table 5D: Reference Design Values for Structural Glued Laminated Hardwood Timber (members
 * stressed primarily in axial tension or compression, or in bending with loads parallel to the wide faces; Table 5C
 * directs members of 2 or 3 laminations to this table). Values are for normal load duration and dry service, as printed (psi, and 10^6 psi for
 * E). Two printed sections: "Visually Graded Hardwoods" (H1-H16, grades N3, N2, N1, SS) and "E-Rated Hardwoods"
 * (H17-H37, grades such as 1.5E3 and 2.0E6), with species groups A to D. The groups and the E-rated grade codes are
 * not defined on these pages and are kept as printed. Extracted from the PDF's positioned words (the pages are printed
 * rotated) and independently from the text layout; both agree on all 37 rows.
 *
 * Column qualifiers are those printed in the header, checked against the column positions: Ft is for 2 or more
 * laminations; Fc is printed for 4 or more laminations and for 2 or 3 laminations; Fby for 4 or more, 3 and 2
 * laminations; Fvy for 4 or more laminations; Fbx is printed for "2 Laminations to 15 in. Deep". There is a single E
 * and Emin (no separate shear-free modulus column).
 *
 * Footnotes as printed (paraphrased), not applied by the data: 1. Fvy x 0.84 (2 laminations) or 0.95 (3
 * laminations); 2. Fvy x 0.4 for 5, 7 or 9 laminations of multiple piece laminations that are not edge bonded, x 0.5 for
 * other multiple piece laminations with unbonded edge joints, cumulative with 1; 3. Fbx x 0.88 for members deeper
 * than 15 in. No footnote marker is printed on any row.
 *
 * The Table 5D Adjustment Factors page (volume factor CV with x = 10, flat use Cfu, wet service CM) is NOT part of
 * this data. This is source data only: NDS 2024 stays blocked for design (see [NdsCodeData]) until the design-value
 * lookup, the 2024 adjustment-factor pages and the species mapping are connected and verified.
 */
object Nds2024Table5D {

    data class Row(
        val symbol: String,
        val speciesGroup: String,
        val grade: String,
        val section: String,
        val e: Double, val eMin: Double,
        val fcPerp: Int,
        /** Tension parallel to grain, 2 or more laminations. */
        val ft: Int,
        /** Compression parallel to grain, 4 or more laminations. */
        val fcLam4Plus: Int,
        /** Compression parallel to grain, 2 or 3 laminations. */
        val fcLam2or3: Int,
        val fbyLam4Plus: Int,
        val fbyLam3: Int,
        val fbyLam2: Int,
        /** Shear parallel to grain, y-y axis, printed for 4 or more laminations (footnotes 1 and 2 adjust it). */
        val fvy: Int,
        /** Bending about the x-x axis, printed for "2 Laminations to 15 in. Deep" (footnote 3 above 15 in.). */
        val fbxLam2To15InDeep: Int,
        val fvx: Int,
        val specificGravity: Double,
        val markers: List<Nds2024Table5B.Marker>
    )

    val rows: List<Row> = listOf(
        Row("H1", "A", "N3", "Visually Graded Hardwoods", 1.3, 0.67, 835, 425, 900, 900, 1250, 1100, 875, 175, 925, 200, 0.63, emptyList()),
        Row("H2", "A", "N2", "Visually Graded Hardwoods", 1.5, 0.78, 835, 875, 1300, 1300, 1700, 1550, 1300, 175, 1200, 200, 0.63, emptyList()),
        Row("H3", "A", "N1", "Visually Graded Hardwoods", 1.7, 0.88, 835, 1000, 1450, 1450, 2000, 1800, 1550, 175, 1600, 200, 0.63, emptyList()),
        Row("H4", "A", "SS", "Visually Graded Hardwoods", 1.7, 0.88, 835, 1150, 1600, 1600, 2000, 1850, 1600, 175, 1700, 200, 0.63, emptyList()),
        Row("H5", "B", "N3", "Visually Graded Hardwoods", 1.2, 0.62, 590, 350, 800, 800, 1050, 900, 750, 160, 750, 180, 0.53, emptyList()),
        Row("H6", "B", "N2", "Visually Graded Hardwoods", 1.3, 0.67, 590, 750, 1150, 1150, 1450, 1300, 1050, 160, 1000, 180, 0.53, emptyList()),
        Row("H7", "B", "N1", "Visually Graded Hardwoods", 1.5, 0.78, 590, 850, 1300, 1300, 1650, 1500, 1300, 160, 1350, 180, 0.53, emptyList()),
        Row("H8", "B", "SS", "Visually Graded Hardwoods", 1.5, 0.78, 590, 950, 1450, 1450, 1700, 1550, 1350, 160, 1400, 180, 0.53, emptyList()),
        Row("H9", "C", "N3", "Visually Graded Hardwoods", 1.0, 0.52, 405, 300, 625, 625, 900, 800, 625, 135, 675, 155, 0.45, emptyList()),
        Row("H10", "C", "N2", "Visually Graded Hardwoods", 1.2, 0.62, 405, 625, 900, 900, 1200, 1100, 925, 135, 875, 155, 0.45, emptyList()),
        Row("H11", "C", "N1", "Visually Graded Hardwoods", 1.3, 0.67, 405, 725, 1000, 1000, 1400, 1300, 1100, 135, 1150, 155, 0.45, emptyList()),
        Row("H12", "C", "SS", "Visually Graded Hardwoods", 1.3, 0.67, 405, 825, 1100, 1100, 1450, 1350, 1150, 135, 1200, 155, 0.45, emptyList()),
        Row("H13", "D", "N3", "Visually Graded Hardwoods", 0.9, 0.47, 285, 250, 575, 575, 775, 675, 550, 110, 575, 125, 0.39, emptyList()),
        Row("H14", "D", "N2", "Visually Graded Hardwoods", 1.1, 0.57, 285, 550, 825, 825, 1050, 950, 800, 110, 750, 125, 0.39, emptyList()),
        Row("H15", "D", "N1", "Visually Graded Hardwoods", 1.2, 0.62, 285, 625, 925, 925, 1200, 1100, 950, 110, 1000, 125, 0.39, emptyList()),
        Row("H16", "D", "SS", "Visually Graded Hardwoods", 1.2, 0.62, 285, 700, 1050, 1050, 1250, 1150, 1000, 110, 1050, 125, 0.39, emptyList()),
        Row("H17", "A", "1.5E3", "E-Rated Hardwoods", 1.4, 0.73, 1015, 1000, 1500, 1350, 1850, 1750, 1550, 175, 1200, 200, 0.63, emptyList()),
        Row("H18", "A", "1.8E3", "E-Rated Hardwoods", 1.7, 0.88, 1015, 1150, 1950, 1850, 2100, 2000, 1750, 175, 1450, 200, 0.63, emptyList()),
        Row("H19", "A", "1.8E6", "E-Rated Hardwoods", 1.7, 0.88, 1015, 1450, 2000, 1900, 2300, 2200, 1950, 175, 1650, 200, 0.63, emptyList()),
        Row("H20", "A", "2.0E3", "E-Rated Hardwoods", 1.9, 0.98, 1015, 1350, 2600, 2200, 2400, 2300, 2100, 175, 1700, 200, 0.63, emptyList()),
        Row("H21", "A", "2.0E6", "E-Rated Hardwoods", 1.9, 0.98, 1015, 1700, 2430, 2300, 2400, 2400, 2300, 175, 2100, 200, 0.63, emptyList()),
        Row("H22", "B", "1.5E3", "E-Rated Hardwoods", 1.4, 0.73, 770, 1000, 1500, 1350, 1850, 1750, 1550, 160, 1200, 180, 0.53, emptyList()),
        Row("H23", "B", "1.8E3", "E-Rated Hardwoods", 1.7, 0.88, 770, 1150, 1950, 1850, 2100, 2000, 1750, 160, 1450, 180, 0.53, emptyList()),
        Row("H24", "B", "1.8E6", "E-Rated Hardwoods", 1.7, 0.88, 770, 1450, 2000, 1900, 2300, 2200, 1950, 160, 1650, 180, 0.53, emptyList()),
        Row("H25", "B", "2.0E3", "E-Rated Hardwoods", 1.9, 0.98, 770, 1350, 2300, 2200, 2400, 2300, 2100, 160, 1700, 180, 0.53, emptyList()),
        Row("H26", "B", "2.0E6", "E-Rated Hardwoods", 1.9, 0.98, 770, 1700, 2400, 2300, 2400, 2400, 2300, 160, 2100, 180, 0.53, emptyList()),
        Row("H27", "C", "1.5E3", "E-Rated Hardwoods", 1.4, 0.73, 590, 1000, 1500, 1350, 1850, 1750, 1550, 135, 1200, 155, 0.45, emptyList()),
        Row("H28", "C", "1.8E3", "E-Rated Hardwoods", 1.7, 0.88, 590, 1150, 1950, 1850, 2100, 2000, 1750, 135, 1450, 155, 0.45, emptyList()),
        Row("H29", "C", "1.8E6", "E-Rated Hardwoods", 1.7, 0.88, 590, 1450, 2000, 1900, 2300, 2200, 1950, 135, 1650, 155, 0.45, emptyList()),
        Row("H30", "C", "2.0E3", "E-Rated Hardwoods", 1.9, 0.98, 590, 1350, 2300, 2200, 2400, 2300, 2100, 135, 1700, 155, 0.45, emptyList()),
        Row("H31", "C", "2.0E6", "E-Rated Hardwoods", 1.9, 0.98, 590, 1700, 2400, 2300, 2400, 2400, 2300, 135, 2100, 155, 0.45, emptyList()),
        Row("H32", "D", "1.5E3", "E-Rated Hardwoods", 1.4, 0.73, 440, 1000, 1500, 1350, 1850, 1750, 1550, 110, 1200, 125, 0.39, emptyList()),
        Row("H33", "D", "1.5E6", "E-Rated Hardwoods", 1.4, 0.73, 440, 1250, 1500, 1400, 2000, 1900, 1700, 110, 1250, 125, 0.39, emptyList()),
        Row("H34", "D", "1.8E3", "E-Rated Hardwoods", 1.7, 0.88, 440, 1150, 1950, 1850, 2100, 2000, 1750, 110, 1450, 125, 0.39, emptyList()),
        Row("H35", "D", "1.8E6", "E-Rated Hardwoods", 1.7, 0.88, 440, 1450, 2000, 1900, 2300, 2200, 1950, 110, 1650, 125, 0.39, emptyList()),
        Row("H36", "D", "2.0E3", "E-Rated Hardwoods", 1.9, 0.98, 440, 1350, 2300, 2200, 2400, 2300, 2100, 110, 1700, 125, 0.39, emptyList()),
        Row("H37", "D", "2.0E6", "E-Rated Hardwoods", 1.9, 0.98, 440, 1700, 2400, 2300, 2400, 2400, 2300, 110, 2100, 125, 0.39, emptyList()),
    )

    fun find(symbol: String): Row? = rows.firstOrNull { it.symbol == symbol }
}
