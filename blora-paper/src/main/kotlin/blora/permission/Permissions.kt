package blora.permission

import org.bukkit.Bukkit
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault

object Permissions {

    val Admin = "bloret.admin"

    object Guild {

        val Create = "bloret.guild.create"
        val Join = "bloret.guild.join"

        fun registerPermissions() {
            registerPermission(Create, 2)
            registerPermission(Join, 2)
        }

    }

    object Mail {

        val CreateSystemMail = "bloret.mail.create.systemmail"

        fun registerPermissions() {
            registerPermission(CreateSystemMail, 0)
        }

    }

    object Chat {

        val MiniMessage = "bloret.chat.mini_message"
        val MentionAll = "bloret.chat.mention_all"

        fun registerPermissions() {
            registerPermission(MentionAll, 0)
            registerPermission(MiniMessage, 2)
        }

    }

    object Commands {

        val Blora = "bloret.command.blora"
        val Mail = "bloret.command.mail"
        val Redeem = "bloret.command.redeem"
        val Tell = "bloret.command.tell"
        val Guild = "bloret.command.guild"
        val Town = "bloret.command.town"
        val Lead = "bloret.command.lead"
        val LeadCoins = "bloret.command.lead.coins"
        val LeadBlroius = "bloret.command.lead.blorius"
        val LeadLevels = "bloret.command.lead.levels"

        fun registerPermissions() {
            registerPermission(Blora, 0)
            registerPermission(Mail, 1)
            registerPermission(Redeem, 1)
            registerPermission(Tell, 1)
            registerPermission(Guild, 1)
            registerPermission(Town, 1)
            registerPermission(Lead, 1)
            registerPermission(LeadCoins, 1)
            registerPermission(LeadBlroius, 1)
            registerPermission(LeadLevels, 1)
        }

    }

    fun registerPermissions() {
        Guild.registerPermissions()
        Mail.registerPermissions()
        Chat.registerPermissions()
        Commands.registerPermissions()

        registerPermission(Admin, 0)
    }

}

fun registerPermission(name: String, modifier: Int) {
    if (Bukkit.getPluginManager().getPermission(name) != null) {
        return
    }
    Bukkit.getPluginManager()
        .addPermission(
            Permission(
                name,
                when (modifier) {
                    1 -> PermissionDefault.TRUE
                    2 -> PermissionDefault.FALSE
                    3 -> PermissionDefault.NOT_OP
                    else -> PermissionDefault.OP
                }
            )
        )
}