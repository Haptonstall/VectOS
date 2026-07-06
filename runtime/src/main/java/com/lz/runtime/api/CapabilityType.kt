package com.lz.runtime.api

/**
 * Defines the categories of functionality that a RuntimeModule may contribute
 * to the VectOS RuntimeEnvironment.
 *
 * The RuntimeEnvironment discovers capabilities dynamically rather than having
 * compile-time knowledge of engineering modules.
 *
 * New capability types may be added in future RuntimeEnvironment versions without
 * impacting existing modules.
 */
enum class CapabilityType {

    /**
     * Engineering calculation tools.
     *
     * Example:
     *  - Beam Calculator
     *  - Column Calculator
     *  - Pole Calculator
     */
    CALCULATOR,

    /**
     * Calculation reports.
     */
    REPORT,

    /**
     * Data exporters.
     */
    EXPORT,

    /**
     * RuntimeModule specific settings pages.
     */
    SETTINGS,

    /**
     * Material database providers.
     */
    MATERIAL_PROVIDER,

    /**
     * Structural section database providers.
     */
    SECTION_PROVIDER,

    /**
     * Design code implementations.
     */
    DESIGN_CODE,

    /**
     * Extensions to project management.
     */
    PROJECT_EXTENSION,

    /**
     * Documentation providers.
     */
    DOCUMENTATION,

    /**
     * Future AI providers.
     */
    AI_PROVIDER
}