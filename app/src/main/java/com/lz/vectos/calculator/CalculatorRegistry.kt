package com.lz.vectos.calculator

object CalculatorRegistry {

    val calculators: List<CalculatorDefinition> = listOf(
        BeamCalculatorDefinition
    )

    fun getById(id: String): CalculatorDefinition =
        calculators.first { it.id == id }
}
