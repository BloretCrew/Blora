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

object ArgumentString : ArgumentType<String>

object ArgumentWord : ArgumentType<String>

object ArgumentGreedyString : ArgumentType<String>

class ArgumentInt(
    val min: Int,
    val max: Int
) : ArgumentType<Int>

class ArgumentLong(
    val min: Long,
    val max: Long
) : ArgumentType<Long>

class ArgumentFloat(
    val min: Float,
    val max: Float
) : ArgumentType<Float>

class ArgumentDouble(
    val min: Double,
    val max: Double
) : ArgumentType<Double>

object ArgumentBoolean : ArgumentType<Boolean>

object ArgumentAngle : ArgumentType<Float>

object ArgumentColor : ArgumentType<TextFormat>

object ArgumentComponent : ArgumentType<Component>

object ArgumentWorld : ArgumentType<World>

object ArgumentEntityAnchor : ArgumentType<EntityAnchor>

object ArgumentEntity : ArgumentType<Entity>
object ArgumentEntities : ArgumentType<EntityList>
object ArgumentOptionalEntities : ArgumentType<EntityList>

object ArgumentPlayer : ArgumentType<Player>
object ArgumentPlayers : ArgumentType<PlayerList>
object ArgumentOptionalPlayers : ArgumentType<PlayerList>
