package com.lz.runtime.screen.internal

import com.lz.runtime.api.NavigationDestination
import com.lz.runtime.screen.api.ScreenProvider

internal data class ScreenRegistration(

    val destination: NavigationDestination,

    val provider: ScreenProvider
)