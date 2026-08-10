package com.lz.runtime.diagnostics

import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.api.RuntimeDiagnostics

class DefaultRuntimeDiagnostics(

    private val context: RuntimeContext

) : RuntimeDiagnostics {

    override fun dump(printer: (String) -> Unit) {

        printer("[$TAG] ========== Runtime ==========")

        printer(
            "[$TAG] Modules: ${context.runtimeModuleRegistry.modules().size}"
        )

        context.runtimeModuleRegistry
            .modules()
            .forEach {

                printer(
                    "[$TAG] Module : ${it.descriptor.id}"
                )

            }

        printer(
            "[$TAG] Capabilities: ${context.capabilityRegistry.capabilities().size}"
        )

        context.capabilityRegistry
            .capabilities()
            .forEach {

                printer(
                    "[$TAG] Capability : ${it.id}"
                )

            }

        printer("[$TAG] =============================")
    }

    companion object {

        private const val TAG = "VectOS.Runtime"

    }
}