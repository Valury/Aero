package wtf.jaren.aurora.velocity.utils

import org.bson.types.ObjectId
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

object SyncUtils {
    val refreshPlayer: ByteArray = run {
        val outputStream = ByteArrayOutputStream()
        val output = DataOutputStream(outputStream)
        output.writeUTF("refreshPlayer")
        outputStream.toByteArray()
    }

    val refreshPlayerGuild: ByteArray = run {
        val outputStream = ByteArrayOutputStream()
        val output = DataOutputStream(outputStream)
        output.writeUTF("refreshPlayerGuild")
        outputStream.toByteArray()
    }

    val reload: ByteArray = run {
        val outputStream = ByteArrayOutputStream()
        val output = DataOutputStream(outputStream)
        output.writeUTF("reload")
        outputStream.toByteArray()
    }

    fun deleteGuild(_id: ObjectId): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val output = DataOutputStream(outputStream)
        output.writeUTF("deleteGuild")
        output.writeUTF(_id.toHexString())
        return outputStream.toByteArray()
    }
}