package com.lz.solver.material

/**
 * NDS 2024 Supplement, Table 5B: Reference Design Values for Structural Glued Laminated Softwood Timber (members
 * stressed primarily in axial tension or compression, or in bending with loads parallel to the wide faces; Table 5A
 * directs 2 and 3 lamination members to this table). Values are
 * for normal load duration and dry service, as printed (psi, and 10^6 psi for the moduli). The table is printed on
 * one page (84) as two sections, "Visually Graded Western Species" (17 combinations) and "Visually Graded Southern
 * Pine" (13). Extracted from the PDF's positioned words (the page is printed rotated) and independently from the text
 * layout; both agree on all 30 rows. Column qualifiers are exactly those printed in the header, checked against the
 * column positions: Ft is for 2 or more laminations; Fc is printed twice, for 4 or more laminations and for 2 or 3
 * laminations; Fby is printed three times (4 or more, 3 and 2 laminations); Fvy has no lamination qualifier in the
 * header (footnotes 1 and 2 reduce it for 2 or 3 laminations and for unbonded multi-piece laminations); Fbx is
 * printed for "2 Laminations to 15 in. Deep" (footnote 4: for members deeper than 15 in. Fbx is multiplied by 0.88).
 *
 * Footnotes as printed (paraphrased), none of them applied by the data: 1. Fvy x 0.84 (2 laminations) or 0.95 (3
 * laminations); 2. Fvy x 0.4 for 5, 7 or 9 laminations of multiple piece laminations that are not edge bonded, x 0.5
 * for other multiple piece laminations with unbonded edge joints, cumulative with 1 and 3; 3. Fvx and Fvy are
 * multiplied by the shear reduction factor Cvr of NDS 5.3.10; 4. Fbx x 0.88 over 15 in. deep; 5. (printed on
 * combination 22, Softwood Species SW) when Western Cedars, Western Cedars (North), Western Woods and Redwood (open
 * grain) are used in SW combinations E is reduced by 100,000 psi and Emin by 52,800 psi, and when Coast Sitka Spruce,
 * Coast Species, Western White Pine and Eastern White Pine are used Fvx and Fvy are reduced by 10 psi, before any
 * other adjustment; 6. Etrue is the shear-free modulus for deflection when shear deflection is considered
 * separately; 7. E is the apparent modulus used for beam deflection; 8. Emin is for beam and column stability.
 * The only footnote marker printed on a row is (5) on combination 22 ([Row.markers]).
 *
 * [Row.symbol] is kept as printed, including the two-part Southern Pine symbols such as "47 1:10" (the second part is
 * printed on a second line under the number). Species codes are as printed (DF, HF, SW, AC, POC, SP) and are not
 * expanded here because these pages do not define them.
 *
 * The Table 5B Adjustment Factors page (volume factor CV with x = 20 for Southern Pine and x = 10 otherwise, flat use
 * Cfu, wet service CM) is NOT part of this data. This is source data only: NDS 2024 stays blocked for design (see
 * [NdsCodeData]) until the design-value lookup, the 2024 adjustment-factor pages and the species mapping are
 * connected and verified.
 */
object Nds2024Table5B {

    data class Marker(val column: String, val footnotes: List<Int>)

    data class Row(
        val symbol: String,
        val species: String,
        val grade: String,
        /** Printed section heading the row sits under. */
        val section: String,
        val eTrue: Double, val e: Double, val eMin: Double,
        val fcPerp: Int,
        /** Tension parallel to grain, 2 or more laminations. */
        val ft: Int,
        /** Compression parallel to grain, 4 or more laminations. */
        val fcLam4Plus: Int,
        /** Compression parallel to grain, 2 or 3 laminations. */
        val fcLam2or3: Int,
        /** Bending about the y-y axis, 4 or more laminations. */
        val fbyLam4Plus: Int,
        val fbyLam3: Int,
        val fbyLam2: Int,
        /** Shear parallel to grain, y-y axis (no lamination qualifier is printed in the header; footnotes 1 to 3). */
        val fvy: Int,
        /** Bending about the x-x axis, printed for "2 Laminations to 15 in. Deep" (footnote 4 above 15 in.). */
        val fbxLam2To15InDeep: Int,
        val fvx: Int,
        val specificGravity: Double,
        val markers: List<Marker>
    )

    val rows: List<Row> = listOf(
        Row("1", "DF", "L3", "Visually Graded Western Species", 1.6, 1.5, 0.79, 560, 950, 1550, 1250, 1450, 1250, 1000, 230, 1250, 265, 0.5, emptyList()),
        Row("2", "DF", "L2", "Visually Graded Western Species", 1.7, 1.6, 0.85, 560, 1250, 1950, 1600, 1800, 1600, 1300, 230, 1700, 265, 0.5, emptyList()),
        Row("3", "DF", "L2D", "Visually Graded Western Species", 2.0, 1.9, 1.0, 650, 1450, 2300, 1900, 2100, 1850, 1550, 230, 2000, 265, 0.5, emptyList()),
        Row("4", "DF", "L1CL", "Visually Graded Western Species", 2.0, 1.9, 1.0, 590, 1400, 2100, 1950, 2200, 2000, 1650, 230, 2100, 265, 0.5, emptyList()),
        Row("5", "DF", "L1", "Visually Graded Western Species", 2.1, 2.0, 1.06, 650, 1650, 2400, 2100, 2400, 2100, 1800, 230, 2200, 265, 0.5, emptyList()),
        Row("14", "HF", "L3", "Visually Graded Western Species", 1.4, 1.3, 0.69, 375, 800, 1100, 1050, 1200, 1050, 850, 190, 1100, 215, 0.43, emptyList()),
        Row("15", "HF", "L2", "Visually Graded Western Species", 1.5, 1.4, 0.74, 375, 1050, 1350, 1350, 1500, 1350, 1100, 190, 1450, 215, 0.43, emptyList()),
        Row("16", "HF", "L1", "Visually Graded Western Species", 1.7, 1.6, 0.85, 375, 1200, 1500, 1500, 1750, 1550, 1300, 190, 1600, 215, 0.43, emptyList()),
        Row("17", "HF", "L1D", "Visually Graded Western Species", 1.8, 1.7, 0.9, 500, 1400, 1750, 1750, 2000, 1850, 1550, 190, 1900, 215, 0.43, emptyList()),
        Row("22", "SW", "L3", "Visually Graded Western Species", 1.1, 1.0, 0.53, 315, 525, 850, 725, 800, 700, 575, 170, 725, 195, 0.35, listOf(Marker("label", listOf(5)))),
        Row("69", "AC", "L3", "Visually Graded Western Species", 1.3, 1.2, 0.63, 470, 725, 1150, 1100, 1100, 975, 775, 230, 1000, 265, 0.46, emptyList()),
        Row("70", "AC", "L2", "Visually Graded Western Species", 1.4, 1.3, 0.69, 470, 975, 1450, 1450, 1400, 1250, 1000, 230, 1350, 265, 0.46, emptyList()),
        Row("71", "AC", "L1D", "Visually Graded Western Species", 1.7, 1.6, 0.85, 560, 1250, 1900, 1900, 1850, 1650, 1400, 230, 1750, 265, 0.46, emptyList()),
        Row("72", "AC", "L1S", "Visually Graded Western Species", 1.7, 1.6, 0.85, 560, 1250, 1900, 1900, 1850, 1650, 1400, 230, 1900, 265, 0.46, emptyList()),
        Row("73", "POC", "L3", "Visually Graded Western Species", 1.4, 1.3, 0.69, 470, 775, 1500, 1200, 1200, 1050, 825, 230, 1050, 265, 0.46, emptyList()),
        Row("74", "POC", "L2", "Visually Graded Western Species", 1.5, 1.4, 0.74, 470, 1050, 1900, 1550, 1450, 1300, 1100, 230, 1400, 265, 0.46, emptyList()),
        Row("75", "POC", "L1D", "Visually Graded Western Species", 1.8, 1.7, 0.9, 560, 1350, 2300, 2050, 1950, 1750, 1500, 230, 1850, 265, 0.46, emptyList()),
        Row("47", "SP", "N2M12", "Visually Graded Southern Pine", 1.5, 1.4, 0.74, 650, 1200, 1900, 1150, 1750, 1550, 1300, 260, 1400, 300, 0.55, emptyList()),
        Row("47 1:10", "SP", "N2M10", "Visually Graded Southern Pine", 1.5, 1.4, 0.74, 650, 1150, 1700, 1150, 1750, 1550, 1300, 260, 1400, 300, 0.55, emptyList()),
        Row("47 1:8", "SP", "N2M", "Visually Graded Southern Pine", 1.5, 1.4, 0.74, 650, 1000, 1500, 1150, 1600, 1550, 1300, 260, 1400, 300, 0.55, emptyList()),
        Row("48", "SP", "N2D12", "Visually Graded Southern Pine", 1.8, 1.7, 0.9, 740, 1400, 2200, 1350, 2000, 1800, 1500, 260, 1600, 300, 0.55, emptyList()),
        Row("48 1:10", "SP", "N2D10", "Visually Graded Southern Pine", 1.8, 1.7, 0.9, 740, 1350, 2000, 1350, 2000, 1800, 1500, 260, 1600, 300, 0.55, emptyList()),
        Row("48 1:8", "SP", "N2D", "Visually Graded Southern Pine", 1.8, 1.7, 0.9, 740, 1150, 1750, 1350, 1850, 1800, 1500, 260, 1600, 300, 0.55, emptyList()),
        Row("49", "SP", "N1M16", "Visually Graded Southern Pine", 1.8, 1.7, 0.9, 650, 1350, 2100, 1450, 1950, 1750, 1500, 260, 1800, 300, 0.55, emptyList()),
        Row("49 1:14", "SP", "N1M14", "Visually Graded Southern Pine", 1.8, 1.7, 0.9, 650, 1350, 2000, 1450, 1950, 1750, 1500, 260, 1800, 300, 0.55, emptyList()),
        Row("49 1:12", "SP", "N1M12", "Visually Graded Southern Pine", 1.8, 1.7, 0.9, 650, 1300, 1900, 1450, 1950, 1750, 1500, 260, 1800, 300, 0.55, emptyList()),
        Row("49 1:10", "SP", "N1M", "Visually Graded Southern Pine", 1.8, 1.7, 0.9, 650, 1150, 1700, 1450, 1850, 1750, 1500, 260, 1800, 300, 0.55, emptyList()),
        Row("50", "SP", "N1D14", "Visually Graded Southern Pine", 2.0, 1.9, 1.0, 740, 1550, 2300, 1700, 2300, 2100, 1750, 260, 2100, 300, 0.55, emptyList()),
        Row("50 1:12", "SP", "N1D12", "Visually Graded Southern Pine", 2.0, 1.9, 1.0, 740, 1500, 2200, 1700, 2300, 2100, 1750, 260, 2100, 300, 0.55, emptyList()),
        Row("50 1:10", "SP", "N1D", "Visually Graded Southern Pine", 2.0, 1.9, 1.0, 740, 1350, 2000, 1700, 2100, 2100, 1750, 260, 2100, 300, 0.55, emptyList()),
    )

    /** The symbol is not unique across sections only if printed so; it is unique in this table. */
    fun find(symbol: String): Row? = rows.firstOrNull { it.symbol == symbol }
}
