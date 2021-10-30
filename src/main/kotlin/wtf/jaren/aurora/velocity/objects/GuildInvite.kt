package wtf.jaren.aurora.velocity.objects

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.scheduler.ScheduledTask
import wtf.jaren.aurora.shared.objects.Guild

data class GuildInvite(
    val guild: Guild,
    val player: Player,
    val task: ScheduledTask
)