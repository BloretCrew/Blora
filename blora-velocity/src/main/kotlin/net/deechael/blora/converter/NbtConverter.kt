package net.deechael.blora.converter

import net.benwoodworth.knbt.NbtByte
import net.benwoodworth.knbt.NbtByteArray
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtDouble
import net.benwoodworth.knbt.NbtFloat
import net.benwoodworth.knbt.NbtInt
import net.benwoodworth.knbt.NbtIntArray
import net.benwoodworth.knbt.NbtList
import net.benwoodworth.knbt.NbtLong
import net.benwoodworth.knbt.NbtLongArray
import net.benwoodworth.knbt.NbtShort
import net.benwoodworth.knbt.NbtString
import net.benwoodworth.knbt.NbtTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.EntityNBTComponent
import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.text.NBTComponent
import net.kyori.adventure.text.ScoreComponent
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.StorageNBTComponent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
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
        map.put("score", NbtCompound(
            mapOf(
                "name" to NbtString(this.name()),
                "objective" to NbtString(this.objective())
            )
        ))
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

    val clickEvent = this.clickEvent()
    if (clickEvent != null) {
        map.put("click_event", clickEvent.toKnbt())
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

fun ClickEvent.toKnbt(): NbtCompound {
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
    }
}

/*

fun String.toNBT(): NbtString {
    return NbtString(this)
}

fun Boolean.toNBT(): NBTByte {
    return NBTByte(this)
}

fun Float.toNBT(): NBTFloat {
    return NBTFloat(this)
}

fun Double.toNBT(): NBTDouble {
    return NBTDouble(this)
}

fun Byte.toNBT(): NBTByte {
    return NBTByte(this)
}

fun Short.toNBT(): NBTShort {
    return NBTShort(this)
}

fun Int.toNBT(): NbtInt {
    return NbtInt(this)
}

fun Long.toNBT(): NBTLong {
    return NBTLong(this)
}

fun Key.toNBT(): NbtString {
    return NbtString(this.asString())
}

fun Component.toNBT(): NbtCompound {
    val compound = NbtCompound()
    if (this is TextComponent) {
        compound.setTag("type", NbtString("text"))
        compound.setTag("text", NbtString(this.content()))
    } else if (this is TranslatableComponent) {
        compound.setTag("type", NbtString("translatable"))
        compound.setTag("translate", NbtString(this.key()))
        val fallback = this.fallback()
        if (fallback != null) {
            compound.setTag("fallback", NbtString(fallback))
        }
        val arguments = this.arguments()
        if (arguments.isNotEmpty()) {
            val list = NBTList(NBTType.COMPOUND)
            for (argument in arguments) {
                list.addTag(argument.asComponent().toNBT())
            }
            compound.setTag("with", list)
        }
    } else if (this is ScoreComponent) {
        compound.setTag("type", NbtString("score"))
        val score = NbtCompound()
        score.setTag("name", NbtString(this.name()))
        score.setTag("objective", NbtString(this.objective()))
        compound.setTag("score", score)
    } else if (this is SelectorComponent) {
        compound.setTag("type", NbtString("selector"))
        compound.setTag("selector", NbtString(this.pattern()))
        val separator = this.separator()
        if (separator != null) {
            compound.setTag("separator", separator.toNBT())
        }
    } else if (this is KeybindComponent) {
        compound.setTag("type", NbtString("keybind"))
        compound.setTag("keybind", NbtString(this.keybind()))
    } else if (this is NBTComponent<*, *>) {
        compound.setTag("type", NbtString("nbt"))
        compound.setTag("nbt", NbtString(this.nbtPath()))
        compound.setTag("interpret", NBTByte(if (this.interpret()) 1 else 0))
        val separator = this.separator()
        if (separator != null) {
            compound.setTag("separator", separator.toNBT())
        }
        when (this) {
            is BlockNBTComponent -> {
                compound.setTag("source", NbtString("block"))
                compound.setTag("block", NbtString(this.pos().asString()))
            }

            is EntityNBTComponent -> {
                compound.setTag("source", NbtString("entity"))
                compound.setTag("entity", NbtString(this.selector()))
            }

            is StorageNBTComponent -> {
                compound.setTag("source", NbtString("storage"))
                compound.setTag("entity", NbtString(this.storage().asString()))
            }
        }
    }

    val children = this.children()
    if (children.isNotEmpty()) {
        val extra = NBTList(NBTType.COMPOUND)
        for (child in children) {
            extra.addTag(child.toNBT())
        }
        compound.setTag("extra", extra)
    }

    val color = this.color()
    if (color != null) {
        compound.setTag("color", NbtString(color.toString()))
    }

    val font = this.font()
    if (font != null) {
        compound.setTag("font", NbtString(font.asString()))
    }

    for ((deco, state) in this.decorations()) {
        if (state != TextDecoration.State.NOT_SET) {
            compound.setTag(deco.toString(), NBTByte(if (state == TextDecoration.State.TRUE) 1 else 0))
        }
    }

    val shadow = this.shadowColor()
    if (shadow != null) {
        compound.setTag("shadow_color", NbtInt(shadow.value()))
    }

    val insertion = this.insertion()
    if (insertion != null) {
        compound.setTag("insertion", NbtString(insertion))
    }

    val clickEvent = this.clickEvent()
    if (clickEvent != null) {
        compound.setTag("click_event", clickEvent.toNBT())
    }

    val hoverEvent = this.hoverEvent().toNBT()
    if (hoverEvent != null) {
        compound.setTag("hover_event", hoverEvent)
    }

    return compound
}

fun HoverEvent<*>?.toNBT(): NbtCompound? {
    if (this != null) {
        return if (this.action() == HoverEvent.Action.SHOW_ITEM) {
            NbtCompound().apply {
                this.setTag("action", NbtString("show_text"))
                this.setTag("value", (this@toNBT.value() as Component).toNBT())
            }
        } else if (this.action() == HoverEvent.Action.SHOW_ITEM) {
            NbtCompound().apply {
                this.setTag("action", NbtString("show_item"))
                val showItem = this@toNBT.value() as HoverEvent.ShowItem
                this.setTag("id", NbtString(showItem.item().asString()))
                if (showItem.count() > 0) {
                    this.setTag("count", NbtInt(showItem.count()))
                }
                val components = showItem.dataComponents()
                if (components.isNotEmpty()) {
                    // TODO
                }
            }
        } else if (this.action() == HoverEvent.Action.SHOW_ENTITY) {
            NbtCompound().apply {
                this.setTag("action", NbtString("show_entity"))
                val showEntity = this@toNBT.value() as HoverEvent.ShowEntity
                this.setTag("id", NbtString(showEntity.type().asString()))

                val name = showEntity.name()
                if (name != null) {
                    this.setTag("name", name.toNBT())
                }

                this.setTag("uuid", NbtString(showEntity.id().toString()))
            }
        } else {
            null
        }
    } else {
        return null
    }
}

fun ClickEvent.toNBT(): NbtCompound {
    return when (this.action()) {
        ClickEvent.Action.OPEN_URL -> NbtCompound().apply {
            this.setTag("action", NbtString("open_url"))
            this.setTag("url", NbtString(this@toNBT.value()))
        }
        ClickEvent.Action.OPEN_FILE -> NbtCompound().apply {
            this.setTag("action", NbtString("open_file"))
            this.setTag("path", NbtString(this@toNBT.value()))
        }
        ClickEvent.Action.RUN_COMMAND -> NbtCompound().apply {
            this.setTag("action", NbtString("run_command"))
            this.setTag("command", NbtString(this@toNBT.value()))
        }
        ClickEvent.Action.SUGGEST_COMMAND -> NbtCompound().apply {
            this.setTag("action", NbtString("suggest_command"))
            this.setTag("command", NbtString(this@toNBT.value()))
        }
        ClickEvent.Action.CHANGE_PAGE -> NbtCompound().apply {
            this.setTag("action", NbtString("change_page"))
            this.setTag("page", NbtInt(this@toNBT.value().toInt()))
        }
        ClickEvent.Action.COPY_TO_CLIPBOARD -> NbtCompound().apply {
            this.setTag("action", NbtString("copy_to_clipboard"))
            this.setTag("value", NbtString(this@toNBT.value()))
        }
    }
}

fun NbtTag.toPacketEvents(): NBT? {
    if (this is NbtCompound) {
        val compound = NbtCompound()
        for ((key, tag) in this.entries) {
            val converted = tag.toPacketEvents()
            if (converted == null) {
                continue
            }
            compound.setTag(key, converted)
        }
        return compound
    } else if (this is NbtList<*>) {
        if (!this.isEmpty()) { // if empty, there is no need to add an empty list
            return internalToPacketEventsList(this, this[0].toPacketEvents()!!.type)
        }
        return null
    } else if (this is NbtByte) {
        return NBTByte(this.value)
    } else if (this is NbtShort) {
        return NBTShort(this.value)
    } else if (this is NbtInt) {
        return NbtInt(this.value)
    } else if (this is NbtLong) {
        return NBTLong(this.value)
    } else if (this is NbtFloat) {
        return NBTFloat(this.value)
    } else if (this is NbtDouble) {
        return NBTDouble(this.value)
    } else if (this is NbtString) {
        return NbtString(this.value)
    } else if (this is NbtByteArray) {
        val bytes = mutableListOf<Byte>()
        for (byte in this) {
            bytes.add(byte.toByte())
        }
        return NBTByteArray(bytes.toByteArray())
    } else if (this is NbtIntArray) {
        val integers = mutableListOf<Int>()
        for (integer in this) {
            integers.add(integer)
        }
        return NbtIntArray(integers.toIntArray())
    } else if (this is NbtLongArray) {
        val longs = mutableListOf<Long>()
        for (long in this) {
            longs.add(long)
        }
        return NBTLongArray(longs.toLongArray())
    } else {
        return NBTEnd.INSTANCE
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <T: NBT> internalToPacketEventsList(nbtList: NbtList<*>, nbtType: NBTType<T>): NBTList<T> {
    val list = NBTList(nbtType)
    for (tag in nbtList) {
        val converted = tag.toPacketEvents()
        if (converted == null) {
            continue
        }
        list.addTag(tag.toPacketEvents() as T)
    }
    return list
}

*/
