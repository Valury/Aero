package wtf.jaren.aero.shared.objects

import org.bson.Document
import java.util.*

data class AeroPlayer(
    val _id: UUID,
    val ip: String,
    val name: String,
    var guild: AeroPlayerGuild?,
    var nick: String?,
    val preferences: AeroPlayerPreferences,
    var balance: Long,
    var vanished: Boolean,
    var disguise: AeroDisguise?
) {
    val effectiveNick: String?
        get() = if (disguise != null) disguise!!.nick else nick
    fun toDocument(): Document {
        val document = Document("_id", _id)
            .append("ip", ip)
            .append("name", name)
        if (guild != null) {
            document.append("guild", guild!!.toDocument())
        }
        if (nick != null) {
            document.append("nick", nick)
        }
        document.append("preferences", preferences.toDocument())
        document.append("vanished", vanished)
        document.append("balance", balance)
        return document
    }

    companion object {
        fun fromDocument(document: Document): AeroPlayer {
            return AeroPlayer(
                document.get("_id", UUID::class.java),
                document.getString("ip"),
                document.getString("name"),
                AeroPlayerGuild.fromDocument(document.get("guild", Document::class.java)),
                document.getString("nick"),
                AeroPlayerPreferences.fromDocument(document.get("preferences", Document::class.java)),
                document.getLong("balance"),
                document.getBoolean("vanished"),
                AeroDisguise.fromDocument(document.get("disguise", Document::class.java))
            )
        }
    }
}