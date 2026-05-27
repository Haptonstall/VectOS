package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.MaterialType
import com.lz.vectos.domain.units.Pressure
import com.lz.vectos.domain.units.ksi
import com.lz.vectos.domain.units.psi
import com.lz.vectos.domain.units.inPsi
import kotlinx.serialization.Serializable

@Serializable
enum class WoodSpecies(val isGlulam: Boolean = false) {
    DF_L,
    HEM_FIR,
    SPF,
    SOUTHERN_PINE,
    GLULAM_WS,
    GLULAM_SP;

    companion object {
        fun fromString(value: String): WoodSpecies = entries.find { it.name == value } ?: DF_L
    }
}

@Serializable
enum class WoodGrade {
    SELECT_STRUCTURAL,
    NO_1,
    NO_2,
    STUD,
    CONSTRUCTION,
    STANDARD,
    UTILITY,
    G_24F_1_8E,
    G_24F_1_7E,
    G_20F_1_5E;

    companion object {
        fun fromString(value: String): WoodGrade = entries.find { it.name == value } ?: NO_2
    }
}

/**
 * Polymorphic material grade model supporting material-specific engineering properties.
 */
@Serializable
sealed class MaterialGrade {
    abstract val id: String
    abstract val name: String
    abstract val type: MaterialType
    abstract val modulusOfElasticity: Pressure
    abstract val shearModulus: Pressure
    abstract val densityPcf: Double

    @Serializable
    data class Steel(
        override val id: String,
        override val name: String,
        val yieldStrength: Pressure,
        val ultimateStrength: Pressure,
        override val modulusOfElasticity: Pressure = 29000.0.ksi,
        override val shearModulus: Pressure = 11200.0.ksi,
        override val densityPcf: Double = 490.0
    ) : MaterialGrade() {
        override val type = MaterialType.STEEL
    }

    @Serializable
    data class Wood(
        override val id: String,
        override val name: String,
        val species: WoodSpecies,
        val grade: WoodGrade,
        val referenceBending: Pressure,
        val referenceShear: Pressure,
        val referenceCompressionParallel: Pressure,
        val referenceCompressionPerp: Pressure,
        val referenceTensionParallel: Pressure,
        override val modulusOfElasticity: Pressure,
        override val shearModulus: Pressure,
        override val densityPcf: Double
    ) : MaterialGrade() {
        override val type = MaterialType.WOOD
    }

    @Serializable
    data class Generic(
        override val id: String,
        override val name: String,
        override val type: MaterialType,
        override val modulusOfElasticity: Pressure,
        override val shearModulus: Pressure,
        override val densityPcf: Double
    ) : MaterialGrade()
}

/**
 * Standardized service for retrieving material data.
 */
interface MaterialDatabaseService {
    fun getMaterial(id: String): MaterialGrade?
    fun getAllMaterials(type: MaterialType): List<MaterialGrade>
}

/**
 * Mock implementation of the material database.
 */
class MockMaterialDatabaseService : MaterialDatabaseService {
    private val materials = mutableMapOf<String, MaterialGrade>()

    init {
        // Steel Grades
        registerSteel("A992", "ASTM A992", 50.0.ksi)
        registerSteel("A36", "ASTM A36", 36.0.ksi)
        
        // Wood Species/Grades (Simplified placeholders for initialization)
        registerWoodPlaceholder("DF_L_NO2", "Douglas Fir-Larch No. 2", WoodSpecies.DF_L, WoodGrade.NO_2, 1600.0.ksi, 35.0)
    }

    private fun registerSteel(idSuffix: String, name: String, yield: Pressure) {
        val id = "STEEL_$idSuffix"
        materials[id] = MaterialGrade.Steel(
            id = id,
            name = name,
            yieldStrength = yield,
            ultimateStrength = (yield.inPsi * 1.3).psi
        )
    }

    private fun registerWoodPlaceholder(idSuffix: String, name: String, species: WoodSpecies, grade: WoodGrade, e: Pressure, density: Double) {
        val id = "WOOD_$idSuffix"
        materials[id] = MaterialGrade.Wood(
            id = id,
            name = name,
            species = species,
            grade = grade,
            referenceBending = 1000.0.psi,
            referenceShear = 180.0.psi,
            referenceCompressionParallel = 1200.0.psi,
            referenceCompressionPerp = 625.0.psi,
            referenceTensionParallel = 675.0.psi,
            modulusOfElasticity = e,
            shearModulus = (e.inPsi / 16.0).psi,
            densityPcf = density
        )
    }

    override fun getMaterial(id: String): MaterialGrade? = materials[id]

    override fun getAllMaterials(type: MaterialType): List<MaterialGrade> {
        return materials.values.filter { it.type == type }
    }
}
