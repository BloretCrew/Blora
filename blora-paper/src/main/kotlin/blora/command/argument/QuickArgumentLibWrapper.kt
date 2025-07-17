package blora.command.argument

import blora.internal.api.command.CommandContext
import blora.internal.api.command.argument.ArgumentType
import blora.command.CommandContextWrapper
import blora.nms.internalGetArgumentValue
import blora.nms.nms
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.network.chat.ComponentUtils
import org.bukkit.entity.Entity
import com.mojang.brigadier.context.CommandContext as NMSCommandContext

internal fun CommandContext.nms(): NMSCommandContext<net.minecraft.commands.CommandSourceStack> {
    return (this as CommandContextWrapper).nmsInstance
}

object QuickArgumentLibWrapper : blora.internal.api.command.argument.QuickArgumentLib {

    override fun getArguments(): blora.internal.api.command.argument.Arguments {
        return ArgumentsWrapper
    }

    override fun updateComponentForEntity(
        context: CommandContext,
        component: Component,
        entity: Entity
    ): Component {
        return PaperAdventure.asAdventure(
            ComponentUtils.updateForEntity(
                context.nms().source,
                PaperAdventure.asVanilla(component),
                entity.nms(),
                0
            )
        )
    }

    override fun <T> getArgumentValue(
        valueType: Class<T>,
        argumentType: ArgumentType<T>,
        context: CommandContext,
        name: String
    ): T {
        return internalGetArgumentValue(valueType, argumentType, context, name)
    }

}