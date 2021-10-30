package wtf.jaren.aurora.spigot.records

import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

data class PendingWarp(val player: Player, val task: BukkitTask)