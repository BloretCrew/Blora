package blora.api.command.argument

import net.kyori.adventure.text.Component
import org.bukkit.entity.Entity
import blora.api.command.CommandContext
import blora.api.command.QuickCommandLib

interface QuickArgumentLib {

    fun getArguments(): Arguments

    fun updateComponentForEntity(context: CommandContext, component: Component, entity: Entity): Component

    fun <T> getArgumentValue(
        valueType: Class<T>,
        argumentType: ArgumentType<T>,
        context: CommandContext,
        name: String
    ): T

    companion object : QuickArgumentLib by QuickCommandLib.Companion.getArgumentLib()

}