package com.lz.beam.runtime

import com.lz.runtime.api.NavigationDestination

object BeamNavigationDestination :
    NavigationDestination {

    override val id =
        "beam.calculator"

    override val title =
        "Beam Calculator"

    override val description =
        "Beam calculations"

    override val moduleId =
        BeamDescriptor.id
}