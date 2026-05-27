package com.lz.vectos.domain.units

import java.util.Locale

object UnitFormattingService {

    fun formatLength(length: Length, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f m", length.inMeters)
        } else {
            String.format(Locale.US, "%.2f ft", length.inFeet)
        }
    }
    
    fun formatSmallLength(length: Length, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.1f mm", length.inMm)
        } else {
            String.format(Locale.US, "%.2f in", length.inInches)
        }
    }

    fun formatForce(force: Force, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            if (force.inNewtons < 1000.0) {
                String.format(Locale.US, "%.0f N", force.inNewtons)
            } else {
                String.format(Locale.US, "%.2f kN", force.inKiloNewtons)
            }
        } else {
            if (force.inPoundsForce < 1000.0) {
                String.format(Locale.US, "%.0f lbf", force.inPoundsForce)
            } else {
                String.format(Locale.US, "%.2f kip", force.inPoundsForce / 1000.0)
            }
        }
    }

    fun formatDistributedLoad(load: ForcePerLength, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.1f N/m", load.newtonsPerMeter)
        } else {
            String.format(Locale.US, "%.1f lb/ft", load.poundsPerFoot)
        }
    }

    fun formatMoment(moment: Moment, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            if (moment.inNewtonMeters < 1000.0) {
                String.format(Locale.US, "%.1f N-m", moment.inNewtonMeters)
            } else {
                String.format(Locale.US, "%.2f kN-m", moment.inNewtonMeters / 1000.0)
            }
        } else {
            if (moment.inPoundFeet < 1000.0) {
                String.format(Locale.US, "%.1f lb-ft", moment.inPoundFeet)
            } else {
                String.format(Locale.US, "%.2f kip-ft", moment.inPoundFeet / 1000.0)
            }
        }
    }

    fun formatSmallMoment(moment: Moment, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.1f N-m", moment.inNewtonMeters)
        } else {
            String.format(Locale.US, "%.2f lb-in", moment.inPoundInches)
        }
    }

    fun formatPressure(pressure: Pressure, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.1f MPa", pressure.inMegaPascals)
        } else {
            String.format(Locale.US, "%.1f ksi", pressure.inPsi / 1000.0)
        }
    }

    /**
     * Formats Elastic Modulus rounded to the nearest whole number.
     */
    fun formatElasticModulus(pressure: Pressure, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.0f MPa", pressure.inMegaPascals)
        } else {
            String.format(Locale.US, "%.0f ksi", pressure.inPsi / 1000.0)
        }
    }

    fun formatStress(pressure: Pressure, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.1f MPa", pressure.inMegaPascals)
        } else {
            String.format(Locale.US, "%.0f psi", pressure.inPsi)
        }
    }

    fun formatArea(area: Area, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2f cm²", area.inCm2)
        } else {
            String.format(Locale.US, "%.2f in²", area.inIn2)
        }
    }

    fun formatInertia(inertia: MomentOfInertia, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2e m⁴", inertia.inM4)
        } else {
            String.format(Locale.US, "%.1f in⁴", inertia.inIn4)
        }
    }

    fun formatSectionModulus(sm: SectionModulus, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.1f cm³", sm.inCm3)
        } else {
            String.format(Locale.US, "%.2f in³", sm.inIn3)
        }
    }

    fun formatWarpingConstant(cw: Double, system: UnitSystem): String {
        return if (system == UnitSystem.METRIC) {
            String.format(Locale.US, "%.2e m⁶", cw)
        } else {
            val in6 = cw * 3.83375866e9
            String.format(Locale.US, "%.2e in⁶", in6)
        }
    }
    
    fun getMomentUnitSymbol(system: UnitSystem): String = if (system == UnitSystem.METRIC) "kN-m" else "kip-ft"
    fun getSmallMomentUnitSymbol(system: UnitSystem): String = if (system == UnitSystem.METRIC) "N-m" else "lb-ft"
    fun getForceUnitSymbol(system: UnitSystem): String = if (system == UnitSystem.METRIC) "kN" else "kips"
    fun getStressUnitSymbol(system: UnitSystem): String = if (system == UnitSystem.METRIC) "MPa" else "ksi"
    fun getSectionModulusUnitSymbol(system: UnitSystem): String = if (system == UnitSystem.METRIC) "cm³" else "in³"
}
