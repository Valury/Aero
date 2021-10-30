package wtf.jaren.aurora.velocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.proxy.ServerConnection
import wtf.jaren.aurora.velocity.Aurora

class WarpLockListener(private val plugin: Aurora) {
    @Subscribe
    fun onPluginMessage(event: PluginMessageEvent) {
        if (event.identifier.id == "aurora:warp_lock" && event.source is ServerConnection) {
            plugin.warpManager.lock((event.source as ServerConnection).player)
        }
        if (event.identifier.id == "aurora:warp_unlock" && event.source is ServerConnection) {
            plugin.warpManager.unlock((event.source as ServerConnection).player)
        }
    }

    @Subscribe
    fun onServerChange(event: ServerConnectedEvent) {
        plugin.warpManager.unlock(event.player)
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        plugin.warpManager.unlock(event.player)
    }
}