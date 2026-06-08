package com.lz.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

/**
 * Kotlinx serialization serializer for java.util.UUID.
 *
 * Serializes UUID as its standard string representation (e.g. "550e8400-e29b-41d4-a716-446655440000").
 * Used wherever UUID fields appear in @Serializable data classes:
 *
 *   @Serializable(with = UUIDSerializer::class)
 *   val id: UUID = UUID.randomUUID()
 */
object UUIDSerializer : KSerializer<UUID> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}