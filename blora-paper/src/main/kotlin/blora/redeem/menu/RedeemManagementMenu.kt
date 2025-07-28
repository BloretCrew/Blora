package blora.redeem.menu

import blora.extension.localization
import blora.extension.openDialog
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.description
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.builder.dataItem
import blora.menu.v2.page.builder.pageId
import blora.menu.v2.page.builder.pageableMenuPage
import blora.menu.v2.page.builder.title
import blora.plugin.BloraPlugin
import blora.redeem.createRedeemDialog
import blora.redeem.dataprovider.RedeemDaoDataProvider
import blora.util.castString
import org.bukkit.Material
import org.bukkit.entity.Player
import plutoproject.adventurekt.text.componentPlaceholder
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.parsedPlaceholder

fun redeemManagementMenu(
    viewer: Player
): Menu {
    return Menu(
        viewer,
        6,
        100L
    ).apply {
        this.closer {
            it.destroy()
        }
        this.stack.push {
            pageableMenuPage(this, RedeemDaoDataProvider()) {
                pageId { "redeem_management" }
                title {
                    localization(viewer) {
                        this.redeem.menuRedeem_code_managementTitle
                    }
                }

                6 to 5 eq {
                    icon { material { Material.APPLE } }
                    name {
                        localization(viewer) {
                            this.redeem.menuRedeem_code_managementButtonAdd_new
                        }
                    }
                    clickEvent { clickContext ->
                        viewer.openDialog(
                            createRedeemDialog(
                                clickContext.viewer,
                                null
                            ) {
                                clickContext.menu.rerender()
                            }
                        )
                    }
                }

                dataItem { viewContext, redeem, dataIndex ->
                    icon { material { Material.NAME_TAG } }
                    name {
                        localization(viewContext.viewer) {
                            "<italic:false><white>" + redeem.code
                        }
                    }
                    description {
                        newline()
                        localization(
                            player = viewContext.viewer,
                            tags = {
                                componentPlaceholder("creator") {
                                    mini(
                                        "<italic:false><white>" + BloraPlugin.database.getPlayerDisplayName(redeem.creator)
                                    )
                                }
                            }
                        ) {
                            "<italic:false><white>" + this.redeem.menuRedeem_code_managementItemTooltipCreator
                        }
                        newline()
                        localization(
                            player = viewContext.viewer,
                            tags = {
                                parsedPlaceholder("date", redeem.createdAt.castString())
                            }
                        ) {
                            "<italic:false><white>" + this.redeem.menuRedeem_code_managementItemTooltipCreated_at
                        }
                        newline()
                        newline()
                        localization(viewContext.viewer) {
                            this.redeem.redeemTooltipManagementLeft_click
                        }
                        newline()
                        localization(viewContext.viewer) {
                            this.redeem.redeemTooltipManagementRight_click
                        }
                    }

                    clickEvent { clickContext ->
                        if (clickContext.click.isLeftClick) {
                            clickContext.stack.push(
                                redeemModifyAttachmentMenuPage(
                                    clickContext.menu,
                                    redeem
                                )
                            )
                        } else if (clickContext.click.isRightClick) {
                            BloraPlugin.database.trans {
                                redeem.delete()
                            }
                            clickContext.menu.rerender()
                        }
                    }
                }
            }
        }
    }
}