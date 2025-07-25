package blora.menu.v2.page

import blora.menu.v2.Menu
import blora.menu.v2.context.CompleteDynamicMenuClickContext
import blora.menu.v2.context.CompleteDynamicMenuViewContext
import blora.menu.v2.page.builder.CompleteDynamicMenuPageBuilder
import blora.menu.v2.page.snapshot.MenuPageSnapshot
import org.bukkit.inventory.Inventory

class CompleteDynamicMenuPage(
    override val pageId: String,
    private val builder: CompleteDynamicMenuPageBuilder,
    private val launchEffect: (CompleteDynamicMenuViewContext) -> Unit,
    private val disposeEffect: (CompleteDynamicMenuViewContext) -> Unit,
) : MenuPage<CompleteDynamicMenuClickContext, CompleteDynamicMenuViewContext> {

    override fun fireLaunchEffect(menu: Menu) {
        this.launchEffect(CompleteDynamicMenuViewContext(menu))
    }

    override fun fireDisposeEffect(menu: Menu) {
        this.disposeEffect(CompleteDynamicMenuViewContext(menu))
    }

    override fun render(
        menu: Menu,
        inventory: Inventory
    ): MenuPageSnapshot<out CompleteDynamicMenuClickContext, out CompleteDynamicMenuViewContext> {
        return this.builder.renderOnce(menu, inventory)
    }

}