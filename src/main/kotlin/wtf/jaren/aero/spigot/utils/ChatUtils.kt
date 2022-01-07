package wtf.jaren.aero.spigot.utils

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.regex.Pattern

object ChatUtils {
    fun getMessageAsComponent(source: CommandSender, viewer: Audience, text: String): TextComponent {
        var newText = text
        val builder = LegacyComponentSerializer.builder()
        if (source.hasPermission("aero.chat.links")) {
            builder.extractUrls()
        }
        if (source.hasPermission("aero.chat.colors")) {
            builder.character(LegacyComponentSerializer.AMPERSAND_CHAR)
            builder.hexCharacter(LegacyComponentSerializer.HEX_CHAR)
        }
        if (source.hasPermission("aero.chat.placeholders")) {
            newText = newText
                .replace("%player%", if (viewer is Player) viewer.name else "Server")

        }
        return builder.build().deserialize(newText)
    }
    fun convertUnicodeToPlainText(text: String): String {
        return text
            .replace("", "[PLAYER]")
            .replace("", "[RECRUIT]")
            .replace("", "[KNIGHT]")
            .replace("", "[GLORIOUS]")
            .replace("", "[DROPLET]")
            .replace("", "[BUILDER]")
            .replace("", "[HELPER]")
            .replace("", "[MOD]")
            .replace("", "[CONTENT]")
            .replace("", "[ADMIN]")
            .replace("", "[DEVELOPER]")
            .replace("", "[OWNER]")
    }

    private val COLOR_PATTERN: Pattern = Pattern.compile("§([0-9a-f]|#[0-9a-f]{6})")
    fun convertUnicodePrefixToColoredText(text: String): String {
        val prefix = convertUnicodeToPlainText(text)
        if (!prefix.startsWith("§f")) {
            return prefix
        }
        val matcher = COLOR_PATTERN.matcher(prefix.substring(2))
        if (!matcher.find()) {
            return prefix
        }
        val color = "§${matcher.group(1)}"
        val newPrefix = "${color}${prefix.substring(2).replace(color, "")}"
        // Downsample hex colors
        return downsampleHexColors(newPrefix)
    }

    fun downsampleHexColors(string: String): String {
        return LegacyComponentSerializer.legacySection().serialize(
            LegacyComponentSerializer.builder().hexColors().build().deserialize(string)
        ).replace("§1", "§9")
    }

    fun downsampleHexColorsAmpersand(string: String): String {
        return LegacyComponentSerializer.legacyAmpersand().serialize(
            LegacyComponentSerializer.builder().character('&').hexColors().build().deserialize(string)
        ).replace("&1", "&9")
    }
}