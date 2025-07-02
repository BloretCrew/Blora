package net.deechael.blora.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModuleBuilder
import net.kyori.adventure.key.Key

object KeySerializer : KSerializer<Key> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Key") {
    }

    override fun deserialize(decoder: Decoder): Key {
        throw RuntimeException()
    }

    override fun serialize(
        encoder: Encoder,
        value: Key
    ) {
        encoder.encodeString(value.asString())
    }

}

fun SerializersModuleBuilder.keySupport() {
    contextual(Key::class, KeySerializer)
}