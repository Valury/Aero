package wtf.jaren.aurora.shared.objects

import org.bson.Document

data class AuroraPlayerPreferences(
    var showNicks: Boolean = true
) {

    fun toDocument(): Document {
        return Document("showNicks", showNicks)
    }

    companion object {
        fun fromDocument(document: Document): AuroraPlayerPreferences {
            return AuroraPlayerPreferences(document.getBoolean("showNicks"))
        }
    }
}