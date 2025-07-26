package blora.json

import blora.serialization.json.ComponentJsonSerializer
import blora.serialization.json.ItemStackJsonSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

val STORE_DATA_JSON = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

val MINECRAFT_DATA_JSON = Json {
    serializersModule = SerializersModule {
        contextual(Component::class, ComponentJsonSerializer)
        contextual(ItemStack::class, ItemStackJsonSerializer)
    }
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
}

val MINECRAFT_PRETTY_DATA_JSON = Json {
    serializersModule = SerializersModule {
        contextual(Component::class, ComponentJsonSerializer)
        contextual(ItemStack::class, ItemStackJsonSerializer)
    }
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
    prettyPrint = true
}