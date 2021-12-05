package wtf.jaren.aero.velocity.objects

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bson.Document
import org.bson.types.ObjectId
import wtf.jaren.aero.velocity.enums.PunishmentType
import wtf.jaren.aero.velocity.utils.PlayerUtils
import wtf.jaren.aero.velocity.utils.TimeUtils
import java.util.*

data class Punishment(
    val _id: ObjectId?,
    val type: PunishmentType,
    val player: PartialPlayer,
    val ip: String?,
    val operator: UUID,
    val reason: String,
    val expires: Date?,
    val revoked: Boolean
) {
    val operatorName: String by lazy {
        if (names.containsKey(operator)) {
            names[operator]!!
        } else {
            PlayerUtils.fetchOfflinePlayer(operator)!!.name
        }
    }

    fun getMessage(includeHeader: Boolean): Component {
        val builder = Component.text()
        if (includeHeader) {
            builder.append(Component.text("«", NamedTextColor.DARK_GRAY))
            builder.append(Component.text(" Valury Network ", NamedTextColor.LIGHT_PURPLE))
            builder.append(Component.text("»", NamedTextColor.DARK_GRAY))
            builder.append(Component.newline())
        }
        builder.append(
            Component.text(
                if (type.hasDuration) "You are currently " else "You have been ",
                NamedTextColor.GRAY
            )
        )
        builder.append(Component.text(type.pastTense, NamedTextColor.LIGHT_PURPLE))
        builder.append(Component.text(".", NamedTextColor.GRAY))
        builder.append(Component.newline())
        builder.append(Component.newline())
        builder.append(Component.text("Reason", NamedTextColor.LIGHT_PURPLE))
        builder.append(Component.text(" » ", NamedTextColor.DARK_GRAY))
        builder.append(Component.text(reason, NamedTextColor.GRAY))
        builder.append(Component.newline())
        builder.append(Component.text("Operator", NamedTextColor.LIGHT_PURPLE))
        builder.append(Component.text(" » ", NamedTextColor.DARK_GRAY))
        builder.append(Component.text(operatorName, NamedTextColor.GRAY))
        builder.append(Component.newline())
        if (type.hasDuration) {
            builder.append(Component.text("Expires", NamedTextColor.LIGHT_PURPLE))
            builder.append(Component.text(" » ", NamedTextColor.DARK_GRAY))
            builder.append(Component.text(TimeUtils.timeString(expires), NamedTextColor.GRAY))
            builder.append(Component.newline())
        }
        return builder.build()
    }

    fun toDocument(): Document {
        val document = Document()
        if (_id != null) {
            document["_id"] = _id
        }
        document["type"] = type.name
        document["player"] = player.uuid
        document["ip"] = ip
        document["operator"] = operator
        document["reason"] = reason
        document["expires"] = expires
        document["revoked"] = revoked
        return document
    }

    companion object {
        val names: HashMap<UUID, String> = HashMap<UUID, String>().apply {
            put(UUID.fromString("6d4ea8ef-7c70-4d13-a0a3-e1c7ba912bc2"), "Console")
            put(UUID.fromString("7f901247-4244-4ef8-84de-b17854551a77"), "Aero")
        }

        fun fromDocument(document: Document): Punishment {
            return Punishment(
                document.getObjectId("_id"),
                PunishmentType.valueOf(document.getString("type")),
                PartialPlayer(document.get("player", UUID::class.java), null),
                document.getString("ip"),
                document.get("operator", UUID::class.java),
                document.getString("reason"),
                document.getDate("expires"),
                document.getBoolean("revoked")
            )
        }
    }
}