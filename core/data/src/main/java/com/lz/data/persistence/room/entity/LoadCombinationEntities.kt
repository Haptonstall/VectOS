package com.lz.data.persistence.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lz.model.structural.DesignMethodology
import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.loads.CombinationType

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
