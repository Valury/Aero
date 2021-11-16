package wtf.jaren.aurora.spigot.managers

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.client.model.Filters
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import wtf.jaren.aurora.shared.objects.AuroraPlayer
import wtf.jaren.aurora.spigot.Aurora
import wtf.jaren.aurora.spigot.utils.aurora
import wtf.jaren.aurora.spigot.utils.updateDisplayName
import java.util.*
import kotlin.collections.HashMap

class PlayerManager(private val plugin: Aurora) {
    val players = HashMap<UUID, AuroraPlayer>()
    private val collection = plugin.database.getCollection("players")

    fun getAuroraPlayer(player: Player): AuroraPlayer {
        return players[player.uniqueId]!!
    }

    fun getAuroraPlayer(player: String, refreshPlayer: Boolean = false): AuroraPlayer? {
        for (auroraPlayer in players.values) {
            if (auroraPlayer.name.equals(player, ignoreCase = true)) {
                if (refreshPlayer) {
                    plugin.playerManager.handlePreLogin(auroraPlayer._id)
                    plugin.playerManager.handleJoin(plugin.server.getPlayer(auroraPlayer._id)!!)
                    return getAuroraPlayer(plugin.server.getPlayer(auroraPlayer._id)!!)
                }
                return auroraPlayer
            }
        }
        return AuroraPlayer.fromDocument(Aurora.instance.database.getCollection("players")
            .find(Filters.eq("name", player))
            .collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
            .first() ?: return null)
    }

    fun handlePreLogin(player: UUID) {
        val auroraPlayer = AuroraPlayer.fromDocument(
            collection.find(Filters.eq("_id", player)).first()!!
        )
        players[player] = auroraPlayer
        plugin.guildManager.handlePlayerPreLogin(auroraPlayer)
    }

    fun handleJoin(player: Player) {
        if (!players.containsKey(player.uniqueId)) {
            plugin.logger.warning("Player " + player.name + " was missing from the cache.")
            handlePreLogin(player.uniqueId)
        }
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (!player.aurora.vanished || onlinePlayer.hasPermission("aurora.vanish")) {
                onlinePlayer.showPlayer(plugin, player)
            } else {
                onlinePlayer.hidePlayer(plugin, player)
            }
            if (!onlinePlayer.aurora.vanished || player.hasPermission("aurora.vanish")) {
                player.showPlayer(plugin, onlinePlayer)
            } else {
                player.hidePlayer(plugin, onlinePlayer)
            }
        }
        player.updateDisplayName()
    }

    fun handleQuit(player: Player) {
        plugin.guildManager.handlePlayerQuit(player)
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (Bukkit.getPlayer(player.uniqueId) == null) {
                players.remove(player.uniqueId)
            }
        }, 5L);
    }
}