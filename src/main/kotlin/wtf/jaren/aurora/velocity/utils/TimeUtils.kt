package wtf.jaren.aurora.velocity.utils

import org.ocpsoft.prettytime.PrettyTime
import java.util.*

object TimeUtils {
    private val timeSuffixes: HashMap<String, Long> = HashMap<String, Long>().apply {
        put("s", 1000L)
        put("m", 60000L)
        put("h", 3600000L)
        put("d", 86400000L)
        put("y", 31557600000L)
    }

    fun parseTime(time: String): Long? {
        val lowerTime = time.lowercase()
        for ((key, value) in timeSuffixes) {
            if (lowerTime.endsWith(key)) {
                return try {
                    lowerTime.split(key).toTypedArray()[0].toLong() * value
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }
        return null
    }

    fun timeString(expiration: Date?): String {
        if (expiration == null) {
            return "Never"
        }
        val time = PrettyTime().format(expiration).replace(" from now", "")
        return if (time == "moments") {
            "1 minute"
        } else time
    }
}