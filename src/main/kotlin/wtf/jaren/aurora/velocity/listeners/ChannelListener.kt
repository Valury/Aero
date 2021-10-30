package wtf.jaren.aurora.velocity.listeners

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.enums.PartyRemoveReason
import wtf.jaren.aurora.velocity.utils.ChatUtils
import wtf.jaren.aurora.velocity.utils.StringUtils
import wtf.jaren.aurora.velocity.utils.aurora

class ChannelListener(private val plugin: Aurora) {
    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        val player = event.player
        plugin.channelManager.channelOverrides.remove(player)
        plugin.partyManager.getPlayerParty(player)?.removeMember(player, PartyRemoveReason.DISCONNECTED)
    }

    @Subscribe(order = PostOrder.LAST)
    fun onPlayerChat(event: PlayerChatEvent) {
        event.result = PlayerChatEvent.ChatResult.denied()
        plugin.channelManager.getPlayerChannel(event.player).sendMessage(
            event.player.aurora,
            ChatUtils.process(event.player, event.message) ?: return
        )
    }
}