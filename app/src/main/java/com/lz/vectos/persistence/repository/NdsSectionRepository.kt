package com.lz.vectos.persistence.repository

import android.content.Context
import com.lz.vectos.domain.beam.*
import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.UnitSystem
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
        return loadDatabase().sections.map { ShapeType.valueOf(it.shapeType) }.distinct()
    }

    override suspend fun getSections(material: MaterialType, shapeType: ShapeType): List<SectionProfile> {
        if (material != MaterialType.WOOD) return emptyList()
        val db = loadDatabase()
        val metadata = getDatabaseMetadata(material)
        
        return db.sections
            .filter { it.shapeType == shapeType.name }
            .map { s ->
                SectionProfile(
                    id = s.id,
                    designation = s.designation,
                    materialType = material,
                    shapeType = shapeType,
                    area = s.area,
                    depth = Length(s.depth),
                    propertiesStrongAxis = SectionAxisProperties(
                        i = s.strongAxis.ix,
                        s = s.strongAxis.sx,
                        z = s.strongAxis.zx,
                        r = s.strongAxis.rx
                    ),
                    propertiesWeakAxis = SectionAxisProperties(
                        i = s.weakAxis.ix,
                        s = s.weakAxis.sx,
                        z = s.weakAxis.zx,
                        r = s.weakAxis.rx
                    ),
                    databaseMetadata = metadata
                )
            }
    }

    override suspend fun getSectionById(id: String): SectionProfile? {
        val db = loadDatabase()
        val s = db.sections.find { it.id == id } ?: return null
        return getSections(MaterialType.WOOD, ShapeType.valueOf(s.shapeType)).find { it.id == id }
    }
}
