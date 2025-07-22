package blora.guild

import java.util.EnumSet

enum class GuildPermissions {

    MODIFY_GUILD_NAME,
    MODIFY_GUILD_ID,
    MODIFY_GUILD_ICON,
    MODIFY_GUILD_VISIBILITY,
    MODIFY_GUILD_JOIN_STRATEGY,
    CREATE_ROLE,
    MODIFY_ROLE,
    DELETE_ROLE,
    KICK_PLAYER,
    REVIEW_PLAYER,
    INVITE_PLAYER,
    REQUEST_ALLY,
    REVIEW_ALLY,
    STOP_ALLY,
    MANAGE_INVITATION_CODE,
    STORE_BANK,
    WITHDRAW_BANK,
    USE_VITALITY,
    BLOCKLIST,
    ENDER_CHEST;

}

fun EnumSet<GuildPermissions>.anyGuildSettings(): Boolean {
    return this.contains(GuildPermissions.MODIFY_GUILD_NAME) ||
            this.contains(GuildPermissions.MODIFY_GUILD_ID) ||
            this.contains(GuildPermissions.MODIFY_GUILD_ICON) ||
            this.contains(GuildPermissions.MODIFY_GUILD_VISIBILITY) ||
            this.contains(GuildPermissions.MODIFY_GUILD_JOIN_STRATEGY)
}

fun EnumSet<GuildPermissions>.anyRoleManagement(): Boolean {
    return this.contains(GuildPermissions.CREATE_ROLE) ||
            this.contains(GuildPermissions.MODIFY_ROLE) ||
            this.contains(GuildPermissions.DELETE_ROLE)
}