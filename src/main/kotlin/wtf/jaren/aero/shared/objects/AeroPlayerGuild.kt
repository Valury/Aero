package wtf.jaren.aero.shared.objects

import org.bson.Document
import org.bson.types.ObjectId

data class AeroPlayerGuild(
    val _id: ObjectId,
    var rank: Int
) {
    fun toDocument(): Document {
        return Document("_id", _id)
            .append("rank", rank)
    }

    companion object {
        fun fromDocument(document: Document?): AeroPlayerGuild? {
            if (document == null) {
                return null
            }
            return AeroPlayerGuild(
                document.getObjectId("_id"),
                document.getInteger("rank")
            )
        }
    }
}