package wtf.jaren.aurora.velocity.managers

import com.velocitypowered.api.proxy.Player
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.objects.WarpRequest
import java.util.concurrent.TimeUnit

class WarpManager {
    private val locked = HashSet<Player>()
    private val requests = HashSet<WarpRequest>()
    fun lock(player: Player): Boolean {
        return locked.add(player)
    }

    fun unlock(player: Player): Boolean {
        return locked.remove(player)
    }

    fun isLocked(player: Player): Boolean {
        return locked.contains(player)
    }

    fun createWarpRequest(player: Player, target: Player): Boolean {
        if (requests.find { it.player == player && it.target == target } != null) {
            return false
        }
        val task = Aurora.instance.server.scheduler.buildTask(Aurora.instance) {
            requests.removeIf { it.player == player && it.target == target }
        }.delay(5, TimeUnit.MINUTES).schedule()
        requests.add(WarpRequest(player, target, task))
        return true
    }

    fun acceptWarpRequest(player: Player, target: Player): Boolean {
        val request = requests.find { it.player == player && it.target == target } ?: return false
        request.task.cancel()
        requests.remove(request)
        return true
    }
}