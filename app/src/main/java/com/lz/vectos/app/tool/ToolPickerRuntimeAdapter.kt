package com.lz.vectos.app.tool

import com.lz.runtime.api.NavigationDestination
import com.lz.runtime.api.RuntimeEnvironment

/**
 * Converts Runtime capabilities into ToolPicker models.
 */
object ToolPickerRuntimeAdapter {

    fun loadTools(

        runtime: RuntimeEnvironment

    ): List<NavigationDestination> {

        return runtime
            .context
            .navigationRegistry
            .destinations()
            .sortedBy {

                it.title

            }

    }

}