package com.lz.vectos.persistence.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "beam_calculations",
    foreignKeys = [
        ForeignKey(
            entity = CalculationRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["calculationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BeamCalculationRoomEntity(
    @PrimaryKey val calculationId: UUID,
    
    // Inputs
    val spanMeters: Double,
    val loadValueBase: Double,
    val materialName: String,
    val momentOfInertiaM4: Double,
    val loadTypeName: String,
    val unitSystemName: String,
    
    // Assumptions
    val isLinearElastic: Boolean,
    val isSmallDeflection: Boolean,
    val isSimplySupported: Boolean,
    
    // Results
    val maxBendingMomentNm: Double,
    val maxShearN: Double,
    val maxDeflectionM: Double
)
