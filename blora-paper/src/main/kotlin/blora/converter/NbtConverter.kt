package blora.converter

import net.benwoodworth.knbt.*
import net.kyori.adventure.text.*
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration

fun Component.toKnbt(): NbtCompound {
    val map = mutableMapOf<String, NbtTag>()
    if (this is TextComponent) {
        map.put("type", NbtString("text"))
        map.put("text", NbtString(this.content()))
    } else if (this is TranslatableComponent) {
        map.put("type", NbtString("translatable"))
        map.put("translate", NbtString(this.key()))
        val fallback = this.fallback()
        if (fallback != null) {
            map.put("fallback", NbtString(fallback))
        }
        val arguments = this.arguments()
        if (arguments.isNotEmpty()) {
            val list = mutableListOf<NbtCompound>()
            for (argument in arguments) {
                list.add(argument.asComponent().toKnbt())
            }
            map.put("with", NbtList(list))
        }
    } else if (this is ScoreComponent) {
        map.put("type", NbtString("score"))
        map.put(
            "score", NbtCompound(
                mapOf(
                    "name" to NbtString(this.name()),
                    "objective" to NbtString(this.objective())
                )
            )
        )
    } else if (this is SelectorComponent) {
        map.put("type", NbtString("selector"))
        map.put("selector", NbtString(this.pattern()))
        val separator = this.separator()
        if (separator != null) {
            map.put("separator", separator.toKnbt())
        }
    } else if (this is KeybindComponent) {
        map.put("type", NbtString("keybind"))
        map.put("keybind", NbtString(this.keybind()))
    } else if (this is NBTComponent<*, *>) {
        map.put("type", NbtString("nbt"))
        map.put("nbt", NbtString(this.nbtPath()))
        map.put("interpret", NbtByte(if (this.interpret()) 1 else 0))
        val separator = this.separator()
        if (separator != null) {
            map.put("separator", separator.toKnbt())
        }
        when (this) {
            is BlockNBTComponent -> {
                map.put("source", NbtString("block"))
                map.put("block", NbtString(this.pos().asString()))
            }

            is EntityNBTComponent -> {
                map.put("source", NbtString("entity"))
                map.put("entity", NbtString(this.selector()))
            }

            is StorageNBTComponent -> {
                map.put("source", NbtString("storage"))
                map.put("entity", NbtString(this.storage().asString()))
            }
        }
    }

    val children = this.children()
    if (children.isNotEmpty()) {
        val extra = mutableListOf<NbtCompound>()
        for (child in children) {
            extra.add(child.toKnbt())
        }
        map.put("extra", NbtList(extra))
    }

    val color = this.color()
    if (color != null) {
        map.put("color", NbtString(color.toString()))
    }

    val font = this.font()
    if (font != null) {
        map.put("font", NbtString(font.asString()))
    }

    for ((deco, state) in this.decorations()) {
        if (state != TextDecoration.State.NOT_SET) {
            map.put(deco.toString(), NbtByte(if (state == TextDecoration.State.TRUE) 1 else 0))
        }
    }

    val shadow = this.shadowColor()
    if (shadow != null) {
        map.put("shadow_color", NbtInt(shadow.value()))
    }

    val insertion = this.insertion()
    if (insertion != null) {
        map.put("insertion", NbtString(insertion))
    }

    val clickEvent = this.clickEvent().toKnbt()
    if (clickEvent != null) {
        map.put("click_event", clickEvent)
    }

    val hoverEvent = this.hoverEvent().toKnbt()
    if (hoverEvent != null) {
        map.put("hover_event", hoverEvent)
    }

    return NbtCompound(
        map
    )
}

fun HoverEvent<*>?.toKnbt(): NbtCompound? {
    if (this != null) {
        return if (this.action() == HoverEvent.Action.SHOW_ITEM) {
            NbtCompound(
                mapOf(
                    "action" to NbtString("show_text"),
                    "value" to (this@toKnbt.value() as Component).toKnbt()
                )
            )
        } else if (this.action() == HoverEvent.Action.SHOW_ITEM) {
            val showItem = this@toKnbt.value() as HoverEvent.ShowItem
            val map: MutableMap<String, NbtTag> = mutableMapOf(
                "action" to NbtString("show_item")
            )
            if (showItem.count() > 0) {
                map.put("count", NbtInt(showItem.count()))
            }
            val components = showItem.dataComponents()
            if (components.isNotEmpty()) {
                // TODO
            }
            NbtCompound(map)
        } else if (this.action() == HoverEvent.Action.SHOW_ENTITY) {
            val showEntity = this@toKnbt.value() as HoverEvent.ShowEntity
            val map: MutableMap<String, NbtTag> = mutableMapOf(
                "action" to NbtString("show_entity"),
                "id" to NbtString(showEntity.type().asString()),
                "uuid" to NbtString(showEntity.id().toString())
            )
            val name = showEntity.name()
            if (name != null) {
                map.put("name", name.toKnbt())
            }
            NbtCompound(map)
        } else {
            null
        }
    } else {
        return null
    }
}


fun ClickEvent?.toKnbt(): NbtCompound? {
    if (this == null) {
        return null
    }
    return when (this.action()) {
        ClickEvent.Action.OPEN_URL -> NbtCompound(
            mapOf(
                "action" to NbtString("open_url"),
                "url" to NbtString(this@toKnbt.value())
            )
        )

        ClickEvent.Action.OPEN_FILE -> NbtCompound(
            mapOf(
                "action" to NbtString("open_file"),
                "path" to NbtString(this@toKnbt.value())
            )
        )

        ClickEvent.Action.RUN_COMMAND -> NbtCompound(
            mapOf(
                "action" to NbtString("run_command"),
                "command" to NbtString(this@toKnbt.value())
            )
        )

        ClickEvent.Action.SUGGEST_COMMAND -> NbtCompound(
            mapOf(
                "action" to NbtString("suggest_command"),
                "command" to NbtString(this@toKnbt.value())
            )
        )

        ClickEvent.Action.CHANGE_PAGE -> NbtCompound(
            mapOf(
                "action" to NbtString("change_page"),
                "page" to NbtInt(this@toKnbt.value().toInt())
            )
        )

        ClickEvent.Action.COPY_TO_CLIPBOARD -> NbtCompound(
            mapOf(
                "action" to NbtString("copy_to_clipboard"),
                "value" to NbtString(this@toKnbt.value())
            )
        )

        else -> null
    }
}

/*

fun ClickEvent?.toKnbt(): NbtCompound? {
    if (this == null) {
        return null
    }
    val payload = this.payload()
    return when (this.action()) {
        ClickEvent.Action.OPEN_URL -> NbtCompound(
            mapOf(
                "action" to NbtString("open_url"),
                "url" to NbtString((payload as ClickEvent.Payload.Text).value())
            )
        )

        ClickEvent.Action.OPEN_FILE -> NbtCompound(
            mapOf(
                "action" to NbtString("open_file"),
                "path" to NbtString((payload as ClickEvent.Payload.Text).value())
            )
        )

        ClickEvent.Action.RUN_COMMAND -> NbtCompound(
            mapOf(
                "action" to NbtString("run_command"),
                "command" to NbtString((payload as ClickEvent.Payload.Text).value())
            )
        )

        ClickEvent.Action.SUGGEST_COMMAND -> NbtCompound(
            mapOf(
                "action" to NbtString("suggest_command"),
                "command" to NbtString((payload as ClickEvent.Payload.Text).value())
            )
        )

        ClickEvent.Action.CHANGE_PAGE -> NbtCompound(
            mapOf(
                "action" to NbtString("change_page"),
                "page" to NbtInt((payload as ClickEvent.Payload.Int).integer())
            )
        )

        ClickEvent.Action.COPY_TO_CLIPBOARD -> NbtCompound(
            mapOf(
                "action" to NbtString("copy_to_clipboard"),
                "value" to NbtString((payload as ClickEvent.Payload.Text).value())
            )
        )

        ClickEvent.Action.SHOW_DIALOG ->
            if ((payload as ClickEvent.Payload.Dialog).dialog() is Dialog)
                NbtCompound(
                    mapOf(
                        "action" to NbtString("show_dialog"),
                        "dialog" to NbtString((payload.dialog() as Dialog).toJson())
                    )
                )
            else
                null
        ClickEvent.Action.CUSTOM -> compound {
            "action" to "custom"
            "id" to (payload as ClickEvent.Payload.Custom).key().asString()
            "payload" to payload.nbt().string()
        }
    }
}*/
