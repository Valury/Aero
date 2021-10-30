package wtf.jaren.aurora.velocity.objects

import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.command.CommandMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import wtf.jaren.aurora.shared.objects.AuroraPlayer
import wtf.jaren.aurora.velocity.Aurora

abstract class Channel(protected val plugin: Aurora) {
    abstract val id: String

    abstract fun isMember(player: AuroraPlayer): Boolean

    open fun shouldRegisterCommand(player: AuroraPlayer): Boolean {
        return isMember(player)
    }

    abstract fun sendMessage(sender: AuroraPlayer, message: String)

    open fun getNoMembershipMessage(player: AuroraPlayer): Component {
        return Component.text("Missing permission.", NamedTextColor.RED)
    }

    open fun getCommandMeta(commandManager: CommandManager): CommandMeta {
        return commandManager.metaBuilder(javaClass.simpleName.substring(0, 1).lowercase() + "c")
            .build()
    }
}