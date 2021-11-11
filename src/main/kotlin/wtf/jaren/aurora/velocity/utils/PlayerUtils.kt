package wtf.jaren.aurora.velocity.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import com.mongodb.client.model.Filters
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import wtf.jaren.aurora.shared.objects.AuroraPlayer
import wtf.jaren.aurora.shared.objects.Guild
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.objects.PartialPlayer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.regex.Pattern

val Player.aurora: AuroraPlayer
    get() = Aurora.instance.playerManager.getAuroraPlayer(this)

val Player.prefix: String
    get() {
        return if (this.aurora.disguise == null) {
            LuckPermsProvider.get().userManager.getUser(this.uniqueId)!!.cachedData.metaData.prefix?.replace('&', '§')
                ?: ""
        } else {
            LuckPermsProvider.get().groupManager.getGroup(this.aurora.disguise!!.rank)!!.cachedData.metaData.prefix?.replace('&', '§')
                ?: ""
        }
    }

val Player.displayName: TextComponent
    get() = LegacyComponentSerializer.legacySection().deserialize(this.prefix + (this.aurora.effectiveNick ?: this.username))

val Player.fullDisplayName: TextComponent
    get() {
        return if (this.aurora.disguise == null && this.aurora.guild != null) {
            val guild = Aurora.instance.guildManager.getGuild(this.aurora.guild!!._id)
            return if (guild.name != null) displayName.append(
                Component.text(
                    " [${guild.name}]",
                    Guild.COLOR_SCHEME
                )
            ) else displayName
        } else {
            displayName
        }
    }

fun Player.displayNameFor(player: Player): TextComponent {
    val name = if (player.aurora.preferences.showNicks) {
        this.aurora.effectiveNick ?: this.username
    } else {
        this.username
    }
    return LegacyComponentSerializer.legacySection().deserialize(this.prefix + name)
}

fun Player.fullDisplayNameFor(player: Player): TextComponent {
    return if (this.aurora.guild != null) {
        val guild = Aurora.instance.guildManager.getGuild(this.aurora.guild!!._id)
        return if (this.aurora.disguise == null && guild.name != null) displayNameFor(player).append(
            Component.text(
                " [${guild.name}]",
                Guild.COLOR_SCHEME
            )
        ) else displayNameFor(player)
    } else {
        displayNameFor(player)
    }
}


val AuroraPlayer.luckperms: User
    get() = LuckPermsProvider.get().userManager.getUser(_id) ?: LuckPermsProvider.get().userManager.loadUser(_id).join()

val AuroraPlayer.displayName: TextComponent
    get() {
        return LegacyComponentSerializer.legacySection().deserialize(
            (this.luckperms.cachedData.metaData.prefix?.replace('&', '§')
                ?: "") + (this.nick ?: this.name)
        )
    }

val AuroraPlayer.fullDisplayName: TextComponent
    get() {
        return if (this.guild != null) {
            displayName.append(
                Component.text(
                    " [${Aurora.instance.guildManager.getGuild(this.guild!!._id).name}]",
                    Guild.COLOR_SCHEME
                )
            )
        } else {
            displayName
        }
    }

fun AuroraPlayer.displayNameFor(player: Player): TextComponent {
    val name = if (player.aurora.preferences.showNicks) {
        this.nick ?: this.name
    } else {
        this.name
    }
    return LegacyComponentSerializer.legacySection().deserialize(
        (this.luckperms.cachedData.metaData.prefix?.replace('&', '§')
            ?: "") + name
    )
}

fun AuroraPlayer.fullDisplayNameFor(player: Player): TextComponent {
    return if (this.guild != null) {
        displayNameFor(player).append(
            Component.text(
                " [${Aurora.instance.guildManager.getGuild(this.guild!!._id).name}]",
                Guild.COLOR_SCHEME
            )
        )
    } else {
        displayNameFor(player)
    }
}

fun AuroraPlayer.hasPermission(permission: String): Boolean {
    return this.luckperms.cachedData.permissionData.checkPermission(permission).asBoolean()
}

fun CommandSource.canSee(player: Player): Boolean {
    return if (this is Player) {
        this.hasPermission("aurora.vanish") || !player.aurora.vanished
    } else {
        true
    }
}


object PlayerUtils {
    private val UUID_REGEX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})")

    fun fetchAuroraPlayer(name: String): AuroraPlayer? {
        var target = Aurora.instance.server.getPlayer(name).orElse(null)?.aurora
        if (target == null) {
            val document = Aurora.instance.database.getCollection("players")
                .find(Filters.eq("name", name))
                .collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
                .first() ?: return null
            target = AuroraPlayer.fromDocument(document)
            val onlinePlayer = Aurora.instance.server.getPlayer(target._id).orElse(null)
            if (onlinePlayer != null) {
                target = onlinePlayer.aurora
            }
        }
        return target
    }

    fun fetchOfflinePlayer(name: String): PartialPlayer? {
        if (name.length > 16) {
            return null
        }
        val plugin: Aurora = Aurora.instance
        val player = plugin.server.getPlayer(name).orElse(null)
        if (player != null && !plugin.playerManager.isDisguised(player)) {
            return PartialPlayer(player.uniqueId, player.username)
        }
        val document = plugin.database.getCollection("players")
            .find(Filters.eq("name", name))
            .collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
            .first()
        if (document != null) {
            return PartialPlayer(
                document.get("_id", UUID::class.java),
                document.getString("name")
            )
        }
        val request: HttpRequest = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(Gson().toJson(listOf(name))))
            .uri(URI.create("https://api.mojang.com/profiles/minecraft"))
            .header("Content-Type", "application/json")
            .build()
        val response: HttpResponse<String> =
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        val players: List<JsonObject> =
            Gson().fromJson(response.body(), object : TypeToken<List<JsonObject>>() {}.type)
        if (players.isEmpty()) {
            return null
        }
        val uuid = UUID_REGEX.matcher(players[0]["id"].asString).replaceAll("$1-$2-$3-$4-$5")
        return PartialPlayer(UUID.fromString(uuid), players[0]["name"].asString)
    }

    fun fetchOfflinePlayer(uuid: UUID): PartialPlayer? {
        val plugin: Aurora = Aurora.instance
        val player = plugin.server.getPlayer(uuid).orElse(null)
        if (player != null && !plugin.playerManager.isDisguised(player)) {
            return PartialPlayer(player.uniqueId, player.username)
        }
        val document = plugin.database.getCollection("players")
            .find(Filters.eq("_id", uuid))
            .collation(Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build())
            .first()
        if (document != null) {
            return PartialPlayer(
                document.get("_id", UUID::class.java),
                document.getString("name")
            )
        }
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.mojang.com/user/profiles/$uuid/names"))
            .build()
        val response: HttpResponse<String> =
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 204) {
            return null
        }
        val names: List<JsonObject> =
            Gson().fromJson(response.body(), object : TypeToken<List<JsonObject>>() {}.type)
        return PartialPlayer(uuid, names[names.size - 1]["name"].asString)
    }

    fun fetchPlayerProfile(uuid: UUID): JsonObject? {
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/${uuid.toString().replace("-", "")}?unsigned=false"))
            .build()
        val response: HttpResponse<String> =
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 204) {
            return null
        }
        return Gson().fromJson(response.body(), object : TypeToken<JsonObject>() {}.type)
    }

    fun fetchPlayerIP(uuid: UUID): String? {
        val plugin: Aurora = Aurora.instance
        val player = plugin.server.getPlayer(uuid).orElse(null)
        if (player != null) {
            return player.remoteAddress.address.hostAddress
        }
        return plugin.database.getCollection("players").find(Filters.eq("_id", uuid)).first()?.getString("ip")
    }
}