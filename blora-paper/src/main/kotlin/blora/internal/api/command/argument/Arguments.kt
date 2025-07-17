package blora.internal.api.command.argument

import blora.api.position.EntityAnchor
import blora.api.types.EntityList
import blora.api.types.PlayerList
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextFormat
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

interface Arguments {

    // brigadier default types
    val string: ArgumentType<String>
    val word: ArgumentType<String>
    val greedyString: ArgumentType<String>
    val boolean: ArgumentType<Boolean>

    fun integer(min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): ArgumentType<Int>
    fun long(min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE): ArgumentType<Long>
    fun float(min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE): ArgumentType<Float>
    fun double(min: Double = Double.MIN_VALUE, max: Double = Double.MAX_VALUE): ArgumentType<Double>

    // minecraft types
    val angle: ArgumentType<Float>
    val color: ArgumentType<TextFormat>
    val component: ArgumentType<Component>
    val world: ArgumentType<World>
    val entityAnchor: ArgumentType<EntityAnchor>

    val entity: ArgumentType<Entity>
    val entities: ArgumentType<EntityList>
    val optionalEntities: ArgumentType<EntityList>
    val player: ArgumentType<Player>
    val players: ArgumentType<PlayerList>
    val optionalPlayers: ArgumentType<PlayerList>

    companion object : Arguments by QuickArgumentLib.getArguments()

}