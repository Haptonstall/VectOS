package com.lz.vectos.data.persistence.room

import com.lz.model.structural.MaterialType
import com.lz.vectos.data.persistence.room.dao.MaterialDao
import com.lz.vectos.data.persistence.room.entity.MaterialRoomEntity

class MaterialSeeder(private val dao: MaterialDao) {
    suspend fun seed() {
        val materials = listOf(
            MaterialRoomEntity(
                id = "A992",
                name = "ASTM A992",
                type = MaterialType.STEEL,
                yieldStrengthPsi = 50000.0,
                ultimateStrengthPsi = 65000.0,
                modulusOfElasticityPsi = 29000000.0,
                shearModulusPsi = 11200000.0,
                densityPcf = 490.0
            ),
            MaterialRoomEntity(
                id = "A36",
                name = "ASTM A36",
                type = MaterialType.STEEL,
                yieldStrengthPsi = 36000.0,
                ultimateStrengthPsi = 58000.0,
                modulusOfElasticityPsi = 29000000.0,
                shearModulusPsi = 11200000.0,
                densityPcf = 490.0
            ),
            MaterialRoomEntity(
                id = "DF_L_No2",
                name = "Douglas Fir-Larch No. 2",
                type = MaterialType.WOOD,
                yieldStrengthPsi = 900.0, // Fb
                ultimateStrengthPsi = 0.0,
                modulusOfElasticityPsi = 1600000.0,
                shearModulusPsi = 0.0,
                densityPcf = 35.0
            )
        )
        dao.insertAll(materials)
    }
}
