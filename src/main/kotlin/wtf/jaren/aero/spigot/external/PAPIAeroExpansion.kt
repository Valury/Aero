package wtf.jaren.aero.spigot.external

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.placeholderapi.expansion.Relational
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import wtf.jaren.aero.spigot.Aero
import wtf.jaren.aero.spigot.utils.*

class PAPIAeroExpansion(val plugin: Aero) : PlaceholderExpansion(), Relational {
    override fun getIdentifier(): String {
        return "aero"
    }

    override fun getAuthor(): String {
        return "Jaren"
    }

    override fun getVersion(): String {
        return "1.0"
    }

    override fun onPlaceholderRequest(player: Player, identifier: String): String? {
        if (identifier == "server") {
            return Aero.instance.serverName
        }
        if (identifier == "prefix") {
            return (if (player.aero.vanished) "&7[V] " else "") + player.prefix.replace('§', '&')
        }
        if (identifier == "suffix") {
            return player.suffix.replace('§', '&')
        }
        if (identifier == "nick") {
            return player.aero.effectiveNick ?: player.name
        }
        return null
    }

    override fun onPlaceholderRequest(one: Player, two: Player, identifier: String): String? {
        if (identifier == "prefix_tab" || identifier == "prefix_tag") {
            var prefix = two.prefixFor(one).replace('§', '&')
            if (one.actualProtocolVersion >= 393) {
                when (two.group) {
                    "content" -> {
                        prefix = plugin.prefixAnimationManager.getCurrentContentPrefix()
                    }
                    "droplet" -> {
                        prefix = plugin.prefixAnimationManager.getCurrentDropletPrefix()
                    }
                    "glorious" -> {
                        prefix = plugin.prefixAnimationManager.getCurrentGloriousPrefix()
                    }
                }
            }
            if ((identifier == "prefix_tab" || prefix.length <= 10 || one.actualProtocolVersion >= 393) && two.aero.vanished) {
                prefix = "&7[V] $prefix";
            }
            if ((one.actualProtocolVersion < 735 || ServerUtil.protocolVersion < 735) && prefix.contains("&#")) {
                prefix = ChatUtils.downsampleHexColorsAmpersand(prefix)
            }
            return prefix;
        }
        if (identifier == "suffix") {
            return two.suffixFor(one).replace('§', '&')
        }
        if (identifier == "nick") {
            return if (one.aero.preferences.showNicks) {
                two.aero.effectiveNick ?: two.name
            } else {
                two.name
            }
        }
        return null
    }
}