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

class VanishCommand(val plugin: Aurora) : RawCommand {
    override fun execute(invocation: RawCommand.Invocation) {
        val player = invocation.source() as Player
        // val arguments = invocation.arguments()
        player.aurora.vanished = !player.aurora.vanished
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", player.uniqueId),
            Document("\$set", Document("vanished", player.aurora.vanished))
        )
        player.currentServer.orElse(null)
            ?.sendPluginMessage(MinecraftChannelIdentifier.create("aurora", "sync"), SyncUtils.refreshPlayer)
        player.sendMessage(Component.text("You " + (if (player.aurora.vanished) "vanished" else "unvanished"), NamedTextColor.GREEN))
    }

    override fun hasPermission(invocation: RawCommand.Invocation): Boolean {
        return invocation.source().hasPermission("aurora.vanish")
    }
}