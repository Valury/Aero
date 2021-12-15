package wtf.jaren.aero.velocity.channels

import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aero.shared.objects.AeroPlayer
import wtf.jaren.aero.shared.objects.Guild
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.enums.PunishmentType
import wtf.jaren.aero.velocity.objects.Channel
import wtf.jaren.aero.velocity.utils.ChatUtils
import wtf.jaren.aero.velocity.utils.aero
import wtf.jaren.aero.velocity.utils.displayNameFor
import wtf.jaren.aero.velocity.utils.luckperms

class GuildChannel(plugin: Aero) : Channel(plugin) {
    override val id: String = "GUILD"

    override fun isMember(player: AeroPlayer): Boolean {
        return player.guild != null
    }

    override fun sendMessage(sender: AeroPlayer, message: String) {
        val punishment = plugin.punishmentManager.getActivePunishment(sender, PunishmentType.MUTE)
        if (punishment != null) {
            plugin.server.getPlayer(sender._id).orElse(null)?.sendMessage(punishment.getMessage(false))
            return
        }
        val guild = plugin.guildManager.getGuild(sender.guild!!._id)
        for (player in plugin.server.allPlayers) {
            if (guild._id == player.aero.guild?._id) {
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

    override fun getNoMembershipMessage(player: AeroPlayer): Component {
        return Component.text("You are not in a guild.", NamedTextColor.RED)
    }

    override fun shouldRegisterCommand(player: AeroPlayer): Boolean {
        return true
    }
}