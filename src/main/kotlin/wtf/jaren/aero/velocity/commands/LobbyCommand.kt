package wtf.jaren.aero.velocity.commands

import com.velocitypowered.api.command.RawCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer

class LobbyCommand(private val server: ProxyServer) : RawCommand {
    override fun execute(invocation: RawCommand.Invocation) {
        (invocation.source() as Player).createConnectionRequest(server.getServer("lobby").orElseThrow())
            .fireAndForget()
    }
}