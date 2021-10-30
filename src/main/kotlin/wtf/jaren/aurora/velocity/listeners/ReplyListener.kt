package wtf.jaren.aurora.velocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import wtf.jaren.aurora.velocity.Aurora

class ReplyListener(private val plugin: Aurora) {
    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        plugin.replyManager.replyMap.remove(event.player)
    }
}