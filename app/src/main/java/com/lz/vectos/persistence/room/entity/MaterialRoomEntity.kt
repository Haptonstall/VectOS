package com.lz.vectos.persistence.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lz.vectos.domain.beam.MaterialType
import com.lz.vectos.domain.structural.WoodGrade
import com.lz.vectos.domain.structural.WoodSpecies

@Entity(tableName = "materials")
data class MaterialRoomEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: MaterialType,
    
    // Physical Properties
    val modulusOfElasticityPsi: Double,
    val shearModulusPsi: Double,
    val densityPcf: Double,

    // Steel specific
    val yieldStrengthPsi: Double = 0.0,
    val ultimateStrengthPsi: Double = 0.0,

    // Wood specific
    val species: WoodSpecies? = null,
    val grade: WoodGrade? = null,
    val referenceBendingPsi: Double = 0.0,
    val referenceShearPsi: Double = 0.0,
    val referenceCompressionParallelPsi: Double = 0.0,
    val referenceCompressionPerpPsi: Double = 0.0,
    val referenceTensionParallelPsi: Double = 0.0
)
