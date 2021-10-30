package wtf.jaren.aurora.velocity.utils

import java.nio.charset.Charset

object StringUtils {
    fun isAscii(string: String): Boolean {
        return Charset.forName("US-ASCII").newEncoder().canEncode(string);
    }
}
