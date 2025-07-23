package blora.menu.v2.context

import blora.menu.v2.Menu

abstract class AbstractMenuViewContext(
    override val menu: Menu,
) : MenuViewContext {
}