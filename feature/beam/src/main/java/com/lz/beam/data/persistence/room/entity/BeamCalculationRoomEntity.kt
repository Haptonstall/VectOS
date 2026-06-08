package com.lz.beam.data.persistence.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "beam_calculations",
    // An index ensures that lookups by calculationId stay lightning-fast
    // without forcing a strict, cross-module compile-time ForeignKey class link.
    indices = [Index(value = ["calculationId"])]
)
data class BeamCalculationRoomEntity(
    // Clamps the record directly to its corresponding core CalculationRoomEntity ID
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