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

class VanishCommand(val plugin: Aero) : RawCommand {
    override fun execute(invocation: RawCommand.Invocation) {
        val player = invocation.source() as Player
        // val arguments = invocation.arguments()
        player.aero.vanished = !player.aero.vanished
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", player.uniqueId),
            Document("\$set", Document("vanished", player.aero.vanished))
        )
        player.currentServer.orElse(null)
            ?.sendPluginMessage(MinecraftChannelIdentifier.create("aero", "sync"), SyncUtils.refreshPlayer)
        player.sendMessage(Component.text("You " + (if (player.aero.vanished) "vanished" else "unvanished"), NamedTextColor.GREEN))
    }

    override fun hasPermission(invocation: RawCommand.Invocation): Boolean {
        return invocation.source().hasPermission("aero.vanish")
    }
}