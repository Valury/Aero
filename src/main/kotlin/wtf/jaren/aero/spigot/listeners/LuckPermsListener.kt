package wtf.jaren.aero.spigot.listeners

import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.Bukkit
import wtf.jaren.aero.spigot.Aero
import wtf.jaren.aero.spigot.utils.updateDisplayName

class LuckPermsListener(val plugin: Aero) {
    init {
        LuckPermsProvider.get().eventBus.subscribe(
            plugin,
            UserDataRecalculateEvent::class.java,
            this::onUserDataRecalculate
        )
    }

    private fun onUserDataRecalculate(event: UserDataRecalculateEvent) {
        // LuckPerms will sometimes send the event after PlayerManager has processed a player quit.
        if (plugin.playerManager.players.containsKey(event.user.uniqueId)) {
            Bukkit.getPlayer(event.user.uniqueId)?.apply {
                updateDisplayName()
            }
        }
    }
}