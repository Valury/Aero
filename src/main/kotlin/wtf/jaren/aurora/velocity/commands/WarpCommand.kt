package wtf.jaren.aurora.velocity.commands

import com.velocitypowered.api.command.RawCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aurora.velocity.Aurora
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class WarpCommand(val plugin: Aurora) : RawCommand {
    override fun execute(invocation: RawCommand.Invocation) {
        val player = invocation.source() as Player
        val target = plugin.server.getPlayer(invocation.arguments()).orElse(null)
        if (target == null) {
            player.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return
        }
        if (!plugin.warpManager.acceptWarpRequest(player, target)) {
            player.sendMessage(
                Component.text(
                    "You do not have an active warp request to that player.",
                    NamedTextColor.RED
                )
            )
            return
        }
        if (plugin.warpManager.isLocked(target)) {
            player.sendMessage(
                Component.text(
                    "That player is currently locked from warping players to them.",
                    NamedTextColor.RED
                )
            )
            return
        }
        val outputStream = ByteArrayOutputStream()
        val output = DataOutputStream(outputStream)
        output.writeInt(1)
        output.writeUTF(player.uniqueId.toString())
        val serverConnection = target.currentServer.orElseThrow()
        serverConnection.sendPluginMessage(
            MinecraftChannelIdentifier.create("aurora", "warp"),
            outputStream.toByteArray()
        )
        if (player.currentServer.orElseThrow().server !== serverConnection.server) {
            player.createConnectionRequest(serverConnection.server).fireAndForget()
        }
    }
}