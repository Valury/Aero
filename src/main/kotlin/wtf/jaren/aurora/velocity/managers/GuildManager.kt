package wtf.jaren.aurora.velocity.managers

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.client.model.Filters
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bson.Document
import org.bson.types.ObjectId
import wtf.jaren.aurora.shared.objects.AuroraPlayer
import wtf.jaren.aurora.shared.objects.AuroraPlayerGuild
import wtf.jaren.aurora.shared.objects.Guild
import wtf.jaren.aurora.shared.objects.GuildPermissions
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.objects.GuildInvite
import wtf.jaren.aurora.velocity.utils.SyncUtils
import wtf.jaren.aurora.velocity.utils.aurora
import wtf.jaren.aurora.velocity.utils.displayNameFor
import java.util.concurrent.TimeUnit

class GuildManager(val plugin: Aurora) {
    val guilds = HashMap<ObjectId, Guild>()
    private val collection = plugin.database.getCollection("guilds")
    private val invites = HashSet<GuildInvite>();

    fun handlePlayerJoin(player: AuroraPlayer) {
        if (player.guild != null && !guilds.containsKey(player.guild!!._id)) {
            val guild =
                Guild.fromDocument(collection.find(Filters.eq("_id", player.guild!!._id)).first()!!)
            guilds[guild._id] = guild
        }
    }

    fun handlePlayerQuit(player: Player) {
        if (player.aurora.guild == null) return
        for (serverPlayer in plugin.server.allPlayers) {
            if (serverPlayer == player) continue
            if (serverPlayer.aurora.guild?._id == player.aurora.guild!!._id) return
        }
        guilds.remove(player.aurora.guild!!._id)
    }

    fun createGuild(name: String, leader: Player): Guild {
        val guild = Guild(
            ObjectId.get(),
            name,
            false,
            10,
            ArrayList(
                listOf(
                    "Leader",
                    "Apprentice",
                    "Knight",
                    "Elder"
                )
            ),
            GuildPermissions(),
            Guild.COLOR_SCHEME
        )
        plugin.database.getCollection("guilds").insertOne(guild.toDocument())
        leader.aurora.guild = AuroraPlayerGuild(
            guild._id,
            0
        )
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", leader.uniqueId),
            Document(
                "\$set", Document(
                    "guild", Document("_id", guild._id).append("rank", 0)
                )
            )
        )
        leader.currentServer.orElse(null)
            ?.sendPluginMessage(MinecraftChannelIdentifier.create("aurora", "sync"), SyncUtils.refreshPlayer)
        return guild
    }

    fun getGuild(_id: ObjectId): Guild {
        return guilds[_id] ?: Guild.fromDocument(collection.find(Filters.eq("_id", _id)).first()!!)
    }

    fun getGuild(name: String): Guild? {
        for (guild in guilds.values) {
            if (guild.name.equals(name, true)) {
                return guild
            }
        }
        val document = plugin.database.getCollection("guilds")
            .find(Filters.eq("name", name))
            .collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
            .first()
        return if (document != null) Guild.fromDocument(document) else null
    }

    fun invitePlayer(guild: Guild, player: Player, inviter: Player): Boolean {
        if (invites.find { it.guild == guild && it.player == player } != null) {
            return false
        }
        val task = plugin.server.scheduler.buildTask(plugin) {
            invites.removeIf { it.guild == guild && it.player == player }
        }.delay(1, TimeUnit.MINUTES).schedule()
        invites.add(GuildInvite(guild, player, task))
        player.sendMessage(
            inviter.identity(),
            Component.text()
                .append(inviter.displayNameFor(player))
                .append(Component.text(" invited you to the guild ${guild.name}.", Guild.COLOR_SCHEME))
                .append(Component.newline())
                .append(Component.text("[Click here]").decoration(TextDecoration.BOLD, true))
                .append(Component.text(" to accept! You have 60 seconds to accept.", Guild.COLOR_SCHEME))
                .clickEvent(ClickEvent.runCommand("/guild join " + guild.name))
                .build()
        )
        return true
    }

    fun acceptInvite(guild: ObjectId, player: Player): Boolean {
        val invite = invites.find { it.guild._id == guild && it.player == player } ?: return false
        invite.task.cancel()
        invites.remove(invite)
        return true
    }
}