package wtf.jaren.aurora.spigot

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.bukkit.plugin.java.JavaPlugin
import wtf.jaren.aurora.spigot.commands.AntiCheatBanCommand
import wtf.jaren.aurora.spigot.commands.GameModeCommand
import wtf.jaren.aurora.spigot.commands.OpMeCommand
import wtf.jaren.aurora.spigot.listeners.*
import wtf.jaren.aurora.spigot.managers.GuildManager
import wtf.jaren.aurora.spigot.managers.PlayerManager
import wtf.jaren.aurora.spigot.external.PAPIAuroraExpansion
import wtf.jaren.aurora.spigot.external.AuroraEconomy

class Aurora : JavaPlugin() {
    val database: MongoDatabase =
        MongoClients.create(System.getenv("MONGO_CONNECTION_STRING"))
            .getDatabase("aurora")

    val playerManager = PlayerManager(this)

    val guildManager = GuildManager(this)

    val serverName: String = System.getenv("NAME") ?: "Unknown"

    override fun onEnable() {
        instance = this
        if (serverName.lowercase() == "lobby") {
            server.pluginManager.registerEvents(LobbyListener(), this)
        }
        getCommand("opme")!!.setExecutor(OpMeCommand())
        getCommand("acb")!!.setExecutor(AntiCheatBanCommand())
        getCommand("gm")!!.setExecutor(GameModeCommand())
        server.pluginManager.registerEvents(PlayerListener(this), this)
        val warpListener = WarpListener()
        server.pluginManager.registerEvents(warpListener, this)
        server.messenger.registerIncomingPluginChannel(this, "aurora:warp", warpListener)
        server.messenger.registerIncomingPluginChannel(this, "aurora:sync", SyncListener(this))
        server.messenger.registerOutgoingPluginChannel(this, "aurora:acb")
        LuckPermsListener(this)
        if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            PAPIAuroraExpansion().register()
            logger.info("Hooked into PlaceholderAPI")
        }
        if (server.pluginManager.isPluginEnabled("Vault")) {
            AuroraEconomy(this).register()
            logger.info("Hooked into Vault")
        }
        for (player in server.operators) {
            player.isOp = false
        }
        for (player in server.onlinePlayers) {
            playerManager.handlePreLogin(player.uniqueId)
            playerManager.handleJoin(player)
        }
    }

    companion object {
        @JvmStatic
        lateinit var instance: Aurora
    }
}