package wtf.jaren.aero.spigot.external

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.placeholderapi.expansion.Relational
import org.bukkit.entity.Player
import wtf.jaren.aero.spigot.Aero
import wtf.jaren.aero.spigot.utils.ChatUtils
import wtf.jaren.aero.spigot.utils.aero
import wtf.jaren.aero.spigot.utils.prefix
import wtf.jaren.aero.spigot.utils.suffix

class PAPIAeroExpansion : PlaceholderExpansion(), Relational {
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
        if (identifier == "prefix") {
            var prefix = two.prefix.replace('§', '&')
            if (one.protocolVersion < 393) {
                prefix = ChatUtils.convertUnicodeToPlainText(prefix);
            }
            if ((prefix.length <= 10 || one.protocolVersion >= 393) && two.aero.vanished) {
                prefix = "&7[V] $prefix";
            }
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