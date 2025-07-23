package blora.database.guild.dao

import blora.converter.jsonToItemStack
import blora.converter.toJson
import blora.database.DB
import blora.database.guild.table.GuildTable
import blora.guild.GuildPermissions
import blora.plugin.BloraPlugin
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class GuildDao(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<GuildDao>(GuildTable)

    var gid: String by GuildTable.gid
    var owner: UUID by GuildTable.owner
    var public: Boolean by GuildTable.public
    var icon: String by GuildTable.icon
    var displayName: String by GuildTable.displayName
    var level by GuildTable.level

    var joinStrategy by GuildTable.joinStrategy

    var members: List<UUID> by GuildTable.members
    var towns: List<String> by GuildTable.towns
    var allys: List<String> by GuildTable.allys
    var blocklist: List<UUID> by GuildTable.blocklist

    var bankBalance: Double by GuildTable.bankBalance
    var bankBalanceMax: Double by GuildTable.bankBalanceMax

    var vitality: Long by GuildTable.vitality

    var createAt: LocalDateTime by GuildTable.createdAt
    var lastOwnerTransferDate: LocalDate by GuildTable.lastOwnerTransferDate

    val maxMembers: Int
        get() {
            val guildLevelConfiguration = BloraPlugin.configuration.guild.level
            var memberLimit = guildLevelConfiguration.basic.member.base
            for (level in 2..this.level) {
                val override = guildLevelConfiguration.overrides[level]
                memberLimit += if (override != null && override.member != null)
                    override.member
                else
                    guildLevelConfiguration.basic.member.increment
            }
            return memberLimit
        }

    val maxClaims: Int
        get() {
            val guildLevelConfiguration = BloraPlugin.configuration.guild.level
            var memberLimit = guildLevelConfiguration.basic.claim.base
            for (level in 2..this.level) {
                val override = guildLevelConfiguration.overrides[level]
                memberLimit += if (override != null && override.claim != null)
                    override.claim
                else
                    guildLevelConfiguration.basic.claim.increment
            }
            return memberLimit
        }

    val nextLevelMaxMembers: Int
        get() {
            val guildLevelConfiguration = BloraPlugin.configuration.guild.level
            var memberLimit = guildLevelConfiguration.basic.member.base
            for (level in 2..(this.level + 1)) {
                val override = guildLevelConfiguration.overrides[level]
                memberLimit += if (override != null && override.member != null)
                    override.member
                else
                    guildLevelConfiguration.basic.member.increment
            }
            return memberLimit
        }

    val nextLevelMaxClaims: Int
        get() {
            val guildLevelConfiguration = BloraPlugin.configuration.guild.level
            var memberLimit = guildLevelConfiguration.basic.claim.base
            for (level in 2..(this.level + 1)) {
                val override = guildLevelConfiguration.overrides[level]
                memberLimit += if (override != null && override.claim != null)
                    override.claim
                else
                    guildLevelConfiguration.basic.claim.increment
            }
            return memberLimit
        }

    val upgradeCost: Int
        get() {
            val guildLevelConfiguration = BloraPlugin.configuration.guild.level
            var upgradeCost = guildLevelConfiguration.basic.upgradeCost.base
            for (level in 2..level) {
                val override = guildLevelConfiguration.overrides[level]
                upgradeCost += if (override != null && override.upgradeCost != null)
                    override.upgradeCost
                else
                    guildLevelConfiguration.basic.upgradeCost.increment
            }
            return upgradeCost
        }

    var parsedIcon: ItemStack
        get() = this.icon.jsonToItemStack()
        set(value) {
            this.icon = value.toJson()
        }

    var parsedAllys: List<GuildDao>
        get() = this.allys.mapNotNull { BloraPlugin.database.getGuildByGid(it) }.toList()
        set(value) {
            this.allys = value.map { it.gid }.toList()
        }

    fun getPlayerPermissions(player: UUID, isMember: Boolean): EnumSet<GuildPermissions> {
        if (!isMember) {
            return EnumSet.noneOf(GuildPermissions::class.java)
        }
        if (this.owner == player)
            return EnumSet.allOf(GuildPermissions::class.java)
        val permissions = EnumSet.noneOf(GuildPermissions::class.java)
        for (permission in DB.listRolesForPlayer(player, this.gid).map { it.permission }) {
            if (permission.modifyGuildName) {
                permissions.add(GuildPermissions.MODIFY_GUILD_NAME)
            }
            if (permission.modifyGuildId) {
                permissions.add(GuildPermissions.MODIFY_GUILD_ID)
            }
            if (permission.modifyGuildIcon) {
                permissions.add(GuildPermissions.MODIFY_GUILD_NAME)
            }
            if (permission.modifyGuildVisibility) {
                permissions.add(GuildPermissions.MODIFY_GUILD_VISIBILITY)
            }
            if (permission.modifyGuildJoinStrategy) {
                permissions.add(GuildPermissions.MODIFY_GUILD_JOIN_STRATEGY)
            }
            if (permission.kickPlayer) {
                permissions.add(GuildPermissions.KICK_PLAYER)
            }
            if (permission.reviewPlayer) {
                permissions.add(GuildPermissions.REVIEW_PLAYER)
            }
            if (permission.invitePlayer) {
                permissions.add(GuildPermissions.INVITE_PLAYER)
            }
            if (permission.requestAlly) {
                permissions.add(GuildPermissions.REQUEST_ALLY)
            }
            if (permission.reviewAlly) {
                permissions.add(GuildPermissions.REVIEW_ALLY)
            }
            if (permission.stopAlly) {
                permissions.add(GuildPermissions.STOP_ALLY)
            }
            if (permission.manageInvitationCode) {
                permissions.add(GuildPermissions.MANAGE_INVITATION_CODE)
            }
            if (permission.storeBank) {
                permissions.add(GuildPermissions.STORE_BANK)
            }
            if (permission.withdrawBank) {
                permissions.add(GuildPermissions.WITHDRAW_BANK)
            }
            if (permission.useVitality) {
                permissions.add(GuildPermissions.USE_VITALITY)
            }
            if (permission.useVitality) {
                permissions.add(GuildPermissions.USE_VITALITY)
            }
            if (permission.blocklist) {
                permissions.add(GuildPermissions.BLOCKLIST)
            }
            if (permission.enderChest) {
                permissions.add(GuildPermissions.ENDER_CHEST)
            }
        }
        return permissions
    }

}