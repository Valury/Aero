package wtf.jaren.aurora.velocity.managers

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bson.Document
import wtf.jaren.aurora.shared.objects.AuroraPlayer
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.enums.PartyRemoveReason
import wtf.jaren.aurora.velocity.enums.PunishmentType
import wtf.jaren.aurora.velocity.objects.CachedMute
import wtf.jaren.aurora.velocity.objects.Punishment
import wtf.jaren.aurora.velocity.objects.Punishment.Companion.fromDocument
import java.util.*

class PunishmentManager(private val plugin: Aurora) {
    private val database: MongoDatabase = plugin.database
    private val collection: MongoCollection<Document> = database.getCollection("punishments")
    val currentMutes = HashMap<Player, CachedMute>()

    fun getActivePunishment(player: AuroraPlayer, type: PunishmentType): Punishment? {
        val serverPlayer = plugin.server.getPlayer(player._id).orElse(null)
        return if (serverPlayer != null) {
            getActivePunishment(serverPlayer, type)
        } else {
            getActivePunishment(player._id, type)
        }
    }
    fun getActivePunishment(player: Player, type: PunishmentType): Punishment? {
        if (type === PunishmentType.MUTE) {
            return currentMutes[player]?.punishment
        }
        return getActivePunishment(player.uniqueId, player.remoteAddress.address.hostAddress, type)
    }

    fun getActivePunishment(player: UUID, type: PunishmentType): Punishment? {
        val documents = collection.find(
            Filters.and(
                Filters.eq("player", player),
                Filters.eq("type", type.name),
                Filters.eq("revoked", false),
                Filters.or(
                    Filters.gt("expires", Date(System.currentTimeMillis())),
                    Filters.eq("expires", null)
                )
            )
        )
        val document = documents.first()
        return if (document != null) fromDocument(document) else null
    }

    fun getActivePunishment(player: UUID, ip: String, type: PunishmentType): Punishment? {
        val documents = collection.find(
            Filters.and(
                Filters.or(
                    Filters.eq("player", player),
                    Filters.eq("ip", ip)
                ),
                Filters.eq("type", type.name),
                Filters.eq("revoked", false),
                Filters.or(
                    Filters.gt("expires", Date(System.currentTimeMillis())),
                    Filters.eq("expires", null)
                )
            )
        )
        val document = documents.first()
        return if (document != null) fromDocument(document) else null
    }

    fun applyPunishment(punishment: Punishment) {
        collection.updateMany(
            Document("player", punishment.player.uuid)
                .append("type", punishment.type.name)
                .append(
                    "expires", Document(
                        "\$gte", Date(System.currentTimeMillis())
                    )
                ),
            Document("\$set", Document("revoked", true))
        )
        collection.insertOne(punishment.toDocument())
        val broadcast = Component.text()
            .append(Component.text(punishment.operatorName, NamedTextColor.LIGHT_PURPLE))
            .append(Component.text(" " + punishment.type.pastTense + " ", NamedTextColor.GRAY))
            .append(Component.text(punishment.player.name, NamedTextColor.LIGHT_PURPLE))
            .build()
        plugin.server.consoleCommandSource.sendMessage(broadcast)
        for (player in plugin.server.allPlayers) {
            if (player.hasPermission("aurora." + punishment.type.name.lowercase())) {
                player.sendMessage(broadcast)
            }
        }
        when (punishment.type) {
            PunishmentType.BAN, PunishmentType.KICK -> {
                plugin.server.getPlayer(punishment.player.uuid).ifPresent { player ->
                    plugin.partyManager.getPlayerParty(player)?.removeMember(player, PartyRemoveReason.PUNISHED)
                    player.disconnect(punishment.getMessage(true))
                }
                if (punishment.ip != null) {
                    for (player in plugin.server.allPlayers) {
                        if (player.remoteAddress.address.hostAddress == punishment.ip) {
                            plugin.partyManager.getPlayerParty(player)?.removeMember(player, PartyRemoveReason.PUNISHED)
                            player.disconnect(punishment.getMessage(true))
                        }
                    }
                }
            }
            PunishmentType.MUTE -> {
                plugin.server.getPlayer(punishment.player.uuid).ifPresent { player ->
                    currentMutes[player] = CachedMute(plugin, punishment)
                    plugin.partyManager.getPlayerParty(player)?.removeMember(player, PartyRemoveReason.PUNISHED)
                }
            }
            PunishmentType.WARN -> {
                plugin.server.getPlayer(punishment.player.uuid)
                    .ifPresent { player -> player.sendMessage(punishment.getMessage(false)) }
            }
        }
    }

    fun revokePunishment(punishment: Punishment, operator: String) {
        collection.updateOne(
            Document("_id", punishment._id), Document(
                "\$set", Document(
                    "revoked", true
                )
            )
        )
        val broadcast = Component.text()
            .append(Component.text(operator, NamedTextColor.LIGHT_PURPLE))
            .append(Component.text(" un" + punishment.type.pastTense + " ", NamedTextColor.GRAY))
            .append(Component.text(punishment.player.name, NamedTextColor.LIGHT_PURPLE))
            .build()
        plugin.server.consoleCommandSource.sendMessage(broadcast)
        for (player in plugin.server.allPlayers) {
            if (player.hasPermission("aurora.un" + punishment.type.name.lowercase())) {
                player.sendMessage(broadcast)
            }
        }
        if (punishment.type === PunishmentType.MUTE) {
            plugin.server.getPlayer(punishment.player.uuid).ifPresent { key: Player ->
                currentMutes[key]?.task?.cancel()
                currentMutes.remove(key)
            }
        }
    }
}