package wtf.jaren.aero.spigot.listeners

import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import wtf.jaren.aero.spigot.Aero
import wtf.jaren.aero.spigot.utils.aero
import wtf.jaren.aero.spigot.utils.displayNameFor

class PlayerListener(private val plugin: Aero) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        try {
            plugin.playerManager.handlePreLogin(event.uniqueId)
        } catch (e: Exception) {
            e.printStackTrace()
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Aero / Something went horribly wrong.", NamedTextColor.RED))
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        try {
            plugin.playerManager.handleJoin(event.player)
            event.joinMessage(null)
            if (!event.player.aero.vanished) {
                for (onlinePlayer in plugin.playerManager.players.values) {
                    Bukkit.getPlayer(onlinePlayer._id)?.apply {
                        this.sendMessage(
                            Identity.identity(event.player.uniqueId), Component.text()
                                .append(event.player.displayNameFor(this))
                                .append(Component.text(" joined the game.", NamedTextColor.YELLOW))
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            event.player.kick(Component.text("Aero / Something went horribly wrong.", NamedTextColor.RED))
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        try {
            event.player.isOp = false
            event.quitMessage(null)
            if (!event.player.aero.vanished) {
                for (onlinePlayer in plugin.playerManager.players.values) {
                    Bukkit.getPlayer(onlinePlayer._id)?.apply {
                        this.sendMessage(
                            Identity.identity(event.player.uniqueId), Component.text()
                                .append(event.player.displayNameFor(this))
                                .append(Component.text(" left the game.", NamedTextColor.YELLOW))
                        )
                    }
                }
            }
            plugin.playerManager.handleQuit(event.player)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onEntityTarget(event: EntityTargetEvent) {
        val target = event.target
        if (target is Player && target.aero.vanished) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (event.entity.aero.vanished) {
            event.deathMessage(null)
        }
    }
}