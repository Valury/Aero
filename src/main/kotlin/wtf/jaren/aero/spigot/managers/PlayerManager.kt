package wtf.jaren.aero.spigot.managers

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.client.model.Filters
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import wtf.jaren.aero.shared.objects.AeroPlayer
import wtf.jaren.aero.spigot.Aero
import wtf.jaren.aero.spigot.utils.aero
import wtf.jaren.aero.spigot.utils.updateDisplayName
import java.util.*

class PlayerManager(private val plugin: Aero) {
    val players = HashMap<UUID, AeroPlayer>()
    private val collection = plugin.database.getCollection("players")

    fun getAeroPlayer(player: Player): AeroPlayer {
        return players[player.uniqueId]!!
    }

    fun getAeroPlayer(player: String, refreshPlayer: Boolean = false): AeroPlayer? {
        for (aeroPlayer in players.values) {
            if (aeroPlayer.name.equals(player, ignoreCase = true)) {
                if (refreshPlayer) {
                    plugin.playerManager.handlePreLogin(aeroPlayer._id)
                    plugin.playerManager.handleJoin(plugin.server.getPlayer(aeroPlayer._id)!!)
                    return getAeroPlayer(plugin.server.getPlayer(aeroPlayer._id)!!)
                }
                return aeroPlayer
            }
        }
        return AeroPlayer.fromDocument(Aero.instance.database.getCollection("players")
            .find(Filters.eq("name", player))
            .collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
            .first() ?: return null)
    }

    fun handlePreLogin(player: UUID) {
        val aeroPlayer = AeroPlayer.fromDocument(
            collection.find(Filters.eq("_id", player)).first()!!
        )
        players[player] = aeroPlayer
        plugin.guildManager.handlePlayerPreLogin(aeroPlayer)
    }

    fun handleJoin(player: Player) {
        if (!players.containsKey(player.uniqueId)) {
            plugin.logger.warning("Player " + player.name + " was missing from the cache.")
            handlePreLogin(player.uniqueId)
        }
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (!player.aero.vanished || onlinePlayer.hasPermission("aero.vanish")) {
                onlinePlayer.showPlayer(plugin, player)
            } else {
                onlinePlayer.hidePlayer(plugin, player)
            }
            if (!onlinePlayer.aero.vanished || player.hasPermission("aero.vanish")) {
                player.showPlayer(plugin, onlinePlayer)
            } else {
                player.hidePlayer(plugin, onlinePlayer)
            }
        }
        player.updateDisplayName()
    }

    fun handleQuit(player: Player) {
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (Bukkit.getPlayer(player.uniqueId) == null) {
                plugin.guildManager.handlePlayerQuit(player)
                players.remove(player.uniqueId)
            }
        }, 5L)
    }
}