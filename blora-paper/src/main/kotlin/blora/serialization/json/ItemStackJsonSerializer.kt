package blora.serialization.json

import blora.converter.jsonToItemStack
import blora.converter.toJson
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
import org.bukkit.inventory.ItemStack

object ItemStackJsonSerializer : KSerializer<ItemStack> {

    private val internalJson = Json {
        explicitNulls = true
        ignoreUnknownKeys = true
    }

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ItemStack") {}

    override fun deserialize(decoder: Decoder): ItemStack {
        return this.internalJson.encodeToString(
            decoder.decodeSerializableValue(JsonElement.serializer())
        ).jsonToItemStack()
    }

    override fun serialize(
        encoder: Encoder,
        value: ItemStack
    ) {
        encoder.encodeSerializableValue(
            JsonElement.serializer(),
            this.internalJson.decodeFromString(
                JsonElement.serializer(),
                value.toJson()
            )
        )
    }

}