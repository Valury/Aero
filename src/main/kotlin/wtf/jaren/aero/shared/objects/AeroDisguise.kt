package wtf.jaren.aero.shared.objects

import org.bson.Document

data class AeroDisguise(
    val name: String?,
    var rank: String,
    var nick: String?,
    val properties: List<AeroDisguiseProperty>?,
) {
    companion object {
        fun fromDocument(document: Document?): AeroDisguise? {
            if (document == null) {
                return null
            }
            return AeroDisguise(document.getString("name"), document.getString("rank") ?: "default", document.getString("nick"), document.getList("properties", Document::class.java)?.map { AeroDisguiseProperty.fromDocument(it) })
        }
    }
}