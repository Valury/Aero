package wtf.jaren.aurora.shared.objects

import org.bson.Document
import org.bson.types.ObjectId

data class AuroraPlayerGuild(
    val _id: ObjectId,
    var rank: Int
) {
    fun toDocument(): Document {
        return Document("_id", _id)
            .append("rank", rank)
    }

    companion object {
        fun fromDocument(document: Document?): AuroraPlayerGuild? {
            if (document == null) {
                return null
            }
            return AuroraPlayerGuild(
                document.getObjectId("_id"),
                document.getInteger("rank")
            )
        }
    }
}