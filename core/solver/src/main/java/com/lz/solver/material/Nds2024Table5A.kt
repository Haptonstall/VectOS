package com.lz.solver.material

/**
 * NDS 2024 Supplement, Table 5A: Reference Design Values for Structural Glued Laminated Softwood Timber (members
 * stressed primarily in bending), with Table 5A Expanded (individual combinations). Values are for normal load
 * duration and dry service, as printed (psi, and 10^6 psi for E). Reference design values are for members with 4 or
 * more laminations (2 and 3 lamination members: Table 5B). Extracted from the PDF's positioned words (the pages are
 * printed rotated) and independently from the text layout; both agree on all 70 rows (7 stress classes in the basic
 * table, the same 7 as headers of the expanded table, and 56 combinations).
 *
 * Axes and columns: x-x is bending with load perpendicular to the wide faces of the laminations, y-y with load
 * parallel to the wide faces. Fbx+ is the bottom of the beam stressed in tension (positive bending), Fbx- the top of
 * the beam stressed in tension (negative bending). [StressClass.exTrue] and [StressClass.eyTrue] are the shear-free
 * (true) moduli for deflection when shear deflection is considered separately; [StressClass.ex]/[StressClass.ey] are
 * the apparent (reference) moduli; [StressClass.exMin]/[StressClass.eyMin] are for stability calculations.
 *
 * FLAGGED printed discrepancy: for 28F-2.1E SP and 30F-2.1E SP the minimum modulus Ex min is printed as 1.09 in the
 * basic table (page 80) and 1.11 in the expanded table (page 82), and the 28F/30F combinations in the expanded table
 * also print 1.11. Both are kept as printed ([stressClasses] has 1.09, [expandedStressClasses] has 1.11). The other
 * classes print Ex min at 0.527 to 0.531 of Ex, which matches 1.11; the choice of value for design is not made here.
 * FLAGGED printed irregularity: combination 20F-E7 HF/HF prints Ex true 1.8 against Ex 1.6 (every other row differs
 * by 0.1).
 *
 * Footnote markers are kept where they are printed ([Marker]; column "label" means the marker is on the stress class
 * or combination name). They matter for design and the data does not apply them: (basic table numbering) 2: Fbx- may
 * be increased to 1850 psi (Douglas Fir) or 1950 psi (Southern Pine) for specific combinations; 3: Southern Pine
 * shear Fvx/Fvy may be increased to 300/260 psi; 4: Fvx/Fvy are multiplied by the shear reduction factor Cvr (NDS
 * 5.3.10); 5: values apply to laminations of a single piece across the width or edge-bonded pieces, otherwise 0.4 (5,
 * 7 or 9 laminations) or 0.5 (other) cumulatively with Cvr; 6: Southern Pine wane reduces Fvx (0.67 wane on both
 * sides, 0.83 on one); 7: 26F/28F/30F availability limited; 8: 30F limited to 6 in. nominal width unless qualified;
 * 9: 28F and 30F with more than 15 laminations use Ex true 2.1, Ex 2.0, Ex min 1.06; 10: Southern Pine fastener
 * specific gravity may be 0.55. The expanded table has its own numbering (see its footnotes: 2 Cvr, 3 multi-piece
 * laminations, 4 wane, 5 availability, 6 30F width, 7 E for more than 15 laminations (Ex 2.0, Ex min 1.06)), so
 * [Marker.footnotes] numbers refer to the table the row was read from.
 *
 * Species codes in [Combination.outer] and [Combination.core] are kept as printed (DF, HF, SP, SPF, AC, POC, ES);
 * their meaning is not defined on these pages and is not expanded here. [Combination.printedUnder] is the stress
 * class heading that precedes the combination in the printed expanded table; it is print position only (for
 * example the 24F-E/SPF combinations are printed in the 20F-1.5E group).
 *
 * The Table 5A Adjustment Factors page (volume factor CV, flat use Cfu, wet service CM, Cvr) is NOT part of this data.
 * This is source data only: NDS 2024 stays blocked for design (see [NdsCodeData]) until the design-value lookup, the
 * 2024 adjustment-factor pages and the species mapping are connected and verified.
 */
object Nds2024Table5A {

    /** A footnote marker printed on a cell: [column] is a property name of the row or "label". */
    data class Marker(val column: String, val footnotes: List<Int>)

    data class StressClass(
        val name: String,
        /** True when the printed name carries the suffix "SP" (Southern Pine). */
        val southernPineSuffix: Boolean,
        val fbxPos: Int, val fbxNeg: Int, val fcPerpX: Int, val fvx: Int,
        val exTrue: Double, val ex: Double, val exMin: Double,
        val fby: Int, val fcPerpY: Int, val fvy: Int,
        val eyTrue: Double, val ey: Double, val eyMin: Double,
        val ft: Int, val fc: Int, val g: Double,
        val markers: List<Marker>
    )

    data class Combination(
        val printedUnder: String,
        val symbol: String,
        val outer: String,
        val core: String,
        val fbxPos: Int, val fbxNeg: Int, val fcPerpXTensionFace: Int, val fcPerpXCompressionFace: Int, val fvx: Int,
        val exTrue: Double, val ex: Double, val exMin: Double,
        val fby: Int, val fcPerpY: Int, val fvy: Int,
        val eyTrue: Double, val ey: Double, val eyMin: Double,
        val ft: Int, val fc: Int,
        val gTopBottomFace: Double, val gSideFace: Double,
        val markers: List<Marker>
    )

    /** The seven stress classes of the basic Table 5A (page 80). */
    val stressClasses: List<StressClass> = listOf(
        StressClass("16F-1.3E", false, 1600, 925, 315, 195, 1.4, 1.3, 0.69, 800, 315, 170, 1.2, 1.1, 0.58, 675, 925, 0.41, emptyList()),
        StressClass("20F-1.5E", false, 2000, 1100, 425, 195, 1.6, 1.5, 0.79, 800, 315, 170, 1.3, 1.2, 0.63, 725, 925, 0.41, listOf(Marker("fvx", listOf(6)))),
        StressClass("24F-1.7E", false, 2400, 1450, 500, 210, 1.8, 1.7, 0.9, 1050, 315, 185, 1.4, 1.3, 0.69, 775, 1000, 0.42, listOf(Marker("fvx", listOf(6)))),
        StressClass("24F-1.8E", false, 2400, 1450, 650, 265, 1.9, 1.8, 0.95, 1450, 560, 230, 1.7, 1.6, 0.85, 1100, 1600, 0.5, listOf(Marker("fbxNeg", listOf(2)), Marker("fvx", listOf(3)), Marker("fvy", listOf(3)), Marker("g", listOf(10)))),
        StressClass("26F-1.9E", false, 2600, 1950, 650, 265, 2.0, 1.9, 1.0, 1600, 560, 230, 1.7, 1.6, 0.85, 1150, 1600, 0.5, listOf(Marker("fvx", listOf(3)), Marker("fvy", listOf(3)), Marker("g", listOf(10)), Marker("label", listOf(7)))),
        StressClass("28F-2.1E", true, 2800, 2300, 805, 300, 2.2, 2.1, 1.09, 1600, 650, 260, 1.8, 1.7, 0.9, 1250, 1750, 0.55, listOf(Marker("label", listOf(7)), Marker("exTrue", listOf(9)), Marker("ex", listOf(9)), Marker("exMin", listOf(9)))),
        StressClass("30F-2.1E", true, 3000, 2400, 805, 300, 2.2, 2.1, 1.09, 1750, 650, 260, 1.8, 1.7, 0.9, 1250, 1750, 0.55, listOf(Marker("label", listOf(7, 8)), Marker("exTrue", listOf(9)), Marker("ex", listOf(9)), Marker("exMin", listOf(9)))),
    )

    /** The same seven stress classes as printed at the head of each group in Table 5A Expanded (pages 81-82). */
    val expandedStressClasses: List<StressClass> = listOf(
        StressClass("16F-1.3E", false, 1600, 925, 315, 195, 1.4, 1.3, 0.69, 800, 315, 170, 1.2, 1.1, 0.58, 675, 925, 0.41, emptyList()),
        StressClass("20F-1.5E", false, 2000, 1100, 425, 195, 1.6, 1.5, 0.79, 800, 315, 170, 1.3, 1.2, 0.63, 725, 925, 0.41, emptyList()),
        StressClass("24F-1.7E", false, 2400, 1450, 500, 210, 1.8, 1.7, 0.9, 1050, 315, 185, 1.4, 1.3, 0.69, 775, 1000, 0.42, emptyList()),
        StressClass("24F-1.8E", false, 2400, 1450, 650, 265, 1.9, 1.8, 0.95, 1450, 560, 230, 1.7, 1.6, 0.85, 1100, 1600, 0.5, emptyList()),
        StressClass("26F-1.9E", false, 2600, 1950, 650, 265, 2.0, 1.9, 1.0, 1600, 560, 230, 1.7, 1.6, 0.85, 1150, 1600, 0.5, listOf(Marker("label", listOf(5)))),
        StressClass("28F-2.1E", true, 2800, 2300, 805, 300, 2.2, 2.1, 1.11, 1600, 650, 260, 1.8, 1.7, 0.9, 1250, 1750, 0.55, listOf(Marker("exMin", listOf(7)), Marker("ex", listOf(7)), Marker("exTrue", listOf(7)), Marker("fbxPos", listOf(5)))),
        StressClass("30F-2.1E", true, 3000, 2400, 805, 300, 2.2, 2.1, 1.11, 1750, 650, 260, 1.8, 1.7, 0.9, 1250, 1750, 0.55, listOf(Marker("ex", listOf(7)), Marker("exTrue", listOf(7)), Marker("exMin", listOf(7)), Marker("fbxPos", listOf(5, 6)))),
    )

    val combinations: List<Combination> = listOf(
        Combination("16F-1.3E", "16F-V3", "DF", "DF", 1600, 1250, 560, 560, 265, 1.6, 1.5, 0.79, 1450, 560, 230, 1.6, 1.5, 0.79, 975, 1500, 0.5, 0.5, emptyList()),
        Combination("16F-1.3E", "16F-V6", "DF", "DF", 1600, 1600, 560, 560, 265, 1.7, 1.6, 0.85, 1450, 560, 230, 1.6, 1.5, 0.79, 1000, 1600, 0.5, 0.5, emptyList()),
        Combination("16F-1.3E", "16F-E2", "HF", "HF", 1600, 1050, 375, 375, 215, 1.5, 1.4, 0.74, 1200, 375, 190, 1.4, 1.3, 0.69, 825, 1150, 0.43, 0.43, emptyList()),
        Combination("16F-1.3E", "16F-E3", "DF", "DF", 1600, 1200, 560, 560, 265, 1.7, 1.6, 0.85, 1400, 560, 230, 1.6, 1.5, 0.79, 975, 1600, 0.5, 0.5, emptyList()),
        Combination("16F-1.3E", "16F-E6", "DF", "DF", 1600, 1600, 560, 560, 265, 1.7, 1.6, 0.85, 1550, 560, 230, 1.6, 1.5, 0.79, 1000, 1600, 0.5, 0.5, emptyList()),
        Combination("16F-1.3E", "16F-E7", "HF", "HF", 1600, 1600, 375, 375, 215, 1.5, 1.4, 0.74, 1350, 375, 190, 1.4, 1.3, 0.74, 875, 1250, 0.43, 0.43, emptyList()),
        Combination("16F-1.3E", "16F-V2", "SP", "SP", 1600, 1400, 740, 650, 300, 1.6, 1.5, 0.79, 1450, 650, 260, 1.5, 1.4, 0.74, 1000, 1300, 0.55, 0.55, emptyList()),
        Combination("16F-1.3E", "16F-V3", "SP", "SP", 1600, 1450, 740, 740, 300, 1.5, 1.4, 0.74, 1450, 650, 260, 1.5, 1.4, 0.74, 975, 1400, 0.55, 0.55, emptyList()),
        Combination("16F-1.3E", "16F-V5", "SP", "SP", 1600, 1600, 650, 650, 300, 1.7, 1.6, 0.85, 1600, 650, 260, 1.6, 1.5, 0.79, 1000, 1550, 0.55, 0.55, emptyList()),
        Combination("16F-1.3E", "16F-E1", "SP", "SP", 1600, 1250, 650, 650, 300, 1.7, 1.6, 0.85, 1400, 650, 260, 1.7, 1.6, 0.85, 1050, 1550, 0.55, 0.55, emptyList()),
        Combination("16F-1.3E", "16F-E3", "SP", "SP", 1600, 1600, 650, 650, 300, 1.8, 1.7, 0.9, 1650, 650, 260, 1.7, 1.6, 0.85, 1100, 1550, 0.55, 0.55, emptyList()),
        Combination("20F-1.5E", "20F-V3", "DF", "DF", 2000, 1450, 650, 560, 265, 1.7, 1.6, 0.85, 1450, 560, 230, 1.6, 1.5, 0.79, 1000, 1550, 0.5, 0.5, emptyList()),
        Combination("20F-1.5E", "20F-V7", "DF", "DF", 2000, 2000, 650, 650, 265, 1.7, 1.6, 0.85, 1450, 560, 230, 1.7, 1.6, 0.85, 1050, 1600, 0.5, 0.5, emptyList()),
        Combination("20F-1.5E", "20F-V12", "AC", "AC", 2000, 1400, 560, 560, 265, 1.6, 1.5, 0.79, 1250, 470, 230, 1.5, 1.4, 0.74, 925, 1500, 0.46, 0.46, emptyList()),
        Combination("20F-1.5E", "20F-V13", "AC", "AC", 2000, 2000, 560, 560, 265, 1.6, 1.5, 0.79, 1250, 470, 230, 1.5, 1.4, 0.74, 950, 1550, 0.46, 0.46, emptyList()),
        Combination("20F-1.5E", "20F-V14", "POC", "POC", 2000, 1450, 560, 560, 265, 1.6, 1.5, 0.79, 1300, 470, 230, 1.5, 1.4, 0.74, 900, 1600, 0.46, 0.46, emptyList()),
        Combination("20F-1.5E", "20F-V15", "POC", "POC", 2000, 2000, 560, 560, 265, 1.6, 1.5, 0.79, 1300, 470, 230, 1.5, 1.4, 0.74, 900, 1600, 0.46, 0.46, emptyList()),
        Combination("20F-1.5E", "20F-E2", "HF", "HF", 2000, 1400, 500, 500, 215, 1.7, 1.6, 0.85, 1200, 375, 190, 1.5, 1.4, 0.74, 925, 1350, 0.43, 0.43, emptyList()),
        Combination("20F-1.5E", "20F-E3", "DF", "DF", 2000, 1200, 560, 560, 265, 1.8, 1.7, 0.9, 1400, 560, 230, 1.7, 1.6, 0.85, 1050, 1600, 0.5, 0.5, emptyList()),
        Combination("20F-1.5E", "20F-E6", "DF", "DF", 2000, 2000, 560, 560, 265, 1.8, 1.7, 0.9, 1550, 560, 230, 1.7, 1.6, 0.85, 1150, 1650, 0.5, 0.5, emptyList()),
        Combination("20F-1.5E", "20F-E7", "HF", "HF", 2000, 2000, 500, 500, 215, 1.8, 1.6, 0.85, 1450, 375, 190, 1.5, 1.4, 0.74, 1050, 1450, 0.43, 0.43, emptyList()),
        Combination("20F-1.5E", "20F-E8", "ES", "ES", 2000, 1300, 450, 450, 200, 1.6, 1.5, 0.79, 1000, 315, 175, 1.5, 1.4, 0.74, 825, 1100, 0.41, 0.41, emptyList()),
        Combination("20F-1.5E", "24F-E/SPF1", "SPF", "SPF", 2400, 2400, 560, 560, 215, 1.7, 1.6, 0.85, 1150, 470, 190, 1.7, 1.6, 0.85, 1150, 2000, 0.42, 0.42, emptyList()),
        Combination("20F-1.5E", "24F-E/SPF3", "SPF", "SPF", 2400, 1550, 560, 650, 215, 1.7, 1.6, 0.85, 1200, 470, 195, 1.6, 1.5, 0.79, 900, 1750, 0.42, 0.42, emptyList()),
        Combination("20F-1.5E", "20F-V2", "SP", "SP", 2000, 1550, 740, 650, 300, 1.6, 1.5, 0.79, 1450, 650, 260, 1.5, 1.4, 0.74, 1000, 1400, 0.55, 0.55, emptyList()),
        Combination("20F-1.5E", "20F-V3", "SP", "SP", 2000, 1450, 650, 650, 300, 1.6, 1.5, 0.79, 1600, 650, 260, 1.6, 1.5, 0.79, 1000, 1400, 0.55, 0.55, emptyList()),
        Combination("20F-1.5E", "20F-V5", "SP", "SP", 2000, 2000, 740, 740, 300, 1.7, 1.6, 0.85, 1450, 650, 260, 1.5, 1.4, 0.74, 1050, 1500, 0.55, 0.55, emptyList()),
        Combination("20F-1.5E", "20F-E1", "SP", "SP", 2000, 1300, 650, 650, 300, 1.8, 1.7, 0.9, 1400, 650, 260, 1.7, 1.6, 0.85, 1050, 1550, 0.55, 0.55, emptyList()),
        Combination("20F-1.5E", "20F-E3", "SP", "SP", 2000, 2000, 650, 650, 300, 1.8, 1.7, 0.9, 1700, 650, 260, 1.7, 1.6, 0.85, 1150, 1600, 0.55, 0.55, emptyList()),
        Combination("24F-1.7E", "24F-V5", "DF", "HF", 2400, 1600, 650, 650, 215, 1.8, 1.7, 0.9, 1350, 375, 200, 1.6, 1.5, 0.79, 1100, 1450, 0.5, 0.43, emptyList()),
        Combination("24F-1.7E", "24F-V10", "DF", "HF", 2400, 2400, 650, 650, 215, 1.9, 1.8, 0.95, 1450, 375, 200, 1.6, 1.5, 0.79, 1150, 1550, 0.5, 0.43, emptyList()),
        Combination("24F-1.7E", "24F-E11", "HF", "HF", 2400, 2400, 500, 500, 215, 1.9, 1.8, 0.95, 1550, 375, 190, 1.6, 1.5, 0.79, 1150, 1550, 0.43, 0.43, emptyList()),
        Combination("24F-1.7E", "24F-E15", "HF", "HF", 2400, 1600, 500, 500, 215, 1.9, 1.8, 0.95, 1200, 375, 190, 1.6, 1.5, 0.79, 975, 1500, 0.43, 0.43, emptyList()),
        Combination("24F-1.7E", "24F-V1", "SP", "SP", 2400, 1750, 740, 650, 300, 1.8, 1.7, 0.9, 1450, 650, 260, 1.6, 1.5, 0.79, 1100, 1500, 0.55, 0.55, emptyList()),
        Combination("24F-1.7E", "24F-V4", "SP", "SP", 2400, 1650, 740, 650, 210, 1.8, 1.7, 0.9, 1350, 470, 230, 1.6, 1.5, 0.79, 975, 1350, 0.55, 0.43, listOf(Marker("label", listOf(4)))),
        Combination("24F-1.7E", "24F-V5", "SP", "SP", 2400, 2400, 740, 740, 300, 1.8, 1.7, 0.9, 1700, 650, 260, 1.7, 1.6, 0.85, 1150, 1600, 0.55, 0.55, emptyList()),
        Combination("24F-1.8E", "24F-V4", "DF", "DF", 2400, 1850, 650, 650, 265, 1.9, 1.8, 0.95, 1450, 560, 230, 1.7, 1.6, 0.85, 1100, 1650, 0.5, 0.5, emptyList()),
        Combination("24F-1.8E", "24F-V8", "DF", "DF", 2400, 2400, 650, 650, 265, 1.9, 1.8, 0.95, 1550, 560, 230, 1.7, 1.6, 0.85, 1100, 1650, 0.5, 0.5, emptyList()),
        Combination("24F-1.8E", "24F-E4", "DF", "DF", 2400, 1450, 650, 650, 265, 1.9, 1.8, 0.95, 1400, 560, 230, 1.8, 1.7, 0.9, 1100, 1700, 0.5, 0.5, emptyList()),
        Combination("24F-1.8E", "24F-E13", "DF", "DF", 2400, 2400, 650, 650, 265, 1.9, 1.8, 0.95, 1750, 560, 230, 1.8, 1.7, 0.9, 1250, 1700, 0.5, 0.5, emptyList()),
        Combination("24F-1.8E", "24F-E18", "DF", "DF", 2400, 2400, 650, 650, 265, 1.9, 1.8, 0.95, 1550, 560, 230, 1.8, 1.7, 0.9, 975, 1700, 0.5, 0.5, emptyList()),
        Combination("24F-1.8E", "24F-V3", "SP", "SP", 2400, 2000, 740, 740, 300, 1.9, 1.8, 0.95, 1700, 650, 260, 1.7, 1.6, 0.85, 1150, 1650, 0.55, 0.55, emptyList()),
        Combination("24F-1.8E", "24F-V8", "SP", "SP", 2400, 2400, 740, 740, 300, 1.9, 1.8, 0.95, 1700, 650, 260, 1.7, 1.6, 0.85, 1150, 1650, 0.55, 0.55, emptyList()),
        Combination("24F-1.8E", "24F-E1", "SP", "SP", 2400, 1450, 805, 650, 300, 1.9, 1.8, 0.95, 1550, 650, 260, 1.8, 1.7, 0.9, 1150, 1600, 0.55, 0.55, emptyList()),
        Combination("24F-1.8E", "24F-E4", "SP", "SP", 2400, 2400, 805, 805, 300, 2.0, 1.9, 1.0, 1850, 650, 260, 1.9, 1.8, 0.95, 1450, 1750, 0.55, 0.55, emptyList()),
        Combination("26F-1.9E", "26F-V1", "DF", "DF", 2600, 1950, 650, 650, 265, 2.1, 2.0, 1.06, 1850, 560, 230, 1.9, 1.8, 0.95, 1350, 1850, 0.5, 0.5, emptyList()),
        Combination("26F-1.9E", "26F-V2", "DF", "DF", 2600, 2600, 650, 650, 265, 2.1, 2.0, 1.06, 1850, 560, 230, 1.9, 1.8, 0.95, 1350, 1850, 0.5, 0.5, emptyList()),
        Combination("26F-1.9E", "26F-V1", "SP", "SP", 2600, 2000, 740, 740, 300, 1.9, 1.8, 0.95, 1700, 650, 260, 1.7, 1.6, 0.85, 1150, 1600, 0.55, 0.55, emptyList()),
        Combination("26F-1.9E", "26F-V2", "SP", "SP", 2600, 2100, 740, 740, 300, 2.0, 1.9, 1.0, 1950, 740, 260, 1.9, 1.8, 0.95, 1300, 1850, 0.55, 0.55, emptyList()),
        Combination("26F-1.9E", "26F-V3", "SP", "SP", 2600, 2100, 740, 740, 300, 2.0, 1.9, 1.0, 1950, 650, 260, 1.9, 1.8, 0.95, 1250, 1800, 0.55, 0.55, emptyList()),
        Combination("26F-1.9E", "26F-V4", "SP", "SP", 2600, 2600, 740, 740, 300, 2.0, 1.9, 1.0, 1700, 650, 260, 1.9, 1.8, 0.95, 1200, 1600, 0.55, 0.55, emptyList()),
        Combination("26F-1.9E", "26F-V5", "SP", "SP", 2600, 2600, 740, 740, 300, 2.0, 1.9, 1.0, 1950, 650, 260, 1.9, 1.8, 0.95, 1300, 1850, 0.55, 0.55, emptyList()),
        Combination("28F-2.1E", "28F-E1", "SP", "SP", 2800, 2300, 805, 805, 300, 2.2, 2.1, 1.11, 1600, 650, 260, 1.8, 1.7, 0.9, 1300, 1850, 0.55, 0.55, listOf(Marker("exMin", listOf(7)), Marker("ex", listOf(7)), Marker("exTrue", listOf(7)))),
        Combination("28F-2.1E", "28F-E2", "SP", "SP", 2800, 2800, 805, 805, 300, 2.2, 2.1, 1.11, 2000, 650, 260, 1.8, 1.7, 0.9, 1300, 1850, 0.55, 0.55, listOf(Marker("exMin", listOf(7)), Marker("ex", listOf(7)), Marker("exTrue", listOf(7)))),
        Combination("30F-2.1E", "30F-E1", "SP", "SP", 3000, 2400, 805, 805, 300, 2.2, 2.1, 1.11, 1750, 650, 260, 1.8, 1.7, 0.9, 1250, 1750, 0.55, 0.55, listOf(Marker("ex", listOf(7)), Marker("exTrue", listOf(7)), Marker("exMin", listOf(7)))),
        Combination("30F-2.1E", "30F-E2", "SP", "SP", 3000, 3000, 805, 805, 300, 2.2, 2.1, 1.11, 1750, 650, 260, 1.8, 1.7, 0.9, 1350, 1750, 0.55, 0.55, listOf(Marker("ex", listOf(7)), Marker("exTrue", listOf(7)), Marker("exMin", listOf(7)))),
    )

    fun stressClass(name: String): StressClass? = stressClasses.firstOrNull { it.name == name }

    /** Combinations are identified by symbol and species; the same symbol is printed for different species. */
    fun combination(symbol: String, outer: String, core: String): Combination? =
        combinations.firstOrNull { it.symbol == symbol && it.outer == outer && it.core == core }
}
