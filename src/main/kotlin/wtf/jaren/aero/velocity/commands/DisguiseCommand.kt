package wtf.jaren.aero.velocity.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.client.model.Filters
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bson.Document
import wtf.jaren.aero.shared.objects.AeroDisguise
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.utils.PlayerUtils
import wtf.jaren.aero.velocity.utils.SyncUtils
import wtf.jaren.aero.velocity.utils.aero

class DisguiseCommand(private val plugin: Aero) {
    fun register() {
        val commandManager = plugin.server.commandManager
        val totalNode = LiteralArgumentBuilder
            .literal<CommandSource>("disguise")
            .requires { source: CommandSource -> source is Player && source.hasPermission("aero.disguise") }
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("name")
                .then(
                    RequiredArgumentBuilder.argument<CommandSource, String>("name", StringArgumentType.string())
                    .executes { context: CommandContext<CommandSource> -> name(context) }
                    .build()
                )
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("skin")
                .then(
                    RequiredArgumentBuilder.argument<CommandSource, String>("name", StringArgumentType.string())
                        .executes { context: CommandContext<CommandSource> -> skin(context) }
                        .build()
                )
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("rank")
                .then(
                    RequiredArgumentBuilder.argument<CommandSource, String>("rank", StringArgumentType.string())
                        .executes { context: CommandContext<CommandSource> -> rank(context) }
                        .build()
                )
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("reset")
                .executes { context: CommandContext<CommandSource> -> reset(context) }
                .build()
            )
            .build()
        commandManager.register(commandManager.metaBuilder("d").build(), BrigadierCommand(totalNode))
    }

    private fun name(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val name = context.getArgument("name", String::class.java)
        if (name.length > 16) {
            player.sendMessage(Component.text("That name is too long.", NamedTextColor.RED))
            return 0
        }
        if (!Regex("^[A-Za-z_]+\$").matches(name)) {
            player.sendMessage(Component.text("That name contains invalid characters.", NamedTextColor.RED))
            return 0
        }
        val existingPlayer = plugin.database.getCollection("players")
            .find(Filters.or(
                Filters.eq("name", name),
                Filters.eq("disguise.name", name)
            ))
            .collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
            .first()
        if (existingPlayer != null) {
            player.sendMessage(Component.text("A player with that name has played on this server before.", NamedTextColor.RED))
            return 0
        }
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", player.uniqueId),
            Document("\$set", Document("disguise.name", name))
        )
        player.sendMessage(Component.text("Success! Your new name will appear the next time you login.", NamedTextColor.GREEN))
        return 0
    }

    private fun skin(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val name = context.getArgument("name", String::class.java)
        val offlinePlayer = PlayerUtils.fetchOfflinePlayer(name)
        if (offlinePlayer == null) {
            player.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return 0
        }
        val properties = PlayerUtils.fetchPlayerProfile(offlinePlayer.uuid)
        if (properties == null) {
            player.sendMessage(Component.text("Something went wrong...", NamedTextColor.RED))
            return 0
        }
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", player.uniqueId),
            Document("\$set", Document("disguise.properties",
                properties.getAsJsonArray("properties")
                    .map { val jsonObject = it.asJsonObject
                        Document("name", jsonObject["name"].asString)
                            .append("value", jsonObject["value"].asString)
                            .append("signature", jsonObject["signature"].asString)
                    }
            ))
        )
        player.sendMessage(Component.text("Success! Your new skin will appear the next time you login.", NamedTextColor.GREEN))
        return 0
    }

    private val ranks = listOf(
        "droplet",
        "glorious",
        "knight",
        "recruit",
        "default"
    )

    private fun rank(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val rank = context.getArgument("rank", String::class.java).lowercase()
        if (ranks.indexOf(rank) == -1) {
            player.sendMessage(Component.text("Invalid rank.", NamedTextColor.RED))
            return 0
        }
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", player.uniqueId),
            Document("\$set", Document("disguise.rank", rank))
        )
        if (player.aero.disguise == null) {
            player.aero.disguise = AeroDisguise(null, rank, null, null)
        } else {
            player.aero.disguise!!.rank = rank
        }
        player.currentServer?.orElse(null)
            ?.sendPluginMessage(MinecraftChannelIdentifier.create("aero", "sync"), SyncUtils.reload)
        player.sendMessage(Component.text("Success!", NamedTextColor.GREEN))
        return 0
    }

    private fun reset(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", player.uniqueId),
            Document("\$unset", Document("disguise", true))
        )
        player.sendMessage(Component.text("Success! Your disguise will reset the next time you login.", NamedTextColor.GREEN))
        return 0
    }
}