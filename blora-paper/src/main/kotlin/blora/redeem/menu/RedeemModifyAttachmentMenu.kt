@file:Suppress("UnstableApiUsage")

package blora.redeem.menu

import blora.database.DB
import blora.database.redeem.dao.RedeemDao
import blora.extension.asDisplayName
import blora.extension.localization
import blora.extension.openDialog
import blora.item.clone
import blora.item.material
import blora.item.momi
import blora.mail.dataprovider.AttachmentDataProvider
import blora.mail.modifyAmountDialog
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.MenuPage
import blora.menu.v2.page.builder.*
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder
import plutoproject.adventurekt.text.raw
import net.momirealms.craftengine.core.util.Key as MomiKey

fun redeemModifyAttachmentMenuPage(menu: Menu, redeem: RedeemDao): MenuPage<*, *> {
    val attachment = redeem.parseAttachment.clone()
    val redeemId = redeem.id
    return pageableMenuPage(menu, AttachmentDataProvider(attachment)) {
        pageId {
            "redeemManagement_modifyAttachment_${redeemId}"
        }

        title {
            localization(menu.viewer) {
                this.mail.menuCreate_system_mailPageModify_attachmentTitle
            }
        }

        inventoryClick { item, clickContext ->
            if (!attachment.itemsContainsLike(item)) {
                attachment.items.add(item.clone().apply { this.amount = 1 } to 1u)
                clickContext.menu.rerender()
            }
            true
        }

        showBackButton()

        6 to 5 eq {
            icon { material { Material.EMERALD } }
            name {
                localization(menu.viewer) {
                    this.menuButtonConfirm
                }
            }
            clickEvent { clickContext ->
                DB.trans {
                    redeem.parseAttachment = attachment
                    redeem.flush()
                }
                clickContext.stack.pop()
            }
        }

        6 to 4 eq {
            icon { momi { MomiKey.of("bloret:coin") } }
            name {
                localization(menu.viewer) {
                    this.mail.menuCreate_system_mailPageModify_attachmentCoinTitle
                }
            }
            description {
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("amount", attachment.coins.toString())
                    }
                ) {
                    this.mail.menuCreate_system_mailPageModify_attachmentCoin_and_bloriusTooltip
                }
                newline()
                localization(menu.viewer) {
                    this.mail.menuCreate_system_mailPageModify_attachmentItemDescription1
                }
                newline()
                localization(menu.viewer) {
                    this.mail.menuCreate_system_mailPageModify_attachmentCoin_and_bloriusDescription2
                }
            }
            clickEvent { clickContext ->
                if (clickContext.click.isLeftClick) {
                    clickContext.menu.viewer.openDialog(
                        modifyAmountDialog(
                            clickContext.menu.viewer,
                            null,
                            component {
                                localization(clickContext.menu.viewer) {
                                    this.mail.dialogModify_amountTitle
                                }
                            },
                            yesCallback = { amount ->
                                attachment.coins = amount
                                clickContext.menu.rerender()
                            },
                            noCallback = {
                                clickContext.menu.rerender()
                            }
                        )
                    )
                } else if (clickContext.click.isRightClick) {
                    attachment.coins = 0u
                    clickContext.menu.rerender()
                }
            }
        }

        6 to 6 eq {
            icon { momi { MomiKey.of("bloret:blorius") } }
            name {
                localization(menu.viewer) {
                    this.mail.menuCreate_system_mailPageModify_attachmentBloriusTitle
                }
            }
            description {
                newline()
                localization(
                    player = menu.viewer,
                    tags = {
                        parsedPlaceholder("amount", attachment.blorius.toString())
                    }
                ) {
                    this.mail.menuCreate_system_mailPageModify_attachmentCoin_and_bloriusTooltip
                }
                newline()
                localization(menu.viewer) {
                    this.mail.menuCreate_system_mailPageModify_attachmentItemDescription1
                }
                newline()
                localization(menu.viewer) {
                    this.mail.menuCreate_system_mailPageModify_attachmentCoin_and_bloriusDescription2
                }
            }
            clickEvent { clickContext ->
                if (clickContext.click.isLeftClick) {
                    clickContext.menu.viewer.openDialog(
                        modifyAmountDialog(
                            clickContext.menu.viewer,
                            null,
                            component {
                                localization(clickContext.menu.viewer) {
                                    this.mail.dialogModify_amountTitle
                                }
                            },
                            yesCallback = { amount ->
                                attachment.blorius = amount
                                clickContext.menu.rerender()
                            },
                            noCallback = {
                                clickContext.menu.rerender()
                            }
                        )
                    )
                } else if (clickContext.click.isRightClick) {
                    attachment.blorius = 0u
                    clickContext.menu.rerender()
                }
            }
        }

        dataItem { viewContext, item, dataIndex ->
            icon { clone { item.first } }
            name {
                localization(
                    player = viewContext.viewer,
                    tags = {
                        parsedPlaceholder("amount", item.second.toString())
                    }
                ) {
                    this.mail.menuCreate_system_mailPageModify_attachmentItemTitle
                }
            }
            description {
                newline()
                localization(viewContext.viewer) {
                    this.mail.menuCreate_system_mailPageModify_attachmentItemTooltip1
                }
                raw { item.first.getData(DataComponentTypes.ITEM_NAME) ?: item.first.asDisplayName() }

                val lore = item.first.getData(DataComponentTypes.LORE)
                if (lore != null && lore.lines().isNotEmpty()) {
                    newline()
                    newline()
                    localization(viewContext.viewer) {
                        this.mail.menuCreate_system_mailPageModify_attachmentItemTooltip2
                    }
                    newline()
                    for (line in lore.lines()) {
                        raw { line }
                    }
                }
                newline()
                newline()
                localization(viewContext.viewer) {
                    this.mail.menuCreate_system_mailPageModify_attachmentItemDescription1
                }
                newline()
                localization(viewContext.viewer) {
                    this.mail.menuCreate_system_mailPageModify_attachmentItemDescription2
                }
            }
            clickEvent { clickContext ->
                if (clickContext.click.isLeftClick) {
                    clickContext.menu.viewer.openDialog(
                        modifyAmountDialog(
                            clickContext.menu.viewer,
                            null,
                            component {
                                localization(clickContext.menu.viewer) {
                                    this.mail.dialogModify_amountTitle
                                }
                            },
                            yesCallback = { amount ->
                                attachment.items[dataIndex] = item.first to amount
                                clickContext.menu.rerender()
                            },
                            noCallback = {}
                        )
                    )
                } else if (clickContext.click.isRightClick) {
                    attachment.items.removeAt(dataIndex)
                    clickContext.menu.rerender()
                }
            }
        }
    }
}