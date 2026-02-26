package com.lz.vectos.export

import com.lz.vectos.domain.beam.BeamCalculation
import com.lz.vectos.domain.units.*
import java.time.format.DateTimeFormatter

/**
 * Shared formatting logic for calculations.
 * Used by ViewModels for UI display and Exporters for file output.
 */
object CalculationFormatter {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun format(calculation: BeamCalculation, system: UnitSystem): FormattedResults {
        val results = calculation.results
        
        val momentUnit = if (system == UnitSystem.METRIC) MomentUnit.NEWTON_METERS else MomentUnit.POUND_FEET
        val shearUnit = if (system == UnitSystem.METRIC) ForceUnit.NEWTONS else ForceUnit.POUNDS_FORCE
        val deflectionUnit = if (system == UnitSystem.METRIC) LengthUnit.METERS else LengthUnit.INCHES

        val momentVal = UnitConverter.toDisplayValue(results.maxBendingMoment, momentUnit)
        val shearVal = UnitConverter.toDisplayValue(results.maxShear, shearUnit)
        val deflectionVal = UnitConverter.toDisplayValue(results.maxDeflection, deflectionUnit)

        return FormattedResults(
            moment = "%.2f %s".format(momentVal, momentUnit.symbol),
            shear = "%.2f %s".format(shearVal, shearUnit.symbol),
            deflection = "%.4f %s".format(deflectionVal, deflectionUnit.symbol),
            timestamp = calculation.metadata.createdAt.format(dateFormatter)
        )
    }

    data class FormattedResults(
        val moment: String,
        val shear: String,
        val deflection: String,
        val timestamp: String
    )
}
