package wtf.jaren.aurora.velocity.commands

import com.velocitypowered.api.command.RawCommand
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bson.Document
import wtf.jaren.aurora.shared.objects.AuroraPlayer
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.utils.NumberUtils
import wtf.jaren.aurora.velocity.utils.fullDisplayName
import wtf.jaren.aurora.velocity.utils.fullDisplayNameFor
import kotlin.math.ceil

class BalanceTopCommand(val plugin: Aurora) : RawCommand {
    override fun execute(invocation: RawCommand.Invocation) {
        val page = try {
            val arg = invocation.arguments().toInt()
            if (arg <= 0) 1 else arg
        } catch (e: NumberFormatException) {
            1
        }
        val players = plugin.database.getCollection("players").find().sort(Document("balance", -1)).limit(10)
            .skip((page - 1) * 10)
        val playerCount = plugin.database.getCollection("players").countDocuments()
        val pages = ceil(playerCount.toDouble() / 10).toInt()
        if (page > pages) {
            invocation.source().sendMessage(Component.text("Invalid page.", NamedTextColor.RED))
            return
        }
        val message = Component.text()
        message.append(Component.text("Player Leaderboard", NamedTextColor.GREEN))
        message.append(Component.text(" [$page/$pages]", NamedTextColor.RED))
        message.append(Component.newline())
        for ((i, playerDocument) in players.withIndex()) {
            message.append(Component.newline())
            val player = AuroraPlayer.fromDocument(playerDocument)
            message.append(
                Component.text()
                    .append(Component.text("${i.inc() + ((page - 1) * 10)}. ", NamedTextColor.RED))
                    .append(if (invocation.source() is Player) player.fullDisplayNameFor(invocation.source() as Player) else player.fullDisplayName)
                    .append(Component.text(", ", NamedTextColor.WHITE))
                    .append(Component.text(NumberUtils.formatBalance(player.balance), NamedTextColor.GREEN))
            )
        }
        invocation.source().sendMessage(message.build())
    }
}