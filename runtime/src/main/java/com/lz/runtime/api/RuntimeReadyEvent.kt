package com.lz.runtime.api

data class RuntimeReadyEvent(

    override val timestamp: Long = System.currentTimeMillis()

) : RuntimeEvent {

    override val id: String =
        "runtime.ready"

}