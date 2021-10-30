package wtf.jaren.aurora.velocity.utils

import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import wtf.jaren.aurora.shared.objects.Guild
import wtf.jaren.aurora.velocity.Aurora

fun Guild.broadcast(identity: Identity, message: Component) {
    for (onlinePlayer in Aurora.instance.server.allPlayers) {
        if (onlinePlayer.aurora.guild?._id == this._id) {
            onlinePlayer.sendMessage(identity, message)
        }
    }
}