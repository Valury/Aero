package wtf.jaren.aero.spigot.utils

import com.viaversion.viaversion.api.Via
import org.bukkit.entity.Player

object ViaVersionUtil {
    fun getPlayerVersion(player: Player): Int {
        return Via.getAPI().getPlayerVersion(player)
    }
}
