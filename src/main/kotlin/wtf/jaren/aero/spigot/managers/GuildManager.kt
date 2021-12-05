package wtf.jaren.aero.spigot.managers

import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import wtf.jaren.aero.shared.objects.AeroPlayer
import wtf.jaren.aero.shared.objects.Guild
import wtf.jaren.aero.spigot.Aero
import wtf.jaren.aero.spigot.utils.aero

class GuildManager(plugin: Aero) {
    val guilds = HashMap<ObjectId, Guild>()
    private val collection = plugin.database.getCollection("guilds")

    fun handlePlayerPreLogin(player: AeroPlayer) {
        if (player.guild != null && !guilds.containsKey(player.guild!!._id)) {
            val guild =
                Guild.fromDocument(collection.find(Filters.eq("_id", player.guild!!._id)).first()!!)
            guilds[guild._id] = guild
        }
    }

    fun handlePlayerQuit(player: Player) {
        if (player.aero.guild == null) return
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (onlinePlayer == player) continue
            if (player.aero.guild!!._id == onlinePlayer.aero.guild?._id) return
        }
        guilds.remove(player.aero.guild!!._id)
    }

    fun getGuild(_id: ObjectId): Guild {
        return guilds[_id]!!
    }
}