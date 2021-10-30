package wtf.jaren.aurora.shared.objects

import org.bson.Document

data class GuildPermissions(
    var invite: Int = 1, // Member
    var rename: Int = 0, // Leader
    var tpa: Int = 0, // Leader
    var withdraw: Int = 0 // Leader
) {
    fun canInvite(player: AuroraPlayer): Boolean {
        return checkPermission(player, invite)
    }

    fun canRename(player: AuroraPlayer): Boolean {
        return checkPermission(player, rename)
    }

    fun canTPA(player: AuroraPlayer): Boolean {
        return checkPermission(player, tpa)
    }

    fun canWithdraw(player: AuroraPlayer): Boolean {
        return checkPermission(player, withdraw)
    }

    private fun checkPermission(player: AuroraPlayer, permission: Int): Boolean {
        if (player.guild!!.rank == 0) return true
        return if (permission != 0) player.guild!!.rank >= permission else false
    }

    fun toDocument(): Document {
        return Document("invite", invite)
            .append("rename", rename)
            .append("tpa", tpa)
            .append("withdraw", withdraw)
    }

    companion object {
        fun fromDocument(document: Document): GuildPermissions {
            return GuildPermissions(
                document.getInteger("invite"),
                document.getInteger("rename"),
                document.getInteger("tpa"),
                document.getInteger("withdraw")
            )
        }
    }
}