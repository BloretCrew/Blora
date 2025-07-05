package blora.extension

import blora.dialog.action.*
import net.kyori.adventure.text.event.ClickEvent

/*

fun ClickEvent.toDialogClickType(): ClickType? {
    val payload = this.payload()
    return when (this.action()) {
        ClickEvent.Action.OPEN_URL -> OpenUrlClickType(
            (payload as ClickEvent.Payload.Text).value()
        )

        ClickEvent.Action.OPEN_FILE -> OpenUrlClickType(
            (payload as ClickEvent.Payload.Text).value()
        )

        ClickEvent.Action.RUN_COMMAND -> OpenUrlClickType(
            (payload as ClickEvent.Payload.Text).value()
        )

        ClickEvent.Action.SUGGEST_COMMAND -> OpenUrlClickType(
            (payload as ClickEvent.Payload.Text).value()
        )

        ClickEvent.Action.CHANGE_PAGE -> ChangePageClickType(
            (payload as ClickEvent.Payload.Int).integer()
        )

        ClickEvent.Action.COPY_TO_CLIPBOARD -> OpenUrlClickType(
            (payload as ClickEvent.Payload.Text).value()
        )

        ClickEvent.Action.SHOW_DIALOG ->
            if ((payload as ClickEvent.Payload.Dialog).dialog() is Dialog)
                ShowDialogClickType(
                    (payload.dialog() as Dialog).toJson()
                )
            else
                null
        ClickEvent.Action.CUSTOM ->
            CustomClickType(
                (payload as ClickEvent.Payload.Custom).key().asString(),
                payload.nbt().string()
            )
    }
}
*/

fun ClickEvent.toDialogClickType(): ClickType? {
    return when (this.action()) {
        ClickEvent.Action.OPEN_URL -> OpenUrlClickType(
            this.value()
        )

        ClickEvent.Action.OPEN_FILE -> OpenFileClickType(
            this.value()
        )

        ClickEvent.Action.RUN_COMMAND -> RunCommandClickType(
            this.value()
        )

        ClickEvent.Action.SUGGEST_COMMAND -> SuggestCommandClickType(
            this.value()
        )

        ClickEvent.Action.CHANGE_PAGE -> ChangePageClickType(
            this.value().toInt()
        )

        ClickEvent.Action.COPY_TO_CLIPBOARD -> CopyToClipboardClickType(
            this.value()
        )

        else -> null
    }
}