package blora.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import io.papermc.paper.command.brigadier.APICommandMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.asCompletableFuture
import org.bukkit.command.BlockCommandSender
import org.bukkit.entity.Player
import blora.api.command.CommandExecutor
import blora.api.command.CommandMeta
import blora.api.scheduler.BukkitAsync
import blora.command.argument.QuickArgumentLibWrapper
import blora.message.DefaultMessages
import blora.nms.nms
import blora.nms.nmsServer
import blora.player.QuickPlayerWrapper
import blora.plugin.BloraPlugin
import java.util.function.Predicate
import com.mojang.brigadier.tree.ArgumentCommandNode as NMSArgumentCommandNode
import com.mojang.brigadier.tree.LiteralCommandNode as NMSLiteralCommandNode

object QuickCommandLibWrapper : blora.api.command.QuickCommandLib {
    override fun createCommand(
        meta: CommandMeta,
        name: String,
        requirement: Predicate<blora.api.command.CommandInvoker>,
        executor: CommandExecutor?,
        playerExecutor: CommandExecutor?,
        blockExecutor: CommandExecutor?,
        children: List<blora.api.command.CommandNode>
    ): blora.api.command.Command {
        return CommandWrapper(
            meta,
            name,
            requirement,
            executor,
            playerExecutor,
            blockExecutor,
            children
        )
    }

    override fun createLiteralCommandNode(
        name: String,
        requirement: Predicate<blora.api.command.CommandInvoker>,
        executor: (blora.api.command.CommandContext.() -> Unit)?,
        playerExecutor: (blora.api.command.CommandContext.() -> Unit)?,
        blockExecutor: (blora.api.command.CommandContext.() -> Unit)?,
        children: List<blora.api.command.CommandNode>
    ): blora.api.command.LiteralCommandNode {
        return LiteralCommandNodeWrapper(
            name,
            requirement,
            executor,
            playerExecutor,
            blockExecutor,
            children
        )
    }

    override fun <T> createArgumentCommandNode(
        argumentType: blora.api.command.argument.ArgumentType<T>,
        suggestions: blora.api.command.Suggestions?,
        name: String,
        requirement: Predicate<blora.api.command.CommandInvoker>,
        executor: (blora.api.command.CommandContext.() -> Unit)?,
        playerExecutor: (blora.api.command.CommandContext.() -> Unit)?,
        blockExecutor: (blora.api.command.CommandContext.() -> Unit)?,
        children: List<blora.api.command.CommandNode>
    ): blora.api.command.ArgumentCommandNode<T> {
        return ArgumentCommandNodeWrapper(
            argumentType,
            suggestions,
            name,
            requirement,
            executor,
            playerExecutor,
            blockExecutor,
            children
        )
    }

    override fun getArgumentLib(): blora.api.command.argument.QuickArgumentLib {
        return QuickArgumentLibWrapper
    }

    override fun buildSuggestions(
        async: Boolean,
        builder: blora.api.command.SuggestionContext.() -> Unit
    ): blora.api.command.Suggestions {
        return SuggestionsWrapper(
            async,
            builder
        )
    }

    override fun createMeta(): blora.api.command.CommandMetaBuilder {
        return CommandMetaBuilderWrapper()
    }

    override fun registerCommand(command: blora.api.command.Command) {
        // (commands as PaperCommands).registerWithFlagsInternal()
        val builtCommandNode = buildCommand(command)
        builtCommandNode.apiCommandMeta = buildPaperMeta(command.meta)
        registerIntoDispatcher(builtCommandNode)
        registerIntoDispatcher(
            io.papermc.paper.command.brigadier.PaperBrigadier.copyLiteral(
                "${command.meta.namespace}:${command.name}",
                builtCommandNode
            )
        )
        for (alias in command.meta.aliases) {
            registerIntoDispatcher(
                io.papermc.paper.command.brigadier.PaperBrigadier.copyLiteral(
                    alias,
                    builtCommandNode
                )
            )
            registerIntoDispatcher(
                io.papermc.paper.command.brigadier.PaperBrigadier.copyLiteral(
                    "${command.meta.namespace}:$alias",
                    builtCommandNode
                )
            )
        }
    }

}

private fun buildPaperMeta(commandMeta: CommandMeta): APICommandMeta {
    return APICommandMeta(
        BloraPlugin.pluginMeta,
        commandMeta.description,
        commandMeta.aliases,
        null
    )
}

private fun tryExecutePlayer(
    source: net.minecraft.commands.CommandSourceStack,
    context: blora.api.command.CommandContext,
    command: blora.api.command.CommandNode
) {
    if (source.isPlayer && command.playerExecutor != null) {
        context.apply(command.playerExecutor!!)
        return
    }
    tryExecuteBlock(source, context, command)
}

private fun tryExecuteBlock(
    source: net.minecraft.commands.CommandSourceStack,
    context: blora.api.command.CommandContext,
    command: blora.api.command.CommandNode
) {
    if (source.bukkitSender is BlockCommandSender) {
        if (command.blockExecutor != null) {
            context.apply(command.blockExecutor!!)
        } else {
            if (command.executor != null) {
                context.apply(command.executor!!)
            } else {
                if (command.playerExecutor != null) {
                    source.bukkitSender.sendMessage(DefaultMessages.Errors.Commands.MUST_BE_PLAYER)
                } else {
                    source.bukkitSender.sendMessage(DefaultMessages.Errors.Commands.NO_SUITABLE_EXECUTOR)
                }
            }
        }
    } else {
        if (command.executor != null) {
            context.apply(command.executor!!)
        } else {
            if (command.playerExecutor != null) {
                source.bukkitSender.sendMessage(DefaultMessages.Errors.Commands.MUST_BE_PLAYER)
            } else if (command.blockExecutor != null) {
                source.bukkitSender.sendMessage(DefaultMessages.Errors.Commands.MUST_BE_BLOCK)
            } else {
                source.bukkitSender.sendMessage(DefaultMessages.Errors.Commands.NO_SUITABLE_EXECUTOR)
            }
        }
    }
}

internal fun buildCommand(command: blora.api.command.Command): NMSLiteralCommandNode<net.minecraft.commands.CommandSourceStack> {
    return buildLiteral(command)
}

internal fun ArgumentBuilder<net.minecraft.commands.CommandSourceStack, *>.buildShared(command: blora.api.command.CommandNode) {
    this.requires {
        return@requires command.requirement.test(
            if (it.isPlayer) {
                QuickPlayerWrapper(it.bukkitSender as Player)
            } else {
                CommandInvokerWrapper(it.bukkitSender)
            }
        )
    }
        .executes(null)
        .apply {
            if (command.executor != null || command.playerExecutor != null || command.blockExecutor != null) {
                this.executes {
                    val context = CommandContextWrapper(it)
                    tryExecutePlayer(it.source, context, command)
                    return@executes 1
                }
            }
        }
        .apply {
            for (node in command.children) {
                if (node is blora.api.command.LiteralCommandNode) {
                    this.then(buildLiteral(node))
                } else if (node is blora.api.command.ArgumentCommandNode<*>) {
                    this.then(buildArgument(node))
                } else {
                    BloraPlugin.slF4JLogger.warn("试图注册一个未知类型的命令节点")
                }
            }
        }
}

internal fun buildLiteral(command: blora.api.command.LiteralCommandNode): NMSLiteralCommandNode<net.minecraft.commands.CommandSourceStack> {
    return net.minecraft.commands.Commands.literal(command.name)
        .apply {
            buildShared(command)
        }
        .build()
}

internal fun buildArgument(command: blora.api.command.ArgumentCommandNode<*>): NMSArgumentCommandNode<net.minecraft.commands.CommandSourceStack, *> {
    val argumentType: ArgumentType<*> = command.argumentType.nms()
    return net.minecraft.commands.Commands.argument(command.name, argumentType)
        .apply {
            buildShared(command)
            val suggestions = command.suggestions
            if (suggestions != null) {
                suggests { context, suggestionsBuilder ->
                    val context = SuggestionContextWrapper(
                        context,
                        suggestionsBuilder
                    )
                    if (suggestions.async) {
                        return@suggests blora.api.scheduler.bukkitTask(Dispatchers.BukkitAsync) {
                            context.apply(suggestions.builder)
                            return@bukkitTask suggestionsBuilder.build()
                        }.asCompletableFuture()
                    } else {
                        context.apply(suggestions.builder)
                        return@suggests suggestionsBuilder.buildFuture()
                    }
                }
            }
        }
        .build()
}

internal fun minecraftCommands(): net.minecraft.commands.Commands {
    return nmsServer().commands
}

internal fun minecraftDispatcher(): CommandDispatcher<net.minecraft.commands.CommandSourceStack> {
    return minecraftCommands().dispatcher
}

internal fun registerIntoDispatcher(commandNode: NMSLiteralCommandNode<net.minecraft.commands.CommandSourceStack>) {
    val existed = minecraftDispatcher().root.getChild(commandNode.literal)
    if (existed != null) {
        minecraftDispatcher().root.removeCommand(commandNode.literal)
    }
    minecraftDispatcher().root.addChild(commandNode)
}

fun updateCommandList(player: Player) {
    // TODO
    (player as org.bukkit.craftbukkit.entity.CraftPlayer)
}