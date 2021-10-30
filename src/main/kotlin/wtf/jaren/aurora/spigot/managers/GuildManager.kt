package wtf.jaren.aurora.spigot.managers

import com.mongodb.client.model.Filters
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import wtf.jaren.aurora.shared.objects.AuroraPlayer
import wtf.jaren.aurora.shared.objects.Guild
import wtf.jaren.aurora.spigot.Aurora
import wtf.jaren.aurora.spigot.utils.aurora

class GuildManager(plugin: Aurora) {
    val guilds = HashMap<ObjectId, Guild>()
    private val collection = plugin.database.getCollection("guilds")

    fun handlePlayerPreLogin(player: AuroraPlayer) {
        if (player.guild != null && !guilds.containsKey(player.guild!!._id)) {
            val guild =
                Guild.fromDocument(collection.find(Filters.eq("_id", player.guild!!._id)).first()!!)
            guilds[guild._id] = guild
        }
    }

    fun handlePlayerQuit(player: Player) {
        if (player.aurora.guild == null) return
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (onlinePlayer == player) continue
            if (player.aurora.guild!!._id == onlinePlayer.aurora.guild?._id) return
        }
        guilds.remove(player.aurora.guild!!._id)
    }

    fun getGuild(_id: ObjectId): Guild {
        return guilds[_id]!!
    }
}