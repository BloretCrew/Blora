package blora.guild.menu.guildview.guildinvitationcode

import blora.database.DB
import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.dialog.guildview.guildinvitationcode.guildInvitationCode_CreateInvitationCodeDialog
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.description
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.lines
import blora.menu.mapping
import blora.menu.menuPage
import blora.menu.title
import blora.util.castString
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import java.time.LocalDate

fun guildInvitationCodeMenu(viewer: Player, guild: GuildDao, currentPage: Int = 1): SimpleMenuPage {
    val invitationCodes = DB.listInvitationCodes(guild.gid)
        .filter {
            if (it.expireAt != null && it.expireAt!! < LocalDate.now() && !it.outdated) {
                DB.trans {
                    it.outdated = true
                    it.flush()
                }
                return@filter true
            }
            return@filter true
        }
        .filter { !it.outdated }
    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_invitation_codeTitle
            }
        }

        mapping(
            "#########",
            "#       #",
            "#       #",
            "#       #",
            "#########",
        )

        '#' eq {
            icon(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
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

        if (currentPage > 1) {
            5 to 1 eq {
                icon(ItemStack(Material.ARROW))
                hoverText {
                    title {
                        localization(viewer) {
                            this.menuButtonPrevious_page
                        }
                    }
                }
                clickEvent {
                    it.stack.pop()
                    it.stack.push(guildInvitationCodeMenu(viewer, guild, currentPage - 1))
                }
            }
        }

        if (invitationCodes.size > (currentPage * 21)) {
            5 to 9 eq {
                icon(ItemStack(Material.ARROW))
                hoverText {
                    title {
                        localization(viewer) {
                            this.menuButtonNext_page
                        }
                    }
                }
                clickEvent {
                    it.stack.pop()
                    it.stack.push(guildInvitationCodeMenu(viewer, guild, currentPage + 1))
                }
            }
        }

        5 to 5 eq {
            icon(ItemStack(Material.APPLE))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuild_invitation_codeButtonCreate
                    }
                }
            }
            clickEvent { menuPageContext ->
                viewer.openDialog(
                    guildInvitationCode_CreateInvitationCodeDialog(viewer, guild) {
                        menuPageContext.stack.replace(guildInvitationCodeMenu(viewer, guild, currentPage))
                        viewer.send {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("code", it)
                                }
                            ) {
                                this.guild.guildInvitation_codeCreate
                            }
                        }
                    }
                )
            }
        }

        invitationCodes.forEachIndexed { index, code ->
            if (index < (currentPage - 1) * 21 || index > currentPage * 21 - 1) // not current page
                return@forEachIndexed
            val counterIndex = index - (currentPage - 1) * 21
            ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
                icon(ItemStack(Material.NAME_TAG))
                hoverText {
                    title {
                        localization(
                            player = viewer,
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
                            player = viewer,
                            tags = {
                                parsedPlaceholder("single_use", code.singleUsable.toString())
                            }
                        ) {
                            this.guild.menu.menuGuild_invitation_codeItemDescriptionSingle_use
                        }
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("creator", DB.getPlayerDisplayName(code.creator))
                            }
                        ) {
                            this.guild.menu.menuGuild_invitation_codeItemDescriptionCreator
                        }
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                parsedPlaceholder("date", code.createdAt.castString())
                            }
                        ) {
                            this.guild.menu.menuGuild_invitation_codeItemDescriptionCreated_at
                        }
                        newline()
                        localization(
                            player = viewer,
                            tags = {
                                if (code.expireAt != null) {
                                    parsedPlaceholder("date", code.expireAt!!.castString())
                                } else {
                                    componentPlaceholder("date") {
                                        localization(viewer) {
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
                        localization(viewer) {
                            this.guild.menu.menuGuild_invitation_codeItemDescriptionRight
                        }
                    }
                }
                clickEvent {
                    if (it.clickType.isRightClick) {
                        DB.trans {
                            code.delete()
                        }
                        viewer.send {
                            localization(
                                player = viewer,
                                tags = {
                                    parsedPlaceholder("code", code.inviteCode)
                                }
                            ) {
                                this.guild.guildInvitation_codeDelete
                            }
                        }
                        if (invitationCodes.size - 1 <= (currentPage - 1) * 21 - 1) { // this page does no longer exist
                            it.stack.replace(guildInvitationCodeMenu(viewer, guild, currentPage - 1))
                        } else {
                            it.stack.replace(guildInvitationCodeMenu(viewer, guild, currentPage))
                        }
                    }
                }
            }
        }
    }
}