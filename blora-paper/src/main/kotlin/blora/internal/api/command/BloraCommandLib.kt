package blora.internal.api.command

import blora.internal.api.QuickLib
import blora.internal.api.command.argument.ArgumentType
import blora.internal.api.command.argument.QuickArgumentLib
import java.util.function.Predicate

interface BloraCommandLib {

    fun createCommand(
        meta: CommandMeta,
        name: String,
        requirement: Predicate<CommandInvoker>,
        executor: (suspend CommandContext.() -> Unit)?,
        playerExecutor: (suspend CommandContext.() -> Unit)?,
        blockExecutor: (suspend CommandContext.() -> Unit)?,
        children: List<CommandNode>
    ): Command

    fun createLiteralCommandNode(
        name: String,
        requirement: Predicate<CommandInvoker>,
        executor: (suspend CommandContext.() -> Unit)?,
        playerExecutor: (suspend CommandContext.() -> Unit)?,
        blockExecutor: (suspend CommandContext.() -> Unit)?,
        children: List<CommandNode>
    ): LiteralCommandNode

    fun <T> createArgumentCommandNode(
        argumentType: ArgumentType<T>,
        suggestions: Suggestions?,
        name: String,
        requirement: Predicate<CommandInvoker>,
        executor: (suspend CommandContext.() -> Unit)?,
        playerExecutor: (suspend CommandContext.() -> Unit)?,
        blockExecutor: (suspend CommandContext.() -> Unit)?,
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