package blora.guild

import java.util.*

sealed class GuildJoinSource {

    abstract fun encodeToString(): String

    class InvitationCode(val code: String) : GuildJoinSource() {
        override fun encodeToString(): String {
            return "invitation:$code"
        }
    }

    class InviteByPlayer(val uuid: UUID) : GuildJoinSource() {
        override fun encodeToString(): String {
            return "invite:$uuid"
        }
    }

    object GuildList : GuildJoinSource() {
        override fun encodeToString(): String {
            return "guild_list"
        }
    }

    object Creator : GuildJoinSource() {
        override fun encodeToString(): String {
            return "creator"
        }
    }

    object Unknown : GuildJoinSource() {
        override fun encodeToString(): String {
            return "unknown"
        }
    }

    companion object {

        fun decodeFromString(string: String): GuildJoinSource {
            return if (string == "creator") {
                Creator
            } else if (string == "guild_list") {
                GuildList
            } else if (string.startsWith("invitation:")) {
                InvitationCode(string.removePrefix("invitation:"))
            } else if (string.startsWith("invite:")) {
                InviteByPlayer(UUID.fromString(string.removePrefix("invite:")))
            } else {
                Unknown
            }
        }

    }

}