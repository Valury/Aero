package wtf.jaren.aero.velocity.commands

import com.velocitypowered.api.command.RawCommand
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aero.velocity.objects.Channel
import wtf.jaren.aero.velocity.utils.ChatUtils
import wtf.jaren.aero.velocity.utils.aero

class ChannelChatCommand(private val channel: Channel) : RawCommand {
    override fun execute(invocation: RawCommand.Invocation) {
        if (invocation.source() !is Player) {
            invocation.source().sendMessage(Component.text("You cannot do this from console.", NamedTextColor.RED))
            return
        }
        val player = invocation.source() as Player
        if (!channel.isMember(player.aero)) {
            invocation.source().sendMessage(channel.getNoMembershipMessage(player.aero))
            return
        }
        channel.sendMessage(player.aero, ChatUtils.process(player, invocation.arguments()) ?: return)
    }

    override fun hasPermission(invocation: RawCommand.Invocation): Boolean {
        return invocation.source() is Player && channel.shouldRegisterCommand((invocation.source() as Player).aero)
    }
}