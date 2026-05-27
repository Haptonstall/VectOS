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
    
    // Serialized Data
    val memberJson: String,
    val resultsJson: String,
    val assumptionsJson: String,
    
    // Summary Results (for quick access/querying)
    val maxBendingMomentLbIn: Double,
    val maxShearLbs: Double,
    val maxDeflectionInches: Double
)
