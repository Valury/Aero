package wtf.jaren.aero.velocity.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.enums.PartyAddReason
import wtf.jaren.aero.velocity.enums.PartyRemoveReason
import wtf.jaren.aero.velocity.enums.PunishmentType
import wtf.jaren.aero.velocity.managers.PartyManager
import wtf.jaren.aero.velocity.objects.Party
import wtf.jaren.aero.velocity.utils.BrigadierUtils
import wtf.jaren.aero.velocity.utils.canSee
import wtf.jaren.aero.velocity.utils.displayNameFor
import java.util.concurrent.CompletableFuture

class PartyCommand(private val plugin: Aero) {
    private val partyManager: PartyManager = plugin.partyManager
    fun register() {
        val commandManager = plugin.server.commandManager
        val totalNode = LiteralArgumentBuilder
            .literal<CommandSource>("party")
            .requires { source: CommandSource -> source is Player }
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
                .literal<CommandSource>("add")
                .requires { source: CommandSource -> source.hasPermission("aero.party.add") }
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { obj: CommandContext<CommandSource>, context: SuggestionsBuilder ->
                        BrigadierUtils.getSuggestionsWithoutSender(obj, context)
                    }
                    .executes { context: CommandContext<CommandSource> -> add(context) }
                    .build())
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("join")
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { obj: CommandContext<CommandSource>, context: SuggestionsBuilder ->
                        BrigadierUtils.getSuggestionsWithoutSender(obj, context)
                    }
                    .executes { context: CommandContext<CommandSource> -> join(context) }
                    .build())
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("promote")
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { context: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                        getPartyMemberSuggestionsWithoutSender(
                            context,
                            builder
                        )
                    }
                    .executes { context: CommandContext<CommandSource> -> promote(context) }
                    .build())
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("kick")
                .then(RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.string())
                    .suggests { context: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                        getPartyMemberSuggestionsWithoutSender(
                            context,
                            builder
                        )
                    }
                    .executes { context: CommandContext<CommandSource> -> kick(context) }
                    .build())
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("leave")
                .executes { context: CommandContext<CommandSource> -> leave(context) }
                .build()
            )
            .then(LiteralArgumentBuilder
                .literal<CommandSource>("warp")
                .executes { context: CommandContext<CommandSource> -> warp(context) }
                .build()
            )
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
            .executes { context: CommandContext<CommandSource> -> help(context) }
            .build()
        commandManager.register(commandManager.metaBuilder("p").build(), BrigadierCommand(totalNode))
    }

    private fun help(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val party = partyManager.getPlayerParty(player)

        val builder = Component.text()
        builder
            .append(Component.text("Party commands", Party.COLOR_SCHEME))
            .append(Component.newline())
            .append(Component.text("/p invite <player>"))
        if (party == null) {
            builder
                .append(Component.newline())
                .append(Component.text("/p join <player>"))
        } else {
            builder
                .append(Component.newline())
                .append(Component.text("/p leave"))
            if (player == party.leader) {
                builder
                    .append(Component.newline())
                    .append(Component.text("/p promote"))
                    .append(Component.newline())
                    .append(Component.text("/p warp"))
                    .append(Component.newline())
                    .append(Component.text("/p " + (if (party.public) "private" else "public")))
            }
        }
        context.source.sendMessage(builder.build())
        return 0
    }

    private fun invite(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val target = plugin.server.getPlayer(context.getArgument("player", String::class.java)).orElse(null)
        if (target == null || !player.canSee(target)) {
            context.source.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return 0
        }
        if (player === target) {
            context.source.sendMessage(Component.text("You cannot invite yourself.", NamedTextColor.RED))
            return 0
        }
        val punishment =
            plugin.punishmentManager.getActivePunishment(player, PunishmentType.MUTE)
        if (punishment != null) {
            context.source.sendMessage(punishment.getMessage(false))
            return 0
        }
        val party = partyManager.getPlayerParty(player) ?: partyManager.createParty(player)
        if (party.members.contains(target)) {
            context.source.sendMessage(
                Component.text()
                    .append(target.displayNameFor(player))
                    .append(Component.text(" is already in this party.", NamedTextColor.RED))
            )
            return 0
        }
        if (party.isInvited(target)) {
            context.source.sendMessage(
                Component.text()
                    .append(target.displayNameFor(player))
                    .append(Component.text(" was already invited to this party.", NamedTextColor.RED))
            )
            return 0
        }
        party.invitePlayer(target, player)
        return 0
    }

    private fun add(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val target = plugin.server.getPlayer(context.getArgument("player", String::class.java)).orElse(null)
        if (target == null || !player.canSee(target)) {
            context.source.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return 0
        }
        if (player === target) {
            context.source.sendMessage(Component.text("You cannot add yourself.", NamedTextColor.RED))
            return 0
        }
        if (partyManager.getPlayerParty(target) != null) {
            context.source.sendMessage(
                Component.text()
                    .append(target.displayNameFor(player))
                    .append(Component.text(" is already in a party.", NamedTextColor.RED))
            )
            return 0
        }
        val punishment =
            plugin.punishmentManager.getActivePunishment(player, PunishmentType.MUTE)
        if (punishment != null) {
            context.source.sendMessage(punishment.getMessage(false))
            return 0
        }
        val party = partyManager.getPlayerParty(player) ?: partyManager.createParty(player)
        party.addMember(target, PartyAddReason.ADDED)
        return 0
    }

    private fun join(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val target = plugin.server.getPlayer(context.getArgument("player", String::class.java)).orElse(null)
        if (target == null || !player.canSee(target)) {
            context.source.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return 0
        }
        if (player === target) {
            context.source.sendMessage(Component.text("You cannot join yourself.", NamedTextColor.RED))
            return 0
        }
        if (partyManager.getPlayerParty(player) != null) {
            context.source.sendMessage(Component.text("You are already in a party.", NamedTextColor.RED))
            return 0
        }
        val party = partyManager.getPlayerParty(target)
        if (party == null || !(party.isInvited(player) || party.public || player.hasPermission("aero.party.override"))) {
            context.source.sendMessage(Component.text("You have not been invited to this party.", NamedTextColor.RED))
            return 0
        }
        val punishment = plugin.punishmentManager.getActivePunishment(player, PunishmentType.MUTE)
        if (punishment != null) {
            context.source.sendMessage(punishment.getMessage(false))
            return 0
        }
        party.addMember(player, PartyAddReason.JOINED)
        return 0
    }

    private fun promote(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val party = partyManager.getPlayerParty(player)
        if (party == null) {
            context.source.sendMessage(Component.text("You are not in a party.", NamedTextColor.RED))
            return 0
        }
        if (!(party.leader === player || player.hasPermission("aero.party.override"))) {
            context.source.sendMessage(Component.text("You are not the party leader.", NamedTextColor.RED))
            return 0
        }
        val target = plugin.server.getPlayer(context.getArgument("player", String::class.java)).orElse(null)
        if (target == null || !player.canSee(target)) {
            context.source.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return 0
        }
        if (!party.members.contains(target)) {
            context.source.sendMessage(
                Component.text()
                    .append(target.displayNameFor(player))
                    .append(Component.text(" is not in this party.", NamedTextColor.RED))
            )
            return 0
        }
        party.leader = target
        for (member in party.members) {
            member.sendMessage(
                Identity.nil(), Component.text()
                    .append(Party.PREFIX)
                    .append(target.displayNameFor(member))
                    .append(Component.text(" was promoted to party leader.", Party.COLOR_SCHEME))
            )
        }
        return 0
    }

    private fun kick(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val party = partyManager.getPlayerParty(player)
        if (party == null) {
            context.source.sendMessage(Component.text("You are not in a party.", NamedTextColor.RED))
            return 0
        }
        if (party.leader !== player) {
            context.source.sendMessage(Component.text("You are not the party leader.", NamedTextColor.RED))
            return 0
        }
        val target = plugin.server.getPlayer(context.getArgument("player", String::class.java)).orElse(null)
        if (target == null || !player.canSee(target)) {
            context.source.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return 0
        }
        if (!party.members.contains(target)) {
            context.source.sendMessage(
                Component.text()
                    .append(target.displayNameFor(player))
                    .append(Component.text(" is not in this party.", NamedTextColor.RED))
            )
            return 0
        }
        party.removeMember(target, PartyRemoveReason.KICKED)
        return 0
    }

    private fun leave(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val party = partyManager.getPlayerParty(player)
        if (party != null) {
            party.removeMember(player, PartyRemoveReason.LEFT)
            player.sendMessage(Party.PREFIX.append(Component.text("You left the party.", Party.COLOR_SCHEME)))
        } else {
            player.sendMessage(Component.text("You are not in a party.", NamedTextColor.RED))
        }
        return 0
    }

    private fun warp(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val party = partyManager.getPlayerParty(player)
        if (party == null) {
            player.sendMessage(Component.text("You are not in a party.", NamedTextColor.RED))
            return 0
        }
        if (!(party.leader === player || player.hasPermission("aero.party.override"))) {
            context.source.sendMessage(Component.text("You are not the party leader.", NamedTextColor.RED))
            return 0
        }
        if (plugin.warpManager.isLocked(player)) {
            context.source.sendMessage(Component.text("You cannot do this right now.", NamedTextColor.RED))
            return 0
        }
        party.warpTo(player)
        return 0
    }

    private fun public(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val party = partyManager.getPlayerParty(player)
        if (party == null) {
            player.sendMessage(Component.text("You are not in a party.", NamedTextColor.RED))
            return 0
        }
        if (!(party.leader === player || player.hasPermission("aero.party.override"))) {
            context.source.sendMessage(Component.text("You are not the party leader.", NamedTextColor.RED))
            return 0
        }
        if (party.public) {
            context.source.sendMessage(Component.text("The party is already public.", NamedTextColor.RED))
            return 0
        }
        party.public = true
        for (member in party.members) {
            member.sendMessage(
                Identity.nil(), Component.text()
                    .append(Party.PREFIX)
                    .append(player.displayNameFor(member))
                    .append(Component.text(" made the party public.", Party.COLOR_SCHEME))
            )
        }
        return 0
    }

    private fun private(context: CommandContext<CommandSource>): Int {
        val player = context.source as Player
        val party = partyManager.getPlayerParty(player)
        if (party == null) {
            player.sendMessage(Component.text("You are not in a party.", NamedTextColor.RED))
            return 0
        }
        if (!(party.leader === player || player.hasPermission("aero.party.override"))) {
            context.source.sendMessage(Component.text("You are not the party leader.", NamedTextColor.RED))
            return 0
        }
        if (!party.public) {
            context.source.sendMessage(Component.text("The party is already private.", NamedTextColor.RED))
            return 0
        }
        party.public = false
        for (member in party.members) {
            member.sendMessage(
                Identity.nil(), Component.text()
                    .append(Party.PREFIX)
                    .append(player.displayNameFor(member))
                    .append(Component.text(" made the party private.", Party.COLOR_SCHEME))
            )
        }
        return 0
    }

    private fun getPartyMemberSuggestionsWithoutSender(
        context: CommandContext<CommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions?> {
        val input = builder.remainingLowerCase
        partyManager.getPlayerParty((context.source as Player))?.let {
            for (player in it.members) {
                if (player.username.lowercase().startsWith(input) && player !== context.source) {
                    builder.suggest(player.username)
                }
            }
        }
        return builder.buildFuture()
    }

}