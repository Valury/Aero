package wtf.jaren.aero.velocity.utils

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

object ColorUtils {
    fun getColorByName(name: String): TextColor? {
        return if (name.startsWith('#')) {
            TextColor.fromCSSHexString(name)
        } else {
            NamedTextColor.NAMES.value(name)
        }
    }
}