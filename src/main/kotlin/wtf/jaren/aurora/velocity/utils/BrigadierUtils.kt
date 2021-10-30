package wtf.jaren.aurora.velocity.utils

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.CommandSource
import wtf.jaren.aurora.velocity.Aurora
import java.util.concurrent.CompletableFuture

object BrigadierUtils {
    fun getSuggestionsWithoutSender(
        context: CommandContext<CommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val input = builder.remainingLowerCase
        for (player in Aurora.instance.server.allPlayers) {
            if (player.username.lowercase().startsWith(input) && player !== context.source && context.source.canSee(player)) {
                builder.suggest(player.username)
            }
        }
        return builder.buildFuture()
    }

    /*fun getSuggestions(
        context: CommandContext<CommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val input = builder.remainingLowerCase
        for (player in Aurora.instance.server.allPlayers) {
            if (player.username.lowercase().startsWith(input)) {
                builder.suggest(player.username)
            }
        }
        return builder.buildFuture()
    }*/
}