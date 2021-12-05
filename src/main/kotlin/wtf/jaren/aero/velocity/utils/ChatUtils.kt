package wtf.jaren.aero.velocity.utils

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.model.user.User
import wtf.jaren.aero.velocity.Aero

object ChatUtils {
    fun getMessageAsComponent(source: CommandSource, text: String): TextComponent {
        val builder = LegacyComponentSerializer.builder()
        if (source.hasPermission("aero.chat.links")) {
            builder.extractUrls()
        }
        if (source.hasPermission("aero.chat.colors")) {
            builder.character(LegacyComponentSerializer.AMPERSAND_CHAR)
            builder.hexCharacter(LegacyComponentSerializer.HEX_CHAR)
        }
        return builder.build().deserialize(text)
    }

    fun getMessageAsComponent(source: User, text: String): TextComponent {
        val builder = LegacyComponentSerializer.builder()
        if (source.cachedData.permissionData.checkPermission("aero.chat.links").asBoolean()) {
            builder.extractUrls()
        }
        if (source.cachedData.permissionData.checkPermission("aero.chat.colors").asBoolean()) {
            builder.character(LegacyComponentSerializer.AMPERSAND_CHAR)
            builder.hexCharacter(LegacyComponentSerializer.HEX_CHAR)
        }
        return builder.build().deserialize(text)
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

    fun escapeDiscordMarkdown(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("*", "\\*")
            .replace("_", "\\_")
            .replace("`", "\\`")
            .replace("~", "\\~")
            .replace("@", "\\@")
    }

    fun process(source: CommandSource, text: String): String? {
        if (Aero.instance.isProd && !StringUtils.isAscii(text)) {
            source.sendMessage(Component.text("That message contains illegal characters.", NamedTextColor.RED))
            return null
        }
        var newText = text
        if (source.hasPermission("aero.chat.emotes")) {
            newText = replaceEmotes(newText)
        }
        return newText
    }

    private fun replaceEmotes(text: String): String {
        return text
            .replace("Kappa", "\uE12B")
    }
}