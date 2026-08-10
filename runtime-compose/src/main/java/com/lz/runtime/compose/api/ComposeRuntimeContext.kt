package com.lz.runtime.compose.api

import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.compose.screen.api.ScreenRegistry

interface ComposeRuntimeContext {
    val runtimeContext: RuntimeContext

    val screenRegistry: ScreenRegistry

}