package blora.menu.v2.context

import blora.menu.v2.Menu
import blora.menu.v2.page.PageableDataProvider

class PageableMenuViewContext<D>(menu: Menu, val currentPage: Int, val maxPage: Int, val dataProvider: PageableDataProvider<D>) : AbstractMenuViewContext(menu) {
}