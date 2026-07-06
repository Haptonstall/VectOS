package com.lz.domain.module

interface CalculatorPlugin {

    val descriptor: ModuleDescriptor
    val entryPoint: ModuleEntryPoint

}