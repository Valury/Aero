package wtf.jaren.aero.velocity.objects

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import com.velocitypowered.api.scheduler.ScheduledTask
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import wtf.jaren.aero.velocity.enums.PartyAddReason
import wtf.jaren.aero.velocity.enums.PartyRemoveReason
import wtf.jaren.aero.velocity.managers.PartyManager
import wtf.jaren.aero.velocity.utils.displayNameFor
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.concurrent.TimeUnit

class Party(private val manager: PartyManager, var leader: Player) {

    val members = LinkedHashSet<Player>()

    var public = false

    init {
        members.add(leader)
    }

    private val invites: HashMap<Player, ScheduledTask> = HashMap()

    fun addMember(player: Player, reason: PartyAddReason) {
        if (invites.containsKey(player)) {
            invites[player]!!.cancel()
            invites.remove(player)
        }
        members.add(player)
        for (member in members) {
            member.sendMessage(
                Identity.nil(), Component.text()
                    .append(PREFIX)
                    .append(player.displayNameFor(member))
                    .append(Component.text(reason.message, COLOR_SCHEME))
            )
        }
    }

    fun removeMember(player: Player, reason: PartyRemoveReason) {
        members.remove(player)
        for (member in members) {
            member.sendMessage(
                Identity.nil(), Component.text()
                    .append(PREFIX)
                    .append(player.displayNameFor(member))
                    .append(Component.text(reason.message, COLOR_SCHEME))
            )
        }
        if (!checkAutoDisband() && player === leader) {
            leader = members.iterator().next()
        }
    }

    fun invitePlayer(player: Player, inviter: Player) {
        val scheduledTask = manager.plugin.server.scheduler.buildTask(manager.plugin) {
            invites.remove(player)
            checkAutoDisband()
        }.delay(1, TimeUnit.MINUTES).schedule()
        invites[player] = scheduledTask
        for (member in members) {
            member.sendMessage(
                Identity.nil(), Component.text()
                    .append(PREFIX)
                    .append(inviter.displayNameFor(member))
                    .append(Component.text(" invited ", COLOR_SCHEME))
                    .append(player.displayNameFor(member))
                    .append(Component.text(" to the party.", COLOR_SCHEME))
            )
        }
        player.sendMessage(
            inviter.identity(),
            Component.text()
                .append(inviter.displayNameFor(player))
                .append(Component.text(" invited you to their party.", COLOR_SCHEME))
                .append(Component.newline())
                .append(Component.text("[Click here]").decoration(TextDecoration.BOLD, true))
                .append(Component.text(" to accept! You have 60 seconds to accept.", COLOR_SCHEME))
                .clickEvent(ClickEvent.runCommand("/party join " + inviter.username))
                .build()
        )
    }

    fun isInvited(player: Player): Boolean {
        return invites.containsKey(player)
    }

    fun broadcast(identity: Identity, message: Component) {
        for (member in members) {
            member.sendMessage(identity, Component.text().append(PREFIX).append(message))
        }
    }

    fun warpTo(player: Player) {
        val serverConnection = player.currentServer.orElseThrow()
        val outputStream = ByteArrayOutputStream()
        val output = DataOutputStream(outputStream)
        output.writeInt(members.size)
        for (member in members) {
            if (player === member) {
                member.sendMessage(
                    Identity.nil(),
                    PREFIX.append(Component.text("You warped your party to you.", COLOR_SCHEME))
                )
            } else {
                member.sendMessage(
                    player.identity(), Component.text()
                        .append(PREFIX)
                        .append(player.displayNameFor(member))
                        .append(Component.text(" warped you to them.", COLOR_SCHEME))
                )
            }
            output.writeUTF(member.uniqueId.toString())
        }
        serverConnection.sendPluginMessage(
            MinecraftChannelIdentifier.create("aero", "warp"),
            outputStream.toByteArray()
        )
        for (member in members) {
            if (member.currentServer.orElseThrow().server !== serverConnection.server) {
                member.createConnectionRequest(serverConnection.server).fireAndForget()
            }
        }
    }

    private fun checkAutoDisband(): Boolean {
        if (members.size == 1 && invites.size == 0 || members.size == 0) {
            broadcast(
                Identity.nil(),
                Component.text(
                    "The party was disbanded because all invites have expired and all members have left.",
                    COLOR_SCHEME
                )
            )
            manager.disbandParty(this)
            return true
        }
        return false
    }

    companion object {
        val COLOR_SCHEME: NamedTextColor = NamedTextColor.AQUA
        val PREFIX = Component.empty().append(Component.text("Party / ", COLOR_SCHEME))
    }
}