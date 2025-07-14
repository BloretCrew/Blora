package blora.modules.redeem

import blora.database.redeem.RedeemDao
import blora.extension.localization
import blora.extension.openDialog
import blora.menu.*
import blora.modules.mail.Attachment
import blora.modules.mail.modifyAmountDialog
import blora.plugin.BloraPlugin
import blora.util.castString
import io.papermc.paper.datacomponent.DataComponentTypes
import net.momirealms.craftengine.bukkit.api.CraftEngineItems
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.audience.send
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.*
import net.momirealms.craftengine.core.util.Key as MomiKey

fun redeemManagementMenu(
    player: Player
): Menu {
    val redeems = BloraPlugin.database.listRedeemCodes().toMutableList()
    return Menu(
        redeems, // use cached system mails
        player,
        6,
        component {
            localization(player) {
                this.menuRedeem_code_managementTitle
            }
        },
        { menu ->
            menu.destroy()
        }
    ) {}.apply { this.stack.replace(redeemManagementPage(redeems, player)) }
}

fun redeemManagementPage(
    redeems: MutableList<RedeemDao>,
    player: Player,
    currentPage: Int = 1
): MenuPage = menuPage {
    title {
        localization(player) {
            this.menuRedeem_code_managementTitle
        }
    }
    lines(6)

    mapping(
        "#########",
        "#       #",
        "#       #",
        "#       #",
        "#       #",
        "#########"
    )
    '#' eq {
        icon(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
    }

    if (redeems.size <= (currentPage - 1) * 28 - 1)
        return@menuPage

    if (currentPage > 1) {
        6 to 1 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(player) {
                        this.menuButtonPrevious_page
                    }
                }
            }
            clickEvent {
                it.stack.pop()
                it.stack.push(redeemManagementPage(redeems, player, currentPage - 1))
            }
        }
    }

    if (redeems.size > (currentPage * 28)) {
        6 to 9 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(player) {
                        this.menuButtonNext_page
                    }
                }
            }
            clickEvent {
                it.stack.pop()
                it.stack.push(redeemManagementPage(redeems, player, currentPage + 1))
            }
        }
    }

    6 to 5 eq {
        icon(ItemStack(Material.APPLE))
        hoverText {
            title {
                localization(player) {
                    this.menuRedeem_code_managementButtonAdd_new
                }
            }
        }
        clickEvent {
            player.openDialog(
                createRedeemDialog(
                    player,
                    null,
                    { redeem ->
                        redeems.add(redeem)
                        it.stack.push(redeemManagementPage(redeems, player, currentPage))
                        player.send {
                            localization(
                                player = player,
                                tags = {
                                    parsedPlaceholder("redeem", redeem.code)
                                }
                            ) {
                                this.redeemCreate
                            }
                        }
                    }
                )
            )
        }
    }

    redeems.forEachIndexed { index, redeem ->
        if (index < (currentPage - 1) * 28 || index > currentPage * 28 - 1) // not current page
            return@forEachIndexed
        val counterIndex = index - (currentPage - 1) * 28
        ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
            icon(ItemStack(Material.NAME_TAG))
            hoverText {
                title {
                    localization(player = player) {
                        "<italic:false><white>" + redeem.code
                    }
                }
                description {
                    newline()
                    localization(
                        player = player,
                        tags = {
                            componentPlaceholder("creator") {
                                mini(
                                    "<italic:false><white>" + BloraPlugin.database.getPlayerDisplayName(redeem.creator)
                                )
                            }
                        }
                    ) {
                        "<italic:false><white>" + this.menuRedeem_code_managementItemTooltipCreator
                    }
                    newline()
                    localization(
                        player = player,
                        tags = {
                            parsedPlaceholder("date", redeem.createdAt.castString())
                        }
                    ) {
                        "<italic:false><white>" + this.menuRedeem_code_managementItemTooltipCreated_at
                    }
                    newline()
                    newline()
                    localization(player) {
                        this.redeemTooltipManagementLeft_click
                    }
                    newline()
                    localization(player) {
                        this.redeemTooltipManagementRight_click
                    }
                }
            }
            clickEvent { clickContext ->
                if (clickContext.clickType.isLeftClick) {
                    clickContext.stack.push(
                        redeemModifyAttachmentMenuPage(
                            redeem.parseAttachment.clone(),
                            player,
                            1
                        ) {
                            BloraPlugin.database.trans {
                                redeem.parseAttachment = it
                                redeem.flush()
                            }
                        }
                    )
                } else if (clickContext.clickType.isRightClick) {
                    BloraPlugin.database.trans {
                        redeem.delete()
                        redeem.flush()
                    }
                    redeems.remove(redeem)
                    clickContext.stack.pop()
                    if (redeems.size <= (currentPage - 1) * 28 - 1) { // this page does no longer exist
                        clickContext.stack.push(redeemManagementPage(redeems, player, currentPage - 1))
                    } else {
                        clickContext.stack.push(redeemManagementPage(redeems, player, currentPage))
                    }
                }
            }
        }
    }
}

fun redeemModifyAttachmentMenuPage(
    attachment: Attachment,
    player: Player,
    currentPage: Int = 1,
    confirmCallback: (newAttachment: Attachment) -> Unit
): MenuPage = menuPage {
    title {
        localization(player) {
            this.menuCreate_system_mailPageModify_attachmentTitle
        }
    }
    lines(6)

    mapping(
        "#########",
        "#       #",
        "#       #",
        "#       #",
        "#       #",
        "#########",
    )

    '#' eq {
        icon(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
    }

    inventoryClick { item, context ->
        if (context.clickType.isLeftClick) {
            if (attachment.itemsContainsLike(item)) {
                return@inventoryClick true
            }
            attachment.items[item.clone().apply { this.amount = 1 }] = 1u

            // rerender cannot solve the add item problem, recreate current page to modify
            context.stack.pop()
            context.stack.push(redeemModifyAttachmentMenuPage(attachment, player, currentPage, confirmCallback))
        }
        return@inventoryClick true
    }

    1 to 1 eq {
        icon(ItemStack(Material.ARROW))
        hoverText {
            title {
                localization(player) {
                    this.menuButtonBack
                }
            }
        }
        clickEvent {
            it.stack.pop()
        }
    }

    // do not show anything
    if (attachment.items.size <= (currentPage - 1) * 28 - 1)
        return@menuPage

    if (currentPage > 1) {
        6 to 1 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(player) {
                        this.menuButtonPrevious_page
                    }
                }
            }
            clickEvent {
                it.stack.pop()
                it.stack.push(redeemModifyAttachmentMenuPage(attachment, player, currentPage - 1, confirmCallback))
            }
        }
    }

    if (attachment.items.size > (currentPage * 28)) {
        6 to 9 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(player) {
                        this.menuButtonNext_page
                    }
                }
            }
            clickEvent {
                it.stack.pop()
                it.stack.push(redeemModifyAttachmentMenuPage(attachment, player, currentPage + 1, confirmCallback))
            }
        }
    }

    6 to 5 eq {
        icon(ItemStack(Material.EMERALD))
        hoverText {
            title {
                localization(player) {
                    this.menuButtonConfirm
                }
            }
        }
        clickEvent { clickContext ->
            clickContext.stack.pop()
            confirmCallback.invoke(attachment)
        }
    }

    6 to 4 eq {
        icon(
            CraftEngineItems.byId(MomiKey.of("bloret:coin"))?.buildItemStack()
                ?: ItemStack(Material.SUNFLOWER) // fallback icon
        )
        hoverText {
            title {
                localization(player) {
                    this.menuCreate_system_mailPageModify_attachmentCoinTitle
                }
            }
            description {
                newline()
                localization(
                    player = player,
                    tags = {
                        parsedPlaceholder("amount", attachment.coins.toString())
                    }
                ) {
                    this.menuCreate_system_mailPageModify_attachmentCoin_and_bloriusTooltip
                }
                newline()
                localization(player) {
                    this.menuCreate_system_mailPageModify_attachmentItemDescription1
                }
                newline()
                localization(player) {
                    this.menuCreate_system_mailPageModify_attachmentCoin_and_bloriusDescription2
                }
            }
        }
        clickEvent { clickContext ->
            if (clickContext.clickType.isLeftClick) {
                clickContext.menu.viewer.openDialog(
                    modifyAmountDialog(
                        clickContext.menu.viewer,
                        null,
                        component {
                            localization(clickContext.menu.viewer) {
                                this.dialogModify_amountTitle
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
            } else if (clickContext.clickType.isRightClick) {
                attachment.coins = 0u
                clickContext.menu.rerender()
            }
        }
    }

    6 to 6 eq {
        icon(
            CraftEngineItems.byId(MomiKey.of("bloret:blorius"))?.buildItemStack()
                ?: ItemStack(Material.DIAMOND) // fallback icon
        )
        hoverText {
            title {
                localization(player) {
                    this.menuCreate_system_mailPageModify_attachmentBloriusTitle
                }
            }
            description {
                newline()
                localization(
                    player = player,
                    tags = {
                        parsedPlaceholder("amount", attachment.blorius.toString())
                    }
                ) {
                    this.menuCreate_system_mailPageModify_attachmentCoin_and_bloriusTooltip
                }
                newline()
                localization(player) {
                    this.menuCreate_system_mailPageModify_attachmentItemDescription1
                }
                newline()
                localization(player) {
                    this.menuCreate_system_mailPageModify_attachmentCoin_and_bloriusDescription2
                }
            }
        }
        clickEvent { clickContext ->
            if (clickContext.clickType.isLeftClick) {
                clickContext.menu.viewer.openDialog(
                    modifyAmountDialog(
                        clickContext.menu.viewer,
                        null,
                        component {
                            localization(clickContext.menu.viewer) {
                                this.dialogModify_amountTitle
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
            } else if (clickContext.clickType.isRightClick) {
                attachment.blorius = 0u
                clickContext.menu.rerender()
            }
        }
    }

    attachment.items.toList().forEachIndexed { index, item ->
        if (index < (currentPage - 1) * 28 || index > currentPage * 28 - 1) // not current page
            return@forEachIndexed
        val counterIndex = index - (currentPage - 1) * 28
        ((counterIndex / 7) + 2) to (counterIndex - ((counterIndex / 7) * 7) + 2) eq {
            icon(item.first.clone())
            hoverText {
                title {
                    localization(
                        player = player,
                        tags = {
                            parsedPlaceholder("amount", item.second.toString())
                        }
                    ) {
                        this.menuCreate_system_mailPageModify_attachmentItemTitle
                    }
                }
                description {
                    newline()
                    localization(player = player) {
                        this.menuCreate_system_mailPageModify_attachmentItemTooltip1
                    }
                    raw { item.first.getData(DataComponentTypes.ITEM_NAME) ?: item.first.displayName() }

                    val lore = item.first.getData(DataComponentTypes.LORE)
                    if (lore != null && lore.lines().isNotEmpty()) {
                        newline()
                        newline()
                        localization(player = player) {
                            this.menuCreate_system_mailPageModify_attachmentItemTooltip2
                        }
                        newline()
                        for (line in lore.lines()) {
                            raw { line }
                        }
                    }
                    newline()
                    newline()
                    localization(player) {
                        this.menuCreate_system_mailPageModify_attachmentItemDescription1
                    }
                    newline()
                    localization(player) {
                        this.menuCreate_system_mailPageModify_attachmentItemDescription2
                    }
                }
            }
            clickEvent { clickContext ->
                if (clickContext.clickType.isLeftClick) {
                    clickContext.menu.viewer.openDialog(
                        modifyAmountDialog(
                            clickContext.menu.viewer,
                            null,
                            component {
                                localization(clickContext.menu.viewer) {
                                    this.dialogModify_amountTitle
                                }
                            },
                            yesCallback = { amount ->
                                attachment.items[item.first] = amount
                                clickContext.stack.pop()
                                clickContext.stack.push(
                                    redeemModifyAttachmentMenuPage(
                                        attachment,
                                        player,
                                        currentPage,
                                        confirmCallback
                                    )
                                )
                            },
                            noCallback = {
                                clickContext.stack.pop()
                                clickContext.stack.push(
                                    redeemModifyAttachmentMenuPage(
                                        attachment,
                                        player,
                                        currentPage,
                                        confirmCallback
                                    )
                                )
                            }
                        )
                    )
                } else if (clickContext.clickType.isRightClick) {
                    attachment.items.remove(item.first)
                    clickContext.stack.pop()
                    if (attachment.items.size <= (currentPage - 1) * 28 - 1) { // this page does no longer exist
                        clickContext.stack.push(
                            redeemModifyAttachmentMenuPage(
                                attachment,
                                player,
                                currentPage - 1,
                                confirmCallback
                            )
                        )
                    } else {
                        clickContext.stack.push(
                            redeemModifyAttachmentMenuPage(
                                attachment,
                                player,
                                currentPage,
                                confirmCallback
                            )
                        )
                    }
                }
            }
        }
    }
}
