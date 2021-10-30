package wtf.jaren.aurora.velocity.commands

import com.mongodb.client.model.Filters
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bson.Document
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.utils.NumberUtils
import wtf.jaren.aurora.velocity.utils.SyncUtils

class AuroraCommand(val plugin: Aurora) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        when (invocation.arguments()[0]) {
            "reload" -> {
                plugin.guildManager.guilds.clear()
                for (onlinePlayer in plugin.server.allPlayers) {
                    try {
                        plugin.playerManager.handleJoin(onlinePlayer)
                    } catch (e: Exception) {
                        onlinePlayer.disconnect(Component.text("Aurora / Something went horribly wrong.", NamedTextColor.RED))
                    }
                }
                for (server in plugin.server.allServers) {
                    server.playersConnected.firstOrNull()?.currentServer?.orElse(null)
                        ?.sendPluginMessage(MinecraftChannelIdentifier.create("aurora", "sync"), SyncUtils.reload)
                }
                invocation.source().sendMessage(Component.text("Success!", NamedTextColor.GREEN))
                plugin.resourcePackManager.reload()
            }
            "migrate" -> {
                for (guild in plugin.database.getCollection("guilds").find(Filters.gt("balance", 0))) {
                    val leader = plugin.database.getCollection("players").find(Filters.and(
                        Filters.eq("guild._id", guild["_id"]),
                        Filters.eq("guild.rank", 0)
                    )).first()
                    if (leader == null) {
                        invocation.source().sendMessage(Component.text("Guild ${guild.getString("name")} doesn't have a leader."))
                        continue
                    }
                    plugin.database.getCollection("players").updateOne(
                        Filters.eq("_id", leader["_id"]),
                        Document("\$inc", Document("balance", guild["balance"]))
                    )
                    plugin.database.getCollection("guilds").updateOne(
                        Filters.eq("_id", guild["_id"]),
                        Document("\$set", Document("balance", 0L))
                    )
                    invocation.source().sendMessage(Component.text("${guild.getString("name")}'s balance of ${NumberUtils.formatBalance(guild.getLong("balance"))} has been transferred to ${leader.getString("name")}", NamedTextColor.AQUA))
                }
            }
        }
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("aurora.admin")
    }
}