package com.lz.vectos.app.navigation

import androidx.compose.runtime.Composable
import com.lz.runtime.compose.screen.RuntimeScreenFactory

interface ComposeScreenFactory :
    RuntimeScreenFactory {

    @Composable
    fun Content()
}