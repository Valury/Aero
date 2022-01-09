package wtf.jaren.aero.spigot.utils

import org.bukkit.Bukkit

object ServerUtil {
    val protocolVersion: Int
        get() {
            if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
                return ViaVersionUtil.getServerVersion()
            }
            val player = Bukkit.getOnlinePlayers().first()
            if (player != null) {
                return player.actualProtocolVersion
            }
            return -1
        }
}