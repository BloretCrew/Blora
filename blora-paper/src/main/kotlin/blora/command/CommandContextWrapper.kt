package blora.command

import blora.player.QuickPlayerWrapper
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import com.mojang.brigadier.context.CommandContext as NMSCommandContext

open class CommandContextWrapper(
    private val context: NMSCommandContext<net.minecraft.commands.CommandSourceStack>
) : blora.api.command.CommandContext {

    val nmsInstance: NMSCommandContext<net.minecraft.commands.CommandSourceStack>
        get() = this.context

    override val invoker: blora.api.command.CommandInvoker
        get() = if (this.context.source.isPlayer) {
            QuickPlayerWrapper(context.source.bukkitSender as Player)
        } else {
            CommandInvokerWrapper(context.source.bukkitSender)
        }
    override val alias: String
        get() {
            val input = this.context.input.trim()
            return if (input.contains(" ")) {
                input.substring(input.indexOf(" "))
            } else {
                input
            }
        }
    override val input: String
        get() = this.context.input

}

class SuggestionContextWrapper(
    context: NMSCommandContext<net.minecraft.commands.CommandSourceStack>,
    private val suggestionsBuilder: SuggestionsBuilder
) : CommandContextWrapper(context), blora.api.command.SuggestionContext {

    override val start: Int
        get() = this.suggestionsBuilder.start
    override val remaining: String
        get() = this.suggestionsBuilder.remaining

    override fun suggest(text: String, tooltip: Component?) {
        if (tooltip != null) {
            this.suggestionsBuilder.suggest(text, io.papermc.paper.adventure.PaperAdventure.asVanilla(tooltip))
        } else {
            this.suggestionsBuilder.suggest(text)
        }
    }

    override fun suggest(text: Int, tooltip: Component?) {
        if (tooltip != null) {
            this.suggestionsBuilder.suggest(text, io.papermc.paper.adventure.PaperAdventure.asVanilla(tooltip))
        } else {
            this.suggestionsBuilder.suggest(text)
        }
    }


}