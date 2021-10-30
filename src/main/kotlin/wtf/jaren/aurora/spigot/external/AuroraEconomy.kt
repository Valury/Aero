package wtf.jaren.aurora.spigot.external

import com.mongodb.client.model.Filters
import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bson.Document
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import wtf.jaren.aurora.spigot.Aurora
import kotlin.math.roundToLong

class AuroraEconomy(val plugin: Aurora) : AbstractEconomy() {
    fun register() {
        Bukkit.getServicesManager().register(Economy::class.java, this, plugin, ServicePriority.Highest)
    }

    override fun isEnabled(): Boolean {
        return plugin.isEnabled
    }

    override fun getName(): String {
        return "Aurora"
    }

    override fun hasBankSupport(): Boolean {
        return false
    }

    override fun fractionalDigits(): Int {
        return 2
    }

    override fun format(amount: Double): String {
        return "$amount ${if (amount == 1.0) currencyNameSingular() else currencyNamePlural()}"
    }

    override fun currencyNamePlural(): String {
        return "coins"
    }

    override fun currencyNameSingular(): String {
        return "coin"
    }

    override fun hasAccount(playerName: String): Boolean {
        return plugin.playerManager.getAuroraPlayer(playerName) != null
    }

    override fun hasAccount(playerName: String, worldName: String): Boolean {
        return hasAccount(playerName)
    }

    override fun getBalance(playerName: String): Double {
        return plugin.playerManager.getAuroraPlayer(playerName)?.balance?.toDouble()?.div(100) ?: 0.0
    }

    override fun getBalance(playerName: String, world: String): Double {
        return getBalance(playerName)
    }

    override fun has(playerName: String, amount: Double): Boolean {
        return getBalance(playerName) >= amount
    }

    override fun has(playerName: String, worldName: String, amount: Double): Boolean {
        return has(playerName, amount)
    }

    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse {
        if (amount < 0.0) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds")
        }
        val auroraPlayer = plugin.playerManager.getAuroraPlayer(playerName, amount > 0.0) ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "User does not exist")
        if (amount == 0.0) {
            return EconomyResponse(amount, auroraPlayer.balance.toDouble().div(100), EconomyResponse.ResponseType.SUCCESS, null)
        }
        val realAmount = (amount * 100).roundToLong()
        if (auroraPlayer.balance < realAmount) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "User does not have enough funds")
        }
        val updateResult = plugin.database.getCollection("players").updateOne(
            Filters.and(
                Filters.eq("_id", auroraPlayer._id),
                Filters.gte("balance", realAmount)
            ),
            Document(
                "\$inc", Document(
                    "balance", -realAmount
                )
            )
        )
        return if (updateResult.modifiedCount > 0) {
            auroraPlayer.balance -= realAmount
            EconomyResponse(amount, auroraPlayer.balance.toDouble().div(100), EconomyResponse.ResponseType.SUCCESS, null)
        } else {
            EconomyResponse(0.0, auroraPlayer.balance.toDouble().div(100), EconomyResponse.ResponseType.FAILURE, "Unknown error")
        }
    }

    override fun withdrawPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return withdrawPlayer(playerName, amount)
    }

    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse {
        if (amount < 0) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds")
        }
        val auroraPlayer = plugin.playerManager.getAuroraPlayer(playerName) ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "User does not exist")
        if (amount == 0.0) {
            return EconomyResponse(amount, auroraPlayer.balance.toDouble().div(100), EconomyResponse.ResponseType.SUCCESS, null)
        }
        val realAmount = (amount * 100).roundToLong()
        plugin.database.getCollection("players").updateOne(
            Filters.eq("_id", auroraPlayer._id),
            Document(
                "\$inc", Document(
                    "balance", realAmount
                )
            )
        )
        auroraPlayer.balance += realAmount
        return EconomyResponse(amount, auroraPlayer.balance.toDouble().div(100), EconomyResponse.ResponseType.SUCCESS, null)
    }

    override fun depositPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return depositPlayer(playerName, amount)
    }

    override fun createBank(name: String, player: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Aurora does not support bank accounts!")
    }

    override fun deleteBank(name: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Aurora does not support bank accounts!")
    }

    override fun bankBalance(name: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Aurora does not support bank accounts!")
    }

    override fun bankHas(name: String, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Aurora does not support bank accounts!")
    }

    override fun bankWithdraw(name: String, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Aurora does not support bank accounts!")
    }

    override fun bankDeposit(name: String, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Aurora does not support bank accounts!")
    }

    override fun isBankOwner(name: String, playerName: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Aurora does not support bank accounts!")
    }

    override fun isBankMember(name: String, playerName: String): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Aurora does not support bank accounts!")
    }

    override fun getBanks(): MutableList<String> {
        return ArrayList()
    }

    override fun createPlayerAccount(playerName: String): Boolean {
        return false
    }

    override fun createPlayerAccount(playerName: String, worldName: String): Boolean {
        return false
    }
}