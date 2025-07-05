package blora.nms

import blora.api.command.CommandContext
import blora.api.command.argument.ArgumentType
import blora.api.position.EntityAnchor
import blora.command.argument.*
import com.mojang.brigadier.arguments.*
import io.papermc.paper.adventure.PaperAdventure
import net.minecraft.commands.arguments.*
import com.mojang.brigadier.arguments.ArgumentType as NMSArgumentType

@Suppress("UNCHECKED_CAST")
fun <T> internalGetArgumentValue(
    valueType: Class<T>,
    argumentType: ArgumentType<T>,
    context: CommandContext,
    name: String
): T {
    if (valueType == String::class.java) {
        return StringArgumentType.getString(context.nms(), name) as T
    } else if (valueType == Int::class.java) {
        return IntegerArgumentType.getInteger(context.nms(), name) as T
    } else if (valueType == Long::class.java) {
        return LongArgumentType.getLong(context.nms(), name) as T
    } else if (valueType == Float::class.java) {
        return FloatArgumentType.getFloat(context.nms(), name) as T
    } else if (valueType == Double::class.java) {
        return DoubleArgumentType.getDouble(context.nms(), name) as T
    } else if (valueType == Boolean::class.java) {
        return BoolArgumentType.getBool(context.nms(), name) as T
    } else if (argumentType == ArgumentAngle) {
        return AngleArgument.getAngle(context.nms(), name) as T
    } else if (argumentType == ArgumentColor) {
        return ColorArgument.getColor(context.nms(), name).toAdventure() as T
    } else if (argumentType == ArgumentComponent) {
        return PaperAdventure.asAdventure(ComponentArgument.getRawComponent(context.nms(), name)) as T
    } else if (argumentType == ArgumentWorld) {
        return DimensionArgument.getDimension(context.nms(), name).world as T
    } else if (argumentType == ArgumentEntityAnchor) {
        return when (EntityAnchorArgument.getAnchor(context.nms(), name)) {
            EntityAnchorArgument.Anchor.EYES -> EntityAnchor.EYES
            EntityAnchorArgument.Anchor.FEET -> EntityAnchor.FEET
        } as T
    } else if (argumentType == ArgumentEntity) {
        return EntityArgument.getEntity(context.nms(), name).bukkitEntity as T
    } else if (argumentType == ArgumentEntities) {
        return EntityArgument.getEntities(context.nms(), name).map { it.bukkitEntity }.toList() as T
    } else if (argumentType == ArgumentOptionalEntities) {
        return EntityArgument.getOptionalEntities(context.nms(), name).map { it.bukkitEntity }.toList() as T
    } else if (argumentType == ArgumentPlayer) {
        return EntityArgument.getPlayer(context.nms(), name).bukkitEntity as T
    } else if (argumentType == ArgumentPlayers) {
        return EntityArgument.getPlayers(context.nms(), name).map { it.bukkitEntity }.toList() as T
    } else if (argumentType == ArgumentOptionalPlayers) {
        return EntityArgument.getOptionalPlayers(context.nms(), name).map { it.bukkitEntity }.toList() as T
    } else {
        throw RuntimeException("未知的参数类型转换")
    }
}

fun ArgumentType<*>.nms(): NMSArgumentType<*> {
    return if (this == ArgumentString) {
        StringArgumentType.string()
    } else if (this == ArgumentWord) {
        StringArgumentType.word()
    } else if (this == ArgumentGreedyString) {
        StringArgumentType.greedyString()
    } else if (this is ArgumentInt) {
        IntegerArgumentType.integer(this.min, this.max)
    } else if (this is ArgumentAngle) {
        AngleArgument.angle()
    } else if (this is ArgumentColor) {
        ColorArgument.color()
    } else if (this is ArgumentComponent) {
        ComponentArgument.textComponent(commandBuildContext)
    } else if (this is ArgumentWorld) {
        DimensionArgument.dimension()
    } else if (this is ArgumentEntityAnchor) {
        EntityAnchorArgument.anchor()
    } else if (this is ArgumentEntity) {
        EntityArgument.entity()
    } else if (this is ArgumentEntities) {
        EntityArgument.entities()
    } else if (this is ArgumentOptionalEntities) {
        EntityArgument.entities()
    } else if (this is ArgumentPlayer) {
        EntityArgument.player()
    } else if (this is ArgumentPlayers) {
        EntityArgument.players()
    } else if (this is ArgumentOptionalPlayers) {
        EntityArgument.players()
    } else {
        throw RuntimeException("不支持的参数类型")
    }
}