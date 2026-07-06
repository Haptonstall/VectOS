package com.lz.runtime.api

/**
 * Represents the current lifecycle state of the RuntimeEnvironment.
 */
enum class RuntimeState {

    /**
     * RuntimeEnvironment object has been created.
     */
    CREATED,

    /**
     * RuntimeEnvironment is discovering installed modules.
     */
    DISCOVERING,

    /**
     * RuntimeEnvironment is initializing services.
     */
    INITIALIZING,

    /**
     * RuntimeEnvironment is fully operational.
     */
    RUNNING,

    /**
     * RuntimeEnvironment is shutting down.
     */
    STOPPING,

    /**
     * RuntimeEnvironment has stopped.
     */
    STOPPED
}