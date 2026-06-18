package com.lz.data.persistence.room.repository

import com.lz.domain.material.MaterialRepository
import com.lz.data.persistence.room.dao.MaterialDao
import com.lz.data.persistence.room.entity.MaterialRoomEntity
import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.MaterialType
import com.lz.model.structural.WoodGrade
import com.lz.model.structural.WoodSpecies
import com.lz.model.units.inPsi
import com.lz.model.units.psi

class RoomMaterialRepository(private val dao: MaterialDao) : MaterialRepository {

    override suspend fun getMaterialsByType(type: MaterialType): List<MaterialGrade> {
        return dao.getMaterialsByType(type).map { it.toDomain() }
    }

    override suspend fun getMaterialById(id: String): MaterialGrade? {
        return dao.getMaterialById(id)?.toDomain()
    }

    override suspend fun saveMaterials(materials: List<MaterialGrade>) {
        dao.insertAll(materials.map { it.toEntity() })
    }

    private fun MaterialRoomEntity.toDomain(): MaterialGrade {
        return when (type) {
            MaterialType.STEEL -> MaterialGrade.Steel(
                id = id,
                name = name,
                yieldStrength = yieldStrengthPsi.psi,
                ultimateStrength = ultimateStrengthPsi.psi,
                modulusOfElasticity = modulusOfElasticityPsi.psi,
                shearModulus = shearModulusPsi.psi,
                densityPcf = densityPcf
            )
            MaterialType.WOOD -> MaterialGrade.Wood(
                id = id,
                name = name,
                species = species ?: WoodSpecies.DF_L,
                grade = grade ?: WoodGrade.NO_2,
                referenceBending = referenceBendingPsi.psi,
                referenceShear = referenceShearPsi.psi,
                referenceCompressionParallel = referenceCompressionParallelPsi.psi,
                referenceCompressionPerp = referenceCompressionPerpPsi.psi,
                referenceTensionParallel = referenceTensionParallelPsi.psi,
                modulusOfElasticity = modulusOfElasticityPsi.psi,
                shearModulus = shearModulusPsi.psi,
                densityPcf = densityPcf
            )
            MaterialType.ALUMINUM -> MaterialGrade.Aluminum(
                id = id,
                name = name,
                yieldStrength = yieldStrengthPsi.psi,
                ultimateStrength = ultimateStrengthPsi.psi,
                modulusOfElasticity = modulusOfElasticityPsi.psi,
                shearModulus = shearModulusPsi.psi,
                densityPcf = densityPcf
            )
            else -> MaterialGrade.Generic(
                id = id,
                name = name,
                type = type,
                modulusOfElasticity = modulusOfElasticityPsi.psi,
                shearModulus = shearModulusPsi.psi,
                densityPcf = densityPcf
            )
        }
    }

    private fun MaterialGrade.toEntity(): MaterialRoomEntity {
        return when (this) {
            is MaterialGrade.Steel -> MaterialRoomEntity(
                id = id,
                name = name,
                type = MaterialType.STEEL,
                modulusOfElasticityPsi = modulusOfElasticity.inPsi,
                shearModulusPsi = shearModulus.inPsi,
                densityPcf = densityPcf,
                yieldStrengthPsi = yieldStrength.inPsi,
                ultimateStrengthPsi = ultimateStrength.inPsi
            )
            is MaterialGrade.Aluminum -> MaterialRoomEntity(
                id = id,
                name = name,
                type = MaterialType.ALUMINUM,
                modulusOfElasticityPsi = modulusOfElasticity.inPsi,
                shearModulusPsi = shearModulus.inPsi,
                densityPcf = densityPcf,
                yieldStrengthPsi = yieldStrength.inPsi,
                ultimateStrengthPsi = ultimateStrength.inPsi
            )

            is MaterialGrade.Wood -> MaterialRoomEntity(
                id = id,
                name = name,
                type = MaterialType.WOOD,
                modulusOfElasticityPsi = modulusOfElasticity.inPsi,
                shearModulusPsi = shearModulus.inPsi,
                densityPcf = densityPcf,
                species = species,
                grade = grade,
                referenceBendingPsi = referenceBending.inPsi,
                referenceShearPsi = referenceShear.inPsi,
                referenceCompressionParallelPsi = referenceCompressionParallel.inPsi,
                referenceCompressionPerpPsi = referenceCompressionPerp.inPsi,
                referenceTensionParallelPsi = referenceTensionParallel.inPsi
            )
            is MaterialGrade.Generic -> MaterialRoomEntity(
                id = id,
                name = name,
                type = type,
                modulusOfElasticityPsi = modulusOfElasticity.inPsi,
                shearModulusPsi = shearModulus.inPsi,
                densityPcf = densityPcf
            )
        }
    }
}
