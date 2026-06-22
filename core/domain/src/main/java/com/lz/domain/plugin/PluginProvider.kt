package com.lz.domain.plugin

interface PluginProvider {

    fun getPlugin(
        moduleId: String
    ): CalculatorPlugin?
}