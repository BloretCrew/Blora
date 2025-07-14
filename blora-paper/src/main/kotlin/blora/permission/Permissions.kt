package blora.permission

import org.bukkit.Bukkit
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault

object Permissions {

    val Admin = "bloret.admin"

    object Mail {

        val CreateSystemMail = "bloret.mail.create.systemmail"

        fun registerPermissions() {
            registerPermission(CreateSystemMail, 0)
        }

    }

    object Commands {

        val Blora = "bloret.command.blora"
        val Mail = "bloret.command.mail"
        val Redeem = "bloret.command.redeem"

        fun registerPermissions() {
            registerPermission(Blora, 0)
            registerPermission(Mail, 1)
            registerPermission(Redeem, 1)
        }

    }

    fun registerPermissions() {
        Mail.registerPermissions()
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