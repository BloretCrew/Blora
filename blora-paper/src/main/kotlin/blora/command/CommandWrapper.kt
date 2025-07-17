package blora.command

import java.util.function.Predicate

abstract class CommandNodeWrapper(
    override val name: String,
    override val requirement: Predicate<blora.internal.api.command.CommandInvoker>,
    override val executor: blora.internal.api.command.CommandExecutor?,
    override val playerExecutor: blora.internal.api.command.CommandExecutor?,
    override val blockExecutor: blora.internal.api.command.CommandExecutor?,
    override val children: List<blora.internal.api.command.CommandNode>
) : blora.internal.api.command.CommandNode

class LiteralCommandNodeWrapper(
    name: String,
    requirement: Predicate<blora.internal.api.command.CommandInvoker>,
    executor: blora.internal.api.command.CommandExecutor?,
    playerExecutor: blora.internal.api.command.CommandExecutor?,
    blockExecutor: blora.internal.api.command.CommandExecutor?,
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
    executor: blora.internal.api.command.CommandExecutor?,
    playerExecutor: blora.internal.api.command.CommandExecutor?,
    blockExecutor: blora.internal.api.command.CommandExecutor?,
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
    executor: blora.internal.api.command.CommandExecutor?,
    playerExecutor: blora.internal.api.command.CommandExecutor?,
    blockExecutor: blora.internal.api.command.CommandExecutor?,
    children: List<blora.internal.api.command.CommandNode>
) : CommandNodeWrapper(
    name,
    requirement,
    executor,
    playerExecutor,
    blockExecutor,
    children
), blora.internal.api.command.Command