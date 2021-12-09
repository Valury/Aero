package wtf.jaren.aero.spigot.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import wtf.jaren.aero.spigot.Aero
import wtf.jaren.aero.spigot.utils.ChatUtils
import wtf.jaren.aero.spigot.utils.aero
import wtf.jaren.aero.spigot.utils.displayNameFor

class PlayerListener(private val plugin: Aero) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        try {
            plugin.playerManager.handlePreLogin(event.uniqueId)
        } catch (e: Exception) {
            e.printStackTrace()
            Bukkit.broadcast(Component.text("Aero / Something went horribly wrong when handling a AsyncPlayerPreLoginEvent.", NamedTextColor.RED), "aero.admin")
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
                            event.player.identity(), Component.text()
                                .append(event.player.displayNameFor(this))
                                .append(Component.text(" joined the game.", NamedTextColor.YELLOW))
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            event.player.kick(Component.text("Aero / Something went horribly wrong.", NamedTextColor.RED))
            Bukkit.broadcast(Component.text("Aero / Something went horribly wrong when handling a PlayerJoinEvent.", NamedTextColor.RED), "aero.admin")
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
                            event.player.identity(), Component.text()
                                .append(event.player.displayNameFor(this))
                                .append(Component.text(" left the game.", NamedTextColor.YELLOW))
                        )
                    }
                }
            }
            plugin.playerManager.handleQuit(event.player)
        } catch (e: Exception) {
            e.printStackTrace()
            Bukkit.broadcast(Component.text("Aero / Something went horribly wrong when handling a PlayerQuitEvent.", NamedTextColor.RED), "aero.admin")
        }
    }

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        event.renderer { source, sourceDisplayName, message, viewer ->
            Component.text()
                .append(if (viewer is Player) source.displayNameFor(viewer) else sourceDisplayName)
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(
                    ChatUtils.getMessageAsComponent(
                        source,
                        viewer,
                        PlainTextComponentSerializer.plainText().serialize(message)
                    )
                )
                .build()
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

    @EventHandler
    fun onPlayerCommand(event: PlayerCommandPreprocessEvent) {
        if (event.message.contains("\${jndi:", ignoreCase = true)) {
            event.isCancelled =  true;
        }
    }
}