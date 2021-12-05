package wtf.jaren.aero.velocity.objects

import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.command.CommandMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aero.shared.objects.AeroPlayer
import wtf.jaren.aero.velocity.Aero

abstract class Channel(protected val plugin: Aero) {
    abstract val id: String

    abstract fun isMember(player: AeroPlayer): Boolean

    open fun shouldRegisterCommand(player: AeroPlayer): Boolean {
        return isMember(player)
    }

    abstract fun sendMessage(sender: AeroPlayer, message: String)

    open fun getNoMembershipMessage(player: AeroPlayer): Component {
        return Component.text("Missing permission.", NamedTextColor.RED)
    }

    open fun getCommandMeta(commandManager: CommandManager): CommandMeta {
        return commandManager.metaBuilder(javaClass.simpleName.substring(0, 1).lowercase() + "c")
            .build()
    }
}