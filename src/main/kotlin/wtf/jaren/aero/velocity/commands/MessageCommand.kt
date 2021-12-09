package wtf.jaren.aero.velocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.enums.PunishmentType
import wtf.jaren.aero.velocity.utils.ChatUtils
import wtf.jaren.aero.velocity.utils.displayName
import wtf.jaren.aero.velocity.utils.displayNameFor

class MessageCommand(private val plugin: Aero) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val arguments = invocation.arguments()
        val source = invocation.source()
        if (arguments.size < 2) {
            source.sendMessage(Component.text("Usage: /msg <player> <message>"))
        }
        val target = plugin.server.getPlayer(arguments[0]).orElse(null)
        if (target == null) {
            source.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return
        }
        var senderName = Component.text("Console")
        if (source is Player) {
            val punishment = plugin.punishmentManager.getActivePunishment(source, PunishmentType.MUTE)
            if (punishment != null) {
                source.sendMessage(punishment.getMessage(false))
                return
            }
            senderName = source.displayNameFor(target)
            plugin.replyManager.replyMap[target] = source.username
        }
        val messageText = ChatUtils.process(source, listOf(*arguments).subList(1, arguments.size).joinToString(" ")) ?: return
        val targetName = if (source is Player) target.displayNameFor(source) else target.displayName
        if (messageText.contains("\${jndi:", ignoreCase = true)) {
            return
        }
        val message = ChatUtils.getMessageAsComponent(
            source,
            ": $messageText"
        )
        target.sendMessage(
            Component.text()
                .append(Component.text("From ", NamedTextColor.BLUE))
                .append(senderName)
                .append(message)
        )
        source.sendMessage(
            Component.text()
                .append(Component.text("To ", NamedTextColor.BLUE))
                .append(targetName)
                .append(message)
        )
    }

    override fun suggest(invocation: SimpleCommand.Invocation): List<String> {
        val arguments = invocation.arguments()
        if (arguments.size <= 1) {
            val usernameArg = if (arguments.isEmpty()) "" else arguments[0].lowercase()
            return plugin.server.allPlayers
                .map { obj: Player -> obj.username }
                .filter { username: String -> username.lowercase().startsWith(usernameArg) }
        }
        return super.suggest(invocation)
    }
}