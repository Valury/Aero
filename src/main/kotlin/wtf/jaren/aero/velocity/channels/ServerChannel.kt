package wtf.jaren.aero.velocity.channels

import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.command.CommandMeta
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import wtf.jaren.aero.shared.objects.AeroPlayer
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.enums.PunishmentType
import wtf.jaren.aero.velocity.objects.Channel
import wtf.jaren.aero.velocity.utils.ChatUtils
import wtf.jaren.aero.velocity.utils.fullDisplayName

class ServerChannel(plugin: Aero) : Channel(plugin) {
    override val id: String = "SERVER"

    override fun isMember(player: AeroPlayer): Boolean {
        return plugin.server.getPlayer(player._id).isPresent
    }

    override fun sendMessage(sender: AeroPlayer, message: String) {
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