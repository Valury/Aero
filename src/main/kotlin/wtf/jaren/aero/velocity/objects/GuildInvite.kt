package wtf.jaren.aero.velocity.objects

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.scheduler.ScheduledTask
import wtf.jaren.aero.shared.objects.Guild

data class GuildInvite(
    val guild: Guild,
    val player: Player,
    val task: ScheduledTask
)