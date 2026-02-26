package com.lz.vectos.domain.beam

import com.lz.vectos.domain.calculation.CalculationMetadata
import com.lz.vectos.domain.units.Force
import com.lz.vectos.domain.units.Length
import com.lz.vectos.domain.units.Moment
import com.lz.vectos.domain.units.Pressure
import com.lz.vectos.domain.units.ForcePerLength
import com.lz.vectos.domain.units.UnitSystem
import kotlin.math.pow

object BeamCalculator {

    fun calculate(metadata: CalculationMetadata, inputs: BeamInputs): BeamCalculation {
        val l = inputs.span
        val e = inputs.material.modulusOfElasticity
        val i = inputs.momentOfInertia

        val results = when (inputs.loadType) {
            LoadType.POINT_LOAD_MIDSPAN -> {
                val p = Force(inputs.loadValue) // In base units (N)
                calculatePointLoad(l, p, e, i.metersToFourth)
            }
            LoadType.UNIFORMLY_DISTRIBUTED_LOAD -> {
                val w = ForcePerLength(inputs.loadValue) // In base units (N/m)
                calculateUDL(l, w, e, i.metersToFourth)
            }
        }

        return BeamCalculation(
            metadata = metadata,
            inputs = inputs,
            results = results,
            assumptions = Assumptions()
        )
    }

    private fun calculatePointLoad(l: Length, p: Force, e: Pressure, i: Double): BeamResults {
        val l_m = l.meters
        val p_n = p.newtons
        val e_pa = e.pascals

        val maxMoment = Moment((p_n * l_m) / 4.0)
        val maxShear = Force(p_n / 2.0)
        val maxDeflection = Length((p_n * l_m.pow(3)) / (48.0 * e_pa * i))

        return BeamResults(maxMoment, maxShear, maxDeflection)
    }

    private fun calculateUDL(l: Length, w: ForcePerLength, e: Pressure, i: Double): BeamResults {
        val l_m = l.meters
        val w_nm = w.newtonsPerMeter
        val e_pa = e.pascals

        val maxMoment = Moment((w_nm * l_m.pow(2)) / 8.0)
        val maxShear = Force((w_nm * l_m) / 2.0)
        val maxDeflection = Length((5.0 * w_nm * l_m.pow(4)) / (384.0 * e_pa * i))

        return BeamResults(maxMoment, maxShear, maxDeflection)
    }
}
