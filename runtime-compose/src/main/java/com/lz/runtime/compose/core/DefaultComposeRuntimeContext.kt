package com.lz.runtime.compose.core

import com.lz.runtime.api.RuntimeContext
import com.lz.runtime.compose.api.ComposeRuntimeContext
import com.lz.runtime.compose.screen.api.ScreenRegistry

class DefaultComposeRuntimeContext (
    override val runtimeContext: RuntimeContext,

    override val screenRegistry: ScreenRegistry
) : ComposeRuntimeContext