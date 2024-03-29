package wtf.jaren.aero.velocity.commands

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.utils.NumberUtils
import wtf.jaren.aero.velocity.utils.SyncUtils

class AeroCommand(val plugin: Aero) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        when (invocation.arguments()[0]) {
            "reload" -> {
                plugin.guildManager.guilds.clear()
                for (onlinePlayer in plugin.server.allPlayers) {
                    try {
                        plugin.playerManager.handleJoin(onlinePlayer)
                    } catch (e: Exception) {
                        onlinePlayer.disconnect(Component.text("Aero / Something went horribly wrong.", NamedTextColor.RED))
                    }
                }
                for (server in plugin.server.allServers) {
                    server.playersConnected.firstOrNull()?.currentServer?.orElse(null)
                        ?.sendPluginMessage(MinecraftChannelIdentifier.create("aero", "sync"), SyncUtils.reload)
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
                        Updates.inc("balance", guild["balance"] as Number)
                    )
                    plugin.database.getCollection("guilds").updateOne(
                        Filters.eq("_id", guild["_id"]),
                        Updates.set("balance", 0L)
                    )
                    invocation.source().sendMessage(Component.text("${guild.getString("name")}'s balance of ${NumberUtils.formatBalance(guild.getLong("balance"))} has been transferred to ${leader.getString("name")}", NamedTextColor.AQUA))
                }
            }
        }
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("aero.admin")
    }
}