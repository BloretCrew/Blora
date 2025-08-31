package blora.town.dialog

import blora.configuration.CONF
import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.database.town.dao.TownChunkDao
import blora.database.town.dao.TownDao
import blora.dialog.ConfirmationDialog
import blora.dialog.Dialog
import blora.dialog.action.ClickAction
import blora.dialog.action.DynamicCustomClickTypeInjected
import blora.dialog.body.PlainMessageDialogBody
import blora.dialog.input.TextInputControl
import blora.extension.*
import blora.item.itemStack
import blora.item.material
import blora.town.TownPermissionContainer
import blora.town.TownTarget
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.parsedPlaceholder
import java.time.LocalDateTime

fun createTownDialog(
    viewer: Player,
    guild: GuildDao,
    rerenderCallback: () -> Unit,
    initialId: String = "",
    initialName: String = "",
    warningMessage: Component? = null
): Dialog {
    return ConfirmationDialog(
        title = component {
            localization(viewer) {
                this.town.dialog.create_townTitle
            }
        },
        body = if (warningMessage != null)
            listOf(
                PlainMessageDialogBody(
                    contents = warningMessage
                )
            )
        else
            emptyList(),
        inputs = listOf(
            TextInputControl(
                key = "town_id",
                label = component {
                    localization(viewer) {
                        this.town.dialog.create_townInputPlaceholderId
                    }
                },
                initial = initialId
            ),
            TextInputControl(
                key = "town_name",
                label = component {
                    localization(viewer) {
                        this.town.dialog.create_townInputPlaceholderName
                    }
                },
                initial = initialName
            )
        ),
        yes = ClickAction(
            label = component {
                localization(viewer) {
                    this.dialogButtonConfirm
                }
            },
            action = DynamicCustomClickTypeInjected(
                callback = {
                    val townId = ((it as NbtCompound)["town_id"] as NbtString).value
                    val townName = (it["town_name"] as NbtString).value.ifBlank { "" }.ifEmpty { townId }
                    if (!townId.containsLetterAndNumberOnly()) {
                        viewer.openDialog(
                            createTownDialog(
                                viewer,
                                guild,
                                rerenderCallback,
                                townId,
                                townName,
                                component {
                                    localization(viewer) {
                                        this.town.dialog.create_townWarningId_illegal
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (townId.length < CONF.town.minIdLength || townId.length > CONF.town.maxIdLength) {
                        viewer.openDialog(
                            createTownDialog(
                                viewer,
                                guild,
                                rerenderCallback,
                                townId,
                                townName,
                                component {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder("min", CONF.town.minIdLength.toString())
                                            parsedPlaceholder("max", CONF.town.maxIdLength.toString())
                                        }
                                    ) {
                                        this.town.dialog.create_townWarningLength
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (TownDao.getByTownId(townId) != null) {
                        viewer.openDialog(
                            createTownDialog(
                                viewer,
                                guild,
                                rerenderCallback,
                                townId,
                                townName,
                                component {
                                    localization(viewer) {
                                        this.town.dialog.create_townWarningId_exists
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    DB.trans {
                        guild.refresh()
                    }
                    val permissions = DB.getRolePermissions(viewer.uniqueId, guild.gid)
                    if (!permissions.townManagement)
                        return@DynamicCustomClickTypeInjected
                    if (CONF.town.firstChunkPrice <= 0)
                        return@DynamicCustomClickTypeInjected
                    if (TownDao.list(guild.gid).size >= CONF.town.townsPerGuild) {
                        viewer.openDialog(
                            createTownDialog(
                                viewer,
                                guild,
                                rerenderCallback,
                                townId,
                                townName,
                                component {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder("limit", CONF.town.townsPerGuild.toString())
                                        }
                                    ) {
                                        this.town.createWarningTowns_limit
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (TownChunkDao.listByGuild(guild.gid).size >= guild.maxClaims) {
                        viewer.openDialog(
                            createTownDialog(
                                viewer,
                                guild,
                                rerenderCallback,
                                townId,
                                townName,
                                component {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder("limit", guild.maxClaims.toString())
                                        }
                                    ) {
                                        this.town.createWarningChunks_limit
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    if (CONF.town.firstChunkPrice > guild.bankBalance) {
                        viewer.openDialog(
                            createTownDialog(
                                viewer,
                                guild,
                                rerenderCallback,
                                townId,
                                townName,
                                component {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder("cost", guild.maxClaims.toString())
                                        }
                                    ) {
                                        this.town.createWarningBank_balance_not_enough
                                    }
                                }
                            )
                        )
                        return@DynamicCustomClickTypeInjected
                    }
                    val chunk = viewer.chunk
                    if (chunk.isClaimedByAnyTown()) {
                        viewer.send {
                            localization(viewer) {
                                this.town.createWarningChunks_occupied
                            }
                        }
                        return@DynamicCustomClickTypeInjected
                    }
                    DB.trans {
                        TownDao.new {
                            this.townId = townId
                            this.guildId = guild.gid

                            this.parsedIcon = itemStack { material { Material.STONE } }
                            this.displayName = townName

                            this.creator = viewer.uniqueId
                            this.createdAt = LocalDateTime.now()

                            this.world = viewer.world.name
                            this.centerChunkX = chunk.x
                            this.centerChunkZ = chunk.z

                            this.permissionContainers = listOf(
                                TownPermissionContainer(
                                    true,
                                    TownTarget.Blocked,
                                    TownTarget.Blocked.defaultPermissions(townId)
                                ),
                                TownPermissionContainer(
                                    true,
                                    TownTarget.Member,
                                    TownTarget.Member.defaultPermissions(townId)
                                ),
                                TownPermissionContainer(
                                    true,
                                    TownTarget.Ally,
                                    TownTarget.Ally.defaultPermissions(townId)
                                ),
                                TownPermissionContainer(
                                    true,
                                    TownTarget.Public,
                                    TownTarget.Public.defaultPermissions(townId)
                                )
                            )
                        }

                        TownChunkDao.new {
                            this.townId = townId
                            this.guildId = guild.gid

                            this.world = viewer.world.name
                            this.chunkX = chunk.x
                            this.chunkZ = chunk.z
                        }

                        guild.bankBalance -= CONF.town.firstChunkPrice
                        guild.flush()
                    }
                    viewer.send {
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("town_name", townName)
                            }
                        ) {
                            this.town.createSuccess
                        }
                    }
                    rerenderCallback()
                }
            )
        ),
        no = ClickAction(
            label = component {
                localization(viewer) {
                    this.dialogButtonCancel
                }
            }
        )
    )
}