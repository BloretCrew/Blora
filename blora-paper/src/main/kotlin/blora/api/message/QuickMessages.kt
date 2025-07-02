package blora.api.message

import plutoproject.adventurekt.text.ComponentKt
import plutoproject.adventurekt.text.space
import plutoproject.adventurekt.text.style.ColorKt
import plutoproject.adventurekt.text.style.darkGray
import plutoproject.adventurekt.text.style.rgb
import plutoproject.adventurekt.text.style.text
import plutoproject.adventurekt.text.text
import plutoproject.adventurekt.text.with

object QuickColors {

    val success = rgb(211, 218, 196)
    val error = rgb(156, 88, 62)

}

fun ComponentKt.prefix(value: String, color: ColorKt) {
    text("[") with darkGray.text
    text(value) with color.text
    text("]") with darkGray.text
    space()
}

fun ComponentKt.commandPrefix() {
    prefix("命令", rgb(156, 169, 134))
}

fun ComponentKt.managementPrefix() {
    prefix("管理", rgb(171, 170, 120))
}

fun ComponentKt.primaryMessage(value: String) {
    text(value) with rgb(187, 184, 185).text
}

fun ComponentKt.primaryHighlightMessage(value: String) {
    text(value) with rgb(225, 218, 211).text
}

fun ComponentKt.successMessage(value: String) {
    text(value) with rgb(211, 218, 196).text
}

fun ComponentKt.errorMessage(value: String) {
    text(value) with rgb(156, 88, 62).text
}