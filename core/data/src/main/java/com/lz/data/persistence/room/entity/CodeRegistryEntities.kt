package com.lz.data.persistence.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "building_codes")
data class BuildingCodeEntity(
    @PrimaryKey val id: String, // e.g., "IBC_2021"
    val short_name: String,
    val long_name: String,
    val base_code_id: String? = null,
    val default_asd_set_id: String? = null,
    val default_lrfd_set_id: String? = null,
    val references_json: String = "{}" // Serialized Map<CodeReferenceKey, String>
)

@Entity(tableName = "standards")
data class StandardEntity(
    @PrimaryKey val id: String, // e.g., "ASCE_7_16"
    val short_name: String,
    val long_name: String,
    val references_json: String = "{}", // Serialized Map<StandardReferenceKey, String>

    // Added in schema v2. Nullable/additive so existing rows (and the
    // v1->v2 migration, which only ALTERs the table) don't need backfilling
    // beyond what the seeder repopulates on next seed run.
    //
    // material_type: MaterialType enum name this standard governs capacity
    //   for (e.g. "STEEL", "WOOD"); null for standards that aren't
    //   material-specific (e.g. ASCE 7, which governs loads).
    // edition_family: which StandardEdition subtype this row maps to —
    //   one of "ASCE7" / "AISC360" / "NDS" / "ACI318"; null if unmapped.
    // edition_key: the specific edition enum constant name within that
    //   family (e.g. "AISC_360_16", "NDS_2018", "ASCE_7_22").
    val material_type: String? = null,
    val edition_family: String? = null,
    val edition_key: String? = null
)

@Entity(
    tableName = "building_code_standard_cross_ref",
    primaryKeys = ["code_id", "standard_id"],
    foreignKeys = [
        ForeignKey(
            entity = BuildingCodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["code_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StandardEntity::class,
            parentColumns = ["id"],
            childColumns = ["standard_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("standard_id")]
)
data class BuildingCodeStandardCrossRef(
    val code_id: String,
    val standard_id: String
)

