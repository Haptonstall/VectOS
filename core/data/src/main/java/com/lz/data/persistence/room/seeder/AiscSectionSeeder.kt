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
        if (aiscDao.getCount() == 0) {
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
            return
        }
        repairHssRowsIfNeeded()
    }

    /**
     * HSS/PIPE rows were seeded (as bare `"HSS"`/`"PIPE"`, never actually
     * exposed by the repository) before HSS support existed — read via the
     * same `d`/`tw`/`bf`/`tf` columns used for W/C/WT/L, which are blank for
     * HSS rows in the source file, so every install prior to this fix has
     * those rows sitting in the DB with depth/wall-thickness/etc. all zero.
     * `seed()` only ever runs once (guarded by `getCount() > 0`), so those
     * zeroed rows would otherwise persist forever on an existing install.
     * Repairs them in place — re-parsing and re-inserting just the type="HSS"
     * rows relies on `insertAll`'s REPLACE conflict strategy to overwrite by
     * id, so this never touches the already-correct W/C/WT/L catalog.
     */
    private suspend fun repairHssRowsIfNeeded() {
        val needsRepair = aiscDao.getSectionsByType("HSS").any { it.depth <= 0.0 }
        if (!needsRepair) return
        try {
            val hssSections = parseAiscFile().filter {
                it.type == ShapeTypeRect || it.type == ShapeTypeRound
            }
            if (hssSections.isNotEmpty()) {
                aiscDao.insertAll(hssSections)
                Log.d("AiscSectionSeeder", "Repaired ${hssSections.size} HSS sections.")
            }
        } catch (e: Exception) {
            Log.e("AiscSectionSeeder", "Error repairing HSS sections", e)
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
        // HSS-only columns — Ht/B/OD/tdes/b/h are blank for every other
        // type (W/C/WT/L/etc. use d/tw/bf/tf above instead).
        val htIdx      = columns.indexOf("Ht")
        val bIdx       = columns.indexOf("B")
        val odIdx      = columns.indexOf("OD")
        val tdesIdx    = columns.indexOf("tdes")
        val flatBIdx   = columns.indexOf("b")
        val flatHIdx   = columns.indexOf("h")

        val maxIdx = listOf(typeIdx, idIdx, labelIdx, areaIdx, depthIdx, twIdx, bfIdx, tfIdx,
            ixIdx, sxIdx, zxIdx, rxIdx, iyIdx, syIdx, zyIdx, ryIdx, jIdx, cwIdx,
            htIdx, bIdx, odIdx, tdesIdx, flatBIdx, flatHIdx).maxOrNull() ?: -1
        if (maxIdx == -1) {
            Log.e("AiscSectionSeeder", "Required columns missing in header")
            return emptyList()
        }

        reader.forEachLine { line ->
            val values = line.split(",")
            if (values.size > maxIdx) {
                try {
                    val rawType = values[typeIdx].trim()
                    val od = values.getOrNull(odIdx)?.toDoubleOrNull()

                    // HSS rows carry geometry in Ht/B/OD/tdes/b/h instead of
                    // d/tw/bf/tf (blank for these rows in the source file) —
                    // and the source file's single "HSS" type covers both
                    // rectangular/square (B populated) and round (OD
                    // populated) shapes, so split it into two distinct
                    // stored types here for the repository to tell apart.
                    val entity = if (rawType == "HSS") {
                        val tdes = values.getOrNull(tdesIdx)?.toDoubleOrNull() ?: 0.0
                        if (od != null) {
                            AiscSectionRoomEntity(
                                id = values[idIdx].trim(),
                                designation = values[labelIdx].trim(),
                                type = ShapeTypeRound,
                                area = values.getOrNull(areaIdx)?.toDoubleOrNull() ?: 0.0,
                                depth = od,
                                webThickness = tdes,
                                flangeWidth = od,
                                flangeThickness = tdes,
                                ix = values.getOrNull(ixIdx)?.toDoubleOrNull() ?: 0.0,
                                sx = values.getOrNull(sxIdx)?.toDoubleOrNull() ?: 0.0,
                                zx = values.getOrNull(zxIdx)?.toDoubleOrNull() ?: 0.0,
                                rx = values.getOrNull(rxIdx)?.toDoubleOrNull() ?: 0.0,
                                iy = values.getOrNull(iyIdx)?.toDoubleOrNull() ?: 0.0,
                                sy = values.getOrNull(syIdx)?.toDoubleOrNull() ?: 0.0,
                                zy = values.getOrNull(zyIdx)?.toDoubleOrNull() ?: 0.0,
                                ry = values.getOrNull(ryIdx)?.toDoubleOrNull() ?: 0.0,
                                torsionalJ = values.getOrNull(jIdx)?.toDoubleOrNull() ?: 0.0,
                                warpingCw = values.getOrNull(cwIdx)?.toDoubleOrNull() ?: 0.0,
                                flatWidthB = null,
                                flatHeightH = null
                            )
                        } else {
                            AiscSectionRoomEntity(
                                id = values[idIdx].trim(),
                                designation = values[labelIdx].trim(),
                                type = ShapeTypeRect,
                                area = values.getOrNull(areaIdx)?.toDoubleOrNull() ?: 0.0,
                                depth = values.getOrNull(htIdx)?.toDoubleOrNull() ?: 0.0,
                                webThickness = tdes,
                                flangeWidth = values.getOrNull(bIdx)?.toDoubleOrNull() ?: 0.0,
                                flangeThickness = tdes,
                                ix = values.getOrNull(ixIdx)?.toDoubleOrNull() ?: 0.0,
                                sx = values.getOrNull(sxIdx)?.toDoubleOrNull() ?: 0.0,
                                zx = values.getOrNull(zxIdx)?.toDoubleOrNull() ?: 0.0,
                                rx = values.getOrNull(rxIdx)?.toDoubleOrNull() ?: 0.0,
                                iy = values.getOrNull(iyIdx)?.toDoubleOrNull() ?: 0.0,
                                sy = values.getOrNull(syIdx)?.toDoubleOrNull() ?: 0.0,
                                zy = values.getOrNull(zyIdx)?.toDoubleOrNull() ?: 0.0,
                                ry = values.getOrNull(ryIdx)?.toDoubleOrNull() ?: 0.0,
                                torsionalJ = values.getOrNull(jIdx)?.toDoubleOrNull() ?: 0.0,
                                warpingCw = values.getOrNull(cwIdx)?.toDoubleOrNull() ?: 0.0,
                                flatWidthB = values.getOrNull(flatBIdx)?.toDoubleOrNull(),
                                flatHeightH = values.getOrNull(flatHIdx)?.toDoubleOrNull()
                            )
                        }
                    } else {
                        AiscSectionRoomEntity(
                            id = values[idIdx].trim(),
                            designation = values[labelIdx].trim(),
                            type = rawType,
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
                    }
                    if (entity.id.isNotEmpty() && entity.type.isNotEmpty()) sections.add(entity)
                } catch (e: Exception) {
                    Log.w("AiscSectionSeeder", "Skipping malformed row: $line", e)
                }
            }
        }
        return sections
    }

    private companion object {
        /** Stored `type` values for HSS rows — see [repairHssRowsIfNeeded]. */
        const val ShapeTypeRect = "HSS_RECT"
        const val ShapeTypeRound = "HSS_ROUND"
    }
}