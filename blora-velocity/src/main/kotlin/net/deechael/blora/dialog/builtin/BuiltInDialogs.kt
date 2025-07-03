package net.deechael.blora.dialog.builtin

import de.themoep.minedown.adventure.MineDown
import net.deechael.blora.dialog.ConfirmationDialog
import net.deechael.blora.dialog.Dialog
import net.deechael.blora.dialog.NoticeDialog
import net.deechael.blora.dialog.action.ClickAction
import net.deechael.blora.dialog.action.CustomClickType
import net.deechael.blora.dialog.action.DynamicCustomClickType
import net.deechael.blora.dialog.body.PlainMessageDialogBody
import net.deechael.blora.dialog.input.*
import net.kyori.adventure.text.Component
import plutoproject.adventurekt.component
import plutoproject.adventurekt.text.raw
import plutoproject.adventurekt.text.text

fun testDialog(): Dialog {
    return NoticeDialog(
        title = component {
            text { "测试" }
        },
        inputs = listOf(
            TextInputControl(
                key = "text_input",
                label = component {
                    text { "text" }
                }
            ),
            NumberRangeInputControl(
                key = "number_input",
                label = component {
                    text { "number range" }
                },
                start = 0.0f,
                end = 10.0f
            ),
            BooleanInputControl(
                key = "boolean_input",
                label = component {
                    text { "boolean" }
                }
            ),
            SingleOptionInputControl(
                key = "single_option_input",
                label = component {
                    text { "single option" }
                },
                options = listOf(
                    InputControlOption(
                        id = "option1",
                    ),
                    InputControlOption(
                        id = "option2",
                    ),
                    InputControlOption(
                        id = "option3",
                    )
                )
            )
        ),
        action = ClickAction(
            label = component {
                text { "do it" }
            },
            action = DynamicCustomClickType(
                id = "blora_test",
            )
        )
    )
}

fun loginDialog(warningMessages: Component? = null): Dialog {
    return NoticeDialog(
        title = component {
            text { "登录" }
        },
        body = if (warningMessages != null) {
            listOf(
                PlainMessageDialogBody(
                    contents = component {
                        raw { warningMessages }
                    }
                )
            )
        } else {
            null
        },
        canCloseWithEscape = false,
        pause = false,
        inputs = listOf(
            TextInputControl(
                key = "blora_password",
                label = component {
                    text { "密码" }
                },
                multiline = Multiline(
                    maxLines = 1
                )
            )
        ),
        action = ClickAction(
            label = component {
                text { "确认" }
            }
        )
    )
}

fun registerDialog(warningMessages: Component? = null): Dialog {
    return NoticeDialog(
        title = component {
            text { "注册" }
        },
        body = if (warningMessages != null) {
            listOf(
                PlainMessageDialogBody(
                    contents = component {
                        raw { warningMessages }
                    }
                )
            )
        } else {
            null
        },
        canCloseWithEscape = false,
        pause = false,
        inputs = listOf(
            TextInputControl(
                key = "blora_password",
                label = component {
                    text { "密码" }
                },
                multiline = Multiline(
                    maxLines = 1
                )
            ),
            TextInputControl(
                key = "blora_confirm_password",
                label = component {
                    text { "确认密码" }
                },
                multiline = Multiline(
                    maxLines = 1
                )
            )
        ),
        action = ClickAction(
            label = component {
                text { "确认" }
            }
        )
    )
}

fun eulaDialog(): Dialog {
    return ConfirmationDialog(
        title = component {
            text { "百络谷玩家守则" }
        },
        canCloseWithEscape = false,
        pause = false,
        body = listOf(
            PlainMessageDialogBody(
                contents = component {
                    raw {
                        MineDown.parse(
                            """
                            在此了解作为百络谷的玩家，您所需要遵守的规则
                            [在您使用百络谷服务器提供的任何游戏服务之前，请确保您已详细阅读并接受本协议。注册百络谷服务器账号表示您已详细阅读并接受本协议。若您不同意该协议中的任何内容，请勿游玩百络谷服务器或接受其提供的任何相关服务。](color=gold)

                            **Ⅰ. 发言与行为规范**
                            1. 注意游戏内用语，自觉不做出或发布涉及政治、反国家、反党、反社会、涉黄赌毒、政治敏感、违法犯罪等行为与言论，经查属实后处以 永久封禁 ，情节严重的 上报有关部门 。
                            2. 玩家见应友好和谐，不发布人身攻击言论，不恶意辱骂、故意挑起争端、挑动玩家群体对立、不正当竞争等。经查属实后处以 1小时以上禁言或24小时及以上封禁 。
                               - 如果您与其他玩家发生冲突，请在事端变大前向管理员寻求帮助。
                               - 任何情况下避免暴力纠纷。这可能会给双方带来不好的影响，您也可能会因过激行为受到处罚。
                            3. 不在游戏内刷屏、发布大量无意义、不健康或令人不适的内容，不散播服务器内容相关谣言、误导其他玩家等。经查属实后处以 24小时以上禁言或3天及以上封禁 。
                            4. 不发布与游戏内容无关的商业广告/宣传内容，不发布损害服务器利益的内容等内容。经查属实后依情况处以 7天或以上封禁 ，严重者或二次违反者处以 永久封禁。
                               - 包括但不限于：恶意通过服务器内聊天，告示牌，交流群，交流频道内宣传其他服务器、公然恶意抹黑服务器，冒充、辱骂服务器服主，管理员等维护、开发人员等。
                            5. 注重他人游戏体验，不破坏、偷盗、恶意占有其他玩家所有财产，不破坏玩家建筑，不过度干预新玩家正常游玩进程，不为利益而欺诈、误导其他玩家等。经查属实后处以 5天或以上封禁 。
                            6. 禁止尝试危害服务器系统安全、社区稳定、市场稳定等。经查属实后处以 14天或以上封禁 。
                               - 包括但不限于：恶意建造可能导致卡服的结构、使用任意方式创建Ban 人塔、Ban 人书、搜寻服务器机制、规则漏洞且蛮不上报。

                            **Ⅱ. 公平游戏规范**
                            1. 严禁使用第三方作弊客户端等恶性外挂、脚本及任何破坏游戏平衡的功能。首次经查属实后依情况处以 7天以上30天以下封禁 ，严重者或二次触犯者处以 永久封禁。
                               - 可以添加部分合理的游戏辅助性辅助模组，例如 Litematica 等，如果不知道某辅助模组是否可使用，建议询问服务器管理员。
                            2. 严禁利用服务器漏洞获利。发现服务器漏洞后应及时报告给服务器管理员。经查属实后处以 14天或以上封禁 ，严重者处以 永久封禁。
                               - 包括但不限于：利用漏洞刷取服务器金币、大厅积分、创悦谷金币、络琅和利用破坏游戏平衡方式获取的游戏内物品、财产。
                            3. 禁止通过任何形式向其他玩家传播服务器漏洞和作弊软件等。严禁在服务器交流群、游戏公平等位置讨论外挂、作弊和漏洞等。经查属实后酌情惩罚，严重者处以 1个月或以上封禁 。
                            4. 不在游戏内进行商业欺诈，游玩时应履行与其他玩家的交易合约等。经查属实后 10倍扣除所得财产 ，并处以 14天或以上封禁 。
                            5. 参与服务器内由管理层举办的活动、比赛时，违法公平竞争原则且未违反其他条例者，举办方有权取消选手比赛成绩、资格，并实施禁赛、限制游戏等处罚，严重者酌情处以 封禁或限制游戏 。
                            6. 严禁通过开小号等特殊手段多次领取服务器礼包、补偿、在竞争中取得优势等。一经发现处以 10倍扣除所得财产 ，并处以 7天或以上封禁 。

                            **Ⅲ. 个人账户与财产安全**
                            1. 玩家间互相交易时，请擦亮眼睛，提防诈骗。
                            2. 不与其他玩家私下使用现实货币、虚拟货币（如人民币、美元、比特币等）交易，一旦被骗后后果自行承担。一经发现此类行为处以 3个月或以上封禁 ，严重者处以 永久封禁。
                            3. 请使用安全性较强的密码，正版玩家可使用 /premium 指令开启正版验证。
                            4. 强烈建议您使用 /setemail 命令为您的账户绑定一个安全邮箱，方便您找回密码。
                            5. 严禁通过任何方式尝试非法占有、使用他人的百络谷服务器账户、骗取其他玩家的正版账户等。一经发现此类行为处以 6个月或以上封禁 。
                            6. 由于擅自通过转让、外借等方式将账户供他人使用导致的账户和财产丢失，后果自行承担。
                            7. 严禁通过特殊方式绕过服务器限制，创建大量账户进入服务器。
                            8. 由于存在加密措施，我们无法获取您注册时填写的密码等信息。我们会妥善保管您提供的安全邮箱等其他信息，除您的要求外，我们不会利用、转让、公开披露您输入的个人信息。如果您想要注销账户并删除个人信息，可以联系服务器管理员。

                            **Ⅳ. 违规处罚与举报奖励**
                            1. 对于违反服务器管理条例的玩家，管理员将根据违规情节轻重，同时参考本管理条例，实施相应处罚。
                            2. 若玩家对处罚结果有异议，可通过 QQ 等向管理员提交你的申诉。封禁代号（若处罚为封禁）、申诉理由、证据等需报告明晰。

                            **Ⅴ. 关于赞助**
                            1. 您赞助的资金我们会全部投入于服务器开发、运营，赞助者有权申请监督。
                            2. 赞助应当完全出于您的自愿。如需赞助，请联系服务器 QQ 群内的 Rhedar 或 Xupipi，获取赞助方式。除此之外暂无其他赞助途径，谨防上当受骗。
                            3. 严禁冒充服务器管理员等骗取其他玩家的赞助。一经发现此类行为处以 永久封禁，严重者上报有关部门。
                            4. 未成年人赞助请得到家长的许可。
                            5. 关于本周目赞助者等级的特殊权益请在游戏内查看。

                            **注释项**
                            文中所有词句，应按照此条目及此条目下辖条目理解。未有标明而存有争议之词句，依一般方向理解；若存在理解分歧，应公开投票解决歧义并修正条文。

                            1. 文中所提及之“处以”指对犯下该款项玩家的处罚；“......或以上封禁”指对该玩家所处封禁时间，无特殊情况下指封禁该账户和该账户所在IP。“或以上”代表所处条款所规定的处罚范围并非定量，具体处罚时间由管理层在依据条款规范下，根据用户行为进行适度量刑。若玩家同时违反多项条例，其处罚可按所有违反的条例叠加。
                            2. 文中所提及之“广告”、“商业广告”指带有利益性质的宣传行为或任何引流行为；需要特别指出的是：不单指代对其他服务器的宣传。
                            3. 文中所提及之“外挂、脚本”本文中指一切可以干涉玩家正常游戏行为并有可能对其他玩家产生负面影响/不当竞争的模组、软件、脚本等，包括但不限于 Killaura、Xray、Bot 等。
                            4. 文中所提及之“扣除......财产”所扣除的皆为游戏内虚拟财产，当财产余额不足以抵扣时，酌情加重原条款所提之封禁处罚，原条款无封禁者，额外处以封禁。
                            5. 文中所提及之“大量账户”，指2个及以上的游戏帐户。
                            6. 文中所提及之“相应处罚”，包括但不限于警告、禁言、临时封禁或永久封禁（IP）、限制创悦谷游戏、QQ 群禁言、踢出群聊、上报有关部门等。需要特别指出的是：处罚最终解释权归服务器管理团队所有。
                            7. 文中所提及之“后果自行承担”，指该条款所提及的行为均为“自甘风险”行为，做出该行为（条款所表述的行为）即代表认同该行为可能导致的危险后果。
                            8. 文中所提及之“服务器”“游戏内”“服务器qq群”等相关词汇除非另外说明，否则均指 bloret·百络谷 服务器。
                        """.trimIndent()
                        )
                    }
                }
            )
        ),
        yes = ClickAction(
            label = component {
                text { "同意" }
            },
            tooltip = component {
                text { "同意后可以正常进入百络谷游玩" }
            },
            action = CustomClickType(
                id = "blora_eula_accept"
            )
        ),
        no = ClickAction(
            label = component {
                text { "不同意" }
            },
            tooltip = component {
                text { "不同意将无法进入服务器，被服务器踢出" }
            },
            action = CustomClickType(
                id = "blora_eula_reject"
            )
        )
    )
}
