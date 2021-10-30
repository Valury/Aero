package wtf.jaren.aurora.velocity.channels

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aurora.shared.objects.AuroraPlayer
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.objects.Channel
import wtf.jaren.aurora.velocity.utils.*

class StaffChannel(plugin: Aurora) : Channel(plugin) {
    override val id: String = "STAFF"

    override fun isMember(player: AuroraPlayer): Boolean {
        return player.hasPermission("aurora.channel.staff")
    }

    override fun sendMessage(sender: AuroraPlayer, message: String) {
        for (player in plugin.server.allPlayers) {
            if (isMember(player.aurora)) {
                player.sendMessage(
                    Component.empty()
                        .append(Component.text("Staff / ", NamedTextColor.YELLOW))
                        .append(sender.displayNameFor(player))
                        .append(ChatUtils.getMessageAsComponent(sender.luckperms, ": $message"))
                )
            }
        }
    }
}