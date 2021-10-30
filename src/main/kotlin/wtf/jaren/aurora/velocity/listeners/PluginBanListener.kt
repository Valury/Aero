package wtf.jaren.aurora.velocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.proxy.ServerConnection
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.enums.PunishmentType
import wtf.jaren.aurora.velocity.managers.PunishmentManager
import wtf.jaren.aurora.velocity.objects.PartialPlayer
import wtf.jaren.aurora.velocity.objects.Punishment
import java.util.*

class PluginBanListener(plugin: Aurora) {
    private val punishmentManager: PunishmentManager = plugin.punishmentManager

    @Subscribe
    fun onPluginMessage(event: PluginMessageEvent) {
        if (event.identifier.id == "aurora:acb" && event.source is ServerConnection) {
            val connection = event.source as ServerConnection
            val punishment = Punishment(
                null,
                PunishmentType.BAN,
                PartialPlayer(connection.player.uniqueId, connection.player.username),
                null,
                UUID.fromString("7f901247-4244-4ef8-84de-b17854551a77"),
                "Blacklisted Modifications",
                Date(System.currentTimeMillis() + 604800000),
                false
            )
            punishmentManager.applyPunishment(punishment)
        }
    }

}