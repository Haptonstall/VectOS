package com.lz.vectos.runtime

import com.lz.runtime.model.InstalledModuleManifest
import com.lz.runtime.repository.InstalledModuleRepository

class DefaultInstalledModuleRepository :
    InstalledModuleRepository {

    override fun installedModules(): List<InstalledModuleManifest> {

        return listOf(

            InstalledModuleManifest(

                moduleId = "beam",

                providerClass =
                    "com.lz.beam.runtime.BeamRuntimeModuleProvider"

            )

        )

    }

}