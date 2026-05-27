package com.lz.vectos.viewmodel

import com.lz.vectos.domain.units.*
import java.util.Locale

object UnitFormattingService {

    fun formatLength(length: Length, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f m", length.inMeters)
        } else {
            String.format(Locale.US, "%.2f ft", length.inFeet)
        }
    }

    fun formatForce(force: Force, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f kN", force.inKiloNewtons)
        } else {
            String.format(Locale.US, "%.2f kip", force.inPoundsForce / 1000.0)
        }
    }

    fun formatMoment(moment: Moment, system: UnitSystem): String {
        // base internal is N-m
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f kN-m", moment.inNewtonMeters / 1000.0)
        } else {
            // lb-ft to kip-ft
            val kipFt = (moment.inNewtonMeters * 0.7375621493) / 1000.0
            String.format(Locale.US, "%.2f kip-ft", kipFt)
        }
    }

    fun formatPressure(pressure: Pressure, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f MPa", pressure.inMegaPascals)
        } else {
            String.format(Locale.US, "%.2f ksi", pressure.inPsi / 1000.0)
        }
    }
}
