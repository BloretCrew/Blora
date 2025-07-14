package blora.menu

import blora.collection.Stack

class MenuStack(val menu: Menu, val lines: Int, var base: MenuPage) {

    private val pages = Stack<MenuPage>()

    init {
        require(lines == menu.lines)
    }

    fun current(): MenuPage {
        if (this.pages.isEmpty())
            return base
        return this.pages.get()
    }

    fun push(page: MenuPage) {
        this.pages.push(page)
        this.menu.update(page)
    }

    fun push(builder: MenuPageBuilder.() -> Unit) {
        this.push(MenuPageBuilder().apply(builder).build())
    }

    fun pop() {
        if (pages.isEmpty())
            return
        this.pages.pop()
        this.menu.update(
            if (this.pages.isEmpty())
                this.base
            else
                this.pages.get()
        )
    }

    fun replace(page: MenuPage) {
        if (pages.isEmpty()) {
            this.base = page
        } else {
            this.pages.replace(page)
        }
        this.menu.update(page)
    }

    fun replace(builder: MenuPageBuilder.() -> Unit) {
        this.replace(MenuPageBuilder().apply(builder).build())
    }

}