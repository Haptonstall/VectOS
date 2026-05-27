package com.lz.vectos.export

import com.lz.vectos.domain.beam.BeamCalculation
import com.lz.vectos.domain.structural.analysis.BeamAnalysisResult
import com.lz.vectos.domain.structural.analysis.ReactionResult
import com.lz.vectos.domain.units.*
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Shared formatting logic for calculations.
 * Used by ViewModels for UI display and Exporters for file output.
 */
object CalculationFormatter {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun format(calculation: BeamCalculation, system: UnitSystem): FormattedResults {
        val results = calculation.results
        
        val analysis = results.analysisResult
        
        val momentUnit = if (system == UnitSystem.METRIC) MomentUnit.NEWTON_METERS else MomentUnit.POUND_FEET
        val shearUnit = if (system == UnitSystem.METRIC) ForceUnit.NEWTONS else ForceUnit.POUNDS_FORCE
        val deflectionUnit = if (system == UnitSystem.METRIC) LengthUnit.METERS else LengthUnit.INCHES

        val momentVal = UnitConverter.toDisplayValue(analysis.maxMoment, momentUnit)
        val shearVal = UnitConverter.toDisplayValue(analysis.maxShear, shearUnit)
        val deflectionVal = UnitConverter.toDisplayValue(analysis.maxDeflection, deflectionUnit)

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

    fun formatAnalysisSummary(analysis: BeamAnalysisResult, system: UnitSystem): FormattedAnalysisSummary {
        return FormattedAnalysisSummary(
            maxMoment = UnitFormattingService.formatMoment(analysis.maxMoment, system),
            minMoment = UnitFormattingService.formatMoment(analysis.minMoment, system),
            maxMomentY = UnitFormattingService.formatMoment(analysis.maxMomentY, system),
            maxShear = UnitFormattingService.formatForce(analysis.maxShear, system),
            maxShearY = UnitFormattingService.formatForce(analysis.maxShearY, system),
            maxTorsion = UnitFormattingService.formatMoment(analysis.maxTorsion, system),
            maxDeflection = UnitFormattingService.formatSmallLength(analysis.maxDeflection, system)
        )
    }

    data class FormattedAnalysisSummary(
        val maxMoment: String,
        val minMoment: String,
        val maxMomentY: String,
        val maxShear: String,
        val maxShearY: String,
        val maxTorsion: String,
        val maxDeflection: String
    )

    fun formatReaction(reaction: ReactionResult, system: UnitSystem): FormattedReaction {
        return FormattedReaction(
            label = reaction.label,
            verticalForce = UnitFormattingService.formatForce(reaction.verticalForce, system),
            lateralForce = UnitFormattingService.formatForce(reaction.lateralForce, system),
            moment = UnitFormattingService.formatMoment(reaction.moment, system),
            momentY = UnitFormattingService.formatMoment(reaction.momentY, system),
            torque = UnitFormattingService.formatMoment(reaction.torque, system)
        )
    }

    data class FormattedReaction(
        val label: String,
        val verticalForce: String,
        val lateralForce: String,
        val moment: String,
        val momentY: String,
        val torque: String
    )
}
