package com.lz.runtime.api

interface RuntimeDiagnostics {
    fun dump(printer: (String) -> Unit = { println(it) })
}