package blora.dialog.action

import kotlinx.serialization.Serializable

@Serializable
data class ChangePageClickType(
    val page: Int,
    val type: String = "change_page"
) : ClickType()