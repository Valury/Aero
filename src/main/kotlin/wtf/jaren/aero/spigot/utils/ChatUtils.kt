package wtf.jaren.aero.spigot.utils

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.regex.Pattern

object ChatUtils {
    val colorsToLegacy = mapOf(
        NamedTextColor.BLACK to '0',
        NamedTextColor.DARK_BLUE to '1',
        NamedTextColor.DARK_GREEN to '2',
        NamedTextColor.DARK_AQUA to '3',
        NamedTextColor.DARK_RED to '4',
        NamedTextColor.DARK_PURPLE to '5',
        NamedTextColor.GOLD to '6',
        NamedTextColor.GRAY to '7',
        NamedTextColor.DARK_GRAY to '8',
        NamedTextColor.BLUE to '9',
        NamedTextColor.GREEN to 'a',
        NamedTextColor.AQUA to 'b',
        NamedTextColor.RED to 'c',
        NamedTextColor.LIGHT_PURPLE to 'd',
        NamedTextColor.YELLOW to 'e',
        NamedTextColor.WHITE to 'f',
    )
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
    private val HEX_PATTERN: Pattern = Pattern.compile("§#[0-9a-f]{6}")
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
        var newString = string
        val matcher = HEX_PATTERN.matcher(string)
        while (matcher.find()) {
            val code = matcher.group()
            val color = TextColor.color(code.substring(2).toInt(16))
            newString = newString.replace(code, "§${colorsToLegacy[NamedTextColor.nearestTo(color)]}")
        }
        return newString
    }
}