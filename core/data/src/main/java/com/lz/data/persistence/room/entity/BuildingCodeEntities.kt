package com.lz.data.persistence.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lz.model.regulatory.LoadCategory
import com.lz.model.regulatory.codes.ServiceabilityLimitType
import com.lz.model.structural.MaterialType

@Entity(
    tableName = "default_material_standards",
    primaryKeys = ["buildingCodeId", "materialType"],
    foreignKeys = [
        ForeignKey(
            entity = BuildingCodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["buildingCodeId"],
            onDelete = ForeignKey.Companion.CASCADE
        ),
        ForeignKey(
            entity = StandardEntity::class,
            parentColumns = ["id"],
            childColumns = ["standardId"],
            onDelete = ForeignKey.Companion.CASCADE
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
            onDelete = ForeignKey.Companion.CASCADE
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
            onDelete = ForeignKey.Companion.CASCADE
        )
    ]
)
data class DefaultLoadCaseRoomEntity(
    val buildingCodeId: String,
    val loadCaseId: String,
    val name: String
)