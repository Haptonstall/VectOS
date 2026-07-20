package com.lz.common

/**
 * High-level classification of a runtime module.
 */
enum class ModuleType {

    /**
     * Engineering calculator.
     */
    CALCULATION,

    /**
     * Reporting module.
     */
    REPORT,

    /**
     * Import / export.
     */
    EXPORT,

    /**
     * Utility or platform extension.
     */
    UTILITY
}