package wtf.jaren.aurora.shared.objects

import org.bson.Document
import java.util.*

data class AuroraPlayer(
    val _id: UUID,
    val ip: String,
    val name: String,
    var guild: AuroraPlayerGuild?,
    var nick: String?,
    val preferences: AuroraPlayerPreferences,
    var balance: Long,
    var vanished: Boolean,
    var disguise: AuroraDisguise?
) {
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
        fun fromDocument(document: Document): AuroraPlayer {
            return AuroraPlayer(
                document.get("_id", UUID::class.java),
                document.getString("ip"),
                document.getString("name"),
                AuroraPlayerGuild.fromDocument(document.get("guild", Document::class.java)),
                document.getString("nick"),
                AuroraPlayerPreferences.fromDocument(document.get("preferences", Document::class.java)),
                document.getLong("balance"),
                document.getBoolean("vanished"),
                AuroraDisguise.fromDocument(document.get("disguise", Document::class.java))
            )
        }
    }
}