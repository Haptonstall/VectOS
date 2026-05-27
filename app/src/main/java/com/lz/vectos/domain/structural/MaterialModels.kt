package com.lz.vectos.domain.structural

import com.lz.vectos.domain.beam.MaterialType
import com.lz.vectos.domain.units.Pressure
import com.lz.vectos.domain.units.psi
import kotlinx.serialization.Serializable

/**
 * Encapsulates physical properties of a structural material.
 * No longer relies on hardcoded enum values.
 */
@Serializable
data class MaterialProperties(
    val id: String,
    val name: String,
    val materialType: MaterialType,
    val modulusOfElasticity: Pressure,
    val shearModulus: Pressure,
    val densityPcf: Double
)

/**
 * Standardized service for retrieving material data.
 */
interface MaterialDatabaseService {
    fun getMaterial(id: String): MaterialProperties?
    fun getAllMaterials(type: MaterialType): List<MaterialProperties>
}

/**
 * Mock implementation of the material database.
 */
class MockMaterialDatabaseService : MaterialDatabaseService {
    private val materials = mutableMapOf<String, MaterialProperties>()

    init {
        // Steel Grades
        registerSteel("A992", "ASTM A992", 29000000.0)
        registerSteel("A36", "ASTM A36", 29000000.0)
        
        // Wood Species/Grades (Density varies by species)
        registerWood("DF_L_NO2", "Douglas Fir-Larch No. 2", 1600000.0, 35.0)
        registerWood("SOUTHERN_PINE_NO2", "Southern Pine No. 2", 1600000.0, 37.0)
        registerWood("SPF_NO2", "Spruce-Pine-Fir No. 2", 1400000.0, 28.0)
        
        // Concrete
        materials["CONCRETE_4000"] = MaterialProperties(
            "CONCRETE_4000", "4000 psi Concrete", MaterialType.CONCRETE,
            3600000.0.psi, 1500000.0.psi, 150.0
        )
    }

    private fun registerSteel(idSuffix: String, name: String, ePsi: Double) {
        val id = "STEEL_$idSuffix"
        materials[id] = MaterialProperties(
            id, name, MaterialType.STEEL,
            ePsi.psi, (ePsi / (2 * (1 + 0.3))).psi, 490.0
        )
    }

    private fun registerWood(idSuffix: String, name: String, ePsi: Double, density: Double) {
        val id = "WOOD_$idSuffix"
        materials[id] = MaterialProperties(
            id, name, MaterialType.WOOD,
            ePsi.psi, (ePsi / 16.0).psi, density
        )
    }

    override fun getMaterial(id: String): MaterialProperties? = materials[id]

    override fun getAllMaterials(type: MaterialType): List<MaterialProperties> {
        return materials.values.filter { it.materialType == type }
    }
}
