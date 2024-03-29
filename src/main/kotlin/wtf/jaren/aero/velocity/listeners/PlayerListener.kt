package wtf.jaren.aero.velocity.listeners

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.GameProfileRequestEvent
import com.velocitypowered.api.util.GameProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aero.shared.objects.AeroPlayer
import wtf.jaren.aero.shared.objects.Guild
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.enums.PunishmentType
import wtf.jaren.aero.velocity.objects.CachedMute
import wtf.jaren.aero.velocity.utils.aero
import java.util.*
import java.util.concurrent.TimeUnit

class PlayerListener(private val plugin: Aero) {
    private val punishmentManager = plugin.punishmentManager

    @Subscribe
    fun onGameProfileRequest(event: GameProfileRequestEvent) {
        val document = plugin.database.getCollection("players").find(Filters.eq("_id", event.originalProfile.id)).first()
            ?: return
        val player = AeroPlayer.fromDocument(document)
        if (player.name != event.originalProfile.name) {
            plugin.database.getCollection("players").updateOne(
                Filters.eq("_id", event.originalProfile.id),
                Updates.set("name", event.originalProfile.name)
            )
        }
        val disguise = player.disguise
        if (disguise != null) {
            if (disguise.name != null) {
                event.gameProfile = event.gameProfile.withName(disguise.name)
            }
            if (disguise.properties != null) {
                event.gameProfile = event.gameProfile.withProperties(
                    disguise.properties.map { GameProfile.Property(it.name, it.value, it.signature) }
                )
            }
        }
    }
    @Subscribe
    fun onLogin(event: LoginEvent) {
        try {
            val player = event.player
            plugin.playerManager.handleJoin(player)
            val activeBan = punishmentManager.getActivePunishment(player, PunishmentType.BAN)
            if (!plugin.isProd || !plugin.isStable) {
                if (player.uniqueId != UUID.fromString("84d63091-85e2-4051-9fb4-890af97f237d")) {
                    event.result = ResultedEvent.ComponentResult.denied(Component.text("This server is currently in maintenance mode.", NamedTextColor.RED))
                    return
                }
            }
            if (activeBan != null) {
                event.result = ResultedEvent.ComponentResult.denied(activeBan.getMessage(true))
                return
            }
            val activeMute = punishmentManager.getActivePunishment(
                player.uniqueId,
                player.remoteAddress.address.hostAddress,
                PunishmentType.MUTE
            )
            activeMute?.let { punishment -> punishmentManager.currentMutes[player] = CachedMute(plugin, punishment) }
            plugin.server.scheduler.buildTask(plugin) { plugin.resourcePackManager.sendResourcePackTo(player) }
                .delay(500, TimeUnit.MILLISECONDS).schedule()
            val playerGuild = player.aero.guild
            if (playerGuild != null) {
                val guild = plugin.guildManager.getGuild(playerGuild._id)
                if (guild.name == null && guild.permissions.canRename(player.aero)) {
                    player.sendMessage(
                        Component.text()
                            .append(Guild.PREFIX)
                            .append(Component.text("Your guild's name was reset by an admin. Please set a new name by running /g rename (name)", Guild.COLOR_SCHEME))
                            .build()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            event.result = ResultedEvent.ComponentResult.denied(Component.text("Aero / Something went horribly wrong.", NamedTextColor.RED))
        }
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        val player = event.player
        plugin.playerManager.handleQuit(event.player)
        punishmentManager.currentMutes[player]?.task?.cancel()
        punishmentManager.currentMutes.remove(player)
    }

}