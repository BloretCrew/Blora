package blora.guild.menu.guildview

import blora.configuration.CONF
import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.guild.GuildJoinSource
import blora.guild.GuildJoinStrategy
import blora.guild.GuildPermissions
import blora.guild.anyGuildSettings
import blora.guild.anyRoleManagement
import blora.guild.menu.guildview.guildbank.guildBankMenu
import blora.guild.menu.guildview.guildinvitationcode.guildInvitationCodeMenu
import blora.guild.menu.guildview.guildmemberlist.guildMemberListMenu
import blora.guild.menu.guildview.guildrolemanagement.guildRoleManagementMenu
import blora.guild.menu.guildview.guildsettings.guildSettingsMenu
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.description
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.lines
import blora.menu.menuPage
import blora.menu.title
import blora.plugin.BloraPlugin
import blora.util.castString
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import java.util.EnumSet

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

fun guildView(viewer: Player, guild: GuildDao, guilds: MutableList<GuildDao>, rerenderGuildViewParent: () -> Unit): SimpleMenuPage {
    val isMember = guild.members.contains(viewer.uniqueId)
    val permissions = guild.getPlayerPermissions(viewer.uniqueId, isMember)
    val buttons = permissions.toButtons(guild, isMember)
    return menuPage {
        lines(6)
        title {
            localization(
                player = viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_viewTitle
            }
        }

        1 to 1 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(viewer) {
                        this.menuButtonBack
                    }
                }
            }
            clickEvent {
                it.stack.pop()
            }
        }

        1 to 5 eq {
            icon(guild.parsedIcon)
            hoverText {
                title {
                    localization(viewer) {
                        guild.displayName
                    }
                }
                description {
                    newline()
                    localization(
                        player = viewer,
                        tags = {
                            parsedPlaceholder("guild_level", guild.level.toString())
                        }
                    ) {
                        "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionLevel
                    }
                    newline()
                    localization(
                        player = viewer,
                        tags = {
                            parsedPlaceholder("guild_owner", BloraPlugin.database.getPlayerDisplayName(guild.owner))
                        }
                    ) {
                        "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionOwner
                    }
                    newline()
                    localization(
                        player = viewer,
                        tags = {
                            parsedPlaceholder("guild_create_date", guild.createAt.castString())
                        }
                    ) {
                        "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionCreated_at
                    }
                    newline()
                    localization(
                        player = viewer,
                        tags = {
                            parsedPlaceholder("guild_members", guild.members.size.toString())
                        }
                    ) {
                        "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionMembers
                    }
                    newline()
                    localization(
                        player = viewer,
                        tags = {
                            parsedPlaceholder("guild_vitality", guild.vitality.toString())
                        }
                    ) {
                        "<italic:false><white>" + this.guild.menu.menuGuild_listGuildDescriptionVitality
                    }
                    newline()
                    localization(
                        player = viewer,
                        tags = {
                            componentPlaceholder("guild_join_strategy") {
                                localization(viewer) {
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
        }
        buttons.forEachIndexed { index, button ->
            guildButtonPositions[index] eq {
                when (button) {
                    GuildViewButtons.GUILD_SETTINGS -> {
                        icon(ItemStack(Material.PAPER))
                        hoverText {
                            title {
                                localization(viewer) {
                                    this.guild.menu.menuGuild_viewButtonGuild_settings
                                }
                            }
                        }
                        clickEvent {
                            it.stack.push(guildSettingsMenu(viewer, guild, guilds, permissions, rerenderGuildViewParent))
                        }
                    }
                    GuildViewButtons.ROLE_MANAGEMENT -> {
                        icon(ItemStack(Material.PAPER))
                        hoverText {
                            title {
                                localization(viewer) {
                                    this.guild.menu.menuGuild_viewButtonRole_management
                                }
                            }
                        }
                        clickEvent {
                            it.stack.push(guildRoleManagementMenu(viewer, guild, permissions))
                        }
                    }
                    GuildViewButtons.MEMBER_LIST -> {
                        icon(ItemStack(Material.PAPER))
                        hoverText {
                            title {
                                localization(viewer) {
                                    this.guild.menu.menuGuild_viewButtonMember_list
                                }
                            }
                        }
                        clickEvent {
                            it.stack.push(guildMemberListMenu(viewer, guild, permissions))
                        }
                    }
                    GuildViewButtons.ALLY_LIST -> {
                        icon(ItemStack(Material.PAPER))
                        hoverText {
                            title {
                                localization(viewer) {
                                    this.guild.menu.menuGuild_viewButtonAlly_list
                                }
                            }
                        }
                    }
                    GuildViewButtons.INVITATION_CODES -> {
                        icon(ItemStack(Material.PAPER))
                        hoverText {
                            title {
                                localization(viewer) {
                                    this.guild.menu.menuGuild_viewButtonInvitation_codes
                                }
                            }
                        }
                        clickEvent { menuPageContext ->
                            menuPageContext.stack.push(guildInvitationCodeMenu(viewer, guild))
                        }
                    }
                    GuildViewButtons.ENDER_CHEST -> {
                        icon(ItemStack(Material.PAPER))
                        hoverText {
                            title {
                                localization(viewer) {
                                    this.guild.menu.menuGuild_viewButtonEnder_chest
                                }
                            }
                        }
                    }
                    GuildViewButtons.BANK -> {
                        icon(ItemStack(Material.PAPER))
                        hoverText {
                            title {
                                localization(viewer) {
                                    this.guild.menu.menuGuild_viewButtonBank
                                }
                            }
                        }
                        clickEvent {
                            it.stack.push(guildBankMenu(viewer, guild, permissions))
                        }
                    }
                    GuildViewButtons.VITALITY_SHOP -> {
                        icon(ItemStack(Material.PAPER))
                        hoverText {
                            title {
                                localization(viewer) {
                                    this.guild.menu.menuGuild_viewButtonVitality_shop
                                }
                            }
                        }
                    }
                    GuildViewButtons.JOIN_GUILD -> {
                        icon(ItemStack(Material.PAPER))
                        hoverText {
                            title {
                                localization(viewer) {
                                    this.guild.menu.menuGuild_viewButtonJoin_guild
                                }
                            }
                        }
                        clickEvent {
                            if (DB.listGuildForPlayer(viewer.uniqueId).size >= CONF.guild.playerMaxJoin) {
                                viewer.send {
                                    localization(
                                        player = viewer,
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
                                DB.guildJoinDirect(guild, viewer.uniqueId, GuildJoinSource.GuildList)
                                DB.announceJoin(guild, viewer)
                                viewer.send {
                                    localization(
                                        player = viewer,
                                        tags = {
                                            parsedPlaceholder("guild", guild.displayName)
                                        }
                                    ) {
                                        this.guild.guildJoinSuccess
                                    }
                                }
                                it.stack.replace(guildView(viewer, guild, guilds, rerenderGuildViewParent))
                            } else if (guild.joinStrategy == GuildJoinStrategy.INVITE_DIRECT || guild.joinStrategy == GuildJoinStrategy.REQUIRE_REVIEW) {
                                DB.createJoinRequest(guild, viewer, GuildJoinSource.GuildList)
                                DB.announceJoinRequest(guild, viewer)
                                viewer.send {
                                    localization(
                                        player = viewer,
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