package com.lz.domain.plugin

interface CalculatorPlugin {

    val descriptor: ModuleDescriptor
    val entryPoint: PluginEntryPoint

}