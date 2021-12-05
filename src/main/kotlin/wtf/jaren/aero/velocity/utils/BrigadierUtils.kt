package wtf.jaren.aero.velocity.utils

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.CommandSource
import wtf.jaren.aero.velocity.Aero
import java.util.concurrent.CompletableFuture

object BrigadierUtils {
    fun getSuggestionsWithoutSender(
        context: CommandContext<CommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val input = builder.remainingLowerCase
        for (player in Aero.instance.server.allPlayers) {
            if (player.username.lowercase().startsWith(input) && player !== context.source && context.source.canSee(player)) {
                builder.suggest(player.username)
            }
        }
        return builder.buildFuture()
    }
}