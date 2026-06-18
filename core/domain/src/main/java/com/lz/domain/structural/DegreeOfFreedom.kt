package com.lz.domain.structural

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