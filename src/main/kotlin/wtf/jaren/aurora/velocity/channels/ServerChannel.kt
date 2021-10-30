package wtf.jaren.aurora.velocity.channels

import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.command.CommandMeta
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import wtf.jaren.aurora.shared.objects.AuroraPlayer
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.enums.PunishmentType
import wtf.jaren.aurora.velocity.objects.Channel
import wtf.jaren.aurora.velocity.utils.ChatUtils
import wtf.jaren.aurora.velocity.utils.fullDisplayName

class ServerChannel(plugin: Aurora) : Channel(plugin) {
    override val id: String = "SERVER"

    override fun isMember(player: AuroraPlayer): Boolean {
        return plugin.server.getPlayer(player._id).isPresent
    }

    override fun sendMessage(sender: AuroraPlayer, message: String) {
        val serverPlayer = plugin.server.getPlayer(sender._id).get()
        val punishment = plugin.punishmentManager.getActivePunishment(serverPlayer, PunishmentType.MUTE)
        if (punishment != null) {
            serverPlayer.sendMessage(punishment.getMessage(false))
            return
        }
        serverPlayer.spoofChatInput(message)
        if (!message.startsWith("/")) {
            plugin.discordClient.jda.textChannels
                .find { it.parent?.id == "888269755813875733" && serverPlayer.currentServer.get().serverInfo.name == it.name }
                ?.sendMessage(
                    ChatUtils.convertUnicodeToPlainText(ChatUtils.escapeDiscordMarkdown(
                        "${
                            PlainTextComponentSerializer.plainText().serialize(serverPlayer.fullDisplayName)
                        }: $message"
                    )
                ))
                ?.allowedMentions(listOf())
                ?.queue()
        }
    }

    override fun getCommandMeta(commandManager: CommandManager): CommandMeta {
        return commandManager.metaBuilder("ac").build()
    }
}