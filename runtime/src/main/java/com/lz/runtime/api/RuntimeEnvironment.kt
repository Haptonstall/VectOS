package com.lz.runtime.api

/**
 * Primary entry point into the VectOS RuntimeEnvironment.
 *
 * The RuntimeEnvironment owns:
 *
 * - Services
 * - Modules
 * - Capabilities
 * - Navigation
 * - Licensing
 * - Marketplace
 *
 * The Application Shell communicates exclusively through
 * this contract.
 */
interface RuntimeEnvironment {

    /**
     * Current RuntimeEnvironment configuration.
     */
    val configuration: RuntimeConfiguration

    /**
     * Current RuntimeEnvironment lifecycle state.
     */
    val state: RuntimeState

    /**
     * RuntimeEnvironment context.
     */
    val context: RuntimeContext

    /**
     * Starts the RuntimeEnvironment.
     */
    fun start()

    /**
     * Stops the RuntimeEnvironment.
     */
    fun stop()
}