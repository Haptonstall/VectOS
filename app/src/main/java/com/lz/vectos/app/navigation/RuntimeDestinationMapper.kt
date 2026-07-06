package com.lz.vectos.app.navigation

import com.lz.runtime.api.NavigationDestination

/**
 * Converts Runtime destinations into navigation routes.
 *
 * Runtime owns destination identifiers.
 *
 * Application owns navigation.
 */
object RuntimeDestinationMapper {

    fun route(

        destination: NavigationDestination

    ): String {

        return destination.id

    }

}