package blora.serialization.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

object ComponentJsonSerializer : KSerializer<Component> {

    private val internalJson = Json {
        explicitNulls = true
        ignoreUnknownKeys = true
    }

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Component") {}

    override fun deserialize(decoder: Decoder): Component {
        return GsonComponentSerializer.gson()
            .deserialize(
                this.internalJson.encodeToString(
                    decoder.decodeSerializableValue(JsonElement.serializer())
                )
            )
    }

    override fun serialize(
        encoder: Encoder,
        value: Component
    ) {
        encoder.encodeSerializableValue(
            JsonElement.serializer(),
            this.internalJson.decodeFromString(
                JsonElement.serializer(),
                GsonComponentSerializer.gson().serialize(value)
            )
        )
    }

}