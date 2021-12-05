package wtf.jaren.aero.velocity.managers

import com.velocitypowered.api.proxy.Player
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.channels.GuildChannel
import wtf.jaren.aero.velocity.channels.PartyChannel
import wtf.jaren.aero.velocity.channels.ServerChannel
import wtf.jaren.aero.velocity.channels.StaffChannel
import wtf.jaren.aero.velocity.commands.ChannelChatCommand
import wtf.jaren.aero.velocity.objects.Channel
import wtf.jaren.aero.velocity.utils.aero

class ChannelManager(private val plugin: Aero) {
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
        if (!channel.isMember(player.aero)) {
            channelOverrides.remove(player)
            return channels[0]
        }
        return channel
    }
}