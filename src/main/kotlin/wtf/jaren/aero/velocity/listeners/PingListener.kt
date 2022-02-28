package wtf.jaren.aero.velocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.utils.aero

class PingListener(private val plugin: Aero) {
    @Subscribe
    fun onProxyPing(e: ProxyPingEvent) {
        val builder = e.ping.asBuilder()
        builder.onlinePlayers(plugin.server.allPlayers.filter { !it.aero.vanished }.size)
        builder.clearSamplePlayers()
        e.ping = builder.build()
    }
}