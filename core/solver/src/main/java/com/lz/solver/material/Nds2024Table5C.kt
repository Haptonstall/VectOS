package com.lz.solver.material

/**
 * NDS 2024 Supplement, Table 5C: Reference Design Values for Structural Glued Laminated Hardwood Timber (members
 * stressed primarily in bending; for members of 4 or more laminations). Values are for normal load duration and dry
 * service, as printed (psi, and 10^6 psi for E). Two printed sections: "Visually Graded Hardwoods" (12 combinations) and
 * "E-Rated Hardwoods" (9). Extracted from the PDF's positioned words (the page is printed rotated) and independently
 * from the text layout; both agree on all 21 rows.
 *
 * Column meaning as printed: Fbx+ bending about x-x with the bottom of the beam stressed in tension (positive
 * bending), Fbx- with the top in tension (negative bending); [Row.fcx] and [Row.fcy] are compression perpendicular to
 * grain for x-x and y-y bending (printed Fcx, Fcy; values are tabulated for loading both perpendicular and parallel
 * to the wide faces); [Row.fvx] and [Row.fvy] shear parallel to grain (printed with the
 * header "(Horizontal)"); Ex and Ey are for deflection, Ex min and Ey min for stability; Ft and Fc are axial.
 * Unlike Tables 5A and 5B there is no shear-free (true) modulus column.
 *
 * Footnotes as printed (paraphrased): 1. the combinations apply to members of 4 or more laminations and are intended
 * for bending with loads perpendicular to the wide faces; Table 5D gives combinations for axial members, for
 * loading parallel to the wide faces and for 2 or 3 laminations; 2. the values apply to timbers with
 * laminations of a single piece across the width or edge-bonded pieces; for unbonded multiple piece laminations
 * multiply by 0.4 (5, 7 or 9 laminations) or 0.5 (other members); 3. (on the fastener specific gravity) the values
 * are for the group of hardwood species permitted in the combination; the actual species' value may be used when
 * known. The data does not apply these.
 *
 * [Row.speciesCode] is the code printed after the symbol on three E-rated rows (24F-E3 "YP", 24F-E4 "RM", 24F-E5
 * "RO"); these pages do not define the codes and they are not expanded here.
 *
 * The Table 5C Adjustment Factors page (volume factor CV with x = 10, flat use Cfu, wet service CM) is NOT part of
 * this data. This is source data only: NDS 2024 stays blocked for design (see [NdsCodeData]) until the design-value
 * lookup, the 2024 adjustment-factor pages and the species mapping are connected and verified.
 */
object Nds2024Table5C {

    data class Marker(val column: String, val footnotes: List<Int>)

    data class Row(
        val symbol: String,
        val speciesCode: String?,
        val section: String,
        val fbxPos: Int, val fbxNeg: Int, val fcx: Int, val fvx: Int,
        val ex: Double, val exMin: Double,
        val fby: Int, val fcy: Int, val fvy: Int,
        val ey: Double, val eyMin: Double,
        val ft: Int, val fc: Int,
        val specificGravity: Double,
        val markers: List<Marker>
    )

    val rows: List<Row> = listOf(
        Row("12F-V1", null, "Visually Graded Hardwoods", 1200, 600, 285, 125, 1.2, 0.63, 1050, 285, 110, 1.0, 0.53, 600, 800, 0.39, emptyList()),
        Row("12F-V2", null, "Visually Graded Hardwoods", 1200, 1200, 285, 125, 1.2, 0.63, 1050, 285, 110, 1.1, 0.58, 625, 860, 0.39, emptyList()),
        Row("14F-V1", null, "Visually Graded Hardwoods", 1400, 700, 405, 155, 1.3, 0.69, 1250, 405, 135, 1.1, 0.58, 700, 950, 0.45, emptyList()),
        Row("14F-V2", null, "Visually Graded Hardwoods", 1400, 700, 590, 180, 1.3, 0.69, 1450, 590, 160, 1.1, 0.58, 750, 1200, 0.53, emptyList()),
        Row("14F-V3", null, "Visually Graded Hardwoods", 1400, 1400, 405, 155, 1.3, 0.69, 1250, 405, 135, 1.1, 0.58, 725, 950, 0.45, emptyList()),
        Row("14F-V4", null, "Visually Graded Hardwoods", 1400, 1400, 590, 180, 1.3, 0.69, 1450, 590, 160, 1.1, 0.58, 775, 1200, 0.53, emptyList()),
        Row("16F-V1", null, "Visually Graded Hardwoods", 1600, 800, 590, 180, 1.4, 0.74, 1400, 590, 160, 1.2, 0.63, 800, 1200, 0.53, emptyList()),
        Row("16F-V2", null, "Visually Graded Hardwoods", 1600, 800, 835, 200, 1.5, 0.79, 1700, 835, 175, 1.3, 0.69, 875, 1250, 0.63, emptyList()),
        Row("16F-V3", null, "Visually Graded Hardwoods", 1600, 1600, 590, 180, 1.4, 0.74, 1400, 590, 160, 1.2, 0.63, 850, 1200, 0.53, emptyList()),
        Row("16F-V4", null, "Visually Graded Hardwoods", 1600, 1600, 835, 200, 1.6, 0.85, 1700, 835, 175, 1.3, 0.69, 900, 1300, 0.63, emptyList()),
        Row("20F-V1", null, "Visually Graded Hardwoods", 2000, 1000, 835, 200, 1.7, 0.9, 1700, 835, 175, 1.4, 0.74, 975, 1400, 0.63, emptyList()),
        Row("20F-V2", null, "Visually Graded Hardwoods", 2000, 2000, 835, 200, 1.7, 0.9, 1700, 835, 175, 1.4, 0.74, 1000, 1400, 0.63, emptyList()),
        Row("16F-E1", null, "E-Rated Hardwoods", 1600, 800, 440, 125, 1.4, 0.74, 1250, 285, 110, 1.2, 0.63, 825, 975, 0.39, emptyList()),
        Row("16F-E2", null, "E-Rated Hardwoods", 1600, 1600, 440, 125, 1.4, 0.74, 1400, 285, 110, 1.2, 0.63, 900, 1000, 0.39, emptyList()),
        Row("20F-E1", null, "E-Rated Hardwoods", 2000, 1000, 590, 155, 1.6, 0.85, 1350, 405, 135, 1.3, 0.69, 950, 1050, 0.45, emptyList()),
        Row("20F-E2", null, "E-Rated Hardwoods", 2000, 2000, 590, 155, 1.6, 0.85, 1600, 405, 135, 1.3, 0.69, 1050, 1100, 0.45, emptyList()),
        Row("24F-E1", null, "E-Rated Hardwoods", 2400, 1200, 770, 180, 1.8, 0.95, 1550, 590, 160, 1.5, 0.79, 1050, 1400, 0.53, emptyList()),
        Row("24F-E2", null, "E-Rated Hardwoods", 2400, 2400, 770, 180, 1.8, 0.95, 1650, 590, 160, 1.5, 0.79, 1050, 1400, 0.53, emptyList()),
        Row("24F-E3", "YP", "E-Rated Hardwoods", 2400, 1200, 590, 155, 1.8, 0.95, 1450, 405, 135, 1.5, 0.79, 975, 1200, 0.45, emptyList()),
        Row("24F-E4", "RM", "E-Rated Hardwoods", 2400, 1200, 895, 220, 1.8, 0.95, 1650, 710, 195, 1.6, 0.85, 1050, 1350, 0.53, emptyList()),
        Row("24F-E5", "RO", "E-Rated Hardwoods", 2400, 1200, 1075, 235, 1.8, 0.95, 1700, 900, 205, 1.5, 0.79, 1100, 1450, 0.63, emptyList()),
    )

    fun find(symbol: String): Row? = rows.firstOrNull { it.symbol == symbol }
}
