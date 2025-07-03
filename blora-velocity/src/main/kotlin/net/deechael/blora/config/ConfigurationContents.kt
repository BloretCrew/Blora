package net.deechael.blora.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

@Serializable
data class ConfigurationContents(
    val server: Server = Server(),
    val authorization: Authorization = Authorization(),
    val security: Security = Security(),
    val database: Database = Database(),
    @TomlComment("消息文本列表，支持 MiniMessage 格式")
    val messages: Messages = Messages(),
)

@Serializable
data class Server(
    val limbo: String = "limbo",
    val lobby: String = "lobby",
)

@Serializable
data class Authorization(
    @TomlComment("是否开启正版验证相关功能，只有这个选项开启其他正版选项才会有效")
    val onlineFeatures: Boolean = false,
    @TomlComment("玩家登录后是否强制传送至大厅服务器，设置为 false 则会将玩家传送回之前所在的服务器")
    val alwaysLobby: Boolean = false,
    @TomlComment("启用用户名安全检查")
    val checkUsername: Boolean = true,
    @TomlComment("用户名字符正则检测，请无修改长度检测，保持为 *（无限长度）")
    val usernameRegex: String = "[0-9a-zA-Z_]*",
    @TomlComment("用户名的最小长度")
    val minUsernameLength: Int = 3,
    @TomlComment("用户名的最大长度")
    val maxUsernameLength: Int = 16,
    @TomlComment(
        """
        玩家 UUID 生成器
        支持以下两个选项：
        MOJANG - 如果这个玩家名存在正版用户，则优先使用正版 UUID，如果原本是离线玩家但后期购买了正版，UUID 会保持为离线的 UUID（推荐使用）
        CRACKED - 所有玩家都按离线玩家计算，缺点是正版用户改名后会因为 UUID 的改变而导致数据丢失
    """
    )
    val uuidGenerator: UUIDGenerator = UUIDGenerator.MOJANG
)

@Serializable
data class Security(
    @TomlComment("允许正版玩家自动登录")
    val allowOnlinePlayerAutoLogin: Boolean = true,
    @TomlComment("对于离线玩家，允许同一用户名登录后退出游戏，在同一 IP 下再次登录时允许自动登录")
    val sameIpAutoLogin: Boolean = false,
    @TomlComment("自动登录的过期时间，单位为秒")
    val autoLoginExpireTime: Long = 600L,
    @TomlComment("密码的最小长度")
    val minPasswordLength: Int = 8,
    @TomlComment("密码的最大长度")
    val maxPasswordLength: Int = 32,
    @TomlComment("定义一系列弱密码，用户无法使用这些密码，不区分大小写")
    val weakPasswords: List<String> = listOf(
        "12345678",
        "abcdefgh",
        "password"
    ),
    @TomlComment(
        """
        密码策略，当前支持的策略如下：
        noUsername - 禁止密码中出现玩家的名称，无论如何大小写改变
        noDuplicated:{数字} - 禁止同一个字符连续 {数字} 次及以上次数出现，无论大小写
        noConsecutive:{数字} - 禁止出现类似 abc, zyx, hij, 123, 567 这样无论正序、倒序连续的字母超过 {数字} 个排列
        uppercaseIncluded - 必须出现大写字母
        numberIncluded - 必须出现数字
        symbolIncluded - 必须出现符号
    """
    )
    val passwordStrategy: List<String> = listOf(
        "noUsername"
    ),
    @TomlComment("IP 限制，小于等于 0 不生效")
    val ipLimit: Int = -1,
    @TomlComment("关闭同一 IP 注册的限制")
    val ipLimitDisableRegister: Boolean = false,
    @TomlComment("关闭同一 IP 登录的限制")
    val ipLimitDisableLogin: Boolean = false,
    @TomlComment(
        """
        对于注册的 IP 限制策略：
        FIRST - 保存所有玩家首次进入服务器的 IP，然后进行注册限制
        LAST - 保存所有玩家最后一次进入服务器的 IP，然后进行注册限制
    """
    )
    val ipLimitStrategyForRegister: Order = Order.FIRST,
    @TomlComment("最大的重试次数，超出将踢出，小于等于 0 的数字则为不限制")
    val maxRetries: Int = 3,
    @TomlComment("未登录时最大在线时间（秒），超时将自动踢出，小于等于 0 的数字则为不限制")
    val maxNotLogin: Int = 300,
)

@Serializable
data class Database(
    val host: String = "",
    val port: Int = 3306,
    val username: String = "",
    val password: String = "",
    val database: String = "",
    @TomlComment("数据库连接保活时间（毫秒），超过时间后使用数据库需重连")
    val maxLifeTime: Long = 600000,
    @TomlComment("连接数据库时使用的 jdbc url，不了解的情况下不建议修改")
    val jdbcUrl: String = "jdbc:mariadb://%host%:%port%/%database%?autoReconnect=true&zeroDateTimeBehavior=convertToNull",
) {

    fun buildDataSource(): HikariDataSource {
        return HikariDataSource(
            HikariConfig().apply {
                this@apply.username = this@Database.username
                this@apply.password = this@Database.password

                this@apply.poolName = "Blora MariaDB Connection Pool"

                this@apply.driverClassName = "org.mariadb.jdbc.Driver"
                this@apply.jdbcUrl = this@Database.jdbcUrl.replace("%host%", host)
                    .replace("%port%", "${this@Database.port}")
                    .replace("%database%", this@Database.database)

                this@apply.addDataSourceProperty("cachePrepStmts", "true")
                this@apply.addDataSourceProperty("prepStmtCacheSize", "250")
                this@apply.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

                this@apply.maxLifetime = this@Database.maxLifeTime
            }
        )
    }

    fun verify(): Boolean {
        try {
            val dataSource = this.buildDataSource()
            dataSource.connection.close()
            dataSource.close()
            return true
        } catch (e: Exception) {
            return false
        }
    }

}

@Serializable
data class Messages(
    @TomlComment(
        """
        当用户名不符合要求时的踢出信息
        支持的替换符：
        <length> - 将会被替换为配置文件中 minUsernameLength 的值
    """
    )
    @SerialName("login_kick_username_too_short")
    val loginKickUsernameTooShort: String = "你的用户名长度低于 <length> 个字符，请更改为符合要求的名称",
    @TomlComment(
        """
        当用户名不符合要求时的踢出信息
        支持的替换符：
        <length> - 将会被替换为配置文件中 minUsernameLength 的值
    """
    )
    @SerialName("login_kick_username_too_long")
    val loginKickUsernameTooLong: String = "你的用户名长度多于 <length> 个字符，请更改为符合要求的名称",
    @TomlComment(
        """
        当用户名不符合要求时的踢出信息
    """
    )
    @SerialName("login_kick_username_contains_invalid_characters")
    val loginKickUsernameContainsInvalidCharacters: String = "用户名必须符合“<regex>”的格式",
    @TomlComment(
        """
        相同 IP 登录用户超出时的踢出提示
    """
    )
    @SerialName("login_kick_same_ip_login_overcount")
    val loginKickSameIpLoginOvercount: String = "当前 IP 下登录的用户超出了限制的玩家数量",
    @TomlComment(
        """
        相同 IP 注册的用户超出时的踢出提示
    """
    )
    @SerialName("login_kick_same_ip_register_overcount")
    val loginKickSameIpRegisterOvercount: String = "当前 IP 下注册的用户超出了限制的玩家数量",
    @TomlComment(
        """
        一种罕见的情况：如果一个正版玩家改名后还未进入服务器，本插件还未处理改名的情况
        会导致无法在 mojang 服务器上找到正版玩家，但是本插件的数据库还把同名的玩家被认为是正版用户
        这种情况只有等改名后的正版玩家重新进入一次服务器后本插件调整完数据才能使用这个名字，先踢出
    """
    )
    @SerialName("login_kick_same_old_name_online_player")
    val loginKickSameOldNameOnlinePlayer: String = "您的用户名曾经被一个正版玩家使用过，但是这个正版玩家修改了名称后没有进入过服务器，在此玩家重新进入一次服务器之前您无法使用此用户名",
    @TomlComment(
        """
        正版用户进入服务器后检测有离线玩家用此用户名登录后踢出的提示
        两种情况：
        1. 一名离线用户先加入了服务器，然后另一名使用相同用户名的玩家在这名玩家加入后才买了正版并进入服务器
        2. 正版用户名改名改成了相同玩家的用户名，这种情况更复杂
    """
    )
    @SerialName("login_kick_same_name_offline_player")
    val loginKickSameNameOfflinePlayer: String = "您的用户名被一名离线玩家占用了，请联系管理员处理",
    @TomlComment(
        """
        一般不会出现的情况：服务器检测到玩家是正版登录，但是却无法从 mojang 的服务器上获取到正版信息
        这个情况我觉得不会出现，是先从 mojang 获取到正版信息才尝试让玩家进行正版登录的，但保险起见添加了此情况的踢出
    """
    )
    @SerialName("login_kick_error_profiling")
    val loginKickErrorProfiling: String = "验证您的正版时出现了错误，请尝试重新进入服务器<newline>如果多次出现此问题请联系管理员处理",
    @TomlComment(
        """
        一般不会出现的情况：两个正版玩家交换了用户名
    """
    )
    @SerialName("login_kick_swap_premium_username")
    val loginKickSwapPremiumUsername: String = "太惊奇了！您和另一名正版用户交换了用户名，这导致服务器无法正常处理数据，请联系管理员处理！",
    @TomlComment(
        """
        当离线用户使用正版用户的用户名进行登录时触发
    """
    )
    @SerialName("login_kick_online_profile_but_offline_join")
    val loginKickOnlineProfileButOfflineJoin: String = "您的用户名为已经加入过服务器的正版玩家使用的用户名，但是您当前为离线登录，请使用正版账号进入服务器",
    @TomlComment(
        """
        当玩家没有同意 EULA 被踢出时显示的信息
    """
    )
    @SerialName("ingame_kick_not_accept_eula")
    val ingameKickNotAcceptEULA: String = "由于您不同意我们的用户协议，您将无法游玩本服务器",
    @TomlComment(
        """
        玩家登录是密码输入次数超出重复次数限制被提出的信息
        支持的替换符：
        <limit> - 最高可重试的次数
    """
    )
    @SerialName("ingame_kick_too_many_retries")
    val ingameKickTooManyRetries: String = "重试次数超出最大限制 <limit> 次",
    @TomlComment(
        """
        当玩家登录时密码错误弹出的警告
    """
    )
    @SerialName("login_warning_login_password_incorrect")
    val loginWarningLoginPasswordIncorrect: String = "密码错误",
    @TomlComment(
        """
        玩家注册时两次密码输入不一致弹出的警告
    """
    )
    @SerialName("login_warning_register_confirm_not_same")
    val loginWarningRegisterConfirmNotSame: String = "两次密码输入不一致",
    @TomlComment(
        """
        玩家注册时密码中出现自己的用户名的时候出现的警告
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_no_username")
    val loginWarningRegisterPasswordStrategyFailureNoUsername: String = "密码中不能存在您的用户名",
    @TomlComment(
        """
        玩家注册时密码中出现重复出现同一字符弹出的警告
        支持的替换符：
        <limit> - 最高可重复出现的次数
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_no_duplicated")
    val loginWarningRegisterPasswordStrategyFailureNoDuplicated: String = "密码中同一字符不能同时出现 <limit> 次",
    @TomlComment(
        """
        玩家注册时密码中出现连续出现顺序字符的情况
        支持的替换符：
        <limit> - 最高可排列的字符数
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_no_consecutive")
    val loginWarningRegisterPasswordStrategyFailureNoConsecutive: String = "密码中类似 abc、123 的顺序字符不能排列超过 <limit> 个",
    @TomlComment(
        """
        玩家注册时密码中未同时出现大小写字母出现的警告
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_uppercase_included")
    val loginWarningRegisterPasswordStrategyFailureUppercaseIncluded: String = "密码中必须同时包含大小写字母",
    @TomlComment(
        """
        玩家注册时密码中未同时出现字母和数字出现的警告
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_number_included")
    val loginWarningRegisterPasswordStrategyFailureNumberIncluded: String = "密码中必须同时包含字母和数字",
    @TomlComment(
        """
        玩家注册时密码中未同时出现数字出现的警告
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_symbol_included")
    val loginWarningRegisterPasswordStrategyFailureSymbolIncluded: String = "密码中必须同时包含字母和符号",
    @TomlComment(
        """
        玩家注册时密码中未同时出现大小写字母和数字出现的警告
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_number_and_uppercase_included")
    val loginWarningRegisterPasswordStrategyFailureNumberAndUppercaseIncluded: String = "密码中必须同时包含大小写字母和数字",
    @TomlComment(
        """
        玩家注册时密码中未同时出现大小写字母和数字出现的警告
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_number_and_symbol_included")
    val loginWarningRegisterPasswordStrategyFailureNumberAndSymbolIncluded: String = "密码中必须同时包含字母、数字和符号",
    @TomlComment(
        """
        玩家注册时密码中未同时出现大小写字母和数字出现的警告
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_uppercase_and_symbol_included")
    val loginWarningRegisterPasswordStrategyFailureUppercaseAndSymbolIncluded: String = "密码中必须同时包含大小写字母和符号",
    @TomlComment(
        """
        玩家注册时密码中未同时出现大小写字母和数字出现的警告
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_uppercase_and_number_and_symbol_included")
    val loginWarningRegisterPasswordStrategyFailureUppercaseAndNumberAndSymbolIncluded: String = "密码中必须同时包含大小写字母、数字和符号",
    @TomlComment(
        """
        玩家注册时密码中出现非法字符出现的警告
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_illegal_characters")
    val loginWarningRegisterPasswordStrategyFailureIllegalCharacters: String = "密码中仅允许大小写字母、数字和一般符号",
    @TomlComment(
        """
        玩家注册时密码长度不符合要求出现的警告
        支持的替换符：
        <max> - 最大长度
        <min> - 最小长度
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_length_not_secure")
    val loginWarningRegisterPasswordStrategyFailureLengthNotSecure: String = "密码长度必须大于等于 <min>，小于等于 <max>",
    @TomlComment(
        """
        玩家注册时密码存在于弱密码字典中
    """
    )
    @SerialName("login_warning_register_password_strategy_failure_in_weak_password_dict")
    val loginWarningRegisterPasswordStrategyFailureInWeakPasswordDict: String = "您当前的密码存在于弱密码字典中，请更换密码",
)

enum class UUIDGenerator {

    MOJANG, CRACKED

}

enum class Order {
    FIRST, LAST
}