package blora.listener

import blora.messaging.packet.clientbound.PlayerAuthorizationResponsePacket
import blora.messaging.packet.clientbound.PlayerAuthorizationUpdatePacket
import blora.messaging.packet.serverbound.PlayerAuthorizationRequestPacket
import blora.plugin.BloraPlugin
import com.destroystokyo.paper.event.block.AnvilDamagedEvent
import com.destroystokyo.paper.event.block.BeaconEffectEvent
import com.destroystokyo.paper.event.entity.*
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import com.destroystokyo.paper.event.player.*
import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent
import io.papermc.paper.event.block.PlayerShearBlockEvent
import io.papermc.paper.event.block.TargetHitEvent
import io.papermc.paper.event.block.VaultChangeStateEvent
import io.papermc.paper.event.entity.*
import io.papermc.paper.event.entity.EntityKnockbackEvent
import io.papermc.paper.event.packet.UncheckedSignChangeEvent
import io.papermc.paper.event.player.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.entity.*
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.*
import org.bukkit.event.raid.RaidTriggerEvent
import org.bukkit.event.server.TabCompleteEvent
import org.bukkit.event.vehicle.*
import java.util.*

object UnauthorizedListener : Listener {

    private val playerStatus: MutableMap<UUID, Boolean> = mutableMapOf()

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

    private fun updatePlayerStatus(playerName: String, status: Boolean) {
        val player = Bukkit.getPlayer(playerName)
        if (player == null)
            return
        playerStatus[player.uniqueId] = status
    }

    fun updatePlayerStatus(packet: PlayerAuthorizationResponsePacket) {
        this.updatePlayerStatus(packet.playerName, packet.authorized)
    }

    fun updatePlayerStatus(packet: PlayerAuthorizationUpdatePacket) {
        this.updatePlayerStatus(packet.playerName, packet.authorized)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        playerStatus[event.player.uniqueId] = false
        BloraPlugin.client.send(PlayerAuthorizationRequestPacket().apply { this.playerName = event.player.name })
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        this.playerStatus.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerKick(event: PlayerKickEvent) {
        this.playerStatus.remove(event.player.uniqueId)
    }

    private fun cancel(player: Player, event: Cancellable) {
        if (this.shouldCancel(player)) {
            event.isCancelled = true
        }
    }

    private fun shouldCancel(player: Player): Boolean {
        if (this.playerStatus.containsKey(player.uniqueId)) {
            if (!this.playerStatus[player.uniqueId]!!) {
                return true
            }
        }
        return false
    }

    // Block events

    @EventHandler
    fun onBellRing(event: BellRingEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onBlockDamage(event: BlockDamageEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onBlockDispenseArmor(event: BlockDispenseArmorEvent) {
        val entity = event.targetEntity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onBlockDispenseLoot(event: BlockDispenseLootEvent) {
        val player = event.player
        if (player != null) {
            this.cancel(player, event)
        }
    }

    @EventHandler
    fun onBlockDropItem(event: BlockDropItemEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onBlockFertilize(event: BlockFertilizeEvent) {
        val player = event.player
        if (player != null) {
            this.cancel(player, event)
        }
    }

    @EventHandler
    fun onBlockIgnite(event: BlockIgniteEvent) {
        val player = event.player
        if (player != null) {
            this.cancel(player, event)
        }
    }

    @EventHandler
    fun onBlockMultiPlace(event: BlockMultiPlaceEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onBlockReceiveGame(event: BlockReceiveGameEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onCauldronLevelChange(event: CauldronLevelChangeEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityBlockForm(event: EntityBlockFormEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onTNTPrime(event: TNTPrimeEvent) {
        val entity = event.primingEntity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    // Enchantment events

    @EventHandler
    fun onEnchantItem(event: EnchantItemEvent) {
        this.cancel(event.enchanter, event)
    }

    @EventHandler
    fun onPrepareItemEnchant(event: PrepareItemEnchantEvent) {
        this.cancel(event.enchanter, event)
    }

    // Entity events

    @EventHandler
    fun onEntityBreed(event: EntityBreedEvent) {
        val entity = event.breeder
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityChangeBlock(event: EntityChangeBlockEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityCombustByBlock(event: EntityCombustByBlockEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityCombustByEntity(event: EntityCombustByEntityEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityDamageByBlock(event: EntityDamageByBlockEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityDeathEvent(event: EntityDeathEvent) {
        val entity = event.damageSource.causingEntity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityDismount(event: EntityDismountEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityEnterLoveModeEvent(event: EntityEnterLoveModeEvent) {
        val entity = event.humanEntity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityExhaustion(event: EntityExhaustionEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityMount(event: EntityMountEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityPlace(event: EntityPlaceEvent) {
        val player = event.player
        if (player != null) {
            this.cancel(player, event)
        }
    }

    @EventHandler
    fun onEntityPortalEnter(event: EntityPortalEnterEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityPortalExit(event: EntityPortalExitEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityResurrect(event: EntityResurrectEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityShootBow(event: EntityShootBowEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityTame(event: EntityTameEvent) {
        val entity = event.owner
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityTarget(event: EntityTargetEvent) {
        val entity = event.target
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityTargetLivingEntity(event: EntityTargetLivingEntityEvent) {
        val entity = event.target
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityToggleGlide(event: EntityToggleGlideEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityToggleSwim(event: EntityToggleSwimEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onPigZombieAnger(event: PigZombieAngerEvent) {
        val entity = event.target
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        this.cancel(event.entity, event)
    }

    @EventHandler
    fun onPotionSplash(event: PotionSplashEvent) {
        val shooter = event.entity.shooter
        if (shooter is Player) {
            this.cancel(shooter, event)
            return
        }
        for (entity in event.affectedEntities) {
            if (entity is Player) {
                if (this.shouldCancel(entity)) {
                    event.setIntensity(entity, 0.0)
                }
            }
        }
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity
        if (hitEntity is Player) {
            this.cancel(hitEntity, event)
            return
        }
        val shooter = event.entity.shooter
        if (shooter is Player) {
            this.cancel(shooter, event)
        }
    }

    @EventHandler
    fun onProjectileLaunch(event: ProjectileLaunchEvent) {
        val shooter = event.entity.shooter
        if (shooter is Player) {
            this.cancel(shooter, event)
        }
    }

    // Hanging events (what is hanging?)

    @EventHandler
    fun onHangingBreakByEntity(event: HangingBreakByEntityEvent) {
        val entity = event.remover
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onHangingPlace(event: HangingPlaceEvent) {
        val player = event.player
        if (player != null) {
            this.cancel(player, event)
        }
    }

    // Inventory events

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        this.cancel(event.whoClicked as Player, event)
    }

    @EventHandler
    fun onInventoryCreative(event: InventoryCreativeEvent) {
        this.cancel(event.whoClicked as Player, event)
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        this.cancel(event.whoClicked as Player, event)
    }

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        this.cancel(event.player as Player, event)
    }

    @EventHandler
    fun onSmithItem(event: SmithItemEvent) {
        this.cancel(event.whoClicked as Player, event)
    }

    @EventHandler
    fun onTradeSelect(event: TradeSelectEvent) {
        this.cancel(event.whoClicked as Player, event)
    }

    // Player events

    @EventHandler
    fun onPlayerAnimation(event: PlayerAnimationEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerArmorStandManipulate(event: PlayerArmorStandManipulateEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerAttemptPickupItem(event: PlayerAttemptPickupItemEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerBedEnter(event: PlayerBedEnterEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerBedLeave(event: PlayerBedLeaveEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerBucketEntity(event: PlayerBucketEntityEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerBucketFill(event: PlayerBucketFillEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerEditBook(event: PlayerEditBookEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerFish(event: PlayerFishEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerGameModeChange(event: PlayerGameModeChangeEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerHarvest(event: PlayerHarvestBlockEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerItemDamage(event: PlayerItemDamageEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerItemMend(event: PlayerItemMendEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerPickupArrow(event: PlayerPickupArrowEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerPortal(event: PlayerPortalEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerRecipeDiscover(event: PlayerRecipeDiscoverEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerShearEntity(event: PlayerShearEntityEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerStatisticIncrement(event: PlayerStatisticIncrementEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerTakeLecternBook(event: PlayerTakeLecternBookEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerUnleashEntity(event: PlayerUnleashEntityEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerVelocity(event: PlayerVelocityEvent) {
        this.cancel(event.player, event)
    }

    // Raid events

    @EventHandler
    fun onRaidTrigger(event: RaidTriggerEvent) {
        this.cancel(event.player, event)
    }

    // Server events

    @EventHandler
    fun onTabComplete(event: TabCompleteEvent) {
        val sender = event.sender
        if (sender is Player) {
            this.cancel(sender, event)
        }
    }

    // Vehicle events

    @EventHandler
    fun onVehicleDamage(event: VehicleDamageEvent) {
        val attacker = event.attacker
        if (attacker is Player) {
            this.cancel(attacker, event)
        }
    }

    @EventHandler
    fun onVehicleDestroy(event: VehicleDestroyEvent) {
        val attacker = event.attacker
        if (attacker is Player) {
            this.cancel(attacker, event)
        }
    }

    @EventHandler
    fun onVehicleEntered(event: VehicleEnterEvent) {
        val entered = event.entered
        if (entered is Player) {
            this.cancel(entered, event)
        }
    }

    @EventHandler
    fun onVehicleEntityCollision(event: VehicleEntityCollisionEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onVehicleExit(event: VehicleExitEvent) {
        val exited = event.exited
        if (exited is Player) {
            this.cancel(exited, event)
        }
    }

    // DestroysTokyo events start

    // Block events

    @EventHandler
    fun onAnvilDamaged(event: AnvilDamagedEvent) {
        this.cancel(event.view.player as Player, event)
    }

    @EventHandler
    fun onBeaconEffect(event: BeaconEffectEvent) {
        this.cancel(event.player, event)
    }

    // Entity events
    @EventHandler
    fun onEnderDragonFireballHit(event: EnderDragonFireballHitEvent) {
        for (entity in event.targets.toList()) {
            if (entity is Player) {
                if (this.shouldCancel(entity)) {
                    event.targets.remove(entity)
                }
            }
        }
    }

    @EventHandler
    fun onEndermanAttackPlayer(event: EndermanAttackPlayerEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onEntityKnockbackByEntity(event: EntityKnockbackByEntityEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
            return
        }
        val hitBy = event.hitBy
        if (hitBy is Player) {
            this.cancel(hitBy, event)
            return
        }
    }

    @EventHandler
    fun onEntityPathfind(event: EntityPathfindEvent) {
        val entity = event.targetEntity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onPlayerNaturallySpawnCreatures(event: PlayerNaturallySpawnCreaturesEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onSlimeTargetLivingEntity(event: SlimeTargetLivingEntityEvent) {
        val target = event.target
        if (target is Player) {
            this.cancel(target, event)
        }
    }

    @EventHandler
    fun onWitchThrowPotion(event: WitchThrowPotionEvent) {
        val target = event.target
        if (target is Player) {
            this.cancel(target, event)
        }
    }

    // Player events

    @EventHandler
    fun onPlayerAdvancementCriterionGrant(event: PlayerAdvancementCriterionGrantEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerAttackEntityCooldownReset(event: PlayerAttackEntityCooldownResetEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerElytraBoost(event: PlayerElytraBoostEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerJump(event: PlayerJumpEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerLaunchProjectile(event: PlayerLaunchProjectileEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerPickupExperience(event: PlayerPickupExperienceEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerReadyArrow(event: PlayerReadyArrowEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerRecipeBookClick(event: PlayerRecipeBookClickEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerStartSpectatingEntity(event: PlayerStartSpectatingEntityEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerStopSpectatingEntity(event: PlayerStopSpectatingEntityEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerTeleportEndGateway(event: PlayerTeleportEndGatewayEvent) {
        this.cancel(event.player, event)
    }

    // Server events

    @EventHandler
    fun onAsyncTabComplete(event: AsyncTabCompleteEvent) {
        val sender = event.sender
        if (sender is Player) {
            this.cancel(sender, event)
        }
    }

    // Paper events start

    // Block events

    @EventHandler
    fun onPlayerShearBlock(event: PlayerShearBlockEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onTargetHit(event: TargetHitEvent) {
        val shooter = event.entity.shooter
        if (shooter is Player) {
            this.cancel(shooter, event)
        }
    }

    @EventHandler
    fun onVaultChangeState(event: VaultChangeStateEvent) {
        val player = event.player
        if (player != null) {
            this.cancel(player, event)
        }
    }

    // Entity events

    @EventHandler
    fun onElderGuardianAppearance(event: ElderGuardianAppearanceEvent) {
        this.cancel(event.affectedPlayer, event)
    }

    @EventHandler
    fun onEntityCompostItem(event: EntityCompostItemEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityDamageItem(event: EntityDamageItemEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityDye(event: EntityDyeEvent) {
        val player = event.player
        if (player != null) {
            this.cancel(player, event)
        }
    }

    @EventHandler
    fun onEntityEffectTick(event: EntityEffectTickEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityFertilizeEgg(event: EntityFertilizeEggEvent) {
        val player = event.breeder
        if (player != null) {
            this.cancel(player, event)
        }
    }

    @EventHandler
    fun onEntityInsideBlock(event: EntityInsideBlockEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityKnockback(event: EntityKnockbackEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityLoadCrossbow(event: EntityLoadCrossbowEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityPortalReady(event: EntityPortalReadyEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityPushedByEntityAttack(event: EntityPushedByEntityAttackEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onEntityToggleSit(event: EntityToggleSitEvent) {
        val entity = event.entity
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onWardenAngerChange(event: WardenAngerChangeEvent) {
        val entity = event.target
        if (entity is Player) {
            this.cancel(entity, event)
        }
    }

    @EventHandler
    fun onWaterBottleSplash(event: WaterBottleSplashEvent) {
        val shooter = event.entity.shooter
        if (shooter is Player) {
            this.cancel(shooter, event)
            return
        }
        for (entity in event.toDamage) {
            if (entity is Player) {
                if (this.shouldCancel(entity)) {
                    event.doNotDamageAsWaterSensitive(entity)
                }
            }
        }
        for (entity in event.toRehydrate.toList()) {
            if (entity is Player) {
                if (this.shouldCancel(entity)) {
                    event.toRehydrate.remove(entity)
                }
            }
        }
        for (entity in event.toExtinguish.toList()) {
            if (entity is Player) {
                if (this.shouldCancel(entity)) {
                    event.toExtinguish.remove(entity)
                }
            }
        }
    }

    @EventHandler
    fun onUncheckedSignChange(event: UncheckedSignChangeEvent) {
        this.cancel(event.player, event)
    }

    // Player events

    @EventHandler
    fun onAsyncChatCommandDecorate(event: AsyncChatCommandDecorateEvent) {
        val player = event.player()
        if (player != null) {
            this.cancel(player, event)
        }
    }

    @EventHandler
    fun onAsyncChatDecorate(event: AsyncChatDecorateEvent) {
        val player = event.player()
        if (player != null) {
            this.cancel(player, event)
        }
    }

    @EventHandler
    fun onAsyncChat(event: AsyncChatEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onCartographyItem(event: CartographyItemEvent) {
        this.cancel(event.whoClicked as Player, event)
    }

    @EventHandler
    fun onPlayerArmSwing(event: PlayerArmSwingEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerBedFailEnter(event: PlayerBedFailEnterEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerChangeBeaconEffect(event: PlayerChangeBeaconEffectEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerDeepSleep(event: PlayerDeepSleepEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerFlowerPotManipulate(event: PlayerFlowerPotManipulateEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerInsertLecternBook(event: PlayerInsertLecternBookEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerItemCooldown(event: PlayerItemCooldownEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerItemFrameChange(event: PlayerItemFrameChangeEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerItemGroupCooldown(event: PlayerItemGroupCooldownEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerLecternPageChange(event: PlayerLecternPageChangeEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerLoomPatternSelect(event: PlayerLoomPatternSelectEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerNameEntity(event: PlayerNameEntityEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerOpenSign(event: PlayerOpenSignEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerPickBlock(event: PlayerPickBlockEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerPickEntity(event: PlayerPickEntityEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerPickItem(event: PlayerPickItemEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerPurchase(event: PlayerPurchaseEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerShieldDisable(event: PlayerShieldDisableEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerSignCommandPreprocess(event: PlayerSignCommandPreprocessEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerStonecutterRecipeSelect(event: PlayerStonecutterRecipeSelectEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerTrackEntity(event: PlayerTrackEntityEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPlayerTrade(event: PlayerTradeEvent) {
        this.cancel(event.player, event)
    }

    @EventHandler
    fun onPrePlayerAttackEntity(event: PrePlayerAttackEntityEvent) {
        this.cancel(event.player, event)
    }

}