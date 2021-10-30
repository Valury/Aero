package wtf.jaren.aurora.velocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent

class PluginMessageListener {
    @Subscribe
    fun onPluginMessage(event: PluginMessageEvent) {
        if (event.identifier.id.startsWith("aurora:")) {
            event.result = PluginMessageEvent.ForwardResult.handled()
        }
    }
}