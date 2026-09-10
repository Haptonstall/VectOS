package com.lz.model.structural

import com.lz.model.regulatory.LoadCategory
import kotlinx.serialization.Serializable

/**
 * Categorizes loads into engineering-standard cases.
 */
@Serializable
data class LoadCase(
    val id: String,
    val name: String,
    val loads: List<Load> = emptyList()
)

object StandardLoadCases {
    const val DEAD = "DL"
    const val LIVE = "LL"
    const val ROOF_LIVE = "RLL"
    const val SNOW = "SL"
    const val WIND = "WL"
    const val SEISMIC = "EL"
    const val RAIN = "RL"
}

/**
 * Maps a [StandardLoadCases] id (the tab/case a load was entered under) to the
 * [LoadCategory] the solver actually groups loads by. Every load previously
 * defaulted to [LoadCategory.DEAD] regardless of which case tab it was added
 * under (nothing ever called this) — that silently folded Live/Snow/Wind/etc.
 * loads into Dead, which is why combos that should differ (e.g. "D" vs "D+L")
 * came out numerically identical.
 */
fun String.toLoadCategory(): LoadCategory = when (this) {
    StandardLoadCases.DEAD -> LoadCategory.DEAD
    StandardLoadCases.LIVE -> LoadCategory.LIVE
    StandardLoadCases.ROOF_LIVE -> LoadCategory.ROOF_LIVE
    StandardLoadCases.SNOW -> LoadCategory.SNOW
    StandardLoadCases.WIND -> LoadCategory.WIND
    StandardLoadCases.SEISMIC -> LoadCategory.SEISMIC
    StandardLoadCases.RAIN -> LoadCategory.RAIN
    else -> LoadCategory.DEAD
}
