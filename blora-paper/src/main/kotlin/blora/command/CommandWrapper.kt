package blora.command

import blora.internal.api.command.CommandContext
import java.util.function.Predicate

abstract class CommandNodeWrapper(
    override val name: String,
    override val requirement: Predicate<blora.internal.api.command.CommandInvoker>,
    override val executor: (suspend CommandContext.() -> Unit)?,
    override val playerExecutor: (suspend CommandContext.() -> Unit)?,
    override val blockExecutor: (suspend CommandContext.() -> Unit)?,
    override val children: List<blora.internal.api.command.CommandNode>
) : blora.internal.api.command.CommandNode

class LiteralCommandNodeWrapper(
    name: String,
    requirement: Predicate<blora.internal.api.command.CommandInvoker>,
    executor: (suspend CommandContext.() -> Unit)?,
    playerExecutor: (suspend CommandContext.() -> Unit)?,
    blockExecutor: (suspend CommandContext.() -> Unit)?,
    children: List<blora.internal.api.command.CommandNode>
) : CommandNodeWrapper(
    name,
    requirement,
    executor,
    playerExecutor,
    blockExecutor,
    children
), blora.internal.api.command.LiteralCommandNode

class ArgumentCommandNodeWrapper<T>(
    override val argumentType: blora.internal.api.command.argument.ArgumentType<T>,
    override val suggestions: blora.internal.api.command.Suggestions?,
    name: String,
    requirement: Predicate<blora.internal.api.command.CommandInvoker>,
    executor: (suspend CommandContext.() -> Unit)?,
    playerExecutor: (suspend CommandContext.() -> Unit)?,
    blockExecutor: (suspend CommandContext.() -> Unit)?,
    children: List<blora.internal.api.command.CommandNode>
) : CommandNodeWrapper(
    name,
    requirement,
    executor,
    playerExecutor,
    blockExecutor,
    children
), blora.internal.api.command.ArgumentCommandNode<T>

class CommandWrapper(
    override val meta: blora.internal.api.command.CommandMeta,
    name: String,
    requirement: Predicate<blora.internal.api.command.CommandInvoker>,
    executor: (suspend CommandContext.() -> Unit)?,
    playerExecutor: (suspend CommandContext.() -> Unit)?,
    blockExecutor: (suspend CommandContext.() -> Unit)?,
    children: List<blora.internal.api.command.CommandNode>
) : CommandNodeWrapper(
    name,
    requirement,
    executor,
    playerExecutor,
    blockExecutor,
    children
), blora.internal.api.command.Command