package com.lz.vectos.ui.tool

import com.lz.vectos.domain.units.*
import java.util.Locale

interface UnitFormatter {
    fun length(value: Double): String
    fun force(value: Double): String
    fun moment(value: Double): String
    fun pressure(value: Double): String
}

class EngineeringUnitFormatter(private val unitSystem: UnitSystem) : UnitFormatter {
    
    override fun length(value: Double): String {
        return if (unitSystem == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f m", value)
        } else {
            String.format(Locale.US, "%.2f ft", value / 0.3048)
        }
    }

    override fun force(value: Double): String {
        return if (unitSystem == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f kN", value / 1000.0)
        } else {
            String.format(Locale.US, "%.2f kip", value / 4448.2216)
        }
    }

    override fun moment(value: Double): String {
        return if (unitSystem == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f kN-m", value / 1000.0)
        } else {
            // N-m to kip-ft
            val kipFt = value * 0.73756 / 1000.0
            String.format(Locale.US, "%.2f kip-ft", kipFt)
        }
    }

    override fun pressure(value: Double): String {
        return if (unitSystem == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f MPa", value / 1e6)
        } else {
            String.format(Locale.US, "%.2f ksi", value / 6894757.29)
        }
    }
}
