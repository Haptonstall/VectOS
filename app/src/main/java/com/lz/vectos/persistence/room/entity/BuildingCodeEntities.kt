package com.lz.vectos.persistence.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lz.vectos.domain.beam.MaterialType
import com.lz.vectos.domain.structural.DesignMethodology
import com.lz.vectos.domain.structural.LimitState
import com.lz.vectos.domain.structural.LoadCategory
import com.lz.vectos.domain.structural.ServiceabilityLimitType

@Entity(
    tableName = "default_material_standards",
    primaryKeys = ["buildingCodeId", "materialType"],
    foreignKeys = [
        ForeignKey(
            entity = BuildingCodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["buildingCodeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StandardEntity::class,
            parentColumns = ["id"],
            childColumns = ["standardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("standardId")]
)
data class DefaultMaterialStandardEntity(
    val buildingCodeId: String,
    val materialType: MaterialType,
    val standardId: String
)

@Entity(
    tableName = "serviceability_criteria",
    foreignKeys = [
        ForeignKey(
            entity = BuildingCodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["buildingCodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("buildingCodeId")]
)
data class ServiceabilityCriterionRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buildingCodeId: String,
    val limitType: ServiceabilityLimitType,
    val name: String,
    val loadCategory: LoadCategory?,
    val spanDenominator: Double,
    val description: String?
)

@Entity(
    tableName = "default_load_cases",
    primaryKeys = ["buildingCodeId", "loadCaseId"],
    foreignKeys = [
        ForeignKey(
            entity = BuildingCodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["buildingCodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DefaultLoadCaseRoomEntity(
    val buildingCodeId: String,
    val loadCaseId: String,
    val name: String
)
