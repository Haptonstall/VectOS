package com.lz.vectos.persistence.room

import android.content.Context
import android.util.Log
import com.lz.vectos.persistence.room.dao.AiscSectionDao
import com.lz.vectos.persistence.room.entity.AiscSectionRoomEntity
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Seeder to populate the AISC sections from the assets TXT file.
 */
class AiscSectionSeeder(
    private val context: Context,
    private val aiscDao: AiscSectionDao
) {
    suspend fun seed() {
        // Check if already seeded to avoid redundant work
        val existing = aiscDao.getAllSections()
        if (existing.isNotEmpty()) {
            Log.d("AiscSectionSeeder", "AISC sections already seeded. Count: ${existing.size}")
            return
        }

        try {
            val sections = parseAiscFile()
            if (sections.isNotEmpty()) {
                aiscDao.insertAll(sections)
                Log.d("AiscSectionSeeder", "Successfully seeded ${sections.size} AISC sections.")
            }
        } catch (e: Exception) {
            Log.e("AiscSectionSeeder", "Error seeding AISC sections", e)
        }
    }

    private fun parseAiscFile(): List<AiscSectionRoomEntity> {
        val sections = mutableListOf<AiscSectionRoomEntity>()
        val inputStream = context.assets.open("AISC Shapes Database v15.0.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))

        // Read header
        val header = reader.readLine() ?: return emptyList()
        val columns = header.split(",")
        
        // Find indices for required columns
        val typeIdx = columns.indexOf("Type")
        val idIdx = columns.indexOf("EDI_Std_Nomenclature")
        val labelIdx = columns.indexOf("AISC_Manual_Label")
        val areaIdx = columns.indexOf("A")
        val depthIdx = columns.indexOf("d")
        val twIdx = columns.indexOf("tw")
        val bfIdx = columns.indexOf("bf")
        val tfIdx = columns.indexOf("tf")
        val ixIdx = columns.indexOf("Ix")
        val sxIdx = columns.indexOf("Sx")
        val zxIdx = columns.indexOf("Zx")
        val rxIdx = columns.indexOf("rx")
        val iyIdx = columns.indexOf("Iy")
        val syIdx = columns.indexOf("Sy")
        val zyIdx = columns.indexOf("Zy")
        val ryIdx = columns.indexOf("ry")
        val jIdx = columns.indexOf("J")
        val cwIdx = columns.indexOf("Cw")

        reader.forEachLine { line ->
            val values = line.split(",")
            if (values.size >= columns.size) {
                try {
                    val entity = AiscSectionRoomEntity(
                        id = values[idIdx].trim(),
                        designation = values[labelIdx].trim(),
                        type = values[typeIdx].trim(),
                        area = values[areaIdx].toDoubleOrNull() ?: 0.0,
                        depth = values[depthIdx].toDoubleOrNull() ?: 0.0,
                        webThickness = values[twIdx].toDoubleOrNull() ?: 0.0,
                        flangeWidth = values[bfIdx].toDoubleOrNull() ?: 0.0,
                        flangeThickness = values[tfIdx].toDoubleOrNull() ?: 0.0,
                        ix = values[ixIdx].toDoubleOrNull() ?: 0.0,
                        sx = values[sxIdx].toDoubleOrNull() ?: 0.0,
                        zx = values[zxIdx].toDoubleOrNull() ?: 0.0,
                        rx = values[rxIdx].toDoubleOrNull() ?: 0.0,
                        iy = values[iyIdx].toDoubleOrNull() ?: 0.0,
                        sy = values[syIdx].toDoubleOrNull() ?: 0.0,
                        zy = values[zyIdx].toDoubleOrNull() ?: 0.0,
                        ry = values[ryIdx].toDoubleOrNull() ?: 0.0,
                        torsionalJ = values[jIdx].toDoubleOrNull() ?: 0.0,
                        warpingCw = values[cwIdx].toDoubleOrNull() ?: 0.0
                    )
                    // Only add if it has a valid ID and type
                    if (entity.id.isNotEmpty() && entity.type.isNotEmpty()) {
                        sections.add(entity)
                    }
                } catch (e: Exception) {
                    // Skip malformed rows
                }
            }
        }
        
        return sections
    }
}
