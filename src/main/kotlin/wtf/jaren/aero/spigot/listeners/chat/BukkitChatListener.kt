package wtf.jaren.aero.spigot.listeners.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import wtf.jaren.aero.spigot.Aero
import wtf.jaren.aero.spigot.utils.ChatUtils
import wtf.jaren.aero.spigot.utils.aeroDisplayName
import wtf.jaren.aero.spigot.utils.displayNameFor

class BukkitChatListener(val plugin: Aero) : Listener {
    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        event.isCancelled = true
        for (recipient in event.recipients) {
            plugin.adventure.player(recipient).sendMessage(
                Component.text()
                    .append(event.player.displayNameFor(recipient))
                    .append(Component.text(": ", NamedTextColor.WHITE))
                    .append(
                        ChatUtils.getMessageAsComponent(
                            event.player,
                            plugin.adventure.player(recipient),
                            event.message
                        )
                    )
                    .build()
            )
        }
        plugin.adventure.console().sendMessage(
            Component.text()
                .append(event.player.aeroDisplayName)
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(
                    ChatUtils.getMessageAsComponent(
                        event.player,
                        plugin.adventure.console(),
                        event.message
                    )
                )
                .build()
        )
    }
}