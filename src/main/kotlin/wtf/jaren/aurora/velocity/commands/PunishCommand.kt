package wtf.jaren.aurora.velocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.enums.PunishmentType
import wtf.jaren.aurora.velocity.managers.PunishmentManager
import wtf.jaren.aurora.velocity.objects.Punishment
import wtf.jaren.aurora.velocity.utils.PlayerUtils.fetchOfflinePlayer
import wtf.jaren.aurora.velocity.utils.PlayerUtils.fetchPlayerIP
import wtf.jaren.aurora.velocity.utils.TimeUtils.parseTime
import java.util.*

class PunishCommand(private val plugin: Aurora) : SimpleCommand {
    private val punishmentManager: PunishmentManager = plugin.punishmentManager
    override fun execute(invocation: SimpleCommand.Invocation) {
        val ip = invocation.alias().startsWith("ip")
        val type = PunishmentType.valueOf(
            (if (ip) invocation.alias().substring(2) else invocation.alias()).uppercase()
        )
        val arguments = invocation.arguments()
        if (arguments.size < 2) {
            invocation.source().sendMessage(getHelpMessage(invocation))
            return
        }
        val target = fetchOfflinePlayer(arguments[0])
        if (target == null) {
            invocation.source().sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return
        }
        var duration: Long? = null
        val reason = ArrayList<String>()
        for (i in 1 until arguments.size) {
            if (i == 1 && type.hasDuration) {
                duration = parseTime(arguments[i])
                if (duration != null) continue
            }
            reason.add(arguments[i])
        }
        if (reason.size == 0) {
            invocation.source().sendMessage(getHelpMessage(invocation))
            return
        }
        val punishment = Punishment(
            null,
            type,
            target,
            if (ip) fetchPlayerIP(target.uuid) else null,
            if (invocation.source() is Player) (invocation.source() as Player).uniqueId else UUID.fromString("6d4ea8ef-7c70-4d13-a0a3-e1c7ba912bc2"),
            java.lang.String.join(" ", reason),
            if (duration != null) Date(System.currentTimeMillis() + duration) else null,
            false
        )
        punishmentManager.applyPunishment(punishment)
    }

    private fun getHelpMessage(invocation: SimpleCommand.Invocation): Component {
        val ip = invocation.alias().startsWith("ip")
        val alias = invocation.alias()
        val type = PunishmentType.valueOf((if (ip) alias.substring(2) else alias).uppercase())
        return Component.text("/" + alias + " <player>" + (if (type.hasDuration) " [duration]" else "") + " <reason>")
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("aurora." + invocation.alias())
    }

    override fun suggest(invocation: SimpleCommand.Invocation): List<String> {
        val arguments = invocation.arguments()
        if (arguments.size <= 1) {
            val current = if (arguments.isEmpty()) "" else arguments[0].lowercase()
            val players = ArrayList<String>()
            for (player in plugin.server.allPlayers) {
                if (player.username.lowercase().startsWith(current)) {
                    players.add(player.username)
                }
            }
            return players
        }
        return emptyList()
    }

}