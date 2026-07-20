package com.lz.vectos.app.platform

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.lz.runtime.compose.navigation.NavigationDestination

@Composable
fun RuntimeDestinationHost(

    destination: NavigationDestination

) {

    val provider =
        AndroidScreenRegistry.providerFor(

            destination.id

        )

    if (provider == null) {

        Text(

            "No provider registered for ${destination.id}"

        )

        return
    }

    provider.Content(

        destination

    )
}