package com.lz.beam.runtime

import com.lz.runtime.compose.navigation.NavigationDestination

/**
 * Logical navigation destination exposed by the Beam module.
 *
 * This contains no Compose implementation details.
 */
object BeamNavigationDestination : NavigationDestination {

    override val id: String =
        "beam.calculator"

    override val title: String =
        "Beam Calculator"

    override val description: String? =
        "Beam analysis and design"

    override val runtimeModuleId: String =
        BeamDescriptor.id

}