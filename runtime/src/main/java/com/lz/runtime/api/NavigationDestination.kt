package com.lz.runtime.api

/**
 * Logical destination exposed by a RuntimeEnvironment capability.
 *
 * A NavigationDestination intentionally has no knowledge of
 * Jetpack Navigation, Compose, Activities or Fragments.
 *
 * The Application Shell maps these destinations to the
 * appropriate UI implementation.
 */
interface NavigationDestination {

    /**
     * Globally unique destination id.
     *
     * Example:
     *
     * beam.calculator
     * beam.results
     * column.calculator
     */
    val id: String

    /**
     * Human readable title.
     */
    val title: String

    /**
     * Optional description.
     */
    val description: String?

    /**
     * Owning module.
     */
    val runtimeModuleId: String
}