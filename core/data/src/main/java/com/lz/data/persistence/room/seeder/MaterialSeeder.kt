package com.lz.data.persistence.room.seeder

import com.lz.data.persistence.room.dao.MaterialDao
import com.lz.data.persistence.room.entity.MaterialRoomEntity
import com.lz.model.structural.MaterialType

class MaterialSeeder(private val dao: MaterialDao) {
    suspend fun seed() {
        if (dao.getCount() > 0) return
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
                yieldStrengthPsi = 0.0,
                ultimateStrengthPsi = 0.0,
                modulusOfElasticityPsi = 1600000.0,
                shearModulusPsi = 0.0,
                densityPcf = 35.0,
                referenceBendingPsi = 900.0
            )
        )
        dao.insertAll(materials)
    }
}