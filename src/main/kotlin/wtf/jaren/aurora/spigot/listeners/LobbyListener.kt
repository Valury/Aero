package wtf.jaren.aurora.spigot.listeners

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent

class LobbyListener : Listener {
    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (event.to.y <= 0) {
            event.player.teleport(Location(Bukkit.getWorld("world"), 90.5, 98.0, -418.5, 180F, 0F))
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.teleport(Location(Bukkit.getWorld("world"), 90.5, 98.0, -418.5, 180F, 0F))
    }
}