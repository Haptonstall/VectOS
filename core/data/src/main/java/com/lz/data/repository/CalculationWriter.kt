package com.lz.data.repository

import com.lz.data.persistence.room.entity.CalculationRoomEntity

/**
 * Contract for writing calculation metadata to the core AppDatabase.
 * Feature modules call this to persist the core CalculationRoomEntity
 * without depending on AppDatabase directly.
 */
interface CalculationWriter {
    suspend fun writeMetadata(entity: CalculationRoomEntity)
}
