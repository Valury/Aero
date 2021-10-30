package wtf.jaren.aurora.velocity.channels

import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aurora.shared.objects.AuroraPlayer
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.objects.Channel
import wtf.jaren.aurora.velocity.objects.Party
import wtf.jaren.aurora.velocity.utils.ChatUtils
import wtf.jaren.aurora.velocity.utils.displayNameFor
import wtf.jaren.aurora.velocity.utils.luckperms

class PartyChannel(plugin: Aurora) : Channel(plugin) {
    override val id: String = "PARTY"

    override fun isMember(player: AuroraPlayer): Boolean {
        val serverPlayer = plugin.server.getPlayer(player._id).orElse(null) ?: return false
        return plugin.partyManager.getPlayerParty(serverPlayer) != null
    }

    override fun sendMessage(sender: AuroraPlayer, message: String) {
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

    override fun getNoMembershipMessage(player: AuroraPlayer): Component {
        return Component.text("You are not in a party.", NamedTextColor.RED)
    }

    override fun shouldRegisterCommand(player: AuroraPlayer): Boolean {
        return true
    }
}