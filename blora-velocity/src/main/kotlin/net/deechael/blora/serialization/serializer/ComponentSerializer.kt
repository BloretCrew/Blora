package net.deechael.blora.serialization.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModuleBuilder
import net.benwoodworth.knbt.NbtCompound
import net.deechael.blora.converter.toKnbt
import net.deechael.blora.dialog.DIALOG_JSON
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

object JsonComponentSerializer : KSerializer<Component> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Component") {
    }

    override fun deserialize(decoder: Decoder): Component {
        throw RuntimeException()
    }

    override fun serialize(
        encoder: Encoder,
        value: Component
    ) {
        encoder.encodeSerializableValue(
            JsonElement.serializer(),
            DIALOG_JSON.decodeFromString<JsonElement>(GsonComponentSerializer.gson().serialize(value))
        )
    }

}

object NbtComponentSerializer : KSerializer<Component> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Component") {
    }

    override fun deserialize(decoder: Decoder): Component {
        throw RuntimeException()
    }

    override fun serialize(
        encoder: Encoder,
        value: Component
    ) {
        encoder.encodeSerializableValue(
            NbtCompound.serializer(),
            value.toKnbt()
        )
    }

}

fun SerializersModuleBuilder.jsonComponentSupport() {
    contextual(Component::class, JsonComponentSerializer)
}

fun SerializersModuleBuilder.nbtComponentSupport() {
    contextual(Component::class, NbtComponentSerializer)
}