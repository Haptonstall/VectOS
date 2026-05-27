package com.lz.vectos.domain.structural

import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.Moment
import kotlin.math.pow

/**
 * Pure Kotlin service to resolve governing load effects across multiple combinations.
 * Uses linear superposition for simply supported beam members.
 */
object LoadResolutionService {

    /**
     * Evaluates all combinations and determines the governing analysis envelope.
     */
    fun resolveEnvelope(
        member: StructuralMember,
        loadCases: List<LoadCase>,
        combinations: List<LoadCombination>,
        modulusOfElasticityPa: Double,
        momentOfInertiaM4: Double
    ): AnalysisEnvelope {
        val results = combinations.map { combo ->
            computeCombinationResult(member, loadCases, combo, modulusOfElasticityPa, momentOfInertiaM4)
        }

        // Determine governing effects (Envelopes)
        // For this framework, we take the absolute maximum of moments, shear, and deflection.
        val governingMoment = results.maxByOrNull { it.value.maxMoment.value.newtonMeters }!!
        val governingShear = results.maxByOrNull { it.value.maxShear.value.newtons }!!
        val governingDeflection = results.maxByOrNull { it.value.maxDeflection.value.meters }!!

        return AnalysisEnvelope(
            maxMoment = GoverningEffect(
                governingMoment.value.maxMoment.value,
                governingMoment.value.maxMoment.location,
                governingMoment.name
            ),
            maxShear = GoverningEffect(
                governingShear.value.maxShear.value,
                governingShear.value.maxShear.location,
                governingShear.name
            ),
            maxDeflection = GoverningEffect(
                governingDeflection.value.maxDeflection.value,
                governingDeflection.value.maxDeflection.location,
                governingDeflection.name
            )
        )
    }

    private fun computeCombinationResult(
        member: StructuralMember,
        loadCases: List<LoadCase>,
        combination: LoadCombination,
        e: Double,
        i: Double
    ): CombinationResult {
        var totalMomentNm = 0.0
        var totalShearN = 0.0
        var totalDeflectionM = 0.0
        
        val l = member.length.meters

        combination.factors.forEach { (caseId, factor) ->
            val case = loadCases.find { it.id == caseId } ?: return@forEach
            case.loads.forEach { load ->
                val effect = computeLoadEffect(load, l, e, i)
                totalMomentNm += effect.momentNm * factor
                totalShearN += effect.shearN * factor
                totalDeflectionM += effect.deflectionM * factor
            }
        }

        return CombinationResult(
            name = combination.name,
            value = AnalysisResult(
                maxMoment = AnalysisValue(Moment(totalMomentNm), l / 2.0),
                maxShear = AnalysisValue(Force(totalShearN), 0.0),
                maxDeflection = AnalysisValue(Length(totalDeflectionM), l / 2.0)
            )
        )
    }

    /**
     * Internal simplified solver for standard load cases on a simply supported beam.
     * These equations are standard engineering formulas.
     */
    private fun computeLoadEffect(load: Load, l: Double, e: Double, i: Double): LoadEffect {
        return when (load) {
            is Load.PointLoad -> {
                val p = load.value
                val a = load.locationStart
                val b = l - a
                LoadEffect(
                    momentNm = if (l > 0) (p * a * b) / l else 0.0,
                    shearN = if (l > 0) maxOf(p * b / l, p * a / l) else 0.0,
                    deflectionM = if (l > 0 && e * i > 0) (p * a * b * (l + a)) * (3 * a * (l + a)).pow(0.5) / (27 * e * i * l) else 0.0 // Approx or specific
                )
            }
            is Load.UniformDistributedLoad -> {
                // Assuming full UDL for base logic as per legacy solver
                val w = load.value
                LoadEffect(
                    momentNm = (w * l.pow(2)) / 8.0,
                    shearN = (w * l) / 2.0,
                    deflectionM = (5.0 * w * l.pow(4)) / (384.0 * e * i)
                )
            }
            is Load.TrapezoidalLoad -> LoadEffect(0.0, 0.0, 0.0) // Placeholder
        }
    }

    private data class CombinationResult(val name: String, val value: AnalysisResult)
    private data class AnalysisResult(val maxMoment: AnalysisValue<Moment>, val maxShear: AnalysisValue<Force>, val maxDeflection: AnalysisValue<Length>)
    private data class AnalysisValue<T>(val value: T, val location: Double)
    private data class LoadEffect(val momentNm: Double, val shearN: Double, val deflectionM: Double)
}
