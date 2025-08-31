package blora.internal.api.command

import blora.internal.api.command.argument.ArgumentType
import blora.internal.api.command.argument.QuickArgumentLib
import blora.internal.api.player.BloraPlayer
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.ComponentKt
import java.util.function.Predicate

interface Argument

interface Command : LiteralCommandNode {

    val meta: CommandMeta

}

typealias CommandExecutor = (suspend CommandContext.() -> Unit)

interface CommandNode {

    val name: String

    val requirement: Predicate<CommandInvoker>

    val executor: CommandExecutor?
    val playerExecutor: CommandExecutor?
    val blockExecutor: CommandExecutor?

    val children: List<CommandNode>

}

interface LiteralCommandNode : CommandNode

interface ArgumentCommandNode<T> : CommandNode {

    val argumentType: ArgumentType<T>
    val suggestions: Suggestions?

}

interface CommandInvoker {

    val isPlayer: Boolean
    val isBlock: Boolean
    val isConsole: Boolean

    val asBukkit: CommandSender

    fun player(): BloraPlayer

    fun hasPermission(permission: String): Boolean {
        return asBukkit.hasPermission(permission)
    }

    fun sendMessage(message: String) {
        this.asBukkit.sendMessage(message)
    }

    fun sendMessage(component: Component) {
        this.asBukkit.sendMessage(component)
    }

    fun sendMessage(builder: ComponentKt.() -> Unit) {
        this.asBukkit.send(builder)
    }

}

interface CommandContext {

    val invoker: CommandInvoker
    val player: BloraPlayer
        get() = this.invoker.player()
    val alias: String
    val input: String

    val isPlayer: Boolean
        get() {
            return this.invoker.isPlayer
        }
    val isBlock: Boolean
        get() {
            return this.invoker.isBlock
        }

    fun Component.updateForEntity(entity: Entity): Component {
        return QuickArgumentLib.updateComponentForEntity(this@CommandContext, this, entity)
    }

}

typealias SuggestionsBuilder = SuggestionContext.() -> Unit

interface Suggestions {

    val async: Boolean
    val builder: SuggestionsBuilder

}

interface SuggestionContext : CommandContext {

    val start: Int
    val remaining: String
    val remainingLowercase: String
        get() = this.remaining.lowercase()

    fun suggest(text: String, tooltip: Component? = null)
    fun suggest(text: String, tooltipBuilder: ComponentKt.() -> Unit) {
        this.suggest(text, component(tooltipBuilder))
    }

    fun suggest(text: Int, tooltip: Component? = null)
    fun suggest(text: Int, tooltipBuilder: ComponentKt.() -> Unit) {
        this.suggest(text, component(tooltipBuilder))
    }

}

interface CommandMeta {

    val namespace: String
    val description: String
    val aliases: List<String>

}

interface CommandMetaBuilder {

    fun namespace(namespace: String): CommandMetaBuilder
    fun description(description: String): CommandMetaBuilder
    fun alias(vararg alias: String): CommandMetaBuilder

    fun build(): CommandMeta

}

abstract class QuickLibCommandBuilder(
    internal val name: String
) {

    val children: MutableMap<String, QuickLibCommandBuilder> = mutableMapOf()
    internal var requirement: Predicate<CommandInvoker> = Predicate { true }
    internal var executor: CommandExecutor? = null
    internal var playerExecutor: CommandExecutor? = null
    internal var blockExecutor: CommandExecutor? = null

}

class QuickLibRootCommandBuilder(
    name: String
) : LiteralCommandBuilder(name) {
    internal var meta: CommandMeta = BloraCommandLib.createMeta().build()
}

open class LiteralCommandBuilder(
    name: String
) : QuickLibCommandBuilder(name)

open class ArgumentCommandBuilder<T>(
    name: String,
    internal val argumentType: ArgumentType<T>
) : QuickLibCommandBuilder(name) {

    var suggestions: Suggestions? = null

}

internal fun buildChildren(children: List<QuickLibCommandBuilder>): List<CommandNode> {
    return children.map {
        when (it) {
            is LiteralCommandBuilder -> {
                return@map BloraCommandLib.createLiteralCommandNode(
                    it.name,
                    it.requirement,
                    it.executor,
                    it.playerExecutor,
                    it.blockExecutor,
                    buildChildren(it.children.values.toList())
                )
            }

            is ArgumentCommandBuilder<*> -> {
                return@map BloraCommandLib.Companion.createArgumentCommandNode(
                    it.argumentType,
                    it.suggestions,
                    it.name,
                    it.requirement,
                    it.executor,
                    it.playerExecutor,
                    it.blockExecutor,
                    buildChildren(it.children.values.toList())
                )
            }

            else -> {
                throw RuntimeException("尝试创建一个未知类型的命令结点")
            }
        }
    }.toList()
}

fun command(name: String, content: QuickLibRootCommandBuilder.() -> Unit): Command {
    val builder = QuickLibRootCommandBuilder(name).apply(content)
    return BloraCommandLib.createCommand(
        builder.meta,
        builder.name,
        builder.requirement,
        builder.executor,
        builder.playerExecutor,
        builder.blockExecutor,
        buildChildren(builder.children.values.toList())
    )
}

fun QuickLibRootCommandBuilder.meta(content: CommandMetaBuilder.() -> Unit) {
    this.meta = BloraCommandLib.createMeta().apply(content).build()
}

fun QuickLibCommandBuilder.literal(name: String, content: QuickLibCommandBuilder.() -> Unit) {
    this.children[name] = LiteralCommandBuilder(name).apply(content)
}

inline fun <reified T> QuickLibCommandBuilder.argument(
    name: String,
    argumentType: ArgumentType<T>,
    content: ArgumentCommandBuilder<T>.(CommandContext.() -> T) -> Unit
) {
    this.children[name] = ArgumentCommandBuilder<T>(name, argumentType).apply {
        this.content {
            return@content QuickArgumentLib.getArgumentValue(T::class.java, argumentType, this, name)
        }
    }
}

fun QuickLibCommandBuilder.executor(executor: CommandExecutor?) {
    this.executor = executor
}

fun QuickLibCommandBuilder.playerExecutor(executor: CommandExecutor?) {
    this.playerExecutor = executor
}

fun QuickLibCommandBuilder.blockExecutor(executor: CommandExecutor?) {
    this.blockExecutor = executor
}

fun QuickLibCommandBuilder.requires(requirement: CommandInvoker.() -> Boolean) {
    this.requirement = Predicate {
        it.requirement()
    }
}

fun QuickLibCommandBuilder.permission(permission: String) {
    this.requirement = Predicate {
        it.asBukkit.hasPermission(permission)
    }
}

fun ArgumentCommandBuilder<*>.suggests(content: SuggestionContext.() -> Unit) {
    this.suggestions = BloraCommandLib.buildSuggestions(false, content)
}

fun ArgumentCommandBuilder<*>.suggestsAsync(content: SuggestionContext.() -> Unit) {
    this.suggestions = BloraCommandLib.buildSuggestions(true, content)
}