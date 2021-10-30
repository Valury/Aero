package wtf.jaren.aurora.shared.objects

import org.bson.Document

data class AuroraDisguiseProperty(
    val name: String,
    val value: String,
    val signature: String
) {
    companion object {
        fun fromDocument(document: Document): AuroraDisguiseProperty {
            return AuroraDisguiseProperty(
                document.getString("name"),
                document.getString("value"),
                document.getString("signature")
            )
        }
    }
}