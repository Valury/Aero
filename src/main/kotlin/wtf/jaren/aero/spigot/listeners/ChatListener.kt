package wtf.jaren.aero.spigot.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import wtf.jaren.aero.spigot.utils.ChatUtils
import wtf.jaren.aero.spigot.utils.displayNameFor

class ChatListener : Listener {
    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        event.renderer { source, sourceDisplayName, message, viewer ->
            Component.text()
                .append(if (viewer is Player) source.displayNameFor(viewer) else sourceDisplayName)
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(
                    ChatUtils.getMessageAsComponent(
                        source,
                        viewer,
                        PlainTextComponentSerializer.plainText().serialize(message)
                    )
                )
                .build()
        }
    }
}