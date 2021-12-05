package wtf.jaren.aero.spigot

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.bukkit.plugin.java.JavaPlugin
import wtf.jaren.aero.spigot.commands.AntiCheatBanCommand
import wtf.jaren.aero.spigot.commands.GameModeCommand
import wtf.jaren.aero.spigot.commands.OpMeCommand
import wtf.jaren.aero.spigot.listeners.*
import wtf.jaren.aero.spigot.managers.GuildManager
import wtf.jaren.aero.spigot.managers.PlayerManager
import wtf.jaren.aero.spigot.external.PAPIAeroExpansion
import wtf.jaren.aero.spigot.external.AeroEconomy

class Aero : JavaPlugin() {
    val database: MongoDatabase =
        MongoClients.create(System.getenv("MONGO_CONNECTION_STRING"))
            .getDatabase(System.getenv("MONGO_DATABASE"))

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
        server.messenger.registerIncomingPluginChannel(this, "aero:warp", warpListener)
        server.messenger.registerIncomingPluginChannel(this, "aero:sync", SyncListener(this))
        server.messenger.registerOutgoingPluginChannel(this, "aero:acb")
        LuckPermsListener(this)
        if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            PAPIAeroExpansion().register()
            logger.info("Hooked into PlaceholderAPI")
        }
        if (server.pluginManager.isPluginEnabled("Vault")) {
            AeroEconomy(this).register()
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
        lateinit var instance: Aero
    }
}