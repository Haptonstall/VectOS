package com.lz.ui.formatting

import com.lz.model.units.UnitSystem
import java.util.Locale

/**
 * Interface for formatting raw double values (assumed to be in internal Imperial base units)
 * into localized strings based on the selected UnitSystem.
 */
interface UnitFormatter {
    fun length(value: Double): String
    fun force(value: Double): String
    fun moment(value: Double): String
    fun pressure(value: Double): String
    fun distributedLoad(value: Double): String
}

class EngineeringUnitFormatter(private val unitSystem: UnitSystem) : UnitFormatter {

    override fun length(value: Double): String {
        // Internal base: Inches
        return if (unitSystem == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f m", value * 0.0254)
        } else {
            String.format(Locale.US, "%.2f ft", value / 12.0)
        }
    }

    override fun force(value: Double): String {
        // Internal base: Pounds (lbf)
        return if (unitSystem == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f kN", (value * 4.4482216153) / 1000.0)
        } else {
            String.format(Locale.US, "%.2f kip", value / 1000.0)
        }
    }

    override fun moment(value: Double): String {
        // Internal base: Pound-Inches (lb-in)
        return if (unitSystem == UnitSystem.METRIC) {
            val knM = (value * 0.112984829) / 1000.0
            String.format(Locale.US, "%.2f kN-m", knM)
        } else {
            String.format(Locale.US, "%.2f k-ft", value / (12.0 * 1000.0))
        }
    }

    override fun pressure(value: Double): String {
        // Internal base: PSI
        return if (unitSystem == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f MPa", (value * 6894.75729) / 1e6)
        } else {
            String.format(Locale.US, "%.2f ksi", value / 1000.0)
        }
    }

    override fun distributedLoad(value: Double): String {
        // Internal base: PLI (lb/in)
        return if (unitSystem == UnitSystem.METRIC) {
            String.format(Locale.US, "%.1f N/m", value * (4.4482216153 / 0.0254))
        } else {
            String.format(Locale.US, "%.1f lb/ft", value * 12.0)
        }
    }
}