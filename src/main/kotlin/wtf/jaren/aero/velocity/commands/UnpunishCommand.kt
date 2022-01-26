package wtf.jaren.aero.velocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.enums.PunishmentType
import wtf.jaren.aero.velocity.utils.PlayerUtils.fetchOfflinePlayer
import wtf.jaren.aero.velocity.utils.aero

class UnpunishCommand(plugin: Aero) : SimpleCommand {
    private val punishmentManager = plugin.punishmentManager
    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()
        val type = PunishmentType.valueOf(invocation.alias().substring(2).uppercase())
        val arguments = invocation.arguments()
        if (arguments.isEmpty()) {
            source.sendMessage(getHelpMessage(invocation))
            return
        }
        val target = fetchOfflinePlayer(arguments[0])
        if (target == null) {
            source.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return
        }
        val punishment = punishmentManager.getActivePunishment(target.uuid, type)
        if (punishment == null) {
            source
                .sendMessage(Component.text("That player is not " + type.pastTense + ".", NamedTextColor.RED))
            return
        }
        punishmentManager.revokePunishment(punishment, if (source is Player) source.aero.name else "Console")
    }

    private fun getHelpMessage(invocation: SimpleCommand.Invocation): Component {
        val alias = invocation.alias()
        return Component.text("/$alias <player>")
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("aero." + invocation.alias())
    }

}