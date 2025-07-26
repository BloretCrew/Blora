package blora.guild

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.menu.storage.StorageMenu
import blora.menu.v2.page.builder.title
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.id.EntityID
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder

object GuildEnderChestManager {

    private val cacheStorage: MutableMap<EntityID<Int>, StorageMenu> = mutableMapOf()

    fun open(guild: GuildDao, player: Player) {
        cacheStorage.getOrPut(guild.id) {
            StorageMenu(
                guild.id,
                guild.enderChest.items,
                6,
                100L, // update every 5 seconds
                this::remove
            ) { newItems ->
                DB.trans {
                    guild.enderChest = GuildEnderChest(newItems)
                    guild.flush()
                }
            }
        }.openForPlayer(
            player,
            component {
                localization(
                    player = player,
                    tags = {
                        parsedPlaceholder("guild", guild.displayName)
                    }
                ) {
                    this.guild.menu.menuGuild_ender_chestTitle
                }
            }
        )
    }

    private fun remove(storageMenu: StorageMenu) {
        this.cacheStorage.remove(storageMenu.identity)
    }

}