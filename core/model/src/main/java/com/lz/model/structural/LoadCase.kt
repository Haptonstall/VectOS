package com.lz.model.structural

/**
 * Categorizes loads into engineering-standard cases.
 */
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