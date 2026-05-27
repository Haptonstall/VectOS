package com.lz.vectos.persistence.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lz.vectos.domain.structural.DesignMethodology
import com.lz.vectos.domain.structural.LoadCategory
import com.lz.vectos.domain.structural.CombinationType

@Entity(tableName = "building_codes")
data class BuildingCodeEntity(
    @PrimaryKey val id: String, // e.g., "IBC_2021"
    val short_name: String,
    val long_name: String,
    val base_code_id: String? = null,
    val default_asd_set_id: String? = null,
    val default_lrfd_set_id: String? = null,
    val references_json: String = "{}" // Serialized Map<StructuralReferenceKey, String>
)

@Entity(tableName = "standards")
data class StandardEntity(
    @PrimaryKey val id: String, // e.g., "ASCE_7_16"
    val short_name: String,
    val long_name: String,
    val references_json: String = "{}" // Serialized Map<StructuralReferenceKey, String>
)

@Entity(
    tableName = "building_code_standard_cross_ref",
    primaryKeys = ["code_id", "standard_id"],
    foreignKeys = [
        ForeignKey(
            entity = BuildingCodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["code_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StandardEntity::class,
            parentColumns = ["id"],
            childColumns = ["standard_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("standard_id")]
)
data class BuildingCodeStandardCrossRef(
    val code_id: String,
    val standard_id: String
)

@Entity(tableName = "load_combination_sets")
data class LoadCombinationSetEntity(
    @PrimaryKey val id: String,
    val source_id: String, // Reference to Code ID or Standard ID
    val source_name: String,
    val methodology: DesignMethodology,
    val description: String
)

@Entity(
    tableName = "load_combinations",
    foreignKeys = [
        ForeignKey(
            entity = LoadCombinationSetEntity::class,
            parentColumns = ["id"],
            childColumns = ["set_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("set_id")]
)
data class LoadCombinationEntity(
    @PrimaryKey val id: String,
    val set_id: String,
    val name: String,
    val type: CombinationType
)

@Entity(
    tableName = "load_factors",
    foreignKeys = [
        ForeignKey(
            entity = LoadCombinationEntity::class,
            parentColumns = ["id"],
            childColumns = ["combination_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("combination_id")]
)
data class LoadFactorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val combination_id: String,
    val category: LoadCategory,
    val factor: Double
)
