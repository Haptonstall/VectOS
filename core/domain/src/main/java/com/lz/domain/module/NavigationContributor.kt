package com.lz.domain.module

import androidx.navigation.NavGraphBuilder

interface NavigationContributor {

    fun registerNavigation(
        navGraphBuilder: NavGraphBuilder
    )
}