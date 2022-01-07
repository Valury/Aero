package wtf.jaren.aero.velocity

import com.google.inject.Inject
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import org.bukkit.Bukkit
import org.slf4j.Logger
import wtf.jaren.aero.velocity.commands.*
import wtf.jaren.aero.velocity.discord.DiscordClient
import wtf.jaren.aero.velocity.listeners.*
import wtf.jaren.aero.velocity.managers.*
import wtf.jaren.aero.velocity.plan.AeroExtension
import wtf.jaren.aero.velocity.plan.PlanAPI

@Plugin(
    id = "aero",
    name = "Aero",
    version = "1.0",
    authors = ["Jaren"],
    dependencies = [Dependency(id = "luckperms"), Dependency(id = "plan", optional = true)]
)
class Aero @Inject constructor(val server: ProxyServer, val logger: Logger) {

    val isProd = (System.getenv("PROD") ?: "false").toBoolean()

    val isStable = true

    @JvmField
    val database: MongoDatabase =
        MongoClients.create(System.getenv("MONGO_CONNECTION_STRING"))
            .getDatabase(System.getenv("MONGO_DATABASE"))

    val playerManager = PlayerManager(this)

    val channelManager = ChannelManager(this)

    val punishmentManager = PunishmentManager(this)

    val guildManager = GuildManager(this)

    val partyManager = PartyManager(this)

    val warpManager = WarpManager()

    val replyManager = ReplyManager()

    val resourcePackManager = ResourcePackManager(this)

    val discordClient = DiscordClient(this)

    var planAPI: PlanAPI? = null

    @Subscribe
    fun onProxyInitialization(@Suppress("UNUSED_PARAMETER") event: ProxyInitializeEvent) {
        Bukkit.getServer().version
        val commandManager = server.commandManager
        commandManager.register(commandManager.metaBuilder("msg").build(), MessageCommand(this))
        commandManager.register(commandManager.metaBuilder("r").build(), ReplyCommand(this))
        commandManager.register(commandManager.metaBuilder("nick").build(), NickCommand(this))
        commandManager.register(commandManager.metaBuilder("togglenicks").build(), ToggleNicksCommand(this))
        commandManager.register(commandManager.metaBuilder("pwarp").build(), WarpCommand(this))
        commandManager.register(commandManager.metaBuilder("lobby").aliases("hub", "l").build(), LobbyCommand(server))
        commandManager.register(commandManager.metaBuilder("aero").build(), AeroCommand(this))
        commandManager.register(commandManager.metaBuilder("discord").build(), DiscordCommand())
        commandManager.register(
            commandManager.metaBuilder("ban").aliases("ipban", "kick", "ipkick", "mute", "warn").build(),
            PunishCommand(this)
        )
        commandManager.register(commandManager.metaBuilder("unban").aliases("unmute").build(), UnpunishCommand(this))
        commandManager.register(
            commandManager.metaBuilder("balancetop").aliases("baltop").build(),
            BalanceTopCommand(this)
        )
        commandManager.register(commandManager.metaBuilder("vanish").aliases("v").build(), VanishCommand(this))
        GuildCommand(this).register()
        PartyCommand(this).register()
        ChannelCommand(this).register()
        DisguiseCommand(this).register()
        server.eventManager.register(this, ChannelListener(this))
        server.eventManager.register(this, PluginMessageListener())
        server.eventManager.register(this, WarpLockListener(this))
        server.eventManager.register(this, PluginBanListener(this))
        server.eventManager.register(this, PlayerListener(this))
        server.eventManager.register(this, ReplyListener(this))
        server.channelRegistrar.register(MinecraftChannelIdentifier.from("aero:warp_lock"))
        server.channelRegistrar.register(MinecraftChannelIdentifier.from("aero:warp_unlock"))
        server.channelRegistrar.register(MinecraftChannelIdentifier.from("aero:acb"))
        channelManager.initialize()
        if (server.pluginManager.isLoaded("plan")) {
            AeroExtension(this).register()
            planAPI = PlanAPI()
            logger.info("Hooked into Plan")
        }
    }

    companion object {
        @JvmStatic
        lateinit var instance: Aero
    }

    init {
        instance = this
    }
}