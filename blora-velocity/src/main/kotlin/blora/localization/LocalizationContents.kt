package blora.localization

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

@Serializable
data class LocalizationContents(
    val commandErrorInvalid: String = "错误的命令用法",
    val commandErrorMust_be_player: String = "只有玩家可以运行这个命令",
    val commandErrorPlayer_not_exists: String = "玩家不存在",
    val commandErrorTarget_cannot_be_yourself: String = "你不能给你自己发私聊",
    val commandErrorNo_one_to_reply: String = "没有可以回复的私聊对象",
    val commandErrorReply_target_offline: String = "对方已离线，无法回复",
    val kickLoginUsername_too_short: String = "你的用户名长度低于 <length> 个字符，请更改为符合要求的名称",
    val kickLoginUsername_too_long: String = "你的用户名长度多于 <length> 个字符，请更改为符合要求的名称",
    val kickLoginUsername_contains_invalid_characters: String = "用户名必须符合“<regex>”的格式",
    val kickLoginSame_ip_login_overcount: String = "当前 IP 下登录的用户超出了限制的玩家数量",
    val kickLoginSame_ip_register_overcount: String = "当前 IP 下注册的用户超出了限制的玩家数量",
    val kickLoginSame_old_name_online_player: String = "您的用户名曾经被一个正版玩家使用过，但是这个正版玩家修改了名称后没有进入过服务器，在此玩家重新进入一次服务器之前您无法使用此用户名",
    val kickLoginSame_name_offline_player: String = "您的用户名被一名离线玩家占用了，请联系管理员处理",
    val kickLoginError_profiling: String = "验证您的正版时出现了错误，请尝试重新进入服务器<newline>如果多次出现此问题请联系管理员处理",
    val kickLoginSwap_premium_username: String = "太惊奇了！您和另一名正版用户交换了用户名，这导致服务器无法正常处理数据，请联系管理员处理！",
    val kickLoginOnline_profile_but_offline_join: String = "您的用户名为已经加入过服务器的正版玩家使用的用户名，但是您当前为离线登录，请使用正版账号进入服务器",
    val kickLoginToo_many_retries: String = "重试次数超出最大限制 <limit> 次",
    val kickLoginOvertime: String = "验证超时",
    val kickLoginExit: String = "玩家主动退出",
    val kickIngameNot_accept_eula: String = "由于您不同意我们的用户协议，您将无法游玩本服务器",
    val warningLoginPassword_incorrect: String = "密码错误",
    val warningRegisterConfirm_not_same: String = "两次密码输入不一致",
    val warningRegisterPassword_strategy_failure_no_username: String = "密码中不能存在您的用户名",
    val warningRegisterPassword_strategy_failure_no_duplicated: String = "密码中同一字符不能同时出现 <limit> 次",
    val warningRegisterPassword_strategy_failure_no_consecutive: String = "密码中类似 abc、123 的顺序字符不能排列超过 <limit> 个",
    val warningRegisterPassword_strategy_failure_uppercase_included: String = "密码中必须同时包含大小写字母",
    val warningRegisterPassword_strategy_failure_number_included: String = "密码中必须同时包含字母和数字",
    val warningRegisterPassword_strategy_failure_symbol_included: String = "密码中必须同时包含字母和符号",
    val warningRegisterPassword_strategy_failure_number_and_uppercase_included: String = "密码中必须同时包含大小写字母和数字",
    val warningRegisterPassword_strategy_failure_number_and_symbol_included: String = "密码中必须同时包含字母、数字和符号",
    val warningRegisterPassword_strategy_failure_uppercase_and_symbol_included: String = "密码中必须同时包含大小写字母和符号",
    val warningRegisterPassword_strategy_failure_uppercase_and_number_and_symbol_included: String = "密码中必须同时包含大小写字母、数字和符号",
    val warningRegisterPassword_strategy_failure_illegal_characters: String = "密码中仅允许大小写字母、数字和一般符号",
    val warningRegisterPassword_strategy_failure_length_not_secure: String = "密码长度必须大于等于 <min>，小于等于 <max>",
    val warningRegisterPassword_strategy_failure_in_weak_password_dict: String = "您当前的密码存在于弱密码字典中，请更换密码",
    val titleDialogEula: String = "百络谷 EULA",
    val titleDialogLogin: String = "登录",
    val titleDialogRegister: String = "注册",
    val titleDialogPlayer_options: String = "百络谷服务器玩家设置选项",
    val titleExternalDialogPlayer_options: String = "百络谷设置",
    val optionsAlways_lobby: String = "始终进入大厅",
    val optionDefault: String = "默认",
    val optionEnable: String = "开启",
    val optionDisable: String = "关闭",
    val buttonConfirm: String = "确认",
    val buttonSave: String = "保存",
    val buttonCancel: String = "取消",
    val buttonExit: String = "退出",
    val buttonDialogEulaAccept: String = "同意",
    val buttonDialogEulaReject: String = "拒绝",
    val inputDialogLoginPassword: String = "密码",
    val inputDialogRegisterPassword: String = "密码",
    val inputDialogRegisterConfirmPassword: String = "确认密码",
    val customPlaceholders: Map<String, String> = mapOf(
        "bloret" to "百络谷"
    ),
    val customColors: Map<String, String> = mapOf(),
) {

    fun resolver(): TagResolver {
        return LocalizationsTagResolver(
            this.customPlaceholders.toMap(),
            this.customColors.toMap()
        )
    }

}
