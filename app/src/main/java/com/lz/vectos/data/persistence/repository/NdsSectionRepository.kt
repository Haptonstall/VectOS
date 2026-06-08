package com.lz.vectos.data.repository

import android.content.Context
import com.lz.model.structural.MaterialType
import com.lz.model.structural.SectionDatabaseMetadata
import com.lz.model.structural.SectionProfile
import com.lz.model.structural.SectionRepository
import com.lz.model.structural.ShapeType
import com.lz.model.structural.WoodProfile
import com.lz.model.units.inches
import com.lz.vectos.domain.beam.*
import com.lz.model.units.*
import com.lz.model.units.UnitSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

@Serializable
private data class AxisPropsJson(
    val ix: Double,
    val sx: Double,
    val zx: Double,
    val rx: Double
)

@Serializable
private data class WoodSectionJson(
    val id: String,
    val designation: String,
    val materialType: String,
    val shapeType: String,
    val area: Double,
    val depth: Double,
    val width: Double,
    val strongAxis: AxisPropsJson,
    val weakAxis: AxisPropsJson
)

@Serializable
private data class DatabaseMetadataJson(
    val source: String,
    val edition: String,
    val publicationYear: Int,
    val units: String
)

@Serializable
private data class WoodDatabaseJson(
    val metadata: DatabaseMetadataJson,
    val sections: List<WoodSectionJson>
)

class NdsSectionRepository(private val context: Context) : SectionRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private var dbCache: WoodDatabaseJson? = null

    private fun loadDatabase(): WoodDatabaseJson {
        dbCache?.let { return it }
        return try {
            context.assets.open("nds_sections.json").use { inputStream ->
                @OptIn(ExperimentalSerializationApi::class)
                val db = json.decodeFromStream<WoodDatabaseJson>(inputStream)
                dbCache = db
                db
            }
        } catch (e: Exception) {
            throw IllegalStateException("Critical: Failed to load NDS database. ${e.message}")
        }
    }

    override suspend fun getDatabaseMetadata(material: MaterialType): SectionDatabaseMetadata? {
        if (material != MaterialType.WOOD) return null
        val db = loadDatabase()
        return SectionDatabaseMetadata(
            source = db.metadata.source,
            edition = db.metadata.edition,
            publicationYear = db.metadata.publicationYear,
            revisionDate = null,
            units = UnitSystem.valueOf(db.metadata.units)
        )
    }

    override suspend fun getMaterials(): List<MaterialType> = listOf(MaterialType.WOOD)

    override suspend fun getShapeTypes(material: MaterialType): List<ShapeType> {
        if (material != MaterialType.WOOD) return emptyList()
        return loadDatabase().sections.map { ShapeType.SOLID_RECTANGULAR }.distinct()
    }

    override suspend fun getSections(material: MaterialType, shapeType: ShapeType): List<SectionProfile> {
        if (material != MaterialType.WOOD) return emptyList()
        val db = loadDatabase()
        val metadata = getDatabaseMetadata(material)

        return db.sections.map { s ->
            WoodProfile(
                id = s.id,
                designation = s.designation,
                nominalWidth = s.width.inches,
                nominalDepth = s.depth.inches,
                dressedWidth = s.width.inches,
                dressedDepth = s.depth.inches,
                databaseMetadata = metadata
            )
        }
    }

    override suspend fun getSectionById(id: String): SectionProfile? {
        val db = loadDatabase()
        db.sections.find { it.id == id } ?: return null
        return getSections(MaterialType.WOOD, ShapeType.SOLID_RECTANGULAR).find { it.id == id }
    }
}
