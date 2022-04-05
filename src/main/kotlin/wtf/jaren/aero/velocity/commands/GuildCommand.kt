package wtf.jaren.aero.velocity.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mongodb.client.model.*
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bson.Document
import wtf.jaren.aero.shared.objects.AeroPlayer
import wtf.jaren.aero.shared.objects.AeroPlayerGuild
import wtf.jaren.aero.shared.objects.Guild
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.utils.*
import java.text.DateFormat
import kotlin.math.ceil

class GuildCommand(private val plugin: Aero) {
    private val guildManager = plugin.guildManager
    fun register() {
        val commandManager = plugin.server.commandManager
        val totalNode = LiteralArgumentBuilder
            .literal<CommandSource>("guild")
            .requires { source: CommandSource -> source is Player }
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("info")
                .executes { context: CommandContext<CommandSource> -> info(context) }
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("create")
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("name", StringArgumentType.string())
                    .executes { context: CommandContext<CommandSource> -> create(context) }
                    .build())
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("invite")
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { obj: CommandContext<CommandSource>, context: SuggestionsBuilder ->
                        BrigadierUtils.getSuggestionsWithoutSender(obj, context)
                    }
                    .executes { context: CommandContext<CommandSource> -> invite(context) }
                    .build())
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("join")
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("name", StringArgumentType.string())
                    .executes { context: CommandContext<CommandSource> -> join(context) }
                    .build())
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("leave")
                .executes { context: CommandContext<CommandSource> -> leave(context) }
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("kick")
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .executes { context: CommandContext<CommandSource> -> kick(context) }
                    .build())
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("promote")
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .executes { context: CommandContext<CommandSource> -> promote(context) }
                    .build())
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("demote")
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .executes { context: CommandContext<CommandSource> -> demote(context) }
                    .build())
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("list")
                .executes { context: CommandContext<CommandSource> -> list(context) }
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("rename")
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("name", StringArgumentType.string())
                    .executes { context: CommandContext<CommandSource> -> rename(context) }
                    .then(LiteralArgumentBuilder
                        .literal<CommandSource>("confirm")
                        .executes { context: CommandContext<CommandSource> -> renameConfirm(context) }
                        .build()
                    )
                    .build())
                .build()
            )
            /*.then(LiteralArgumentBuilder
                .literal<CommandSource>("color")
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("color", StringArgumentType.string())
                    .executes { context: CommandContext<CommandSource> -> color(context) }
                    .build())
                .build()
            )*/
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("public")
                .executes { context: CommandContext<CommandSource> -> public(context) }
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("private")
                .executes { context: CommandContext<CommandSource> -> private(context) }
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("tpa")
                .executes { context: CommandContext<CommandSource> -> tpa(context) }
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("disband")
                .executes { context: CommandContext<CommandSource> -> disband(context) }
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("leaderboard")
                .then(RequiredArgumentBuilder.argument<CommandSource, Int>("page", IntegerArgumentType.integer(1))
                    .executes { context: CommandContext<CommandSource> -> leaderboard(context) }
                    .build())
                .executes { context: CommandContext<CommandSource> -> leaderboard(context) }
                .build()
            )
            //.executes { context: CommandContext<CommandSource> -> help(context) }
            .build()
        commandManager.register(commandManager.metaBuilder("g").build(), BrigadierCommand(totalNode))
    }

    private fun info(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        if (player.aero.guild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        val guild = guildManager.getGuild(player.aero.guild!!._id)
        val memberCount = plugin.database.getCollection("players").find(Filters.eq("guild._id", guild._id)).count()
        player.sendMessage(
            Component.text()
                .append(Component.text("-- ${guild.name ?: "Unnamed Guild"} --", Guild.COLOR_SCHEME))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Created", Guild.COLOR_SCHEME))
                .append(Component.text(": ${DateFormat.getDateInstance().format(guild._id.date)}"))
                .append(Component.newline())
                .append(Component.text("Members", Guild.COLOR_SCHEME))
                .append(Component.text(": $memberCount/${guild.slots}"))
                .append(Component.newline())
                .append(Component.text("Public", Guild.COLOR_SCHEME))
                .append(Component.text(": ${if (guild.public) "Yes" else "No"}"))
            //.append(Component.newline())
            //.append(Component.text("Balance", Guild.COLOR_SCHEME))
            //.append(Component.text(": ${NumberUtils.formatBalance(guild.balance)}"))
        )
        return 0
    }

    private fun create(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val planAPI = plugin.planAPI
        if (planAPI != null) {
            if (planAPI.getPlaytime(player.uniqueId) < 7200000L) {
                player.sendMessage(
                    Component.text(
                        "You must have at least 2 hours of playtime in order to do this.",
                        NamedTextColor.RED
                    )
                )
                return 0
            }
        }
        if (player.aero.guild != null) {
            player.sendMessage(Component.text("You are already in a guild.", NamedTextColor.RED))
            return 0
        }
        val name = context.getArgument("name", String::class.java)
        val nameError = Guild.getNameError(name)
        if (nameError != null) {
            player.sendMessage(Component.text(nameError, NamedTextColor.RED))
            return 0
        }
        val existing = plugin.database.getCollection("guilds")
            .find(Filters.eq("name", name))
            .collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
            .first()
        if (existing != null) {
            player.sendMessage(Component.text("A guild with this name already exists.", NamedTextColor.RED))
            return 0
        }
        guildManager.createGuild(name, player)
        player.sendMessage(
            Component.text()
                .append(Guild.PREFIX)
                .append(Component.text("You have successfully created the guild $name.", Guild.COLOR_SCHEME))
        )
        return 0
    }

    private fun invite(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        if (player.aero.guild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        val guild = guildManager.getGuild(player.aero.guild!!._id)
        if (!guild.permissions.canInvite(player.aero)) {
            player.sendMessage(Component.text("You do not have permission to do this.", NamedTextColor.RED))
            return 0
        }
        if (guild.name == null) {
            context.source.sendMessage(Component.text("You cannot invite players to an unnamed guild.", NamedTextColor.RED))
            return 0
        }
        val target = plugin.server.getPlayer(context.getArgument("player", String::class.java)).orElse(null)
        if (target == null || !player.canSee(target)) {
            context.source.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return 0
        }
        if (player === target) {
            context.source.sendMessage(Component.text("You cannot invite yourself.", NamedTextColor.RED))
            return 0
        }
        if (target.aero.guild != null && target.aero.disguise == null) {
            player.sendMessage(
                Component.text()
                    .append(target.displayNameFor(player))
                    .append(Component.text(" is already in a guild.", NamedTextColor.RED))
            )
            return 0
        }
        if (!guildManager.invitePlayer(guild, target, player)) {
            player.sendMessage(
                Component.text()
                    .append(target.displayNameFor(player))
                    .append(Component.text(" was already invited to this guild.", NamedTextColor.RED))
            )
            return 0
        }
        for (onlinePlayer in plugin.server.allPlayers) {
            if (onlinePlayer.aero.guild?._id == guild._id) {
                onlinePlayer.sendMessage(
                    player.identity(), Component.text()
                        .append(Guild.PREFIX)
                        .append(player.displayNameFor(onlinePlayer))
                        .append(Component.text(" invited ", Guild.COLOR_SCHEME))
                        .append(target.displayNameFor(onlinePlayer))
                        .append(Component.text(" to the guild.", Guild.COLOR_SCHEME))
                        .build()
                )
            }
        }
        return 0
    }

    private fun join(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val aeroPlayer = player.aero
        if (aeroPlayer.guild != null) {
            player.sendMessage(Component.text("You are already in a guild.", NamedTextColor.RED))
            return 0
        }
        val guild = guildManager.getGuild(context.getArgument("name", String::class.java))
        if (guild == null) {
            player.sendMessage(Component.text("That guild doesn't exist.", NamedTextColor.RED))
            return 0
        }
        if (!(guildManager.acceptInvite(guild._id, player) || guild.public)) {
            player.sendMessage(Component.text("You haven't been invited to this guild.", NamedTextColor.RED))
            return 0
        }
        val memberCount =
            plugin.database.getCollection("players").find(Filters.eq("guild._id", guild._id)).count()
        if (memberCount >= guild.slots) {
            player.sendMessage(
                Component.text(
                    "This guild has reached the maximum number of members.",
                    NamedTextColor.RED
                )
            )
            return 0
        }
        guildManager.guilds[guild._id] = guild
        aeroPlayer.guild = AeroPlayerGuild(guild._id, 1)
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", player.uniqueId),
            Updates.set("guild", Document("_id", guild._id).append("rank", 1))
        )
        for (onlinePlayer in plugin.server.allPlayers) {
            if (onlinePlayer.aero.guild?._id == guild._id) {
                onlinePlayer.sendMessage(
                    player.identity(),
                    Component.text()
                        .append(Guild.PREFIX)
                        .append(aeroPlayer.displayNameFor(onlinePlayer))
                        .append(Component.text(" joined the guild.", Guild.COLOR_SCHEME))
                )
            }
        }
        player.currentServer.orElse(null)
            ?.sendPluginMessage(MinecraftChannelIdentifier.create("aero", "sync"), SyncUtils.refreshPlayer)
        return 0
    }

    private fun leave(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val aeroPlayer = player.aero
        val playerGuild = aeroPlayer.guild
        if (playerGuild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        if (playerGuild.rank == 0) {
            val otherLeader = plugin.database.getCollection("players").find(
                Filters.and(
                    Filters.eq("guild._id", playerGuild._id),
                    Filters.eq("guild.rank", 0),
                    Filters.not(Filters.eq("_id", player.uniqueId))
                )
            ).first()
            if (otherLeader == null) {
                player.sendMessage(
                    Component.text(
                        "You cannot leave this guild, as you are the only leader.",
                        NamedTextColor.RED
                    )
                )
                return 0
            }
        }
        for (onlinePlayer in plugin.server.allPlayers) {
            if (onlinePlayer.aero.guild?._id == playerGuild._id) {
                onlinePlayer.sendMessage(
                    player.identity(),
                    Component.text()
                        .append(Guild.PREFIX)
                        .append(aeroPlayer.displayNameFor(onlinePlayer))
                        .append(Component.text(" left the guild.", Guild.COLOR_SCHEME))
                )
            }
        }
        plugin.guildManager.handlePlayerQuit(player)
        aeroPlayer.guild = null
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", player.uniqueId),
            Updates.unset("guild")
        )
        player.currentServer.orElse(null)
            ?.sendPluginMessage(MinecraftChannelIdentifier.create("aero", "sync"), SyncUtils.refreshPlayer)
        return 0
    }

    private fun kick(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val playerGuild = player.aero.guild
        if (playerGuild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        if (playerGuild.rank != 0) {
            player.sendMessage(Component.text("You do not have permission to do this.", NamedTextColor.RED))
            return 0
        }
        val target = PlayerUtils.fetchAeroPlayer(context.getArgument("player", String::class.java))
        if (target == null) {
            player.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return 0
        }
        if (player.uniqueId == target._id) {
            player.sendMessage(Component.text("You cannot kick yourself.", NamedTextColor.RED))
            return 0
        }
        if (playerGuild._id != target.guild?._id) {
            player.sendMessage(
                Component.text()
                    .append(target.displayNameFor(player))
                    .append(Component.text(" is not in your guild.", NamedTextColor.RED))
            )
            return 0
        }
        target.guild = null
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", target._id),
            Updates.unset("guild")
        )
        plugin.server.getPlayer(target._id).ifPresent {
            plugin.guildManager.handlePlayerQuit(it)
            it.currentServer.orElse(null)
                ?.sendPluginMessage(MinecraftChannelIdentifier.create("aero", "sync"), SyncUtils.refreshPlayer)
        }
        for (onlinePlayer in plugin.server.allPlayers) {
            if (onlinePlayer.aero.guild?._id == playerGuild._id) {
                onlinePlayer.sendMessage(
                    Identity.nil(), Component.text()
                        .append(Guild.PREFIX)
                        .append(target.displayNameFor(onlinePlayer))
                        .append(Component.text(" was kicked from the guild.", Guild.COLOR_SCHEME))
                )
            }
        }
        return 0
    }

    private fun promote(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val playerGuild = player.aero.guild
        if (playerGuild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        if (playerGuild.rank != 0) {
            player.sendMessage(Component.text("You do not have permission to do this.", NamedTextColor.RED))
            return 0
        }
        val target = PlayerUtils.fetchAeroPlayer(context.getArgument("player", String::class.java))
        if (target == null) {
            player.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return 0
        }
        if (player.uniqueId == target._id) {
            player.sendMessage(Component.text("You cannot promote yourself.", NamedTextColor.RED))
            return 0
        }
        if (playerGuild._id != target.guild?._id) {
            player.sendMessage(
                Component.text()
                    .append(target.displayNameFor(player))
                    .append(Component.text(" is not in your guild.", NamedTextColor.RED))
            )
            return 0
        }
        val guild = guildManager.getGuild(playerGuild._id)
        val targetGuild = target.guild!!
        if (targetGuild.rank == 0) {
            player.sendMessage(
                Component.text(
                    "You cannot promote ${EnglishUtils.a(guild.ranks[0].lowercase())}.",
                    NamedTextColor.RED
                )
            )
            return 0
        }
        if (targetGuild.rank == guild.ranks.size - 1) {
            targetGuild.rank = 0
        } else {
            targetGuild.rank++
        }
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", target._id),
            Updates.set("guild.rank", targetGuild.rank)
        )
        for (onlinePlayer in plugin.server.allPlayers) {
            if (onlinePlayer.aero.guild?._id == playerGuild._id) {
                onlinePlayer.sendMessage(
                    Identity.nil(),
                    Component.text()
                        .append(Guild.PREFIX)
                        .append(target.displayNameFor(onlinePlayer))
                        .append(
                            Component.text(
                                " was promoted to ${guild.ranks[targetGuild.rank]}.",
                                Guild.COLOR_SCHEME
                            )
                        )
                )
            }
        }
        return 0
    }

    private fun demote(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val playerGuild = player.aero.guild
        if (playerGuild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        if (playerGuild.rank != 0) {
            player.sendMessage(Component.text("You do not have permission to do this.", NamedTextColor.RED))
            return 0
        }
        val target = PlayerUtils.fetchAeroPlayer(context.getArgument("player", String::class.java))
        if (target == null) {
            player.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return 0
        }
        if (player.uniqueId == target._id) {
            player.sendMessage(Component.text("You cannot demote yourself.", NamedTextColor.RED))
            return 0
        }
        if (playerGuild._id != target.guild?._id) {
            player.sendMessage(
                Component.text()
                    .append(target.displayNameFor(player))
                    .append(Component.text(" is not in your guild.", NamedTextColor.RED))
            )
            return 0
        }
        val guild = guildManager.getGuild(playerGuild._id)
        val targetGuild = target.guild!!
        if (targetGuild.rank == 1) {
            player.sendMessage(
                Component.text(
                    "You cannot demote ${EnglishUtils.a(guild.ranks[1]).lowercase()}.",
                    NamedTextColor.RED
                )
            )
            return 0
        }
        if (targetGuild.rank == 0) {
            targetGuild.rank = guild.ranks.size - 1
        } else {
            targetGuild.rank--
        }
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", target._id),
            Updates.set("guild.rank", targetGuild.rank)
        )
        for (onlinePlayer in plugin.server.allPlayers) {
            if (onlinePlayer.aero.guild?._id == playerGuild._id) {
                onlinePlayer.sendMessage(
                    Identity.nil(),
                    Component.text()
                        .append(Guild.PREFIX)
                        .append(target.displayNameFor(onlinePlayer))
                        .append(Component.text(" was demoted to ${guild.ranks[targetGuild.rank]}.", Guild.COLOR_SCHEME))
                )
            }
        }
        return 0
    }

    private fun list(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val playerGuild = player.aero.guild
        if (playerGuild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        val guild = guildManager.getGuild(playerGuild._id)
        val message = Component.text()
        for (index in 0 until guild.ranks.size) {
            val i = if (index == 0) 0 else guild.ranks.size - index
            message.append(Component.text("-- ${guild.ranks[i]} --", Guild.COLOR_SCHEME))
                .append(Component.newline())
            val guildPlayers = plugin.database.getCollection("players").find(
                Filters.and(
                    Filters.eq("guild._id", guild._id),
                    Filters.eq("guild.rank", i)
                )
            ).iterator()
            while (guildPlayers.hasNext()) {
                val guildPlayer = AeroPlayer.fromDocument(guildPlayers.next())
                message.append(guildPlayer.displayNameFor(player))
                val targetPlayer = plugin.server.getPlayer(guildPlayer._id).orElse(null)
                val isOnline = targetPlayer != null && context.source.canSee(targetPlayer)
                message.append(Component.text(" • ", if (isOnline) NamedTextColor.GREEN else NamedTextColor.RED))
            }
            if (index != guild.ranks.size - 1) {
                message.append(Component.newline())
            }
        }
        player.sendMessage(message)
        return 0
    }

    private fun rename(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val playerGuild = player.aero.guild
        if (playerGuild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        val guild = guildManager.getGuild(playerGuild._id)
        if (!guild.permissions.canRename(player.aero)) {
            player.sendMessage(Component.text("You do not have permission to do this.", NamedTextColor.RED))
            return 0
        }
        val name = context.getArgument("name", String::class.java)
        val nameError = Guild.getNameError(name)
        if (nameError != null) {
            player.sendMessage(Component.text(nameError, NamedTextColor.RED))
            return 0
        }
        val existing = plugin.database.getCollection("guilds")
            .find(Filters.eq("name", name))
            .collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
            .first()
        if (existing != null) {
            player.sendMessage(Component.text("A guild with this name already exists.", NamedTextColor.RED))
            return 0
        }
        if (guild.name == null) {
            plugin.database.getCollection("guilds").updateOne(
                Filters.and(
                    Filters.eq("_id", guild._id)
                ),
                Updates.set("name", name)
            )
            guild.name = name
            val alreadySent = HashSet<RegisteredServer>()
            for (onlinePlayer in plugin.server.allPlayers) {
                if (onlinePlayer.aero.guild?._id == playerGuild._id) {
                    onlinePlayer.sendMessage(
                        Identity.nil(), Component.text()
                            .append(Guild.PREFIX)
                            .append(player.displayNameFor(onlinePlayer))
                            .append(Component.text(" renamed the guild to ${name}.", Guild.COLOR_SCHEME))
                    )
                    onlinePlayer.currentServer.ifPresent {
                        if (alreadySent.contains(it.server)) return@ifPresent
                        it.sendPluginMessage(
                            MinecraftChannelIdentifier.create("aero", "sync"),
                            SyncUtils.refreshPlayerGuild
                        )
                    }
                }
            }
            return 0
        }
        val hasEnough = plugin.database.getCollection("players").countDocuments(Filters.and(
            Filters.eq("_id", player.uniqueId),
            Filters.gte("balance", Guild.RENAME_COST)
        )) == 1L
        if (!hasEnough) {
            player.sendMessage(Component.text("You don't have enough coins to do this.", NamedTextColor.RED))
            return 0
        }
        player.sendMessage(
            Component.text()
                .append(Guild.PREFIX)
                .append(Component.text("Renaming your guild will cost ", Guild.COLOR_SCHEME))
                .append(Component.text(NumberUtils.formatBalance(Guild.RENAME_COST), NamedTextColor.GREEN))
                .append(Component.text(". Click here to confirm.", Guild.COLOR_SCHEME))
                .clickEvent(ClickEvent.runCommand("/guild rename $name confirm"))
        )
        return 0
    }

    private fun color(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val playerGuild = player.aero.guild
        if (playerGuild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        val guild = guildManager.getGuild(playerGuild._id)
        if (!guild.permissions.canRename(player.aero)) {
            player.sendMessage(Component.text("You do not have permission to do this.", NamedTextColor.RED))
            return 0
        }
        val color = ColorUtils.getColorByName(context.getArgument("color", String::class.java).lowercase())
        if (color == null) {
            player.sendMessage(Component.text("Invalid color.", NamedTextColor.RED))
            return 0
        }
        if (color.asHSV().v() < 0.5) {
            player.sendMessage(Component.text("This color cannot be used as it is too dark.", NamedTextColor.RED))
            return 0
        }

        guild.color = color
        plugin.database.getCollection("guilds").updateOne(
            Filters.eq("_id", guild._id),
            Updates.set("color", color.value())
        )
        val alreadySent = HashSet<RegisteredServer>()
        for (onlinePlayer in plugin.server.allPlayers) {
            if (onlinePlayer.aero.guild?._id == playerGuild._id) {
                onlinePlayer.currentServer.ifPresent {
                    if (alreadySent.contains(it.server)) return@ifPresent
                    it.sendPluginMessage(
                        MinecraftChannelIdentifier.create("aero", "sync"),
                        SyncUtils.refreshPlayerGuild
                    )
                }
            }
        }

        player.sendMessage(Component.text("Success!", NamedTextColor.GREEN))
        return 0
    }

    private fun renameConfirm(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val playerGuild = player.aero.guild
        if (playerGuild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        val guild = guildManager.getGuild(playerGuild._id)
        if (!guild.permissions.canRename(player.aero)) {
            player.sendMessage(Component.text("You do not have permission to do this.", NamedTextColor.RED))
            return 0
        }
        if (guild.name == null) {
            player.sendMessage(Component.text("Renaming an unnamed guild doesn't need confirmation.", NamedTextColor.RED))
            return 0
        }
        val name = context.getArgument("name", String::class.java)
        val nameError = Guild.getNameError(name)
        if (nameError != null) {
            player.sendMessage(Component.text(nameError, NamedTextColor.RED))
            return 0
        }
        val existing = plugin.database.getCollection("guilds")
            .find(Filters.eq("name", name))
            .collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
            .first()
        if (existing != null) {
            player.sendMessage(Component.text("A guild with this name already exists.", NamedTextColor.RED))
            return 0
        }
        val updateResult = plugin.database.getCollection("players").updateOne(
            Filters.and(
                Filters.eq("_id", player.uniqueId),
                Filters.gte("balance", Guild.RENAME_COST)
            ),
            Updates.inc("balance", -Guild.RENAME_COST)
        )
        if (updateResult.matchedCount == 0L) {
            player.sendMessage(Component.text("You do not have enough coins.", NamedTextColor.RED))
            return 0
        }
        player.aero.balance -= Guild.RENAME_COST
        plugin.database.getCollection("guilds").updateOne(
            Filters.eq("_id", guild._id),
            Updates.set("name", name)
        )
        guild.name = name
        val alreadySent = HashSet<RegisteredServer>()
        for (onlinePlayer in plugin.server.allPlayers) {
            if (onlinePlayer.aero.guild?._id == playerGuild._id) {
                onlinePlayer.sendMessage(
                    Identity.nil(), Component.text()
                        .append(Guild.PREFIX)
                        .append(player.displayNameFor(onlinePlayer))
                        .append(Component.text(" renamed the guild to ${name}.", Guild.COLOR_SCHEME))
                )
                onlinePlayer.currentServer.ifPresent {
                    if (alreadySent.contains(it.server)) return@ifPresent
                    it.sendPluginMessage(
                        MinecraftChannelIdentifier.create("aero", "sync"),
                        SyncUtils.refreshPlayerGuild
                    )
                }
            }
        }

        return 0
    }

    private fun public(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val playerGuild = player.aero.guild
        if (playerGuild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        if (playerGuild.rank != 0) {
            player.sendMessage(Component.text("You do not have permission to do this.", NamedTextColor.RED))
            return 0
        }
        val guild = guildManager.getGuild(playerGuild._id)
        if (guild.public) {
            player.sendMessage(Component.text("This guild is already public.", NamedTextColor.RED))
            return 0
        }
        guild.public = true
        plugin.database.getCollection("guilds").updateOne(
            Filters.eq("_id", guild._id),
            Updates.set("public", true)
        )
        for (onlinePlayer in plugin.server.allPlayers) {
            if (onlinePlayer.aero.guild?._id == playerGuild._id) {
                onlinePlayer.sendMessage(
                    Identity.nil(), Component.text()
                        .append(Guild.PREFIX)
                        .append(player.displayNameFor(onlinePlayer))
                        .append(Component.text(" made the guild public.", Guild.COLOR_SCHEME))
                )
            }
        }
        return 0
    }

    private fun private(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val playerGuild = player.aero.guild
        if (playerGuild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        if (playerGuild.rank != 0) {
            player.sendMessage(Component.text("You do not have permission to do this.", NamedTextColor.RED))
            return 0
        }
        val guild = guildManager.getGuild(playerGuild._id)
        if (!guild.public) {
            player.sendMessage(Component.text("This guild is already private.", NamedTextColor.RED))
            return 0
        }
        guild.public = false
        plugin.database.getCollection("guilds").updateOne(
            Filters.eq("_id", guild._id),
            Updates.set("public", false)
        )
        for (onlinePlayer in plugin.server.allPlayers) {
            if (onlinePlayer.aero.guild?._id == playerGuild._id) {
                onlinePlayer.sendMessage(
                    Identity.nil(), Component.text()
                        .append(Guild.PREFIX)
                        .append(player.displayNameFor(onlinePlayer))
                        .append(Component.text(" made the guild private.", Guild.COLOR_SCHEME))
                )
            }
        }
        return 0
    }

    private fun tpa(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val playerGuild = player.aero.guild
        if (playerGuild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        val guild = guildManager.getGuild(playerGuild._id)
        if (!guild.permissions.canTPA(player.aero)) {
            player.sendMessage(Component.text("You do not have permission to do this.", NamedTextColor.RED))
            return 0
        }
        player.sendMessage(
            Identity.nil(), Component.text()
                .append(Guild.PREFIX)
                .append(Component.text("You requested your guild to teleport to you.", Guild.COLOR_SCHEME))
        )
        for (onlinePlayer in plugin.server.allPlayers) {
            if (onlinePlayer.aero.guild?._id == playerGuild._id) {
                if (player == onlinePlayer) continue
                if (plugin.warpManager.createWarpRequest(onlinePlayer, player)) {
                    onlinePlayer.sendMessage(
                        Identity.nil(), Component.text()
                            .append(Guild.PREFIX)
                            .append(player.displayNameFor(onlinePlayer))
                            .append(Component.text(" has requested that you teleport to them.", Guild.COLOR_SCHEME))
                            .append(Component.text(" [Accept]", Guild.COLOR_SCHEME).decorate(TextDecoration.BOLD))
                            .clickEvent(ClickEvent.runCommand("/pwarp " + player.username))
                    )
                }
            }
        }
        return 0
    }

    private fun disband(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val playerGuild = player.aero.guild
        if (playerGuild == null) {
            player.sendMessage(Component.text("You are not in a guild.", NamedTextColor.RED))
            return 0
        }
        if (playerGuild.rank != 0) {
            player.sendMessage(Component.text("You do not have permission to do this.", NamedTextColor.RED))
            return 0
        }
        val guild = playerGuild._id
        plugin.database.getCollection("players").updateMany(
            Filters.eq("guild._id", guild),
            Updates.unset("guild")
        )
        for (onlinePlayer in plugin.server.allPlayers) {
            if (onlinePlayer.aero.guild?._id == guild) {
                onlinePlayer.sendMessage(
                    Identity.nil(), Component.text()
                        .append(Guild.PREFIX)
                        .append(player.displayNameFor(onlinePlayer))
                        .append(Component.text(" has disbanded the guild.", Guild.COLOR_SCHEME))
                )
                onlinePlayer.aero.guild = null
                onlinePlayer.currentServer.ifPresent {
                    it.sendPluginMessage(MinecraftChannelIdentifier.create("aero", "sync"), SyncUtils.refreshPlayer)
                }
            }
        }
        plugin.database.getCollection("guilds").deleteOne(Filters.eq("_id", guild))
        guildManager.guilds.remove(guild)
        for (server in plugin.server.allServers) {
            server.playersConnected.firstOrNull()?.currentServer?.orElse(null)
                ?.sendPluginMessage(MinecraftChannelIdentifier.create("aero", "sync"), SyncUtils.deleteGuild(guild))
        }
        return 0
    }

    private fun leaderboard(context: CommandContext<CommandSource>): Int {
        val page = try {
            context.getArgument("page", Int::class.java)
        } catch (e: IllegalArgumentException) {
            1
        }
        val guilds = plugin.database.getCollection("guilds").aggregate(
            listOf(
                Aggregates.match(Filters.not(Filters.eq("name", null))),
                Aggregates.lookup("players", "_id", "guild._id", "players"),
                Aggregates.unwind("\$players"),
                Aggregates.group(
                    "\$_id",
                    Accumulators.first("name", "\$name"),
                    Accumulators.first("color", "\$color"),
                    Accumulators.sum("balance", "\$players.balance")
                ),
                Aggregates.sort(Sorts.descending("balance")),
                Aggregates.skip((page - 1) * 10),
                Aggregates.limit(10)
            )
        )
        val guildCount = plugin.database.getCollection("guilds").countDocuments(Filters.not(Filters.eq("name", null)))
        val pages = ceil(guildCount.toDouble() / 10).toInt()
        if (page > pages) {
            context.source.sendMessage(Component.text("Invalid page.", NamedTextColor.RED))
            return 0
        }
        val message = Component.text()
        message.append(Component.text("Guild Leaderboard", Guild.COLOR_SCHEME))
        message.append(Component.text(" [$page/$pages]", NamedTextColor.RED))
        message.append(Component.newline())
        for ((i, guild) in guilds.withIndex()) {
            message.append(Component.newline())
            message.append(
                Component.text()
                    .append(Component.text("${i.inc() + ((page - 1) * 10)}. ", NamedTextColor.RED))
                    .append(Component.text("[${guild.getString("name")}]", TextColor.color(guild.getInteger("color"))))
                    .append(Component.text(", ", NamedTextColor.WHITE))
                    .append(Component.text(NumberUtils.formatBalance(guild.getLong("balance")), NamedTextColor.GREEN))
            )
        }
        context.source.sendMessage(message.build())
        return 0
    }
}