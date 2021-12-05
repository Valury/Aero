package wtf.jaren.aero.velocity.objects

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.scheduler.ScheduledTask

data class WarpRequest(
    val player: Player,
    val target: Player,
    val task: ScheduledTask
)
