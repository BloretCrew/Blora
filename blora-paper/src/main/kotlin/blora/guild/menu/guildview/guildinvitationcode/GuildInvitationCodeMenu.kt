package blora.guild.menu.guildview.guildinvitationcode

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.dataprovider.GuildInviteCodeDaoDataProvider
import blora.guild.dialog.guildview.guildinvitationcode.guildInvitationCode_CreateInvitationCodeDialog
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.dataItem
import blora.menu.v2.page.builder.pageableMenuPage
import blora.menu.v2.page.builder.showBackButton
import blora.menu.v2.page.builder.title
import blora.util.castString
import org.bukkit.Material
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildInvitationCodeMenu(menu: Menu, guild: GuildDao): MenuPage<*, *> {
    return pageableMenuPage(menu, GuildInviteCodeDaoDataProvider(guild.gid)) {
        title {
            localization(
                player = menu.viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_invitation_codeTitle
            }
        }
        showBackButton()

        5 to 5 eq {
            icon { material { Material.APPLE } }
            name {
                localization(menu.viewer) {
                    this.guild.menu.menuGuild_invitation_codeButtonCreate
                }
            }
            clickEvent { clickContext ->
                clickContext.viewer.openDialog(
                    guildInvitationCode_CreateInvitationCodeDialog(clickContext.viewer, guild) {
                        clickContext.menu.rerender()
                        if (DB.getRolePermissions(clickContext.viewer.uniqueId, guild.gid).manageInvitationCode) {
                            clickContext.viewer.send {
                                localization(
                                    player = clickContext.viewer,
                                    tags = {
                                        parsedPlaceholder("code", it)
                                    }
                                ) {
                                    this.guild.guildInvitation_codeCreate
                                }
                            }
                        }
                    }
                )
            }
        }

        dataItem { viewContext, code ->
            icon { material { Material.NAME_TAG } }
            name {
                localization(
                    player = viewContext.viewer,
                    tags = {
                        parsedPlaceholder("code", code.inviteCode)
                    }
                ) {
                    this.guild.menu.menuGuild_invitation_codeItemFormat
                }
            }
            description {
                newline()
                localization(
                    player = viewContext.viewer,
                    tags = {
                        parsedPlaceholder("single_use", code.singleUsable.toString())
                    }
                ) {
                    this.guild.menu.menuGuild_invitation_codeItemDescriptionSingle_use
                }
                newline()
                localization(
                    player = viewContext.viewer,
                    tags = {
                        parsedPlaceholder("creator", DB.getPlayerDisplayName(code.creator))
                    }
                ) {
                    this.guild.menu.menuGuild_invitation_codeItemDescriptionCreator
                }
                newline()
                localization(
                    player = viewContext.viewer,
                    tags = {
                        parsedPlaceholder("date", code.createdAt.castString())
                    }
                ) {
                    this.guild.menu.menuGuild_invitation_codeItemDescriptionCreated_at
                }
                newline()
                localization(
                    player = viewContext.viewer,
                    tags = {
                        if (code.expireAt != null) {
                            parsedPlaceholder("date", code.expireAt!!.castString())
                        } else {
                            componentPlaceholder("date") {
                                localization(viewContext.viewer) {
                                    this.guild.menu.menuGuild_invitation_codeItemDescriptionExpireInfinite
                                }
                            }
                        }
                    }
                ) {
                    this.guild.menu.menuGuild_invitation_codeItemDescriptionExpire_at
                }
                newline()
                newline()
                localization(viewContext.viewer) {
                    this.guild.menu.menuGuild_invitation_codeItemDescriptionRight
                }
            }
            clickEvent { clickContext ->
                if (clickContext.click.isRightClick) {
                    if (!DB.getRolePermissions(clickContext.viewer.uniqueId, guild.gid).manageInvitationCode)
                        return@clickEvent
                    DB.trans {
                        code.delete()
                    }
                    clickContext.viewer.send {
                        localization(
                            player = clickContext.viewer,
                            tags = {
                                parsedPlaceholder("code", code.inviteCode)
                            }
                        ) {
                            this.guild.guildInvitation_codeDelete
                        }
                    }
                    clickContext.menu.rerender()
                }
            }
        }
    }
}