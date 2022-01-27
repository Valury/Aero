package wtf.jaren.aero.spigot.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wtf.jaren.aero.spigot.Aero
import wtf.jaren.aero.spigot.utils.aeroDisplayName
import wtf.jaren.aero.spigot.utils.displayNameFor

class GameModeCommand(val plugin: Aero) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var playerIndex = 0
        val gameMode = when (label) {
            "gms" -> GameMode.SURVIVAL
            "gmc" -> GameMode.CREATIVE
            "gma" -> GameMode.ADVENTURE
            "gmsp" -> GameMode.SPECTATOR
            else -> {
                if (args.isNotEmpty()) {
                    playerIndex = 1
                    parseGameMode(args[0])
                } else {
                    null
                }
            }
        }
        if (gameMode == null) {
            plugin.adventure.sender(sender).sendMessage(usage(label, sender is Player));
            return true
        }
        val player = if (args.size > playerIndex || sender !is Player) {
            if (args.size > playerIndex) {
                Bukkit.getPlayer(args[playerIndex])
            } else {
                null
            }
        } else {
            sender
        }
        if (player == null) {
            plugin.adventure.sender(sender).sendMessage(usage(label, sender is Player));
            return true
        }
        if (!sender.hasPermission("aero.gamemode.${if (sender == player) "self" else "others"}.${gameMode.name.lowercase()}")) {
            plugin.adventure.sender(sender).sendMessage(Component.text("You do not have permission to do this.", NamedTextColor.RED))
            return true
        }
        player.gameMode = gameMode
        plugin.adventure.sender(sender).sendMessage(
            Component.text()
                .append(Component.text("Set game mode ", NamedTextColor.GREEN))
                .append(Component.text(gameMode.name.lowercase(), NamedTextColor.RED))
                .append(Component.text(" for ", NamedTextColor.GREEN))
                .append(if (sender is Player) player.displayNameFor(sender) else player.aeroDisplayName)
        )
        return true
    }

    private fun parseGameMode(name: String): GameMode? {
        val intVal = name.toIntOrNull()
        if (intVal != null) {
            return GameMode.getByValue(intVal)
        }
        return try {
            GameMode.valueOf(name.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun usage(label: String, isPlayer: Boolean): TextComponent {
        val player = if (isPlayer) "[player]" else "<player>"
        return if (label == "gamemode" || label == "gm") {
            Component.text("Usage: /$label <gamemode> $player", NamedTextColor.RED)
        } else {
            Component.text("Usage: /$label $player", NamedTextColor.RED)
        }
    }
}