package com.lz.vectos.domain.beam

import com.lz.vectos.domain.structural.MaterialGrade

interface MaterialRepository {
    suspend fun getMaterialsByType(type: MaterialType): List<MaterialGrade>
    suspend fun getMaterialById(id: String): MaterialGrade?
    suspend fun saveMaterials(materials: List<MaterialGrade>)
}
