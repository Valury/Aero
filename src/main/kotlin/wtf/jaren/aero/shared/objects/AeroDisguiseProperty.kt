package wtf.jaren.aero.shared.objects

import org.bson.Document

data class AeroDisguiseProperty(
    val name: String,
    val value: String,
    val signature: String
) {
    companion object {
        fun fromDocument(document: Document): AeroDisguiseProperty {
            return AeroDisguiseProperty(
                document.getString("name"),
                document.getString("value"),
                document.getString("signature")
            )
        }
    }
}