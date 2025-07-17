package blora.internal.api.command

import blora.api.QuickLib
import blora.api.command.argument.ArgumentType
import blora.api.command.argument.QuickArgumentLib
import java.util.function.Predicate

interface BloraCommandLib {

    fun createCommand(
        meta: CommandMeta,
        name: String,
        requirement: Predicate<CommandInvoker>,
        executor: CommandExecutor?,
        playerExecutor: CommandExecutor?,
        blockExecutor: CommandExecutor?,
        children: List<CommandNode>
    ): Command

    fun createLiteralCommandNode(
        name: String,
        requirement: Predicate<CommandInvoker>,
        executor: CommandExecutor?,
        playerExecutor: CommandExecutor?,
        blockExecutor: CommandExecutor?,
        children: List<CommandNode>
    ): LiteralCommandNode

    fun <T> createArgumentCommandNode(
        argumentType: ArgumentType<T>,
        suggestions: Suggestions?,
        name: String,
        requirement: Predicate<CommandInvoker>,
        executor: CommandExecutor?,
        playerExecutor: CommandExecutor?,
        blockExecutor: CommandExecutor?,
        children: List<CommandNode>
    ): ArgumentCommandNode<T>

    fun getArgumentLib(): QuickArgumentLib

    fun buildSuggestions(async: Boolean, builder: SuggestionsBuilder): Suggestions

    fun createMeta(): CommandMetaBuilder

    fun registerCommand(command: Command)

    fun registerCommand(name: String, builder: QuickLibRootCommandBuilder.() -> Unit) {
        this.registerCommand(command(name, builder))
    }

    companion object : BloraCommandLib by QuickLib.getCommandLib()

}