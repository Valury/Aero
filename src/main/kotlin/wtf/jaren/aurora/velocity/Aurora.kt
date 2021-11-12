package wtf.jaren.aurora.velocity

import com.google.inject.Inject
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import org.slf4j.Logger
import wtf.jaren.aurora.velocity.commands.*
import wtf.jaren.aurora.velocity.discord.DiscordClient
import wtf.jaren.aurora.velocity.listeners.*
import wtf.jaren.aurora.velocity.managers.*
import wtf.jaren.aurora.velocity.plan.AuroraExtension
import wtf.jaren.aurora.velocity.plan.PlanAPI

@Plugin(
    id = "aurora",
    name = "Aurora",
    version = "1.0",
    authors = ["Jaren"],
    dependencies = [Dependency(id = "luckperms"), Dependency(id = "plan", optional = true)]
)
class Aurora @Inject constructor(val server: ProxyServer, val logger: Logger) {

    val isProd = (System.getenv("PROD") ?: "false").toBoolean()

    val isStable = true

    @JvmField
    val database: MongoDatabase =
        MongoClients.create(System.getenv("MONGO_CONNECTION_STRING"))
            .getDatabase("aurora")

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
        val commandManager = server.commandManager
        commandManager.register(commandManager.metaBuilder("msg").build(), MessageCommand(this))
        commandManager.register(commandManager.metaBuilder("r").build(), ReplyCommand(this))
        commandManager.register(commandManager.metaBuilder("nick").build(), NickCommand(this))
        commandManager.register(commandManager.metaBuilder("togglenicks").build(), ToggleNicksCommand(this))
        commandManager.register(commandManager.metaBuilder("pwarp").build(), WarpCommand(this))
        commandManager.register(commandManager.metaBuilder("lobby").aliases("hub", "l").build(), LobbyCommand(server))
        commandManager.register(commandManager.metaBuilder("aurora").build(), AuroraCommand(this))
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
        server.channelRegistrar.register(MinecraftChannelIdentifier.from("aurora:warp_lock"))
        server.channelRegistrar.register(MinecraftChannelIdentifier.from("aurora:warp_unlock"))
        server.channelRegistrar.register(MinecraftChannelIdentifier.from("aurora:acb"))
        channelManager.initialize()
        if (server.pluginManager.isLoaded("plan")) {
            AuroraExtension(this).register()
            planAPI = PlanAPI()
            logger.info("Hooked into Plan")
        }
    }

    companion object {
        @JvmStatic
        lateinit var instance: Aurora
    }

    init {
        instance = this
    }
}