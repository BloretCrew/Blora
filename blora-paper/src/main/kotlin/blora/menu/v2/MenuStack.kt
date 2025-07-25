package blora.menu.v2

import blora.collection.Stack
import blora.menu.v2.page.MenuPage

class MenuStack(val menu: Menu) {

    private val pages = Stack<MenuPage<*, *>>()

    fun current(): MenuPage<*, *> {
        return this.pages.get()
    }

    fun currentOrNull(): MenuPage<*, *>? {
        return this.pages.getOrNull()
    }

    fun push(page: MenuPage<*, *>) {
        if (this.pages.any { it.pageId == page.pageId })
            throw IllegalArgumentException("Page ID ${page.pageId} already exists.")
        this.pages.push(page)
        page.fireLaunchEffect(menu)
        this.menu.rerender()
    }

    fun push(page: () -> MenuPage<*, *>) {
        this.push(page())
    }

    fun pop() {
        this.pages.popSafely()?.fireDisposeEffect(menu)
        this.menu.rerender()
    }

    fun popUntil(pageId: String, include: Boolean = false) {
        if (!this.pages.any { it.pageId == pageId })
            return
        this.pages.popUntil(this.pages.first { it.pageId == pageId }, include)
        this.menu.rerender()
    }

    fun replace(page: MenuPage<*, *>) {
        // when replace current page will be pop, so don't consider it when checking page id
        if (this.pages.toList().subList(0, this.pages.toList().size - 2).any { it.pageId == page.pageId })
            throw IllegalArgumentException("Page ID ${page.pageId} already exists.")
        this.pages.replace(page.apply { this.fireLaunchEffect(menu) })?.fireDisposeEffect(menu)
        this.menu.rerender()
    }

    fun replace(page: () -> MenuPage<*, *>) {
        this.replace(page())
    }

    fun clear() {
        this.pages.clear()
    }

}