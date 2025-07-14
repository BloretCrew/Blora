package blora.adventure

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player

class PlaceholderAPITagResolver(val player: Player) : TagResolver {

    override fun resolve(
        name: String,
        arguments: ArgumentQueue,
        ctx: Context
    ): Tag? {
        if (name != "papi")
            return null

        val placeholder = arguments.popOr("papi tag requires an argument").value();
        val parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, "%$placeholder%");

        return Tag.selfClosingInserting(LegacyComponentSerializer.legacySection().deserialize(parsedPlaceholder));
    }

    override fun has(name: String): Boolean {
        return name == "papi"
    }

}