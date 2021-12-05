package wtf.jaren.aero.spigot.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class OpMeCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val player = sender as Player
        if (!player.isOp) {
            player.isOp = true
            player.sendMessage(Component.text("You are now op.", NamedTextColor.GREEN))
        } else {
            player.isOp = false
            player.sendMessage(Component.text("You are no longer op.", NamedTextColor.RED))
        }
        return true
    }
}