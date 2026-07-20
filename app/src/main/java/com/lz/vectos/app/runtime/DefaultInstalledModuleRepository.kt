package com.lz.vectos.app.runtime

import com.lz.common.Version
import com.lz.runtime.api.marketplace.InstallState
import com.lz.runtime.api.marketplace.InstalledModule
import com.lz.runtime.api.marketplace.ModuleEntryPoint
import com.lz.runtime.api.marketplace.ModuleSource
import com.lz.runtime.repository.InstalledModuleRepository

class DefaultInstalledModuleRepository :
    InstalledModuleRepository {

    override fun installedModules(): List<InstalledModule> {

        return listOf(

            InstalledModule(

                moduleId = "beam",

                displayName = "Beam Calculator",

                version = Version(1, 0, 0),

                installState = InstallState.INSTALLED,

                enabled = true,

                source = ModuleSource.BUNDLED,

                featureName = null,

                entryPoint = ModuleEntryPoint(
                    value =
                        "com.lz.beam.api.BeamRuntimeModuleProvider"
                ),

                signature = null

            )

        )

    }

}
