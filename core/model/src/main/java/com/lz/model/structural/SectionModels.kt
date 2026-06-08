package com.lz.model.structural

import com.lz.model.units.Area
import com.lz.model.units.Length
import com.lz.model.units.MomentOfInertia
import com.lz.model.units.SectionModulus
import com.lz.model.units.UnitSystem
import com.lz.model.units.in2
import com.lz.model.units.in3
import com.lz.model.units.in4
import com.lz.model.units.inIn2
import com.lz.model.units.inIn3
import com.lz.model.units.inIn4
import com.lz.model.units.inches
import java.util.UUID
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Axis-specific structural properties.
 */
data class SectionAxisProperties(
    val i: MomentOfInertia,
    val s: SectionModulus,
    val z: SectionModulus,
    val r: Length
) {
    val inIn4: Double get() = i.inIn4
}

/**
 * Metadata about the source section database.
 */
data class SectionDatabaseMetadata(
    val source: String,
    val edition: String,
    val publicationYear: Int,
    val revisionDate: String?,
    val units: UnitSystem
)

/**
 * Unified interface for all structural section profiles.
 */
sealed interface SectionProfile {
    val id: String
    val designation: String
    val materialType: MaterialType
    val shapeType: ShapeType
    val area: Area
    val depth: Length
    val propertiesStrongAxis: SectionAxisProperties
    val propertiesWeakAxis: SectionAxisProperties
    val databaseMetadata: SectionDatabaseMetadata?

    fun getWeightLbFt(materialDensityPcf: Double): Double = (area.inIn2 / 144.0) * materialDensityPcf
    val weightLbFt: Double get() = getWeightLbFt(materialType.defaultDensityPcf)
    val ixx: SectionAxisProperties get() = propertiesStrongAxis

    /**
     * Creates a new profile by interpolating properties between this and another profile.
     * Useful for tapered beam analysis.
     */
    fun interpolate(other: SectionProfile, ratio: Double): SectionProfile
}

/**
 * Implementation for Hot-Rolled Steel shapes (AISC).
 */
data class SteelProfile(
    override val id: String,
    override val designation: String,
    override val shapeType: ShapeType,
    override val area: Area,
    override val depth: Length,
    val webThickness: Length,
    val flangeWidth: Length,
    val flangeThickness: Length,
    val torsionalConstantJ: Double,
    val warpingConstantCw: Double,
    override val propertiesStrongAxis: SectionAxisProperties,
    override val propertiesWeakAxis: SectionAxisProperties,
    override val databaseMetadata: SectionDatabaseMetadata? = null
) : SectionProfile {
    override val materialType: MaterialType = MaterialType.STEEL
    override fun interpolate(other: SectionProfile, ratio: Double): SectionProfile = interpolateToCustom(other, ratio)
}

/**
 * Implementation for Built-up Steel I-shapes with potentially asymmetric flanges.
 * Geometric properties are calculated dynamically using the Parallel Axis Theorem.
 */
data class BuiltUpIProfile(
    override val id: String = UUID.randomUUID().toString(),
    override val designation: String,
    val topFlangeWidth: Length,
    val topFlangeThickness: Length,
    val webDepth: Length,
    val webThickness: Length,
    val bottomFlangeWidth: Length,
    val bottomFlangeThickness: Length,
    override val databaseMetadata: SectionDatabaseMetadata? = null
) : SectionProfile {
    override val materialType: MaterialType = MaterialType.STEEL
    override val shapeType: ShapeType = ShapeType.WIDE_FLANGE

    val torsionalConstantJ: Double get() {
        val wtf = topFlangeWidth.inches
        val ttf = topFlangeThickness.inches
        val wbf = bottomFlangeWidth.inches
        val tbf = bottomFlangeThickness.inches
        val dw = webDepth.inches
        val tw = webThickness.inches
        return (wtf * ttf.pow(3) + wbf * tbf.pow(3) + dw * tw.pow(3)) / 3.0
    }

    val warpingConstantCw: Double get() {
        val wtf = topFlangeWidth.inches
        val ttf = topFlangeThickness.inches
        val wbf = bottomFlangeWidth.inches
        val tbf = bottomFlangeThickness.inches
        val dw = webDepth.inches

        val ho = dw + (ttf / 2.0) + (tbf / 2.0)
        val iyTop = (ttf * wtf.pow(3)) / 12.0
        val iyBot = (tbf * wbf.pow(3)) / 12.0

        return (iyTop * iyBot / (iyTop + iyBot)) * ho.pow(2)
    }

    override val area: Area
        get() {
        val aTf = topFlangeWidth.inches * topFlangeThickness.inches
        val aW = webDepth.inches * webThickness.inches
        val aBf = bottomFlangeWidth.inches * bottomFlangeThickness.inches
        return (aTf + aW + aBf).in2
    }

    override val depth: Length get() = (topFlangeThickness.inches + webDepth.inches + bottomFlangeThickness.inches).inches

    override val propertiesStrongAxis: SectionAxisProperties get() {
        val dtf = topFlangeThickness.inches
        val wtf = topFlangeWidth.inches
        val dw = webDepth.inches
        val tw = webThickness.inches
        val dbf = bottomFlangeThickness.inches
        val wbf = bottomFlangeWidth.inches

        val atf = wtf * dtf
        val aw = dw * tw
        val abf = wbf * dbf
        val totalArea = atf + aw + abf

        // Center of gravity heights from bottom
        val ybf = dbf / 2.0
        val yw = dbf + dw / 2.0
        val ytf = dbf + dw + dtf / 2.0

        val neutralAxis = (abf * ybf + aw * yw + atf * ytf) / totalArea

        val itf = (wtf * dtf.pow(3) / 12.0) + atf * (ytf - neutralAxis).pow(2)
        val iw = (tw * dw.pow(3) / 12.0) + aw * (yw - neutralAxis).pow(2)
        val ibf = (wbf * dbf.pow(3) / 12.0) + abf * (ybf - neutralAxis).pow(2)

        val ix = itf + iw + ibf
        val cMax = max(neutralAxis, depth.inches - neutralAxis)
        val sx = ix / cMax

        // Plastic Modulus Zx
        val halfArea = totalArea / 2.0
        val pnaFromBottom = when {
            abf >= halfArea -> halfArea / wbf
            (abf + aw) >= halfArea -> dbf + (halfArea - abf) / tw
            else -> dbf + dw + (halfArea - abf - aw) / wtf
        }

        val zx = calculateZx(pnaFromBottom)

        return SectionAxisProperties(
            i = ix.in4,
            s = sx.in3,
            z = zx.in3,
            r = sqrt(ix / totalArea).inches
        )
    }

    private fun calculateZx(pna: Double): Double {
        fun rectZ(w: Double, yStart: Double, yEnd: Double, pna: Double): Double {
            return if (pna <= yStart) {
                w * ((yEnd.pow(2)/2.0 - pna*yEnd) - (yStart.pow(2)/2.0 - pna*yStart))
            } else if (pna >= yEnd) {
                w * ((pna*yEnd - yEnd.pow(2)/2.0) - (pna*yStart - yStart.pow(2)/2.0))
            } else {
                rectZ(w, yStart, pna, pna) + rectZ(w, pna, yEnd, pna)
            }
        }

        val dbf = bottomFlangeThickness.inches
        val dw = webDepth.inches
        val dtf = topFlangeThickness.inches

        return rectZ(bottomFlangeWidth.inches, 0.0, dbf, pna) +
               rectZ(webThickness.inches, dbf, dbf + dw, pna) +
               rectZ(topFlangeWidth.inches, dbf + dw, dbf + dw + dtf, pna)
    }

    override val propertiesWeakAxis: SectionAxisProperties get() {
        val dtf = topFlangeThickness.inches
        val wtf = topFlangeWidth.inches
        val dw = webDepth.inches
        val tw = webThickness.inches
        val dbf = bottomFlangeThickness.inches
        val wbf = bottomFlangeWidth.inches

        val iy = (dtf * wtf.pow(3) / 12.0) + (dw * tw.pow(3) / 12.0) + (dbf * wbf.pow(3) / 12.0)
        val ry = sqrt(iy / area.inIn2)
        val sy = iy / (max(wtf, wbf) / 2.0)

        return SectionAxisProperties(
            i = iy.in4,
            s = sy.in3,
            z = sy.in3,
            r = ry.inches
        )
    }

    override fun interpolate(other: SectionProfile, ratio: Double): SectionProfile {
        if (other is BuiltUpIProfile) {
            return copy(
                id = UUID.randomUUID().toString(),
                designation = "Interpolated Built-up",
                topFlangeWidth = (topFlangeWidth.inches + (other.topFlangeWidth.inches - topFlangeWidth.inches) * ratio).inches,
                topFlangeThickness = (topFlangeThickness.inches + (other.topFlangeThickness.inches - topFlangeThickness.inches) * ratio).inches,
                webDepth = (webDepth.inches + (other.webDepth.inches - webDepth.inches) * ratio).inches,
                webThickness = (webThickness.inches + (other.webThickness.inches - webThickness.inches) * ratio).inches,
                bottomFlangeWidth = (bottomFlangeWidth.inches + (other.bottomFlangeWidth.inches - bottomFlangeWidth.inches) * ratio).inches,
                bottomFlangeThickness = (bottomFlangeThickness.inches + (other.bottomFlangeThickness.inches - bottomFlangeThickness.inches) * ratio).inches
            )
        }
        return interpolateToCustom(other, ratio)
    }
}

/**
 * Implementation for Wood shapes (NDS Dressed).
 */
data class WoodProfile(
    override val id: String,
    override val designation: String,
    val nominalWidth: Length,
    val nominalDepth: Length,
    val dressedWidth: Length,
    val dressedDepth: Length,
    override val databaseMetadata: SectionDatabaseMetadata? = null
) : SectionProfile {
    override val materialType: MaterialType = MaterialType.WOOD
    override val shapeType: ShapeType = ShapeType.SOLID_RECTANGULAR

    override val area: Area get() = Area(dressedWidth.inches * dressedDepth.inches)
    override val depth: Length get() = dressedDepth

    override val propertiesStrongAxis: SectionAxisProperties get() {
        val b = dressedWidth.inches
        val d = dressedDepth.inches
        return SectionAxisProperties(
            i = (b * d.pow(3) / 12.0).in4,
            s = (b * d.pow(2) / 6.0).in3,
            z = (b * d.pow(2) / 4.0).in3,
            r = (d / sqrt(12.0)).inches
        )
    }

    override val propertiesWeakAxis: SectionAxisProperties get() {
        val b = dressedWidth.inches
        val d = dressedDepth.inches
        return SectionAxisProperties(
            i = (d * b.pow(3) / 12.0).in4,
            s = (d * b.pow(2) / 6.0).in3,
            z = (d * b.pow(2) / 4.0).in3,
            r = (b / sqrt(12.0)).inches
        )
    }

    override fun interpolate(other: SectionProfile, ratio: Double): SectionProfile {
        if (other is WoodProfile) {
            return copy(
                id = UUID.randomUUID().toString(),
                designation = "Interpolated Wood",
                nominalWidth = (nominalWidth.inches + (other.nominalWidth.inches - nominalWidth.inches) * ratio).inches,
                nominalDepth = (nominalDepth.inches + (other.nominalDepth.inches - nominalDepth.inches) * ratio).inches,
                dressedWidth = (dressedWidth.inches + (other.dressedWidth.inches - dressedWidth.inches) * ratio).inches,
                dressedDepth = (dressedDepth.inches + (other.dressedDepth.inches - dressedDepth.inches) * ratio).inches
            )
        }
        return interpolateToCustom(other, ratio)
    }
}

/**
 * Implementation for Custom User-defined shapes.
 */
data class CustomProfile(
    override val id: String = UUID.randomUUID().toString(),
    override val designation: String,
    override val materialType: MaterialType,
    override val shapeType: ShapeType,
    override val area: Area,
    override val depth: Length,
    override val propertiesStrongAxis: SectionAxisProperties,
    override val propertiesWeakAxis: SectionAxisProperties,
    override val databaseMetadata: SectionDatabaseMetadata? = null
) : SectionProfile {
    override fun interpolate(other: SectionProfile, ratio: Double): SectionProfile = interpolateToCustom(other, ratio)
}

/**
 * Helper to create a custom profile from interpolated properties.
 */
fun SectionProfile.interpolateToCustom(other: SectionProfile, ratio: Double): CustomProfile {
    val lerpArea = area.inIn2 + (other.area.inIn2 - area.inIn2) * ratio
    val lerpDepth = depth.inches + (other.depth.inches - depth.inches) * ratio

    val p1S = propertiesStrongAxis
    val p2S = other.propertiesStrongAxis
    val lerpStrong = SectionAxisProperties(
        i = (p1S.i.inIn4 + (p2S.i.inIn4 - p1S.i.inIn4) * ratio).in4,
        s = (p1S.s.inIn3 + (p2S.s.inIn3 - p1S.s.inIn3) * ratio).in3,
        z = (p1S.z.inIn3 + (p2S.z.inIn3 - p1S.z.inIn3) * ratio).in3,
        r = (p1S.r.inches + (p2S.r.inches - p1S.r.inches) * ratio).inches
    )

    val p1W = propertiesWeakAxis
    val p2W = other.propertiesWeakAxis
    val lerpWeak = SectionAxisProperties(
        i = (p1W.i.inIn4 + (p2W.i.inIn4 - p1W.i.inIn4) * ratio).in4,
        s = (p1W.s.inIn3 + (p2W.s.inIn3 - p1W.s.inIn3) * ratio).in3,
        z = (p1W.z.inIn3 + (p2W.z.inIn3 - p1W.z.inIn3) * ratio).in3,
        r = (p1W.r.inches + (p2W.r.inches - p1W.r.inches) * ratio).inches
    )

    return CustomProfile(
        id = UUID.randomUUID().toString(),
        designation = "Interpolated ($ratio)",
        materialType = materialType,
        shapeType = shapeType,
        area = lerpArea.in2,
        depth = lerpDepth.inches,
        propertiesStrongAxis = lerpStrong,
        propertiesWeakAxis = lerpWeak
    )
}

/**
 * Supported structural shape types.
 */
enum class ShapeType {
    WIDE_FLANGE,
    CHANNEL,
    TEE,
    ANGLE,
    RECTANGULAR_HSS,
    ROUND_HSS,
    PIPE,
    SOLID_RECTANGULAR,
    SOLID_ROUND
}

/**
 * Axis orientation for design checks.
 */
enum class SectionOrientation {
    STRONG_AXIS,
    WEAK_AXIS
}


/**
 * Data-only container for section property lookups.
 */
interface SectionRepository {
    suspend fun getDatabaseMetadata(material: MaterialType): SectionDatabaseMetadata?
    suspend fun getMaterials(): List<MaterialType>
    suspend fun getShapeTypes(material: MaterialType): List<ShapeType>
    suspend fun getSections(material: MaterialType, shapeType: ShapeType): List<SectionProfile>
    suspend fun getSectionById(id: String): SectionProfile?
}