package wtf.jaren.aurora.velocity.objects

import com.velocitypowered.api.scheduler.ScheduledTask
import wtf.jaren.aurora.velocity.Aurora
import java.time.Duration
import java.time.Instant

data class CachedMute(val plugin: Aurora, val punishment: Punishment) {
    val task: ScheduledTask? = if (punishment.expires != null) {
        plugin.server.scheduler.buildTask(plugin) {
            plugin.punishmentManager.currentMutes.remove(plugin.server.getPlayer(punishment.player.uuid).get())
        }.delay(Duration.between(Instant.now(), punishment.expires.toInstant())).schedule()
    } else {
        null
    }
}