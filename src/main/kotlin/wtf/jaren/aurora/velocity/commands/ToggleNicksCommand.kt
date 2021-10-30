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

class ToggleNicksCommand(val plugin: Aurora) : RawCommand {
    override fun execute(invocation: RawCommand.Invocation) {
        val player = invocation.source() as Player
        player.aurora.preferences.showNicks = !player.aurora.preferences.showNicks
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", player.uniqueId), Document(
                "\$set", Document("preferences.showNicks", player.aurora.preferences.showNicks)
            )
        )
        player.sendMessage(
            Component.text(
                "Successfully toggled nicks ${if (player.aurora.preferences.showNicks) "on" else "off"}.",
                NamedTextColor.GREEN
            )
        )
        player.currentServer.orElse(null)
            ?.sendPluginMessage(MinecraftChannelIdentifier.create("aurora", "sync"), SyncUtils.refreshPlayer)
    }
}