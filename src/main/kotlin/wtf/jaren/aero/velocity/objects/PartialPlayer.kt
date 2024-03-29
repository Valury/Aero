package wtf.jaren.aero.velocity.objects

import wtf.jaren.aero.velocity.utils.PlayerUtils
import java.util.*

data class PartialPlayer(val uuid: UUID, private val _name: String?) {
    val name: String by lazy {
        _name ?: PlayerUtils.fetchOfflinePlayer(uuid)!!.name
    }
}