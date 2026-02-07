package blora.listener

import blora.database.town.dao.TownChunkDao
import blora.database.town.dao.TownDao
import blora.entity.EntityCategory
import blora.extension.getClaimedTown
import blora.extension.localization
import blora.localization.LocalizationContents
import blora.location.Aabb
import blora.location.ChunkLocation
import blora.plugin.BloraPlugin
import blora.town.TownPermission
import blora.town.TownPermissionStatus
import com.bekvon.bukkit.residence.event.*
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import io.papermc.paper.block.TileStateInventoryHolder
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockMultiPlaceEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.*
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder

object TownListener : Listener {

    private var registered: Boolean = false

    fun register() {
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(this, BloraPlugin)
            registered = true
        }
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
        this.registered = false
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        this.handleEnter(event)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        this.handleEnter(event)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val block = event.block
        this.handleChunk(event, event.player, block.chunk, TownPermission.PlaceBlock) {
            this.town.permissionPlaceBlockWarning
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockMultiPlace(event: BlockMultiPlaceEvent) {
        val block = event.block
        this.handleChunk(event, event.player, block.chunk, TownPermission.PlaceBlock) {
            this.town.permissionDestroyBlockWarning
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        this.handleChunk(event, event.player, block.chunk, TownPermission.DestroyBlock) {
            this.town.permissionDestroyBlockWarning
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val damagee = event.entity
        val damager = event.damager
        if (damager !is Player)
            return
        this.handleChunk(
            event,
            damager,
            damagee.chunk,
            if (damagee is Player) {
                TownPermission.Pvp
            } else if (EntityCategory.Friendly.contains(damagee.type)) {
                TownPermission.KillFriendlyMobs
            } else if (EntityCategory.Hostile.contains(damagee.type)) {
                TownPermission.KillHostileMobs
            } else {
                TownPermission.KillOtherMobs
            }
        ) {
            if (damagee is Player) {
                this.town.permissionPvpWarning
            } else if (EntityCategory.Friendly.contains(damagee.type)) {
                this.town.permissionKillFriendlyMobsWarning
            } else if (EntityCategory.Hostile.contains(damagee.type)) {
                this.town.permissionKillFriendlyMobsWarning
            } else {
                this.town.permissionKillFriendlyMobs
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        this.handleChunk(event, event.player, event.rightClicked.chunk, TownPermission.InteractEntity) {
            this.town.permissionInteractEntityWarning
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        this.handleChunk(event, event.player, event.rightClicked.chunk, TownPermission.InteractEntity) {
            this.town.permissionInteractEntityWarning
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteractAtEntity(event: PlayerInteractEvent) {
        val block = event.clickedBlock
        val location = event.interactionPoint
        if (block != null) {
            if (block.state is TileStateInventoryHolder) { // container
                this.handleChunk(event, event.player, block.chunk, TownPermission.InteractContainer) {
                    this.town.permissionInteractContainerWarning
                }
            } else {
                this.handleChunk(event, event.player, block.chunk, TownPermission.InteractBlock) {
                    this.town.permissionInteractBlockWarning
                }
            }
        } else if (location != null) {
            this.handleChunk(event, event.player, location.chunk, TownPermission.InteractBlock) {
                this.town.permissionInteractBlockWarning
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (this.handleChunk(event, event.player, event.player.chunk, TownPermission.DropItem) {
                this.town.permissionInteractBlockWarning
            })
            return
        this.handleChunk(event, event.player, event.itemDrop.chunk, TownPermission.DropItem) {
            this.town.permissionDropItemWarning
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        if (event.entity !is Player)
            return
        val player = event.entity as Player
        if (this.handleChunk(event, player, player.chunk, TownPermission.PickupItem))
            return
        this.handleChunk(event, player, event.item.chunk, TownPermission.PickupItem)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerPickupExperience(event: PlayerPickupExperienceEvent) {
        if (this.handleChunk(event, event.player, event.player.chunk, TownPermission.PickupExp))
            return
        this.handleChunk(event, event.player, event.experienceOrb.chunk, TownPermission.PickupExp)
    }

    // Residence related things

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onResidenceSizeChange(event: ResidenceSizeChangeEvent) {
        if (event.oldArea.isAreaWithinArea(event.newArea)) // size shrinks
            return
        val newAabb = Aabb.of(event.newArea.lowLocation, event.newArea.highLocation)
        val oldAabb = Aabb.of(event.oldArea.lowLocation, event.oldArea.highLocation)
        this.handleResidenceAreaCreationAllow(
            event,
            newAabb.getContainedChunks() - oldAabb.getContainedChunks()
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onResidenceAreaAdd(event: ResidenceAreaAddEvent) {
        this.handleResidenceAreaCreationAllow(
            event,
            Aabb.of(event.physicalArea.lowLocation, event.physicalArea.highLocation).getContainedChunks()
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onResidenceAreaCreation(event: ResidenceCreationEvent) {
        this.handleResidenceAreaCreationAllow(
            event,
            Aabb.of(event.physicalArea.lowLocation, event.physicalArea.highLocation).getContainedChunks()
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onResidenceSubzoneCreation(event: ResidenceSubzoneCreationEvent) {
        this.handleResidenceAreaCreationAllow(
            event,
            Aabb.of(event.physicalArea.lowLocation, event.physicalArea.highLocation).getContainedChunks()
        )
    }

    // internal implementation

    private fun handleChunk(
        event: Cancellable,
        player: Player,
        chunk: Chunk,
        permission: TownPermission,
        message: (LocalizationContents.() -> String)? = null
    ): Boolean {
        val townChunk = chunk.getClaimedTown()
        if (townChunk == null)
            return false
        val town = TownDao.getByTownId(townChunk.townId)
        if (town == null)
            return false

        if (town.getPlayerPermission(player.uniqueId).getOrElse(permission) {
                TownPermissionStatus.DENY
            } == TownPermissionStatus.ALLOW)
            return false
        event.isCancelled = true
        if (message != null)
            player.sendActionBar {
                component {
                    localization(
                        player = player,
                        tags = {
                            parsedPlaceholder("town", town.displayName)
                        }
                    ) {
                        this.message()
                    }
                }
            }
        return true
    }

    private fun handleEnter(event: PlayerMoveEvent) {
        if (event.from.chunk == event.to.chunk) // not out of current chunk
            return
        if (this.handleChunk(
                event,
                event.player,
                event.to.chunk,
                TownPermission.EnterTown
            ) { this.town.permissionEnterTownWarning }
        )
            return
        this.handleEnterOrExitMessage(event)
    }

    private fun handleEnterOrExitMessage(event: PlayerMoveEvent) {
        val fromChunkTown = event.from.chunk.getClaimedTown()
        val toChunkTown = event.to.chunk.getClaimedTown()
        if (event.from.chunk.x == event.to.chunk.x && event.from.chunk.z == event.to.chunk.z) // in same chunk
            return
        if (fromChunkTown != null && (toChunkTown == null || fromChunkTown.townId != toChunkTown.townId)) {
            val fromTown = TownDao.getByTownId(fromChunkTown.townId)
            if (fromTown != null) {
                component {
                    localization(
                        player = event.player,
                        tags = {
                            parsedPlaceholder("town", fromTown.displayName)
                        }
                    ) {
                        fromTown.goodbyeMessage ?: this.town.defaultGoodbyeMessage
                    }
                }
            }
        }
        if (toChunkTown != null && (fromChunkTown == null || fromChunkTown.townId != toChunkTown.townId)) {
            val toTown = TownDao.getByTownId(toChunkTown.townId)
            if (toTown != null) {
                component {
                    localization(
                        player = event.player,
                        tags = {
                            parsedPlaceholder("town", toTown.displayName)
                        }
                    ) {
                        toTown.welcomeMessage ?: this.town.defaultWelcomeMessage
                    }
                }
            }
        }
    }

    private fun getTowns(list: List<Chunk>): List<TownDao> {
        val towns = mutableListOf<TownDao>()
        for (chunk in list) {
            val townChunk = chunk.getClaimedTown()
            if (townChunk == null)
                continue
            if (towns.any { it.townId == townChunk.townId })
                continue
            val town = TownDao.getByTownId(townChunk.townId)
            if (town == null)
                continue
            towns.add(town)
        }
        return towns.toList()
    }

    private fun handleResidenceAreaCreationAllow(event: CancellableResidencePlayerEvent, list: List<ChunkLocation>): Boolean {
        val towns = mutableListOf<TownDao>()
        for (chunk in list) {
            val townChunk = chunk.getClaimedTown()
            if (townChunk == null)
                continue
            if (towns.any { it.townId == townChunk.townId })
                continue
            val town = TownDao.getByTownId(townChunk.townId)
            if (town == null)
                continue
            if (town.getPlayerPermission(event.player.uniqueId).getOrElse(
                    TownPermission.CreateResidence
                ) { TownPermissionStatus.DENY } != TownPermissionStatus.ALLOW
            ) {
                event.isCancelled = true
                event.player.send {
                    localization(event.player) {
                        this.town.thirdparty.residencePlayer_area_no_permission
                    }
                }
                return true
            }
            towns.add(town)
        }
        return false
    }

    private fun handleResidenceAreaCreationDeny(event: CancellableResidencePlayerEvent, chunks: List<Chunk>): Boolean {
        for (chunk in chunks) {
            if (TownChunkDao.find(chunk.world.name, chunk.x, chunk.z) == null) {
                event.isCancelled = true
                event.player.send {
                    localization(event.player) {
                        this.town.thirdparty.residencePlayer_area_cannot_overlap_town_area
                    }
                }
                return true
            }
        }
        return false
    }

}