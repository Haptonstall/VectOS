package com.lz.data.persistence.room.seeder

import android.content.Context
import android.util.Log
import com.lz.data.persistence.room.dao.catalog.AiscSectionDao
import com.lz.data.persistence.room.entity.catalog.AiscSectionRoomEntity
import java.io.BufferedReader
import java.io.InputStreamReader

class AiscSectionSeeder(
    private val context: Context,
    private val aiscDao: AiscSectionDao
) {
    suspend fun seed() {
        if (aiscDao.getCount() > 0) return
        try {
            val sections = parseAiscFile()
            if (sections.isNotEmpty()) {
                aiscDao.insertAll(sections)
                Log.d("AiscSectionSeeder", "Seeded ${sections.size} AISC sections.")
            } else {
                Log.w("AiscSectionSeeder", "No AISC sections parsed from file.")
            }
        } catch (e: Exception) {
            Log.e("AiscSectionSeeder", "Error seeding AISC sections", e)
        }
    }

    private fun parseAiscFile(): List<AiscSectionRoomEntity> {
        val sections = mutableListOf<AiscSectionRoomEntity>()
        val inputStream = context.assets.open("AISC Shapes Database v15.0.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))

        val header = reader.readLine() ?: return emptyList()
        val columns = header.split(",")

        val typeIdx    = columns.indexOf("Type")
        val idIdx      = columns.indexOf("EDI_Std_Nomenclature")
        val labelIdx   = columns.indexOf("AISC_Manual_Label")
        val areaIdx    = columns.indexOf("A")
        val depthIdx   = columns.indexOf("d")
        val twIdx      = columns.indexOf("tw")
        val bfIdx      = columns.indexOf("bf")
        val tfIdx      = columns.indexOf("tf")
        val ixIdx      = columns.indexOf("Ix")
        val sxIdx      = columns.indexOf("Sx")
        val zxIdx      = columns.indexOf("Zx")
        val rxIdx      = columns.indexOf("rx")
        val iyIdx      = columns.indexOf("Iy")
        val syIdx      = columns.indexOf("Sy")
        val zyIdx      = columns.indexOf("Zy")
        val ryIdx      = columns.indexOf("ry")
        val jIdx       = columns.indexOf("J")
        val cwIdx      = columns.indexOf("Cw")

        val maxIdx = listOf(typeIdx, idIdx, labelIdx, areaIdx, depthIdx, twIdx, bfIdx, tfIdx,
            ixIdx, sxIdx, zxIdx, rxIdx, iyIdx, syIdx, zyIdx, ryIdx, jIdx, cwIdx).maxOrNull() ?: -1
        if (maxIdx == -1) {
            Log.e("AiscSectionSeeder", "Required columns missing in header")
            return emptyList()
        }

        reader.forEachLine { line ->
            val values = line.split(",")
            if (values.size > maxIdx) {
                try {
                    val entity = AiscSectionRoomEntity(
                        id = values[idIdx].trim(),
                        designation = values[labelIdx].trim(),
                        type = values[typeIdx].trim(),
                        area = values.getOrNull(areaIdx)?.toDoubleOrNull() ?: 0.0,
                        depth = values.getOrNull(depthIdx)?.toDoubleOrNull() ?: 0.0,
                        webThickness = values.getOrNull(twIdx)?.toDoubleOrNull() ?: 0.0,
                        flangeWidth = values.getOrNull(bfIdx)?.toDoubleOrNull() ?: 0.0,
                        flangeThickness = values.getOrNull(tfIdx)?.toDoubleOrNull() ?: 0.0,
                        ix = values.getOrNull(ixIdx)?.toDoubleOrNull() ?: 0.0,
                        sx = values.getOrNull(sxIdx)?.toDoubleOrNull() ?: 0.0,
                        zx = values.getOrNull(zxIdx)?.toDoubleOrNull() ?: 0.0,
                        rx = values.getOrNull(rxIdx)?.toDoubleOrNull() ?: 0.0,
                        iy = values.getOrNull(iyIdx)?.toDoubleOrNull() ?: 0.0,
                        sy = values.getOrNull(syIdx)?.toDoubleOrNull() ?: 0.0,
                        zy = values.getOrNull(zyIdx)?.toDoubleOrNull() ?: 0.0,
                        ry = values.getOrNull(ryIdx)?.toDoubleOrNull() ?: 0.0,
                        torsionalJ = values.getOrNull(jIdx)?.toDoubleOrNull() ?: 0.0,
                        warpingCw = values.getOrNull(cwIdx)?.toDoubleOrNull() ?: 0.0
                    )
                    if (entity.id.isNotEmpty() && entity.type.isNotEmpty()) sections.add(entity)
                } catch (e: Exception) {
                    Log.w("AiscSectionSeeder", "Skipping malformed row: $line", e)
                }
            }
        }
        return sections
    }
}