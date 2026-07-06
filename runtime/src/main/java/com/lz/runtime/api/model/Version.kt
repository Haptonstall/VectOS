package com.lz.runtime.api.model

/**
 * Semantic version.
 *
 * Examples:
 *
 * 1.0.0
 * 1.2.5
 * 2.0.0
 */
data class Version(

    val major: Int,

    val minor: Int,

    val patch: Int

) : Comparable<Version> {

    override fun compareTo(
        other: Version
    ): Int {

        if (major != other.major) {
            return major.compareTo(other.major)
        }

        if (minor != other.minor) {
            return minor.compareTo(other.minor)
        }

        return patch.compareTo(other.patch)
    }

    override fun toString(): String {

        return "$major.$minor.$patch"
    }

    companion object {

        val ZERO = Version(0, 0, 0)

        fun parse(
            value: String
        ): Version {

            val pieces =
                value.split('.')

            require(
                pieces.size == 3
            ) {
                "Invalid semantic version: $value"
            }

            return Version(

                major = pieces[0].toInt(),

                minor = pieces[1].toInt(),

                patch = pieces[2].toInt()

            )
        }
    }
}