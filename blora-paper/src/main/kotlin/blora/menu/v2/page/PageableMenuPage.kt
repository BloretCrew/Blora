package blora.menu.v2.page

import blora.menu.v2.Menu
import blora.menu.v2.context.PageableMenuClickContext
import blora.menu.v2.context.PageableMenuViewContext
import blora.menu.v2.page.builder.PageableMenuPageBuilder
import blora.menu.v2.page.snapshot.MenuPageSnapshot
import org.bukkit.inventory.Inventory

class PageableMenuPage<D>(
    private val builder: PageableMenuPageBuilder<D>,
    private val launchEffect: (PageableMenuViewContext<D>) -> Unit,
    private val disposeEffect: (PageableMenuViewContext<D>) -> Unit,
) : MenuPage<PageableMenuClickContext<D>, PageableMenuViewContext<D>> {

    var currentPage = 1
        internal set

    override fun fireLaunchEffect(menu: Menu) {
        this.builder.dataProvider
        this.launchEffect(PageableMenuViewContext(menu, this.currentPage, this.builder.calculateMaxPage(menu.lines), this.builder.dataProvider))
    }

    override fun fireDisposeEffect(menu: Menu) {
        this.disposeEffect(PageableMenuViewContext(menu, this.currentPage, this.builder.calculateMaxPage(menu.lines), this.builder.dataProvider))
    }

    override fun render(
        menu: Menu,
        inventory: Inventory
    ): MenuPageSnapshot<out PageableMenuClickContext<D>, out PageableMenuViewContext<D>> {
        return this.builder.renderOnce(this, this.currentPage, menu, inventory)
    }

}

interface PageableDataProvider<D> {

    val size: Int

    operator fun get(index: Int): D

    fun refresh()

}