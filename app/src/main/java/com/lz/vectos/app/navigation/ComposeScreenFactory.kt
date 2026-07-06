package com.lz.vectos.app.navigation

import androidx.compose.runtime.Composable
import com.lz.runtime.api.NavigationDestination
import com.lz.runtime.api.screen.RuntimeScreenFactory

interface ComposeScreenFactory :
    RuntimeScreenFactory {

    @Composable
    fun Content()
}