@file:Suppress("UnstableApiUsage")

package blora.adventure

import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.ComponentKt
import plutoproject.adventurekt.text.actions
import plutoproject.adventurekt.text.newlineAction
import plutoproject.adventurekt.util.cleanBuild

fun itemLore(builder: ComponentKt.() -> Unit): ItemLore {
    val lines = mutableListOf<Component>()
    component {
        actions {
            newlineAction {
                lines.add(it.cleanBuild())
            }
        }

        builder()
    }.apply { lines.add(this) }

    return ItemLore.lore(lines)
}