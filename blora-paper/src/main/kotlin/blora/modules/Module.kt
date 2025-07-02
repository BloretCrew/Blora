package blora.modules

import net.kyori.adventure.text.Component

interface Module {

    val id: String
    val name: String
    val displayName: Component
    var enabled: Boolean
    val dependencies: List<String>

    fun enable() {}
    fun disable() {}

}