package wtf.jaren.aero.spigot.managers

import wtf.jaren.aero.spigot.utils.MathUtils

class PrefixAnimationManager {
    // 50ms
    private val content = listOf("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")
    // 50ms
    private val droplet = listOf("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")
    // 100ms
    private val glorious = listOf("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")

    private var tick = 0
    private val maxTicks = MathUtils.lcm(intArrayOf(content.size, droplet.size, glorious.size * 2))

    fun tick() {
        tick += 1
        tick %= maxTicks
    }

    fun getCurrentContentPrefix(): String {
        return "§f${content[tick % content.size]} "
    }

    fun getCurrentDropletPrefix(): String {
        return "§f${droplet[tick % droplet.size]} §9"
    }

    fun getCurrentGloriousPrefix(): String {
        return "§f${glorious[(tick / 2) % glorious.size]} §6"
    }

}