package com.lz.vectos.app.runtime

import androidx.lifecycle.ViewModel
import com.lz.runtime.api.RuntimeEnvironment

/**
 * Exposes Runtime to the UI layer.
 */
class RuntimeViewModel : ViewModel() {

    val runtime: RuntimeEnvironment

        get() = RuntimeManager.runtime()

}