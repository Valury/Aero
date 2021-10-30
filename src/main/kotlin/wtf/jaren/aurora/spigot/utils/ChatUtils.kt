package wtf.jaren.aurora.spigot.utils

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ChatUtils {
    fun getMessageAsComponent(source: CommandSender, viewer: Audience, text: String): TextComponent {
        var newText = text
        val builder = LegacyComponentSerializer.builder()
        if (source.hasPermission("aurora.chat.links")) {
            builder.extractUrls()
        }
        if (source.hasPermission("aurora.chat.colors")) {
            builder.character(LegacyComponentSerializer.AMPERSAND_CHAR)
            builder.hexCharacter(LegacyComponentSerializer.HEX_CHAR)
        }
        if (source.hasPermission("aurora.chat.placeholders")) {
            newText = newText
                .replace("%player%", if (viewer is Player) viewer.name else "Server")

        }
        return builder.build().deserialize(newText)
    }
}