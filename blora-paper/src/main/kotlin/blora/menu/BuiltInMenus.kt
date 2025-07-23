package blora.menu

import blora.extension.localization
import blora.item.material
import blora.menu.v2.Menu
import blora.menu.v2.item.clickEvent
import blora.menu.v2.item.icon
import blora.menu.v2.item.name
import blora.menu.v2.page.StaticMenuPage
import blora.menu.v2.page.builder.backButton
import blora.menu.v2.page.builder.staticMenuPage
import blora.menu.v2.page.builder.title
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import plutoproject.adventurekt.text.raw

fun line5_confirmrationMenu(
    menu: Menu,
    title: Component,
    confirmCallback: () -> Unit
): StaticMenuPage {
    return staticMenuPage(menu) {
        title { title }
        backButton()
        3 to 3 eq {
            icon { material { Material.LIME_CONCRETE } }
            name {
                localization(menu.viewer) {
                    this.menuButtonConfirm
                }
            }
            clickEvent {
                it.stack.pop()
                confirmCallback()
            }
        }
        3 to 7 eq {
            icon { material { Material.RED_CONCRETE } }
            name {
                localization(menu.viewer) {
                    this.menuButtonCancel
                }
            }
            clickEvent {
                it.stack.pop()
            }
        }
    }
}

fun confirmationMenuLine5(
    viewer: Player,
    title: Component,
    confirmCallback: () -> Unit
) = menuPage {
    lines(6)
    title {
        raw { title }
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
        icon(ItemStack(Material.LIME_CONCRETE))
        hoverText {
            title {
                localization(viewer) {
                    this.menuButtonConfirm
                }
            }
        }
        clickEvent {
            it.stack.pop()
            confirmCallback()
        }
    }
    3 to 7 eq {
        icon(ItemStack(Material.RED_CONCRETE))
        hoverText {
            title {
                localization(viewer) {
                    this.menuButtonCancel
                }
            }
        }
        clickEvent {
            it.stack.pop()
        }
    }
}

fun confirmationMenuLine6(
    viewer: Player,
    title: Component,
    confirmCallback: () -> Unit
) = menuPage {
    lines(6)
    title {
        raw { title }
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
        icon(ItemStack(Material.LIME_CONCRETE))
        hoverText {
            title {
                localization(viewer) {
                    this.menuButtonConfirm
                }
            }
        }
        clickEvent {
            it.stack.pop()
            confirmCallback()
        }
    }
    3 to 7 eq {
        icon(ItemStack(Material.RED_CONCRETE))
        hoverText {
            title {
                localization(viewer) {
                    this.menuButtonCancel
                }
            }
        }
        clickEvent {
            it.stack.pop()
        }
    }
}

fun errorMenuLine1(viewer: Player): SimpleMenuPage {
    return menuPage {
        lines(1)
        title {
            localization(viewer) {
                this.menuErrorTitle
            }
        }
        1 to 5 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(viewer) {
                        this.menuButtonBack
                    }
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.pop()
            }
        }
    }
}

fun errorMenuLine2(viewer: Player): SimpleMenuPage {
    return menuPage {
        lines(2)
        title {
            localization(viewer) {
                this.menuErrorTitle
            }
        }
        1 to 5 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(viewer) {
                        this.menuButtonBack
                    }
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.pop()
            }
        }
    }
}

fun errorMenuLine3(viewer: Player): SimpleMenuPage {
    return menuPage {
        lines(3)
        title {
            localization(viewer) {
                this.menuErrorTitle
            }
        }
        2 to 5 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(viewer) {
                        this.menuButtonBack
                    }
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.pop()
            }
        }
    }
}

fun errorMenuLine4(viewer: Player): SimpleMenuPage {
    return menuPage {
        lines(4)
        title {
            localization(viewer) {
                this.menuErrorTitle
            }
        }
        2 to 5 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(viewer) {
                        this.menuButtonBack
                    }
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.pop()
            }
        }
    }
}

fun errorMenuLine5(viewer: Player): SimpleMenuPage {
    return menuPage {
        lines(5)
        title {
            localization(viewer) {
                this.menuErrorTitle
            }
        }
        3 to 5 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(viewer) {
                        this.menuButtonBack
                    }
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.pop()
            }
        }
    }
}

fun errorMenuLine6(viewer: Player): SimpleMenuPage {
    return menuPage {
        lines(6)
        title {
            localization(viewer) {
                this.menuErrorTitle
            }
        }
        3 to 5 eq {
            icon(ItemStack(Material.ARROW))
            hoverText {
                title {
                    localization(viewer) {
                        this.menuButtonBack
                    }
                }
            }
            clickEvent { clickContext ->
                clickContext.stack.pop()
            }
        }
    }
}