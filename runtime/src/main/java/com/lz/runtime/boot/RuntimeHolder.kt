package com.lz.runtime.boot

import com.lz.runtime.api.RuntimeEnvironment

object RuntimeHolder {

    lateinit var runtimeEnvironment: RuntimeEnvironment
        private set

    fun initialize(
        runtimeEnvironment: RuntimeEnvironment
    ) {

        this.runtimeEnvironment = runtimeEnvironment

    }

    fun isInitialized(): Boolean {

        return ::runtimeEnvironment.isInitialized

    }
}