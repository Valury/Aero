package wtf.jaren.aero.velocity.utils

import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import wtf.jaren.aero.shared.objects.Guild
import wtf.jaren.aero.velocity.Aero

fun Guild.broadcast(identity: Identity, message: Component) {
    for (onlinePlayer in Aero.instance.server.allPlayers) {
        if (onlinePlayer.aero.guild?._id == this._id) {
            onlinePlayer.sendMessage(identity, message)
        }
    }
}