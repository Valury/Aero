package wtf.jaren.aurora.velocity.channels

import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aurora.shared.objects.AuroraPlayer
import wtf.jaren.aurora.shared.objects.Guild
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.enums.PunishmentType
import wtf.jaren.aurora.velocity.objects.Channel
import wtf.jaren.aurora.velocity.utils.ChatUtils
import wtf.jaren.aurora.velocity.utils.aurora
import wtf.jaren.aurora.velocity.utils.displayNameFor
import wtf.jaren.aurora.velocity.utils.luckperms

class GuildChannel(plugin: Aurora) : Channel(plugin) {
    override val id: String = "GUILD"

    override fun isMember(player: AuroraPlayer): Boolean {
        return player.guild != null
    }

    override fun sendMessage(sender: AuroraPlayer, message: String) {
        val punishment = plugin.punishmentManager.getActivePunishment(sender, PunishmentType.MUTE)
        if (punishment != null) {
            plugin.server.getPlayer(sender._id).orElse(null)?.sendMessage(punishment.getMessage(false))
            return
        }
        val guild = plugin.guildManager.getGuild(sender.guild!!._id)
        for (player in plugin.server.allPlayers) {
            if (guild._id == player.aurora.guild?._id) {
                player.sendMessage(
                    Identity.identity(sender._id),
                    Component.text()
                        .append(Guild.PREFIX)
                        .append(sender.displayNameFor(player))
                        .append(Component.text(" [${guild.ranks[sender.guild!!.rank]}]", Guild.COLOR_SCHEME))
                        .append(ChatUtils.getMessageAsComponent(sender.luckperms, ": $message"))
                        .build()
                )
            }
        }
    }

    override fun getNoMembershipMessage(player: AuroraPlayer): Component {
        return Component.text("You are not in a guild.", NamedTextColor.RED)
    }

    override fun shouldRegisterCommand(player: AuroraPlayer): Boolean {
        return true
    }
}