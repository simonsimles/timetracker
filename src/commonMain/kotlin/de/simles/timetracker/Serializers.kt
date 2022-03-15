package de.simles.timetracker

import de.simles.timetracker.models.Time
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object TimeSerializer : KSerializer<Time> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("de.simonsimles.timetracker.models.Time", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Time {
        return decoder.decodeString().split(":").let { splits -> Time(splits[0].toInt(), splits[1].toInt()) }
    }

    override fun serialize(encoder: Encoder, value: Time) {
        encoder.encodeString(value.toString())
    }
}