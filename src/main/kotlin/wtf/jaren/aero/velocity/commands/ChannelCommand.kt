package wtf.jaren.aero.velocity.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.managers.ChannelManager
import wtf.jaren.aero.velocity.utils.aero

class ChannelCommand(private val plugin: Aero) {
    private val channelManager: ChannelManager = plugin.channelManager
    fun register() {
        val totalNode = LiteralArgumentBuilder
            .literal<CommandSource>("channel")
            .requires { source: CommandSource? -> source is Player }
            .executes { context: CommandContext<CommandSource> -> helpCommand(context) }
            .build()
        val channelNode =
            RequiredArgumentBuilder.argument<CommandSource, String>("channel", StringArgumentType.string())
                .suggests { context: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                    for (channel in channelManager.channels) {
                        if (channel.id.lowercase()
                                .startsWith(builder.remainingLowerCase) && channel.isMember((context.source as Player).aero)
                        ) {
                            builder.suggest(channel.id)
                        }
                    }
                    builder.buildFuture()
                }
                .executes { context: CommandContext<CommandSource> -> channelCommand(context) }
                .build()
        totalNode.addChild(channelNode)
        plugin.server.commandManager.register(BrigadierCommand(totalNode))
    }

    private fun helpCommand(context: CommandContext<CommandSource>): Int {
        context.source.sendMessage(Component.text("Usage: /channel <channel>"))
        return 0
    }

    private fun channelCommand(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val channelName = context.getArgument("channel", String::class.java)
        val channel = channelManager.channels.find { it.id.equals(channelName, true) }
        if (channel == null) {
            player.sendMessage(Component.text("Invalid channel.", NamedTextColor.RED))
            return 0
        }
        if (!channel.isMember(player.aero)) {
            player.sendMessage(channel.getNoMembershipMessage(player.aero))
            return 0
        }
        if (channel.id == "SERVER") {
            channelManager.channelOverrides.remove(player)
        } else {
            channelManager.channelOverrides[player] = channel
        }
        player.sendMessage(Component.text("You are now in the " + channel.id + " channel.", NamedTextColor.GREEN))
        return 0
    }
}