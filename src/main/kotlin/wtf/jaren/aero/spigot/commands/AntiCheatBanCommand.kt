package wtf.jaren.aero.spigot.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wtf.jaren.aero.spigot.Aero

class AntiCheatBanCommand(val plugin: Aero) : CommandExecutor {
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
        player.sendPluginMessage(Aero.instance, "aero:acb", byteArrayOf())
        return true
    }
}