package wtf.jaren.aurora.spigot.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import wtf.jaren.aurora.spigot.Aurora
import wtf.jaren.aurora.spigot.utils.aurora
import wtf.jaren.aurora.spigot.utils.updateDisplayName
import java.io.ByteArrayInputStream
import java.io.DataInputStream

class SyncListener(val plugin: Aurora) : PluginMessageListener {
    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        val input = DataInputStream(ByteArrayInputStream(message))
        when (input.readUTF()) {
            "refreshPlayer" -> {
                plugin.playerManager.handlePreLogin(player.uniqueId)
                plugin.playerManager.handleJoin(player)
            }
            "refreshPlayerGuild" -> {
                val playerGuild = player.aurora.guild!!
                plugin.guildManager.guilds.remove(playerGuild._id)
                plugin.guildManager.handlePlayerPreLogin(player.aurora)
                for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                    if (playerGuild._id == onlinePlayer.aurora.guild?._id) {
                        onlinePlayer.updateDisplayName()
                    }
                }
            }
            "deleteGuild" -> {
                plugin.guildManager.guilds.remove(ObjectId(input.readUTF()))
            }
            "reload" -> {
                plugin.guildManager.guilds.clear()
                for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                    try {
                        plugin.playerManager.handlePreLogin(onlinePlayer.uniqueId)
                        plugin.playerManager.handleJoin(onlinePlayer)
                        onlinePlayer.updateDisplayName()
                    } catch (e: Exception) {
                        onlinePlayer.kick(Component.text("Aurora / Something went horribly wrong.", NamedTextColor.RED))
                    }
                }
            }
        }
    }
}