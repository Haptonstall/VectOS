package com.lz.model.structural

import kotlinx.serialization.Serializable

@Serializable
enum class DegreeOfFreedom (
    val displayName: String
) {
    UX("Translation X"),
    UY("Translation Y"),
    UZ("Translation Z"),
    RX("Rotation X"),
    RY("Rotation Y"),
    RZ("Rotation Z")
}