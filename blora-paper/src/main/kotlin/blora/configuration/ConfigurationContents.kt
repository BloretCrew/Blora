package blora.configuration

import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

@Serializable
data class ConfigurationContents(
    @TomlComment("服务器间通信，插件部分重要功能都依赖此功能")
    val messageing: Messaging = Messaging(),
    @TomlComment("模块化管理，关闭后，模块中部分功能将无法在本服务器使用，具体功能请看各自模块的注释")
    val modules: Modules = Modules(),
)

@Serializable
data class Messaging(
    @TomlComment("通信服务器 IP")
    val host: String = "127.0.0.1",
    @TomlComment("通信服务器端口")
    val port: Int = 11732,
    @TomlComment("请保证每个子服务器的该值都不相同")
    val conv: Int = 0,
    @TomlComment("当前服务器在代理端的名称，填写错误将会被代理端拒绝连接")
    val serverName: String = "server",
    @TomlComment("服务器通信密码")
    val password: String = "X1$&al1*&lakd*#@kak!LKD",
)

@Serializable
data class Modules(
    @TomlComment(
        """
        邮件模块
        - 无论是否开启都可以浏览邮件内容
        - 关闭后将无法在此服务器领取邮件的附件
    """
    )
    val mail: Boolean = true
)