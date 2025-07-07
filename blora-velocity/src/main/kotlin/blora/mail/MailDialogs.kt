package blora.mail

import blora.dialog.Dialog
import blora.dialog.MultiActionDialog
import blora.dialog.action.ClickAction
import blora.dialog.body.ItemDialogBody
import blora.dialog.body.PlainMessageDialogBody
import blora.item.ItemStack
import net.kyori.adventure.key.Key
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.mini
import plutoproject.adventurekt.text.newline
import plutoproject.adventurekt.text.text

fun mailDialog(): Dialog {
    return MultiActionDialog(
        title = component {
            text { "【奖励】兑换码兑换内容" }
        },
        body = listOf(
            PlainMessageDialogBody(
                contents = component {
                    mini("<bold>发件人：</bold>系统")
                    newline()
                }
            ),
            PlainMessageDialogBody(
                contents = component {
                    mini(
                        """
                        你好你好你好你好你好你好
                        你好你好你好你好你好你好你好你好你好你好
                        
                        你好你好你好你好你好你好你好你好你好你好
                        你好你好你好你好
                        
                        你好你好你好你好你好你好你好你好你好你好你好你好
                        
                        你好你好你好你好你好你好你好你好
                    """.trimIndent()
                    )
                }
            ),
            PlainMessageDialogBody(
                contents = component {
                    mini("<bold>附件：</bold>")
                    newline()
                    text { "络琅图标 x1200" }
                    newline()
                    text { "金币图标 x240000" }
                }
            ),
            ItemDialogBody(
                item = ItemStack(
                    id = Key.key("minecraft:diamond"),
                    count = 16
                )
            ),
            ItemDialogBody(
                item = ItemStack(
                    id = Key.key("minecraft:netherite_sword"),
                    count = 1
                )
            ),
            ItemDialogBody(
                item = ItemStack(
                    id = Key.key("minecraft:apple"),
                    count = 64
                )
            ),
        ),
        actions = listOf(
            ClickAction(
                label = component {
                    text { "领取附件" }
                }
            ),
            ClickAction(
                label = component {
                    text { "回复" }
                }
            ),
            ClickAction(
                label = component {
                    text { "删除" }
                }
            ),
            ClickAction(
                label = component {
                    text { "退出" }
                }
            )
        ),
    )
}