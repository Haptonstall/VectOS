package com.lz.data.repository

import com.lz.data.persistence.room.dao.CalculationDao
import com.lz.data.persistence.room.entity.CalculationRoomEntity

class RoomCalculationWriter(
    private val calculationDao: CalculationDao
) : CalculationWriter {

    override suspend fun writeMetadata(entity: CalculationRoomEntity) {
        calculationDao.insert(entity)
    }
}
