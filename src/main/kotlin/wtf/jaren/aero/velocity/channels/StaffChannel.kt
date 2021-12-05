package wtf.jaren.aero.velocity.channels

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aero.shared.objects.AeroPlayer
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.objects.Channel
import wtf.jaren.aero.velocity.utils.*

class StaffChannel(plugin: Aero) : Channel(plugin) {
    override val id: String = "STAFF"

    override fun isMember(player: AeroPlayer): Boolean {
        return player.hasPermission("aero.channel.staff")
    }

    override fun sendMessage(sender: AeroPlayer, message: String) {
        for (player in plugin.server.allPlayers) {
            if (isMember(player.aero)) {
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