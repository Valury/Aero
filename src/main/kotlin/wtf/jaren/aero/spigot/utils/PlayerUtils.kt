package wtf.jaren.aero.spigot.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.LuckPermsProvider
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import wtf.jaren.aero.shared.objects.AeroPlayer
import wtf.jaren.aero.spigot.Aero


val Player.aero
    get() = Aero.instance.playerManager.getAeroPlayer(this)

val Player.prefix: String
    get() {
        return if (this.aero.disguise == null) {
            LuckPermsProvider.get().userManager.getUser(this.uniqueId)!!.cachedData.metaData.prefix?.replace('&', '§')
                ?: ""
        } else {
            LuckPermsProvider.get().groupManager.getGroup(this.aero.disguise!!.rank)!!.cachedData.metaData.prefix?.replace('&', '§') ?: ""
        }
    }

val Player.group: String
    get() {
        return if (this.aero.disguise == null) {
            LuckPermsProvider.get().userManager.getUser(this.uniqueId)!!.primaryGroup
        } else {
            this.aero.disguise!!.rank
        }
    }

val Player.suffix: String
    get() {
        return if (this.aero.disguise == null && this.aero.guild != null) {
            val guild = Aero.instance.guildManager.getGuild(this.aero.guild!!._id)
            return if (guild.name != null) LegacyComponentSerializer.builder().hexColors().build().serialize(
                Component.text(" [${guild.name}]", guild.color)
            ) else ""
        } else {
            ""
        }
    }
val Player.actualProtocolVersion: Int
    get() {
        return if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
            ViaVersionUtil.getPlayerVersion(this)
        } else {
            this.protocolVersion
        }
    }
fun Player.prefixFor(player: Player): String {
    return if (player.actualProtocolVersion >= 393) this.prefix else ChatUtils.convertUnicodePrefixToColoredText(this.prefix)
}

fun Player.displayNameFor(player: Player): TextComponent {
    val name = if (player.aero.preferences.showNicks) {
        this.aero.effectiveNick ?: this.name
    } else {
        this.name
    }
    return LegacyComponentSerializer.legacySection().deserialize(
        this.prefixFor(player) + name + this.suffix
    )
}

fun Player.updateDisplayName() {
    this.displayName(
        LegacyComponentSerializer.legacySection().deserialize(
            this.prefix + (this.aero.effectiveNick ?: this.name) + this.suffix
        )
    )
}