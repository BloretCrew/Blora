package blora.modules.mail

import net.kyori.adventure.text.Component

sealed class Sender {

    class Player(val uuid: String) : Sender() {
        override fun getDisplayName(): Component {
            return Component.empty()
        }
    }

    class Custom(val value: Component) : Sender() {
        override fun getDisplayName(): Component {
            return this.value
        }
    }

    object Server : Sender() {
        override fun getDisplayName(): Component {
            return Component.text("服务器")
        }
    }

    abstract fun getDisplayName(): Component

}