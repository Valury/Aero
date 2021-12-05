package wtf.jaren.aero.velocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent

class PluginMessageListener {
    @Subscribe
    fun onPluginMessage(event: PluginMessageEvent) {
        if (event.identifier.id.startsWith("aero:")) {
            event.result = PluginMessageEvent.ForwardResult.handled()
        }
    }
}