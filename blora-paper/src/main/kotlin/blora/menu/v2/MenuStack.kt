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

    fun replace(page: MenuPage<*, *>) {
        this.pages.replace(page.apply { this.fireLaunchEffect(menu) })?.fireDisposeEffect(menu)
        this.menu.rerender()
    }

    fun replace(page: () -> MenuPage<*, *>) {
        this.replace(page())
    }

}