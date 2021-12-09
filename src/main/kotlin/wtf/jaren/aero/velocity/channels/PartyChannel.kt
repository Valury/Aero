package wtf.jaren.aero.velocity.channels

import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aero.shared.objects.AeroPlayer
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.objects.Channel
import wtf.jaren.aero.velocity.objects.Party
import wtf.jaren.aero.velocity.utils.ChatUtils
import wtf.jaren.aero.velocity.utils.displayNameFor
import wtf.jaren.aero.velocity.utils.luckperms

class PartyChannel(plugin: Aero) : Channel(plugin) {
    override val id: String = "PARTY"

    override fun isMember(player: AeroPlayer): Boolean {
        val serverPlayer = plugin.server.getPlayer(player._id).orElse(null) ?: return false
        return plugin.partyManager.getPlayerParty(serverPlayer) != null
    }

    override fun sendMessage(sender: AeroPlayer, message: String) {
        if (message.contains("\${jndi:", ignoreCase = true)) {
            return
        }
        val player = plugin.server.getPlayer(sender._id).get()
        val party = plugin.partyManager.getPlayerParty(player)!!
        for (member in party.members) {
            member.sendMessage(
                Identity.identity(sender._id), Component.text()
                    .append(Party.PREFIX)
                    .append(player.displayNameFor(member))
                    .append(ChatUtils.getMessageAsComponent(sender.luckperms, ": $message"))
            )
        }
    }

    override fun getNoMembershipMessage(player: AeroPlayer): Component {
        return Component.text("You are not in a party.", NamedTextColor.RED)
    }

    override fun shouldRegisterCommand(player: AeroPlayer): Boolean {
        return true
    }
}