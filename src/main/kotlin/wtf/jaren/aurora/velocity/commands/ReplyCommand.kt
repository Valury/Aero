package wtf.jaren.aurora.velocity.commands

import com.velocitypowered.api.command.RawCommand
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aurora.velocity.Aurora

class ReplyCommand(private val plugin: Aurora) : RawCommand {
    override fun execute(invocation: RawCommand.Invocation) {
        if (invocation.arguments().trim() == "") {
            invocation.source().sendMessage(Component.text("Usage: /r <message>"))
            return
        }
        val player = invocation.source() as Player
        if (plugin.replyManager.replyMap.containsKey(player)) {
            plugin.server.commandManager.executeAsync(
                player,
                "msg " + plugin.replyManager.replyMap[player] + " " + invocation.arguments()
            )
        } else {
            player.sendMessage(Component.text("Nobody has messaged you recently.", NamedTextColor.RED))
        }
    }

}