package wtf.jaren.aero.velocity.discord

import com.mongodb.client.model.Filters
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import wtf.jaren.aero.shared.objects.AeroPlayer
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.enums.PunishmentType
import wtf.jaren.aero.velocity.utils.ChatUtils
import wtf.jaren.aero.velocity.utils.StringUtils
import wtf.jaren.aero.velocity.utils.displayNameFor
import wtf.jaren.aero.velocity.utils.luckperms

class DiscordClient(val plugin: Aero) : ListenerAdapter() {
    val jda = JDABuilder.createDefault(System.getenv("DISCORD_TOKEN"))
        .addEventListeners(this)
        .build()

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.channel.parent?.id == "888269755813875733" && !event.author.isBot) {
            var message = event.message.contentDisplay
                .replace('‘', '\'')
                .replace('’', '\'')
            if (!StringUtils.isAscii(message)) {
                if (plugin.isProd) {
                    event.message.delete().queue()
                    event.author.openPrivateChannel().queue {
                        it.sendMessage("Your message contains illegal characters.").queue()
                    }
                }
                return
            }
            val playerDocument =
                plugin.database.getCollection("players").find(Filters.eq("discord_id", event.author.idLong)).first()
            if (playerDocument == null) {
                if (plugin.isProd) {
                    event.message.delete().queue()
                    event.author.openPrivateChannel().queue {
                        it.sendMessage("Your discord account doesn't appear to be linked. Please contact an admin.").queue()
                    }
                }
                return
            }
            val aeroPlayer = AeroPlayer.fromDocument(playerDocument)
            val punishment = plugin.punishmentManager.getActivePunishment(aeroPlayer, PunishmentType.BAN) ?: plugin.punishmentManager.getActivePunishment(aeroPlayer, PunishmentType.MUTE)
            if (punishment != null) {
                if (plugin.isProd) {
                    event.message.delete().queue()
                    event.author.openPrivateChannel().queue {
                        it.sendMessage(PlainTextComponentSerializer.plainText().serialize(punishment.getMessage(false))).queue()
                    }
                }
                return
            }
            val guild = if (aeroPlayer.guild != null) {
                plugin.guildManager.getGuild(aeroPlayer.guild!!._id)
            } else {
                null
            }
            for (onlinePlayer in plugin.server.allPlayers) {
                onlinePlayer.currentServer.ifPresent {
                    if (it.server.serverInfo.name == event.channel.name) {
                        val builder = Component.text()
                            .append(aeroPlayer.displayNameFor(onlinePlayer))
                        if (guild?.name != null) {
                            builder.append(Component.text(" [${guild.name}]", guild.color))
                        }
                        builder.append(
                            ChatUtils.getMessageAsComponent(
                                aeroPlayer.luckperms,
                                ": $message"
                            )
                        )
                        onlinePlayer.sendMessage(Identity.identity(aeroPlayer._id), builder.build())
                    }
                }
            }
        }
        if (event.message.contentRaw == "aero!info") {
            event.channel.sendMessage("[${if (plugin.isProd) "PROD" else "DEV"}] Running Aero on ${plugin.server.version.name} by ${plugin.server.version.vendor} (${plugin.server.version.version})").queue()
        }
    }

    override fun onSlashCommand(event: SlashCommandEvent) {
        event.reply(event.name).queue()
        Aero.instance.logger.info(event.name)
    }
}