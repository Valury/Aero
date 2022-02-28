package wtf.jaren.aero.velocity.commands

import com.mongodb.client.model.Filters
import com.velocitypowered.api.command.RawCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bson.Document
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.utils.SyncUtils
import wtf.jaren.aero.velocity.utils.aero

class NickCommand(val plugin: Aero) : RawCommand {
    override fun execute(invocation: RawCommand.Invocation) {
        val player = invocation.source() as Player
        var newNick = invocation.arguments()
        if (newNick.isEmpty()) {
            player.sendMessage(Component.text("Usage: /nick <nick>", NamedTextColor.RED))
            return
        }
        for (c in newNick) {
            if (!(c > 32.toChar() && c < 127.toChar())) {
                player.sendMessage(Component.text("That nick contains illegal characters.", NamedTextColor.RED))
                return
            }
        }
        if (newNick == "reset") {
            if (player.aero.disguise == null) {
                player.aero.nick = null
                plugin.database.getCollection("players").updateOne(
                    Filters.eq("_id", player.uniqueId), Document(
                        "\$unset", Document("nick", true)
                    )
                )
            } else {
                player.aero.disguise!!.nick = null
                plugin.database.getCollection("players").updateOne(
                    Filters.eq("_id", player.uniqueId), Document(
                        "\$unset", Document("disguise.nick", true)
                    )
                )
            }
            player.currentServer.orElse(null)
                ?.sendPluginMessage(MinecraftChannelIdentifier.create("aero", "sync"), SyncUtils.refreshPlayer)
            player.sendMessage(Component.text("Success!", NamedTextColor.GREEN))
            return
        }
        if (!player.hasPermission("aero.nick.pure")) {
            newNick = "~$newNick"
        }
        if (newNick.length > 16) {
            player.sendMessage(Component.text("That nick is too long.", NamedTextColor.RED))
        }
        val existing = plugin.database.getCollection("players").find(Filters.eq("nick", newNick)).first()
        if (existing != null) {
            player.sendMessage(Component.text("That nick is already in use.", NamedTextColor.RED))
            return
        }
        if (player.aero.disguise == null) {
            player.aero.nick = newNick
            plugin.database.getCollection("players").updateOne(
                Filters.eq("_id", player.uniqueId), Document(
                    "\$set", Document("nick", newNick)
                )
            )
        } else {
            player.aero.disguise!!.nick = newNick
            plugin.database.getCollection("players").updateOne(
                Filters.eq("_id", player.uniqueId), Document(
                    "\$set", Document("disguise.nick", newNick)
                )
            )
        }
        player.sendMessage(Component.text("Success!", NamedTextColor.GREEN))
        player.currentServer.orElse(null)
            ?.sendPluginMessage(MinecraftChannelIdentifier.create("aero", "sync"), SyncUtils.refreshPlayer)
    }

    override fun hasPermission(invocation: RawCommand.Invocation): Boolean {
        return invocation.source().hasPermission("aero.nick")
    }
}