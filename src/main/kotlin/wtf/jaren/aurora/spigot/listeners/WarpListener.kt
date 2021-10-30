package wtf.jaren.aurora.spigot.listeners

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.messaging.PluginMessageListener
import wtf.jaren.aurora.spigot.Aurora
import wtf.jaren.aurora.spigot.records.PendingWarp
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.*

class WarpListener : PluginMessageListener, Listener {
    var pendingWarps = HashMap<UUID, PendingWarp>()

    @EventHandler(priority = EventPriority.HIGH)
    fun onJoin(event: PlayerJoinEvent) {
        if (pendingWarps.containsKey(event.player.uniqueId)) {
            val pendingWarp = pendingWarps[event.player.uniqueId]!!
            pendingWarp.task.cancel()
            event.player.teleport(pendingWarp.player)
            pendingWarps.remove(event.player.uniqueId)
        }
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        val input = DataInputStream(ByteArrayInputStream(message))
        val size = input.readInt()
        for (i in 0 until size) {
            val uuid = UUID.fromString(input.readUTF())
            val warpPlayer = Bukkit.getPlayer(uuid)
            if (warpPlayer != null) {
                if (player !== warpPlayer) {
                    warpPlayer.teleport(player)
                }
            } else {
                val bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(
                    Aurora.instance,
                    Runnable { pendingWarps.remove(uuid) },
                    100L
                )
                pendingWarps[uuid] = PendingWarp(player, bukkitTask)
            }
        }
    }
}