package blora.command.hook

import net.minecraft.server.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.craftbukkit.command.VanillaCommandWrapper

object VanillaCommandHooker {

    private val toBeRemoved = listOf(
        "w",
        "whisper",
        "tell",
        "msg",
    )

    fun hookEnableStage() {
        for (command in this.toBeRemoved) {
            MinecraftServer.getServer().commands.dispatcher.root.removeCommand(command)
            MinecraftServer.getServer().commands.dispatcher.root.removeCommand("minecraft:$command")
        }
    }

    fun hookLoadedStage() {
        val knownCommands = Bukkit.getServer().commandMap.knownCommands
        for (command in this.toBeRemoved) {
            MinecraftServer.getServer().commands.dispatcher.root.removeCommand(command)
            removeFromKnownCommands(knownCommands, command)
        }
    }

    private fun removeFromKnownCommands(knownCommands: MutableMap<String, Command>, name: String) {
        if (!name.contains(":")) {
            return
        }
        val command = knownCommands[name]
        if (command is VanillaCommandWrapper) {
            knownCommands.remove(name)
            command.aliases.forEach { removeFromKnownCommands(knownCommands, it) }
        }
    }

}