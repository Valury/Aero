package wtf.jaren.aero.velocity.plan

import com.djrapitops.plan.extension.DataExtension
import com.djrapitops.plan.extension.ExtensionService
import com.djrapitops.plan.extension.annotation.PluginInfo
import com.djrapitops.plan.extension.annotation.Tab
import com.djrapitops.plan.extension.annotation.TabInfo
import com.djrapitops.plan.extension.annotation.TableProvider
import com.djrapitops.plan.extension.icon.Color
import com.djrapitops.plan.extension.icon.Family
import com.djrapitops.plan.extension.icon.Icon
import com.djrapitops.plan.extension.table.Table
import com.mongodb.client.FindIterable
import com.mongodb.client.model.Filters
import org.bson.Document
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.enums.PunishmentType
import wtf.jaren.aero.velocity.objects.Punishment
import wtf.jaren.aero.velocity.utils.PlayerUtils
import java.text.SimpleDateFormat
import java.util.*


@PluginInfo(name = "Aero")
@TabInfo.Multiple(
    TabInfo(tab = "Bans", iconName = "gavel", elementOrder = []),
    TabInfo(tab = "Mutes", iconName = "bell-slash", iconFamily = Family.REGULAR, elementOrder = []),
    TabInfo(tab = "Warnings", iconName = "exclamation-triangle", elementOrder = []),
    TabInfo(tab = "Kicks", iconName = "user-times", elementOrder = [])
)
class AeroExtension(private val plugin: Aero) : DataExtension {
    private val formatter = SimpleDateFormat("MMM d yyyy, HH:mm")

    fun register() {
        ExtensionService.getInstance().register(this)
    }

    @TableProvider(tableColor = Color.RED)
    @Tab("Bans")
    fun bans(player: UUID): Table {
        val table: Table.Factory = playerTable()
        addPunishmentsToTable(getPunishments(player, PunishmentType.BAN), table, true)
        return table.build()
    }

    @TableProvider(tableColor = Color.DEEP_ORANGE)
    @Tab("Mutes")
    fun mutes(player: UUID): Table {
        val table: Table.Factory = playerTable()
        addPunishmentsToTable(getPunishments(player, PunishmentType.MUTE), table, true)
        return table.build()
    }

    @TableProvider(tableColor = Color.AMBER)
    @Tab("Warnings")
    fun warns(player: UUID): Table {
        val table: Table.Factory = playerTable()
        addPunishmentsToTable(getPunishments(player, PunishmentType.WARN), table, true)
        return table.build()
    }

    @TableProvider(tableColor = Color.BROWN)
    @Tab("Kicks")
    fun kicks(player: UUID): Table {
        val table: Table.Factory = playerTable()
        addPunishmentsToTable(getPunishments(player, PunishmentType.KICK), table, true)
        return table.build()
    }

    @TableProvider(tableColor = Color.RED)
    @Tab("Bans")
    fun serverBans(): Table {
        val table: Table.Factory = serverTable()
        addPunishmentsToTable(getPunishments(PunishmentType.BAN), table, false)
        return table.build()
    }

    @TableProvider(tableColor = Color.DEEP_ORANGE)
    @Tab("Mutes")
    fun serverMutes(): Table {
        val table: Table.Factory = serverTable()
        addPunishmentsToTable(getPunishments(PunishmentType.MUTE), table, false)
        return table.build()
    }

    @TableProvider(tableColor = Color.AMBER)
    @Tab("Warnings")
    fun serverWarns(): Table {
        val table: Table.Factory = serverTable()
        addPunishmentsToTable(getPunishments(PunishmentType.WARN), table, false)
        return table.build()
    }

    @TableProvider(tableColor = Color.BROWN)
    @Tab("Kicks")
    fun serverKicks(): Table {
        val table: Table.Factory = serverTable()
        addPunishmentsToTable(getPunishments(PunishmentType.KICK), table, false)
        return table.build()
    }

    private fun getPunishments(type: PunishmentType): FindIterable<Document> {
        return plugin.database.getCollection("punishments").find(
            Filters.and(
                Filters.eq("type", type.name)
            )
        )
    }

    private fun getPunishments(player: UUID, type: PunishmentType): FindIterable<Document> {
        return plugin.database.getCollection("punishments").find(
            Filters.and(
                Filters.eq("type", type.name),
                Filters.eq("player", player)
            )
        )
    }

    private fun formatDate(date: Date?): String {
        return if (date == null) "Never" else formatter.format(date)
    }

    private fun addPunishmentsToTable(punishments: FindIterable<Document>, table: Table.Factory, player: Boolean) {
        val nameCache = HashMap<UUID, String>()
        for (punishment in punishments) {
            if (player) {
                table.addRow(
                    punishment.getString("reason"),
                    getPlayerName(punishment.get("operator", UUID::class.java), nameCache),
                    formatDate(punishment.getObjectId("_id").date),
                    formatDate(punishment.getDate("expires")) + (if (punishment.getBoolean("revoked")) " (Revoked)" else "")
                )
            } else {
                val ip = punishment.getString("ip")
                table.addRow(
                    getPlayerName(
                        punishment.get("player", UUID::class.java),
                        nameCache
                    ) + (if (ip != null) " ($ip)" else ""),
                    punishment.getString("reason"),
                    getPlayerName(punishment.get("operator", UUID::class.java), nameCache),
                    formatDate(punishment.getObjectId("_id").date),
                    formatDate(punishment.getDate("expires")) + (if (punishment.getBoolean("revoked")) " (Revoked)" else "")
                )
            }
        }
    }

    private fun getPlayerName(uuid: UUID, nameCache: HashMap<UUID, String>): String {
        if (Punishment.names.containsKey(uuid)) {
            return Punishment.names[uuid]!!
        }
        if (!nameCache.containsKey(uuid)) {
            nameCache[uuid] = PlayerUtils.fetchOfflinePlayer(uuid)!!.name
        }
        return nameCache[uuid]!!
    }

    private fun playerTable(): Table.Factory {
        return Table.builder()
            .columnOne("Reason", Icon.called("balance-scale").build())
            .columnTwo("Operator", Icon.called("user-shield").build())
            .columnThree("Created", Icon.called("clock").build())
            .columnFour("Expires", Icon.called("clock").of(Family.REGULAR).build())
    }

    private fun serverTable(): Table.Factory {
        return Table.builder()
            .columnOne("Player", Icon.called("user").build())
            .columnTwo("Reason", Icon.called("balance-scale").build())
            .columnThree("Operator", Icon.called("user-shield").build())
            .columnFour("Created", Icon.called("clock").build())
            .columnFive("Expires", Icon.called("clock").of(Family.REGULAR).build())
    }
}