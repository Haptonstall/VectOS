package com.lz.runtime.api

/**
 * Base RuntimeEnvironment event.
 *
 * RuntimeEnvironment services communicate using immutable events
 * instead of direct coupling.
 */
interface RuntimeEvent {

    /**
     * Event identifier.
     */
    val id: String

    /**
     * Time event occurred.
     */
    val timestamp: Long
}