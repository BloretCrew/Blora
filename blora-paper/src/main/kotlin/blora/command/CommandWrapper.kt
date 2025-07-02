package blora.command

import java.util.function.Predicate

abstract class CommandNodeWrapper(
    override val name: String,
    override val requirement: Predicate<blora.api.command.CommandInvoker>,
    override val executor: blora.api.command.CommandExecutor?,
    override val playerExecutor: blora.api.command.CommandExecutor?,
    override val blockExecutor: blora.api.command.CommandExecutor?,
    override val children: List<blora.api.command.CommandNode>
) : blora.api.command.CommandNode

class LiteralCommandNodeWrapper(
    name: String,
    requirement: Predicate<blora.api.command.CommandInvoker>,
    executor: blora.api.command.CommandExecutor?,
    playerExecutor: blora.api.command.CommandExecutor?,
    blockExecutor: blora.api.command.CommandExecutor?,
    children: List<blora.api.command.CommandNode>
) : CommandNodeWrapper(
    name,
    requirement,
    executor,
    playerExecutor,
    blockExecutor,
    children
), blora.api.command.LiteralCommandNode

class ArgumentCommandNodeWrapper<T>(
    override val argumentType: blora.api.command.argument.ArgumentType<T>,
    override val suggestions: blora.api.command.Suggestions?,
    name: String,
    requirement: Predicate<blora.api.command.CommandInvoker>,
    executor: blora.api.command.CommandExecutor?,
    playerExecutor: blora.api.command.CommandExecutor?,
    blockExecutor: blora.api.command.CommandExecutor?,
    children: List<blora.api.command.CommandNode>
) : CommandNodeWrapper(
    name,
    requirement,
    executor,
    playerExecutor,
    blockExecutor,
    children
), blora.api.command.ArgumentCommandNode<T>

class CommandWrapper(
    override val meta: blora.api.command.CommandMeta,
    name: String,
    requirement: Predicate<blora.api.command.CommandInvoker>,
    executor: blora.api.command.CommandExecutor?,
    playerExecutor: blora.api.command.CommandExecutor?,
    blockExecutor: blora.api.command.CommandExecutor?,
    children: List<blora.api.command.CommandNode>
) : CommandNodeWrapper(
    name,
    requirement,
    executor,
    playerExecutor,
    blockExecutor,
    children
), blora.api.command.Command