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

class ToggleNicksCommand(val plugin: Aero) : RawCommand {
    override fun execute(invocation: RawCommand.Invocation) {
        val player = invocation.source() as Player
        player.aero.preferences.showNicks = !player.aero.preferences.showNicks
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", player.uniqueId), Document(
                "\$set", Document("preferences.showNicks", player.aero.preferences.showNicks)
            )
        )
        player.sendMessage(
            Component.text(
                "Successfully toggled nicks ${if (player.aero.preferences.showNicks) "on" else "off"}.",
                NamedTextColor.GREEN
            )
        )
        player.currentServer.orElse(null)
            ?.sendPluginMessage(MinecraftChannelIdentifier.create("aero", "sync"), SyncUtils.refreshPlayer)
    }
}