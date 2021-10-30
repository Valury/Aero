package wtf.jaren.aurora.velocity.managers

import com.velocitypowered.api.proxy.Player
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.channels.GuildChannel
import wtf.jaren.aurora.velocity.channels.PartyChannel
import wtf.jaren.aurora.velocity.channels.ServerChannel
import wtf.jaren.aurora.velocity.channels.StaffChannel
import wtf.jaren.aurora.velocity.commands.ChannelChatCommand
import wtf.jaren.aurora.velocity.objects.Channel
import wtf.jaren.aurora.velocity.utils.aurora

class ChannelManager(private val plugin: Aurora) {
    val channels: Array<Channel> = arrayOf(
        ServerChannel(plugin),
        GuildChannel(plugin),
        PartyChannel(plugin),
        StaffChannel(plugin)
    )
    val channelOverrides = HashMap<Player, Channel>()
    fun initialize() {
        val commandManager = plugin.server.commandManager
        for (channel in channels) {
            commandManager.register(channel.getCommandMeta(commandManager), ChannelChatCommand(channel))
        }
    }

    fun getPlayerChannel(player: Player): Channel {
        val channel = channelOverrides.getOrDefault(player, channels[0])
        if (!channel.isMember(player.aurora)) {
            channelOverrides.remove(player)
            return channels[0]
        }
        return channel
    }
}