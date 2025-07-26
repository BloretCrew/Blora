package blora.guild.menu.guildview

import blora.configuration.CONF
import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.format
import blora.extension.localization
import blora.guild.*
import blora.guild.menu.guildview.guildallylist.guildAllyListMenu
import blora.guild.menu.guildview.guildbank.guildBankMenu
import blora.guild.menu.guildview.guildinvitationcode.guildInvitationCodeMenu
import blora.guild.menu.guildview.guildmemberlist.guildMemberListMenu
import blora.guild.menu.guildview.guildrolemanagement.guildRoleManagementMenu
import blora.guild.menu.guildview.guildsettings.guildSettingsMenu
import blora.guild.menu.guildview.guildvitalityshop.guildVitalityShopMenu
import blora.item.clone
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.backButton
import blora.menu.v2.page.builder.completeDynamicMenuPage
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.title
import blora.plugin.BloraPlugin
import blora.util.castString
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import java.util.*

private enum class GuildViewButtons {

    GUILD_SETTINGS,
    ROLE_MANAGEMENT,
    MEMBER_LIST,
    ALLY_LIST,
    INVITATION_CODES,
    ENDER_CHEST,
    BANK,
    VITALITY_SHOP,
    JOIN_GUILD

}

private fun EnumSet<GuildPermissions>.toButtons(guild: GuildDao, isMember: Boolean): List<GuildViewButtons> {
    val permissions = this
    return buildList {
        if (permissions.anyGuildSettings())
            this.add(GuildViewButtons.GUILD_SETTINGS)
        if (permissions.anyRoleManagement())
            this.add(GuildViewButtons.ROLE_MANAGEMENT)
        this.add(GuildViewButtons.MEMBER_LIST)
        this.add(GuildViewButtons.ALLY_LIST)
        if (permissions.contains(GuildPermissions.MANAGE_INVITATION_CODE))
            this.add(GuildViewButtons.INVITATION_CODES)
        if (permissions.contains(GuildPermissions.ENDER_CHEST))
            this.add(GuildViewButtons.ENDER_CHEST)
        if (isMember)
            this.add(GuildViewButtons.BANK)
        if (permissions.contains(GuildPermissions.USE_VITALITY))
            this.add(GuildViewButtons.VITALITY_SHOP)
        if (!isMember && guild.joinStrategy != GuildJoinStrategy.NOT_ALLOW)
            this.add(GuildViewButtons.JOIN_GUILD)
    }
}

private val guildButtonPositions = listOf(
    3 to 2,
    3 to 4,
    3 to 6,
    3 to 8,
    4 to 2,
    4 to 4,
    4 to 6,
    4 to 8
)

fun guildViewMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    val guildId = guild.id // not using gid but id is because id won't be duplicated event guild admin changed gid
    return completeDynamicMenuPage(menu) {
        DB.trans {
            guild.refresh()
        } // ensure data updated for security
        val isMember = guild.members.contains(menu.viewer.uniqueId)
        val permissions = guild.getPlayerPermissions(menu.viewer.uniqueId, isMember)
        val buttons = permissions.toButtons(guild, isMember)

        pageId {
            "guild_$guildId"
        }

        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_viewTitle
            }
        }

        backButton()

        1 to 5 eq {
            icon { clone { guild.parsedIcon } }
            name {
                localization(menu.viewer) {
                    guild.displayName
                }
            }
            description {
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_level", guild.level.toString())
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionLevel
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_owner", BloraPlugin.database.getPlayerDisplayName(guild.owner))
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionOwner
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_create_date", guild.createAt.castString())
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionCreated_at
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_members", guild.members.size.toString())
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionMembers
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("guild_vitality", guild.vitality.format(2))
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionVitality
                }
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        componentPlaceholder("guild_join_strategy") {
                            localization(menu.viewer) {
                                when (guild.joinStrategy) {
                                    GuildJoinStrategy.DIRECT -> this.guild.guildJoin_strategyDirect
                                    GuildJoinStrategy.INVITE_DIRECT -> this.guild.guildJoin_strategyInvite_direct
                                    GuildJoinStrategy.REQUIRE_REVIEW -> this.guild.guildJoin_strategyRequire_review
                                    GuildJoinStrategy.NOT_ALLOW -> this.guild.guildJoin_strategyNot_allow
                                }
                            }
                        }
                    }
                ) {
                    "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionJoin_strategy
                }
            }
        }

        buttons.forEachIndexed { index, button ->
            guildButtonPositions[index] eq {
                when (button) {
                    GuildViewButtons.GUILD_SETTINGS -> {
                        icon { material { Material.PAPER } }
                        name {
                            localization(menu.viewer) {
                                this.guild.menu.menuGuild_viewButtonGuild_settings
                            }
                        }
                        clickEvent {
                            it.stack.push {
                                guildSettingsMenu(menu, guild)
                            }
                        }
                    }

                    GuildViewButtons.ROLE_MANAGEMENT -> {
                        icon { material { Material.PAPER } }
                        name {
                            localization(menu.viewer) {
                                this.guild.menu.menuGuild_viewButtonRole_management
                            }
                        }
                        clickEvent {
                            it.stack.push {
                                guildRoleManagementMenu(menu, guild)
                            }
                        }
                    }

                    GuildViewButtons.MEMBER_LIST -> {
                        icon { material { Material.PAPER } }
                        name {
                            localization(menu.viewer) {
                                this.guild.menu.menuGuild_viewButtonMember_list
                            }
                        }
                        clickEvent {
                            it.stack.push {
                                guildMemberListMenu(menu, guild)
                            }
                        }
                    }

                    GuildViewButtons.ALLY_LIST -> {
                        icon { material { Material.ALLAY_SPAWN_EGG } }
                        name {
                            localization(menu.viewer) {
                                this.guild.menu.menuGuild_viewButtonAlly_list
                            }
                        }
                        clickEvent {
                            it.stack.push {
                                guildAllyListMenu(menu, guild)
                            }
                        }
                    }

                    GuildViewButtons.INVITATION_CODES -> {
                        icon { material { Material.NAME_TAG } }
                        name {
                            localization(menu.viewer) {
                                this.guild.menu.menuGuild_viewButtonInvitation_codes
                            }
                        }
                        clickEvent {
                            it.stack.push {
                                guildInvitationCodeMenu(menu, guild)
                            }
                        }
                    }

                    GuildViewButtons.ENDER_CHEST -> {
                        icon { material { Material.ENDER_CHEST } }
                        name {
                            localization(menu.viewer) {
                                this.guild.menu.menuGuild_viewButtonEnder_chest
                            }
                        }
                        clickEvent { clickContext ->
                            clickContext.menu.destroy()
                            GuildEnderChestManager.open(guild, clickContext.viewer)
                        }
                    }

                    GuildViewButtons.BANK -> {
                        icon { material { Material.CHEST } }
                        name {
                            localization(menu.viewer) {
                                this.guild.menu.menuGuild_viewButtonBank
                            }
                        }
                        clickEvent {
                            it.stack.push {
                                guildBankMenu(menu, guild)
                            }
                        }
                    }

                    GuildViewButtons.VITALITY_SHOP -> {
                        icon { material { Material.BEEHIVE } }
                        name {
                            localization(menu.viewer) {
                                this.guild.menu.menuGuild_viewButtonVitality_shop
                            }
                        }
                        clickEvent {
                            it.stack.push {
                                guildVitalityShopMenu(menu, guild)
                            }
                        }
                    }

                    GuildViewButtons.JOIN_GUILD -> {
                        icon { material { Material.IRON_PICKAXE } }
                        name {
                            localization(menu.viewer) {
                                this.guild.menu.menuGuild_viewButtonJoin_guild
                            }
                        }
                        clickEvent { clickContext ->
                            if (DB.listGuildForPlayer(clickContext.viewer.uniqueId).size >= CONF.guild.playerMaxJoin) {
                                clickContext.viewer.send {
                                    localization(
                                        player = clickContext.viewer,
                                        tags = {
                                            parsedPlaceholder("amount", CONF.guild.playerMaxJoin.toString())
                                        }
                                    ) {
                                        this.guild.guildJoinLimit
                                    }
                                }
                                return@clickEvent
                            }
                            if (guild.joinStrategy == GuildJoinStrategy.DIRECT) {
                                DB.guildJoinDirect(guild, clickContext.viewer.uniqueId, GuildJoinSource.GuildList)
                                DB.announceJoin(guild, clickContext.viewer)
                                clickContext.viewer.send {
                                    localization(
                                        player = clickContext.viewer,
                                        tags = {
                                            parsedPlaceholder("guild", guild.displayName)
                                        }
                                    ) {
                                        this.guild.guildJoinSuccess
                                    }
                                }
                                clickContext.menu.rerender()
                            } else if (guild.joinStrategy == GuildJoinStrategy.INVITE_DIRECT || guild.joinStrategy == GuildJoinStrategy.REQUIRE_REVIEW) {
                                DB.createJoinRequest(guild, clickContext.viewer, GuildJoinSource.GuildList)
                                DB.announceJoinRequest(guild, clickContext.viewer)
                                clickContext.viewer.send {
                                    localization(
                                        player = clickContext.viewer,
                                        tags = {
                                            parsedPlaceholder("guild", guild.displayName)
                                        }
                                    ) {
                                        this.guild.guildJoinRequest_success
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}