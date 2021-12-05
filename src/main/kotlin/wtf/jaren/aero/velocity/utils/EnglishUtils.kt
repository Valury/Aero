package wtf.jaren.aero.velocity.utils

object EnglishUtils {
    private fun isVowel(char: Char): Boolean {
        return when (char.lowercaseChar()) {
            'a', 'e', 'i', 'o', 'u' -> true
            else -> false
        }
    }

    fun a(noun: String): String {
        return "a${if (isVowel(noun[0])) "n" else ""} $noun"
    }
}