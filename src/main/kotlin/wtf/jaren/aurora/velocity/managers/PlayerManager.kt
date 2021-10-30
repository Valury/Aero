package wtf.jaren.aurora.velocity.managers

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.velocitypowered.api.proxy.Player
import org.bson.Document
import wtf.jaren.aurora.shared.objects.AuroraPlayer
import wtf.jaren.aurora.shared.objects.AuroraPlayerPreferences
import wtf.jaren.aurora.velocity.Aurora
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class PlayerManager(val plugin: Aurora) {
    private val auroraPlayers = HashMap<Player, AuroraPlayer>()
    private val collection: MongoCollection<Document> = plugin.database.getCollection("players")
    private val disguisedPlayers = HashSet<Player>()

    fun handleJoin(player: Player) {
        val playerDocument = collection.find(Filters.eq("_id", player.uniqueId)).first()
        lateinit var auroraPlayer: AuroraPlayer
        if (playerDocument != null) {
            auroraPlayer = AuroraPlayer.fromDocument(playerDocument)
        } else {
            auroraPlayer = AuroraPlayer(
                player.uniqueId,
                player.remoteAddress.address.hostAddress,
                player.username,
                null,
                null,
                AuroraPlayerPreferences(),
                0,
                false,
                null
            )
            collection.insertOne(auroraPlayer.toDocument())
        }
        if (auroraPlayer.disguise != null) {
            disguisedPlayers.add(player)
        }
        auroraPlayers[player] = auroraPlayer
        plugin.guildManager.handlePlayerJoin(auroraPlayer)
    }

    fun handleQuit(player: Player) {
        plugin.guildManager.handlePlayerQuit(player)
        auroraPlayers.remove(player)
        disguisedPlayers.remove(player)
    }

    fun getAuroraPlayer(player: Player): AuroraPlayer {
        return auroraPlayers[player]!!
    }

    fun isDisguised(player: Player): Boolean {
        return disguisedPlayers.contains(player)
    }
}