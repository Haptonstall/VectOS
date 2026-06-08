package com.lz.domain.material

import com.lz.model.structural.MaterialGrade
import com.lz.model.structural.MaterialType

interface MaterialRepository {
    suspend fun getMaterialsByType(type: MaterialType): List<MaterialGrade>
    suspend fun getMaterialById(id: String): MaterialGrade?
    suspend fun saveMaterials(materials: List<MaterialGrade>)
}