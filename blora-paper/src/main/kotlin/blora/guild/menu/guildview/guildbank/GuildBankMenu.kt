package blora.guild.menu.guildview.guildbank

import blora.database.guild.dao.GuildDao
import blora.extension.localization
import blora.extension.openDialog
import blora.guild.GuildPermissions
import blora.guild.dialog.guildview.guildbank.guildBank_storeDialog
import blora.guild.dialog.guildview.guildbank.guildBank_withdrawDialog
import blora.menu.SimpleMenuPage
import blora.menu.clickEvent
import blora.menu.description
import blora.menu.hoverText
import blora.menu.icon
import blora.menu.lines
import blora.menu.menuPage
import blora.menu.title
import blora.plugin.ThirdPartys
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.text.parsedPlaceholder

fun guildBankMenu(viewer: Player, guild: GuildDao, permissions: Collection<GuildPermissions>): SimpleMenuPage {
    return menuPage {
        lines(5)
        title {
            localization(
                player = viewer,
                tags = {
                    parsedPlaceholder("guild", guild.displayName)
                }
            ) {
                this.guild.menu.menuGuild_bankTitle
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

        3 to 3 eq {
            icon(ItemStack(Material.CHEST))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuild_bankButtonStore
                    }
                }
                description {
                    localization(
                        player = viewer,
                        tags = {
                            parsedPlaceholder("amount", ThirdPartys.vaultApi.getBalance(viewer).toString())
                        }
                    ) {
                        this.guild.menu.menuGuild_bankButtonStoreDescription
                    }
                }
            }
            clickEvent {
                if (!(ThirdPartys.vaultApi.getBalance(viewer) > 0)) {
                    viewer.send {
                        localization(viewer) {
                            this.guild.menu.menuGuild_bankButtonStoreWarning
                        }
                    }
                    return@clickEvent
                }
                if (!permissions.contains(GuildPermissions.STORE_BANK)) {
                    viewer.send {
                        localization(viewer) {
                            this.guild.guildBankStoreNo_permission
                        }
                    }
                    return@clickEvent
                }
                viewer.openDialog(
                    guildBank_storeDialog(viewer, guild, { it.menu.rerender() })
                )
            }
        }

        3 to 7 eq {
            icon(ItemStack(Material.ENDER_CHEST))
            hoverText {
                title {
                    localization(viewer) {
                        this.guild.menu.menuGuild_bankButtonWithdraw
                    }
                }
                description {
                    localization(
                        player = viewer,
                        tags = {
                            parsedPlaceholder("amount", guild.bankBalance.toString())
                        }
                    ) {
                        this.guild.menu.menuGuild_bankButtonWithdrawDescription
                    }
                }
            }
            clickEvent {
                if (!(guild.bankBalance > 0)) {
                    viewer.send {
                        localization(viewer) {
                            this.guild.menu.menuGuild_bankButtonWithdrawWarning
                        }
                    }
                    return@clickEvent
                }
                if (!permissions.contains(GuildPermissions.WITHDRAW_BANK)) {
                    viewer.send {
                        localization(viewer) {
                            this.guild.guildBankWithdrawNo_permission
                        }
                    }
                    return@clickEvent
                }
                viewer.openDialog(
                    guildBank_withdrawDialog(viewer, guild, { it.menu.rerender() })
                )
            }
        }
    }
}