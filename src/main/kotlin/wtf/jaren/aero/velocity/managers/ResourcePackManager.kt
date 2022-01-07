package wtf.jaren.aero.velocity.managers

import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.api.proxy.Player
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.utils.toHex
import java.io.FileInputStream
import java.security.DigestInputStream
import java.security.MessageDigest

class ResourcePackManager(private val plugin: Aero) {
    var hash = generateHash()

    private fun generateHash(): ByteArray? {
        if (!plugin.isProd) return null
        var md: MessageDigest = MessageDigest.getInstance("SHA-1")
        DigestInputStream(FileInputStream("/home/container/public/pack.zip"), md).use { dis ->
            while (dis.read() != -1);
            md = dis.messageDigest
        }
        return md.digest()
    }

    fun sendResourcePackTo(player: Player) {
        if (hash == null) return
        if (player.protocolVersion < ProtocolVersion.MINECRAFT_1_13) return
        player.sendResourcePackOffer(
            plugin.server.createResourcePackBuilder("https://valury.network/pack.zip?${hash!!.toHex()}")
                .setShouldForce(true)
                .setHash(hash)
                .build()
        )
    }

    fun reload(): Boolean {
        if (!plugin.isProd) return false
        val newHash = generateHash()
        if (hash.contentEquals(newHash)) {
            return false
        }
        hash = newHash
        for (player in plugin.server.allPlayers) {
            sendResourcePackTo(player)
        }
        return true
    }
}