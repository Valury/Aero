package wtf.jaren.aurora.spigot.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.LuckPermsProvider
import org.bukkit.entity.Player
import wtf.jaren.aurora.spigot.Aurora


val Player.aurora
    get() = Aurora.instance.playerManager.getAuroraPlayer(this)

val Player.prefix: String
    get() {
        return if (this.aurora.disguise == null) {
            LuckPermsProvider.get().userManager.getUser(this.uniqueId)!!.cachedData.metaData.prefix?.replace('&', '§')
                ?: ""
        } else {
            LuckPermsProvider.get().groupManager.getGroup(this.aurora.disguise!!.rank)!!.cachedData.metaData.prefix?.replace('&', '§') ?: ""
        }
    }

val Player.group: String
    get() {
        return if (this.aurora.disguise == null) {
            LuckPermsProvider.get().userManager.getUser(this.uniqueId)!!.primaryGroup
        } else {
            this.aurora.disguise!!.rank
        }
    }

val Player.suffix: String
    get() {
        return if (this.aurora.disguise == null && this.aurora.guild != null) {
            val guild = Aurora.instance.guildManager.getGuild(this.aurora.guild!!._id)
            return if (guild.name != null) LegacyComponentSerializer.builder().hexColors().build().serialize(
                Component.text(" [${guild.name}]", guild.color)
            ) else ""
        } else {
            ""
        }
    }

fun Player.displayNameFor(player: Player): TextComponent {
    val name = if (player.aurora.preferences.showNicks) {
        this.aurora.effectiveNick ?: this.name
    } else {
        this.name
    }
    return LegacyComponentSerializer.legacySection().deserialize(
        this.prefix + name + this.suffix
    )
}

fun Player.updateDisplayName() {
    this.displayName(
        LegacyComponentSerializer.legacySection().deserialize(
            this.prefix + (this.aurora.effectiveNick ?: this.name) + this.suffix
        )
    )
}