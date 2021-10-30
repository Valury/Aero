package wtf.jaren.aurora.velocity.utils

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }