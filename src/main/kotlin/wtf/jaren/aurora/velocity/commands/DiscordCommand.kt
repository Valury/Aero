package wtf.jaren.aurora.velocity.commands

import com.velocitypowered.api.command.RawCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor

class DiscordCommand : RawCommand {
    override fun execute(invocation: RawCommand.Invocation) {
        invocation.source().sendMessage(Component.text("Discord: https://discord.gg/CTbPAVxMDS", TextColor.color(0x5865F2)).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/CTbPAVxMDS")))
    }

}