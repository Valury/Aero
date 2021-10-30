package wtf.jaren.aurora.velocity.commands

import com.velocitypowered.api.command.RawCommand
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.objects.Channel
import wtf.jaren.aurora.velocity.utils.ChatUtils
import wtf.jaren.aurora.velocity.utils.StringUtils
import wtf.jaren.aurora.velocity.utils.aurora

class ChannelChatCommand(private val channel: Channel) : RawCommand {
    override fun execute(invocation: RawCommand.Invocation) {
        if (invocation.source() !is Player) {
            invocation.source().sendMessage(Component.text("You cannot do this from console.", NamedTextColor.RED))
            return
        }
        val player = invocation.source() as Player
        if (!channel.isMember(player.aurora)) {
            invocation.source().sendMessage(channel.getNoMembershipMessage(player.aurora))
            return
        }
        channel.sendMessage(player.aurora, ChatUtils.process(player, invocation.arguments()) ?: return)
    }

    override fun hasPermission(invocation: RawCommand.Invocation): Boolean {
        return invocation.source() is Player && channel.shouldRegisterCommand((invocation.source() as Player).aurora)
    }
}