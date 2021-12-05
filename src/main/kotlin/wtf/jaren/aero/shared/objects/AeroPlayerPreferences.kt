package wtf.jaren.aero.shared.objects

import org.bson.Document

data class AeroPlayerPreferences(
    var showNicks: Boolean = true
) {

    fun toDocument(): Document {
        return Document("showNicks", showNicks)
    }

    companion object {
        fun fromDocument(document: Document): AeroPlayerPreferences {
            return AeroPlayerPreferences(document.getBoolean("showNicks"))
        }
    }
}