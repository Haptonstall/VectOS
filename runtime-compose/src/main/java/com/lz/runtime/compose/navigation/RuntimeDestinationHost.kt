package com.lz.runtime.compose.navigation

import androidx.compose.runtime.Composable
import com.lz.runtime.compose.screen.api.ScreenProvider

/**
 * Resolves the correct ScreenProvider for a destination and delegates
 * rendering to the owning RuntimeModule.
 */
@Composable
fun RuntimeDestinationHost(

    destination: NavigationDestination,

    providers: List<ScreenProvider>

) {

    val provider =

        providers.firstOrNull { provider ->

            provider
                .destinations()
                .any { destinationItem ->

                    destinationItem.id == destination.id

                }

        }

            ?: error(

                "No ScreenProvider registered for destination '${destination.id}'."

            )

    provider.Content(destination)

}