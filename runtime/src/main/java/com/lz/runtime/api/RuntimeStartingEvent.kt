package com.lz.runtime.api

data class RuntimeStartingEvent(

    override val timestamp: Long = System.currentTimeMillis()

) : RuntimeEvent {

    override val id: String =
        "runtime.starting"

}