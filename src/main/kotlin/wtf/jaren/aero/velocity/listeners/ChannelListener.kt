package wtf.jaren.aero.velocity.listeners

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.enums.PartyRemoveReason
import wtf.jaren.aero.velocity.utils.ChatUtils
import wtf.jaren.aero.velocity.utils.aero

class ChannelListener(private val plugin: Aero) {
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
            event.player.aero,
            ChatUtils.process(event.player, event.message) ?: return
        )
    }
}