package com.lz.runtime.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import com.lz.runtime.compose.screen.api.ScreenProvider

val LocalRuntimeNavigationArguments =
    compositionLocalOf<Map<String, String>> { emptyMap() }

/**
 * Resolves the correct ScreenProvider for a destination and delegates
 * rendering to the owning RuntimeModule.
 */
@Composable
fun RuntimeDestinationHost(

    destination: NavigationDestination,

    providers: List<ScreenProvider>,

    arguments: Map<String, String> = emptyMap()

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

    CompositionLocalProvider(LocalRuntimeNavigationArguments provides arguments) {
        provider.Content(destination)
    }

}
