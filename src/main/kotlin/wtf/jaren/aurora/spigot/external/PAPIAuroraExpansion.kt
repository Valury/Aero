package wtf.jaren.aurora.spigot.external

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.clip.placeholderapi.expansion.Relational
import org.bukkit.entity.Player
import wtf.jaren.aurora.spigot.Aurora
import wtf.jaren.aurora.spigot.utils.aurora
import wtf.jaren.aurora.spigot.utils.prefix
import wtf.jaren.aurora.spigot.utils.suffix

class PAPIAuroraExpansion : PlaceholderExpansion(), Relational {
    override fun getIdentifier(): String {
        return "aurora"
    }

    override fun getAuthor(): String {
        return "Jaren"
    }

    override fun getVersion(): String {
        return "1.0"
    }

    override fun onPlaceholderRequest(player: Player, identifier: String): String? {
        if (identifier == "server") {
            return Aurora.instance.serverName
        }
        if (identifier == "prefix") {
            return (if (player.aurora.vanished) "&7[V] " else "") + player.prefix.replace('§', '&')
        }
        if (identifier == "suffix") {
            return player.suffix.replace('§', '&')
        }
        if (identifier == "nick") {
            return player.aurora.nick ?: player.name
        }
        return null
    }

    override fun onPlaceholderRequest(one: Player, two: Player, identifier: String): String? {
        if (identifier == "nick") {
            return if (one.aurora.preferences.showNicks) {
                two.aurora.nick ?: two.name
            } else {
                two.name
            }
        }
        return null
    }
}