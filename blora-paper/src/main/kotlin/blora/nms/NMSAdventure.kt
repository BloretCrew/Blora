package blora.nms

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextFormat
import net.kyori.adventure.text.serializer.legacy.Reset
import net.minecraft.ChatFormatting

fun ChatFormatting.toAdventure(): TextFormat {
    return when (this) {
        ChatFormatting.BLACK -> NamedTextColor.BLACK
        ChatFormatting.DARK_BLUE -> NamedTextColor.DARK_BLUE
        ChatFormatting.DARK_GREEN -> NamedTextColor.DARK_GREEN
        ChatFormatting.DARK_AQUA -> NamedTextColor.DARK_AQUA
        ChatFormatting.DARK_RED -> NamedTextColor.DARK_RED
        ChatFormatting.DARK_PURPLE -> NamedTextColor.DARK_PURPLE
        ChatFormatting.GOLD -> NamedTextColor.GOLD
        ChatFormatting.GRAY -> NamedTextColor.GRAY
        ChatFormatting.DARK_GRAY -> NamedTextColor.DARK_GRAY
        ChatFormatting.BLUE -> NamedTextColor.BLUE
        ChatFormatting.GREEN -> NamedTextColor.GREEN
        ChatFormatting.AQUA -> NamedTextColor.AQUA
        ChatFormatting.RED -> NamedTextColor.RED
        ChatFormatting.LIGHT_PURPLE -> NamedTextColor.LIGHT_PURPLE
        ChatFormatting.YELLOW -> NamedTextColor.YELLOW
        ChatFormatting.WHITE -> NamedTextColor.WHITE
        ChatFormatting.OBFUSCATED -> TextDecoration.OBFUSCATED
        ChatFormatting.BOLD -> TextDecoration.BOLD
        ChatFormatting.STRIKETHROUGH -> TextDecoration.STRIKETHROUGH
        ChatFormatting.UNDERLINE -> TextDecoration.UNDERLINED
        ChatFormatting.ITALIC -> TextDecoration.ITALIC
        ChatFormatting.RESET -> Reset.INSTANCE
    }
}