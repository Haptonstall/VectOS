package com.lz.runtime.api

data class RuntimeStoppedEvent(

    override val timestamp: Long = System.currentTimeMillis()

) : RuntimeEvent {

    override val id: String =
        "runtime.stopped"

}