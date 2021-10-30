package wtf.jaren.aurora.shared.objects

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bson.Document
import org.bson.types.ObjectId

class Guild(
    val _id: ObjectId,
    var name: String?,
    var public: Boolean,
    val slots: Int,
    var balance: Long,
    val ranks: ArrayList<String>,
    val permissions: GuildPermissions,
    var color: TextColor
) {
    fun toDocument(): Document {
        return Document("_id", _id)
            .append("name", name)
            .append("public", public)
            .append("slots", slots)
            .append("balance", balance)
            .append("ranks", ranks)
            .append("permissions", permissions.toDocument())
            .append("color", color.value())
    }

    companion object {
        val COLOR_SCHEME: NamedTextColor = NamedTextColor.LIGHT_PURPLE
        val PREFIX = Component.empty().append(Component.text("Guild / ", COLOR_SCHEME))
        const val RENAME_COST = 2000000L
        fun fromDocument(document: Document): Guild {
            return Guild(
                document.getObjectId("_id"),
                document.getString("name"),
                document.getBoolean("public"),
                document.getInteger("slots"),
                document.getLong("balance"),
                ArrayList(document.getList("ranks", String::class.java)),
                GuildPermissions.fromDocument(document.get("permissions", Document::class.java)),
                TextColor.color(document.getInteger("color"))
            )
        }
        private val nonAllowedCharactersRegex = Regex("[^A-Za-z0-9\\-_]")
        private val nonAlphanumericRegex = Regex("[^A-Za-z0-9]")
        fun getNameError(name: String): String? {
            if (name.length > 10) {
                return "That name is too long."
            }
            if (name.length < 3) {
                return "That name is too short."
            }
            if (nonAllowedCharactersRegex.containsMatchIn(name)) {
                return "That name contains illegal characters."
            }
            if (nonAlphanumericRegex.replace(name, "").length < 3) {
                return "That name contains too little alphanumeric characters."
            }
            return null
        }
    }
}