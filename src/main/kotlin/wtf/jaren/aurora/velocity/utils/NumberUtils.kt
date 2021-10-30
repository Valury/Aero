package wtf.jaren.aurora.velocity.utils

import java.text.DecimalFormat

object NumberUtils {
    fun formatBalance(balance: Long): String {
        var format = DecimalFormat("#,###.00").format(balance.toDouble().div(100))
        if (format.startsWith(".")) {
            format = "0$format"
        }
        return "$$format"
    }
}