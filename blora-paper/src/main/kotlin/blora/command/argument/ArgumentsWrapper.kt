package blora.command.argument

import blora.api.command.argument.ArgumentType
import blora.api.position.EntityAnchor
import blora.api.types.EntityList
import blora.api.types.PlayerList
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextFormat
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

object ArgumentsWrapper : blora.api.command.argument.Arguments {

    override val string: ArgumentType<String>
        get() = ArgumentString
    override val word: ArgumentType<String>
        get() = ArgumentWord
    override val greedyString: ArgumentType<String>
        get() = ArgumentGreedyString
    override val boolean: ArgumentType<Boolean>
        get() = ArgumentBoolean

    override fun integer(
        min: Int,
        max: Int
    ): ArgumentType<Int> {
        return ArgumentInt(min, max)
    }

    override fun long(
        min: Long,
        max: Long
    ): ArgumentType<Long> {
        return ArgumentLong(min, max)
    }

    override fun float(
        min: Float,
        max: Float
    ): ArgumentType<Float> {
        return ArgumentFloat(min, max)
    }

    override fun double(
        min: Double,
        max: Double
    ): ArgumentType<Double> {
        return ArgumentDouble(min, max)
    }

    override val angle: ArgumentType<Float>
        get() = ArgumentAngle
    override val color: ArgumentType<TextFormat>
        get() = ArgumentColor
    override val component: ArgumentType<Component>
        get() = ArgumentComponent
    override val world: ArgumentType<World>
        get() = ArgumentWorld
    override val entityAnchor: ArgumentType<EntityAnchor>
        get() = ArgumentEntityAnchor
    override val entity: ArgumentType<Entity>
        get() = ArgumentEntity
    override val entities: ArgumentType<EntityList>
        get() = ArgumentEntities
    override val optionalEntities: ArgumentType<EntityList>
        get() = ArgumentOptionalEntities
    override val player: ArgumentType<Player>
        get() = ArgumentPlayer
    override val players: ArgumentType<PlayerList>
        get() = ArgumentPlayers
    override val optionalPlayers: ArgumentType<PlayerList>
        get() = ArgumentOptionalPlayers

}