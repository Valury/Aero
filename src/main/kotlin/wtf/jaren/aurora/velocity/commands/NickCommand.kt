package wtf.jaren.aurora.velocity.commands

import com.mongodb.client.model.Filters
import com.velocitypowered.api.command.RawCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bson.Document
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.utils.SyncUtils
import wtf.jaren.aurora.velocity.utils.aurora

class NickCommand(val plugin: Aurora) : RawCommand {
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
            if (player.aurora.disguise == null) {
                player.aurora.nick = null
                plugin.database.getCollection("players").updateOne(
                    Filters.eq("_id", player.uniqueId), Document(
                        "\$unset", Document("nick", true)
                    )
                )
            } else {
                player.aurora.disguise!!.nick = null
                plugin.database.getCollection("players").updateOne(
                    Filters.eq("_id", player.uniqueId), Document(
                        "\$unset", Document("disguise.nick", true)
                    )
                )
            }
            player.currentServer.orElse(null)
                ?.sendPluginMessage(MinecraftChannelIdentifier.create("aurora", "sync"), SyncUtils.refreshPlayer)
            player.sendMessage(Component.text("Success!", NamedTextColor.GREEN))
            return
        }
        if (!player.hasPermission("aurora.nick.pure")) {
            newNick = "~$newNick"
        }
        val existing = plugin.database.getCollection("players").find(Filters.eq("nick", newNick)).first()
        if (existing != null) {
            player.sendMessage(Component.text("That nick is already in use.", NamedTextColor.RED))
            return
        }
        if (player.aurora.disguise == null) {
            player.aurora.nick = newNick
            plugin.database.getCollection("players").updateOne(
                Filters.eq("_id", player.uniqueId), Document(
                    "\$set", Document("nick", newNick)
                )
            )
        } else {
            player.aurora.disguise!!.nick = newNick
            plugin.database.getCollection("players").updateOne(
                Filters.eq("_id", player.uniqueId), Document(
                    "\$set", Document("disguise.nick", newNick)
                )
            )
        }
        player.sendMessage(Component.text("Success!", NamedTextColor.GREEN))
        player.currentServer.orElse(null)
            ?.sendPluginMessage(MinecraftChannelIdentifier.create("aurora", "sync"), SyncUtils.refreshPlayer)
    }

    override fun hasPermission(invocation: RawCommand.Invocation): Boolean {
        return invocation.source().hasPermission("aurora.nick")
    }
}