package blora.guild

import blora.database.guild.dao.GuildDao
import blora.database.guild.dao.GuildJoinLogDao
import blora.database.guild.dao.GuildMemberInfoDao
import blora.database.guild.dao.GuildPlayerOnlineDao
import blora.database.guild.dao.GuildRoleDao
import blora.database.guild.table.GuildJoinLogTable
import blora.guild.role.RolePermissions
import blora.plugin.BloraPlugin
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.and
import java.time.LocalDate
import java.time.LocalDateTime

object GuildModule {

    fun createGuild(owner: Player, context: CreatingGuildContext): GuildDao {
        return BloraPlugin.database.trans {
            createAdminRole(owner, context.id)
            createMemberRole(owner, context.id)
            GuildMemberInfoDao.new {
                this.guildId = context.id
                this.player = owner.uniqueId
                this.joinAt = LocalDateTime.now()

                this.parsedJoinSource = GuildJoinSource.Creator
            }
            GuildJoinLogDao.new {
                this.guildId = context.id
                this.player = owner.uniqueId
                this.firstJoin = LocalDateTime.now()
            }
            GuildPlayerOnlineDao.new {
                this.guildId = context.id
                this.player = owner.uniqueId
                this.lastCalculate = LocalDateTime.now()
            }
            GuildDao.new {
                this.gid = context.id
                this.owner = owner.uniqueId
                this.public = true
                this.parsedIcon = context.icon
                this.displayName = context.displayName.ifEmpty { context.id }

                this.members = listOf(owner.uniqueId)
                this.towns = emptyList()
                this.allys = emptyList()
                this.blocklist = emptyList()

                this.bankBalance = 0.0
                this.bankBalanceMax = 0.0

                this.vitality = 0.0

                this.createAt = LocalDateTime.now()
                this.lastOwnerTransferDate = LocalDate.now()
            }
        }
    }

    private fun createAdminRole(owner: Player, gid: String) {
        BloraPlugin.database.trans {
            GuildRoleDao.new {
                this.roleId = "admin"
                this.displayName = "管理员"
                this.guildId = gid
                this.systemCreated = true
                this.priority = 3
                this.permission = RolePermissions(
                    modifyGuildName = true,
                    modifyGuildId = true,
                    modifyGuildIcon = true,
                    modifyGuildVisibility = true,
                    modifyGuildJoinStrategy = true,
                    kickPlayer = true,
                    reviewPlayer = true,
                    invitePlayer = true,
                    manageInvitationCode = true,
                    storeBank = true,
                    withdrawBank = true,
                    useVitality = true
                )
                this.ownedMembers = listOf(owner.uniqueId)
            }
        }
    }

    private fun createMemberRole(owner: Player, gid: String) {
        BloraPlugin.database.trans {
            GuildRoleDao.new {
                this.roleId = "member"
                this.displayName = "成员"
                this.guildId = gid
                this.systemCreated = true
                this.priority = 1
                this.permission = RolePermissions()
                this.ownedMembers = listOf(owner.uniqueId)
            }
        }
    }

}