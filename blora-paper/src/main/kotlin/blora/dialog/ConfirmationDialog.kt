@file:OptIn(ExperimentalSerializationApi::class)

package blora.dialog

import blora.dialog.action.*
import blora.dialog.body.DialogBody
import blora.dialog.body.ItemDialogBody
import blora.dialog.body.PlainMessageDialogBody
import blora.dialog.input.*
import io.papermc.paper.adventure.PaperAdventure
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.minecraft.network.chat.ClickEvent
import net.minecraft.server.dialog.*
import net.minecraft.server.dialog.action.StaticAction
import net.minecraft.server.dialog.body.ItemBody
import net.minecraft.server.dialog.body.PlainMessage
import net.minecraft.server.dialog.input.BooleanInput
import net.minecraft.server.dialog.input.NumberRangeInput
import net.minecraft.server.dialog.input.SingleOptionInput
import net.minecraft.server.dialog.input.TextInput
import org.bukkit.Registry
import org.bukkit.craftbukkit.inventory.CraftItemStack
import java.net.URI
import java.util.*
import net.minecraft.server.dialog.ConfirmationDialog as NMSConfirmationDialog

internal val defaultYes = ClickAction(
    label = Component.translatable("gui.ok")
)

internal val defaultNo = ClickAction(
    label = Component.translatable("gui.no")
)

@Serializable
data class ConfirmationDialog(
    @Contextual
    val title: Component,
    @SerialName("external_title")
    @Contextual
    val externalTitle: Component? = null,
    val body: List<DialogBody>? = null,
    val inputs: List<InputControl>? = null,
    @SerialName("can_close_with_escape")
    val canCloseWithEscape: Boolean = true,
    val pause: Boolean = true,
    @SerialName("after_action")
    val afterAction: AfterAction = AfterAction.CLOSE,
    val yes: ClickAction = defaultYes,
    val no: ClickAction = defaultNo
) : Dialog() {

    override fun toNms(): NMSConfirmationDialog {
        return NMSConfirmationDialog(
            CommonDialogData(
                PaperAdventure.asVanilla(this.title),
                Optional.ofNullable(PaperAdventure.asVanilla(this.externalTitle)),
                this.canCloseWithEscape,
                this.pause,
                when (this.afterAction) {
                    AfterAction.CLOSE -> DialogAction.CLOSE
                    AfterAction.NONE -> DialogAction.NONE
                    AfterAction.WAIT_FOR_RESPONSE -> DialogAction.WAIT_FOR_RESPONSE
                },
                this.body?.map {
                    return@map when (it) {
                        is ItemDialogBody -> ItemBody(
                            CraftItemStack.asNMSCopy(Registry.ITEM.get(it.item.id)!!.createItemStack()),
                            if (it.description == null) Optional.empty() else Optional.of(
                                PlainMessage(
                                    PaperAdventure.asVanilla(it.description.contents),
                                    it.description.width
                                )
                            ),
                            it.showDecorations,
                            it.showTooltip,
                            it.width,
                            it.height
                        )

                        is PlainMessageDialogBody -> PlainMessage(
                            PaperAdventure.asVanilla(it.contents),
                            it.width
                        )
                    }
                }?.toList() ?: emptyList(),
                this.inputs?.map {
                    return@map when (it) {
                        is BooleanInputControl -> Input(
                            it.key,
                            BooleanInput(
                                PaperAdventure.asVanilla(it.label),
                                it.initial,
                                it.onTrue,
                                it.onFalse
                            )
                        )

                        is NumberRangeInputControl -> Input(
                            it.key,
                            NumberRangeInput(
                                it.width,
                                PaperAdventure.asVanilla(it.label),
                                it.labelFormat,
                                NumberRangeInput.RangeInfo(
                                    it.start,
                                    it.end,
                                    Optional.ofNullable(it.initial),
                                    Optional.ofNullable(it.step)
                                )
                            )
                        )

                        is SingleOptionInputControl -> Input(
                            it.key,
                            SingleOptionInput(
                                it.width,
                                it.options.map {
                                    return@map SingleOptionInput.Entry(
                                        it.id,
                                        Optional.ofNullable(PaperAdventure.asVanilla(it.display)),
                                        it.initial
                                    )
                                }.toList(),
                                PaperAdventure.asVanilla(it.label),
                                it.labelVisible
                            )
                        )

                        is TextInputControl -> Input(
                            it.key,
                            TextInput(
                                it.width,
                                PaperAdventure.asVanilla(it.label),
                                it.labelVisible,
                                it.initial,
                                it.maxLength,
                                if (it.multiline != null)
                                    Optional.of(
                                        TextInput.MultilineOptions(
                                            Optional.ofNullable(it.multiline.maxLines),
                                            Optional.ofNullable(it.multiline.height),
                                        )
                                    )
                                else
                                    Optional.empty()
                            )
                        )
                    }
                }?.toList() ?: emptyList(),
            ),
            ActionButton(
                CommonButtonData(
                    PaperAdventure.asVanilla(this.yes.label),
                    Optional.ofNullable(PaperAdventure.asVanilla(this.yes.tooltip)),
                    this.yes.width
                ),
                Optional.ofNullable(
                    when (this.yes.action) {
                        is ChangePageClickType -> StaticAction(ClickEvent.ChangePage(this.yes.action.page))
                        is CopyToClipboardClickType -> StaticAction(ClickEvent.CopyToClipboard(this.yes.action.value))
                        is CustomClickType -> StaticAction(
                            ClickEvent.Custom(
                                PaperAdventure.asVanilla(Key.key(this.yes.action.id)),
                                Optional.empty()
                            )
                        )

                        is OpenFileClickType -> StaticAction(ClickEvent.OpenFile(this.yes.action.path))
                        is OpenUrlClickType -> StaticAction(ClickEvent.OpenUrl(URI(this.yes.action.url)))
                        is RunCommandClickType -> StaticAction(ClickEvent.RunCommand(this.yes.action.command))
                        is SuggestCommandClickType -> StaticAction(ClickEvent.SuggestCommand(this.yes.action.command))
                        else -> null
                    }
                )
            ),
            ActionButton(
                CommonButtonData(
                    PaperAdventure.asVanilla(this.no.label),
                    Optional.ofNullable(PaperAdventure.asVanilla(this.yes.tooltip)),
                    this.yes.width
                ),
                Optional.ofNullable(
                    when (this.yes.action) {
                        is ChangePageClickType -> StaticAction(ClickEvent.ChangePage(this.yes.action.page))
                        is CopyToClipboardClickType -> StaticAction(ClickEvent.CopyToClipboard(this.yes.action.value))
                        is CustomClickType -> StaticAction(
                            ClickEvent.Custom(
                                PaperAdventure.asVanilla(Key.key(this.yes.action.id)),
                                Optional.empty()
                            )
                        )

                        is OpenFileClickType -> StaticAction(ClickEvent.OpenFile(this.yes.action.path))
                        is OpenUrlClickType -> StaticAction(ClickEvent.OpenUrl(URI(this.yes.action.url)))
                        is RunCommandClickType -> StaticAction(ClickEvent.RunCommand(this.yes.action.command))
                        is SuggestCommandClickType -> StaticAction(ClickEvent.SuggestCommand(this.yes.action.command))
                        else -> null
                    }
                )
            ),
        )
    }

}