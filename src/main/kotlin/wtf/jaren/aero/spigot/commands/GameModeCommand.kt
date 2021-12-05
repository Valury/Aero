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

class GameModeCommand : CommandExecutor {
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
            sender.sendMessage(usage(label, sender is Player));
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
            sender.sendMessage(usage(label, sender is Player));
            return true
        }
        player.gameMode = gameMode
        sender.sendMessage(
            Component.text()
                .append(Component.text("Set game mode ", NamedTextColor.GREEN))
                .append(Component.text(gameMode.name.lowercase(), NamedTextColor.RED))
                .append(Component.text(" for ", NamedTextColor.GREEN))
                .append(player.displayName())
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
        return if (label == "gm") {
            Component.text("Usage: /gm <gamemode> $player", NamedTextColor.RED)
        } else {
            Component.text("Usage: /$label $player", NamedTextColor.RED)
        }
    }
}