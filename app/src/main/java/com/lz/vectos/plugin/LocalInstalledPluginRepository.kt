package com.lz.vectos.plugin

class LocalInstalledPluginRepository :
    InstalledPluginRepository {

    private val plugins =
        mutableListOf<PluginManifest>()

    override suspend fun getInstalledPlugins():
            List<PluginManifest> {

        return plugins.toList()
    }

    override suspend fun savePlugin(
        manifest: PluginManifest
    ) {

        plugins.removeAll {
            it.descriptor.id ==
                    manifest.descriptor.id
        }

        plugins.add(manifest)
    }

    override suspend fun removePlugin(
        moduleId: String
    ) {

        plugins.removeAll {
            it.descriptor.id == moduleId
        }
    }
}