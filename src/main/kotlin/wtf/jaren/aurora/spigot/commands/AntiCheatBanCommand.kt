package wtf.jaren.aurora.spigot.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wtf.jaren.aurora.spigot.Aurora

class AntiCheatBanCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            sender.sendMessage(Component.text("Players cannot run this command.", NamedTextColor.RED))
            return true
        }
        if (args.isEmpty()) {
            return false
        }
        val player = Bukkit.getPlayer(args[0])
        if (player == null) {
            sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return true
        }
        player.world.strikeLightningEffect(player.location)
        player.sendPluginMessage(Aurora.instance, "aurora:acb", byteArrayOf())
        return true
    }
}