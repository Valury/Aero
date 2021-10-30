package wtf.jaren.aurora.shared.objects

import org.bson.Document

data class AuroraDisguise(
    val name: String?,
    var rank: String,
    val properties: List<AuroraDisguiseProperty>?,
) {
    companion object {
        fun fromDocument(document: Document?): AuroraDisguise? {
            if (document == null) {
                return null
            }
            return AuroraDisguise(document.getString("name"), document.getString("rank") ?: "default", document.getList("properties", Document::class.java)?.map { AuroraDisguiseProperty.fromDocument(it) })
        }
    }
}