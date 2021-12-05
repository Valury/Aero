package wtf.jaren.aero.velocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import wtf.jaren.aero.velocity.Aero

class ReplyListener(private val plugin: Aero) {
    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        plugin.replyManager.replyMap.remove(event.player)
    }
}