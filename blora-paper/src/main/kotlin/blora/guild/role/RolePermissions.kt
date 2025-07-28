package blora.guild.role

import kotlinx.serialization.Serializable

@Serializable
data class RolePermissions(
    var modifyGuildName: Boolean = false,
    var modifyGuildId: Boolean = false,
    var modifyGuildIcon: Boolean = false,
    var modifyGuildVisibility: Boolean = false,
    var modifyGuildJoinStrategy: Boolean = false,
    var createRole: Boolean = false,
    var modifyRole: Boolean = false,
    var deleteRole: Boolean = false,
    var kickPlayer: Boolean = false,
    var reviewPlayer: Boolean = false,
    var invitePlayer: Boolean = true,
    var requestAlly: Boolean = false,
    var reviewAlly: Boolean = false,
    var stopAlly: Boolean = false,
    var manageInvitationCode: Boolean = false,
    var storeBank: Boolean = true,
    var withdrawBank: Boolean = false,
    var useVitality: Boolean = false,
    var blocklist: Boolean = false,
    var enderChest: Boolean = true,
    var townManagement: Boolean = false,
) {

    fun merge(another: RolePermissions): RolePermissions {
        return RolePermissions(
            modifyGuildName = this.modifyGuildName || another.modifyGuildName,
            modifyGuildId = this.modifyGuildId || another.modifyGuildId,
            modifyGuildIcon = this.modifyGuildIcon || another.modifyGuildIcon,
            modifyGuildVisibility = this.modifyGuildVisibility || another.modifyGuildVisibility,
            modifyGuildJoinStrategy = this.modifyGuildJoinStrategy || another.modifyGuildJoinStrategy,
            createRole = this.createRole || another.createRole,
            modifyRole = this.modifyRole || another.modifyRole,
            deleteRole = this.deleteRole || another.deleteRole,
            kickPlayer = this.kickPlayer || another.kickPlayer,
            reviewPlayer = this.reviewPlayer || another.reviewPlayer,
            invitePlayer = this.invitePlayer || another.invitePlayer,
            requestAlly = this.requestAlly || another.requestAlly,
            reviewAlly = this.reviewAlly || another.reviewAlly,
            stopAlly = this.stopAlly || another.stopAlly,
            manageInvitationCode = this.manageInvitationCode || another.manageInvitationCode,
            storeBank = this.storeBank || another.storeBank,
            withdrawBank = this.withdrawBank || another.withdrawBank,
            useVitality = this.useVitality || another.useVitality,
            blocklist = this.blocklist || another.blocklist,
            enderChest = this.enderChest || another.enderChest,
            townManagement = this.townManagement || another.townManagement,
        )
    }

    fun clone(): RolePermissions {
        return RolePermissions(
            modifyGuildName = modifyGuildName,
            modifyGuildId = modifyGuildId,
            modifyGuildIcon = modifyGuildIcon,
            modifyGuildVisibility = modifyGuildVisibility,
            modifyGuildJoinStrategy = modifyGuildJoinStrategy,
            createRole = createRole,
            modifyRole = modifyRole,
            deleteRole = deleteRole,
            kickPlayer = kickPlayer,
            reviewPlayer = reviewPlayer,
            invitePlayer = invitePlayer,
            requestAlly = requestAlly,
            reviewAlly = reviewAlly,
            stopAlly = stopAlly,
            manageInvitationCode = manageInvitationCode,
            storeBank = storeBank,
            withdrawBank = withdrawBank,
            useVitality = useVitality,
            blocklist = blocklist,
            enderChest = enderChest,
            townManagement = townManagement,
        )
    }

    companion object {

        fun allowAll(): RolePermissions {
            return RolePermissions(
                modifyGuildName = true,
                modifyGuildId = true,
                modifyGuildIcon = true,
                modifyGuildVisibility = true,
                modifyGuildJoinStrategy = true,
                createRole = true,
                modifyRole = true,
                deleteRole = true,
                kickPlayer = true,
                reviewPlayer = true,
                invitePlayer = true,
                requestAlly = true,
                reviewAlly = true,
                stopAlly = true,
                manageInvitationCode = true,
                storeBank = true,
                withdrawBank = true,
                useVitality = true,
                blocklist = true,
                enderChest = true,
                townManagement = true,
            )
        }

        fun denyAll(): RolePermissions {
            return RolePermissions(
                modifyGuildName = false,
                modifyGuildId = false,
                modifyGuildIcon = false,
                modifyGuildVisibility = false,
                modifyGuildJoinStrategy = false,
                createRole = false,
                modifyRole = false,
                deleteRole = false,
                kickPlayer = false,
                reviewPlayer = false,
                invitePlayer = false,
                requestAlly = false,
                reviewAlly = false,
                stopAlly = false,
                manageInvitationCode = false,
                storeBank = false,
                withdrawBank = false,
                useVitality = false,
                blocklist = false,
                enderChest = false,
                townManagement = false,
            )
        }

    }

}

fun Collection<RolePermissions>.merge(): RolePermissions {
    var base = RolePermissions.denyAll()
    for (permission in this) {
        base = base.merge(permission)
    }
    return base
}