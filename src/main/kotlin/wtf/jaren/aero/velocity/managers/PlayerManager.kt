package wtf.jaren.aero.velocity.managers

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.velocitypowered.api.proxy.Player
import org.bson.Document
import wtf.jaren.aero.shared.objects.AeroPlayer
import wtf.jaren.aero.shared.objects.AeroPlayerPreferences
import wtf.jaren.aero.velocity.Aero

class PlayerManager(val plugin: Aero) {
    private val aeroPlayers = HashMap<Player, AeroPlayer>()
    private val collection: MongoCollection<Document> = plugin.database.getCollection("players")
    private val disguisedPlayers = HashSet<Player>()

    fun handleJoin(player: Player) {
        val playerDocument = collection.find(Filters.eq("_id", player.uniqueId)).first()
        lateinit var aeroPlayer: AeroPlayer
        if (playerDocument != null) {
            aeroPlayer = AeroPlayer.fromDocument(playerDocument)
        } else {
            aeroPlayer = AeroPlayer(
                player.uniqueId,
                player.remoteAddress.address.hostAddress,
                player.username,
                null,
                null,
                AeroPlayerPreferences(),
                0,
                false,
                null
            )
            collection.insertOne(aeroPlayer.toDocument())
        }
        if (aeroPlayer.disguise != null) {
            disguisedPlayers.add(player)
        }
        if (aeroPlayer.ip != player.remoteAddress.address.hostAddress) {
            collection.updateOne(
                Filters.eq("_id", player.uniqueId),
                Updates.set("ip", player.remoteAddress.address.hostAddress)
            )
        }
        aeroPlayers[player] = aeroPlayer
        plugin.guildManager.handlePlayerJoin(aeroPlayer)
    }

    fun handleQuit(player: Player) {
        plugin.guildManager.handlePlayerQuit(player)
        aeroPlayers.remove(player)
        disguisedPlayers.remove(player)
    }

    fun getAeroPlayer(player: Player): AeroPlayer {
        return aeroPlayers[player]!!
    }

    fun isDisguised(player: Player): Boolean {
        return disguisedPlayers.contains(player)
    }
}