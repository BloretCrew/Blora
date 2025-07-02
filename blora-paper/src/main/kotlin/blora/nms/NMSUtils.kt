package blora.nms

import org.bukkit.Bukkit

fun nmsServer(): net.minecraft.server.MinecraftServer {
    return (Bukkit.getServer() as org.bukkit.craftbukkit.CraftServer).server
}