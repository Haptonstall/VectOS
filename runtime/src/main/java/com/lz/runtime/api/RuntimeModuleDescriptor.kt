package com.lz.runtime.api

import com.lz.common.Version

/**
 * Immutable description of an installed VectOS RuntimeModule.
 *
 * This class contains only metadata.
 *
 * RuntimeEnvironment state such as installation status and licensing is managed
 * elsewhere.
 */
data class RuntimeModuleDescriptor(

    /**
     * Globally unique module identifier.
     *
     * Example:
     * beam
     * column
     * foundation
     */
    val id: String,

    /**
     * Human readable name.
     */
    val displayName: String,

    /**
     * Short description shown in Marketplace.
     */
    val description: String,

    /**
     * Semantic version.
     */
    val version: Version,

    /**
     * Author or publisher.
     */
    val publisher: String,

    /**
     * Minimum RuntimeEnvironment version required.
     */
    val minimumRuntimeEnvironmentVersion: Version,

    /**
     * RuntimeModule icon resource name.
     *
     * RuntimeEnvironment intentionally stores the resource name rather than a drawable
     * reference so that this contract remains Android-independent.
     */
    val iconKey: String,

    /**
     * Fully-qualified class name of this module's RuntimeModuleProvider
     * (e.g. "com.lz.beam.api.BeamRuntimeModuleProvider").
     *
     * Mirrors the same-named field on the domain-layer ModuleDescriptor
     * (see LocalModuleCatalogRepository), which is the not-yet-installed
     * counterpart of this descriptor.
     */
    val entryPointClassName: String,

    /**
     * Capability categories implemented by the module.
     */
    val capabilityTypes: Set<CapabilityType>
)