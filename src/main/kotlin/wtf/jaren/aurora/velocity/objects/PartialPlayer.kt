package wtf.jaren.aurora.velocity.objects

import wtf.jaren.aurora.velocity.utils.PlayerUtils
import java.util.*

data class PartialPlayer(val uuid: UUID, private val _name: String?) {
    val name: String by lazy {
        _name ?: PlayerUtils.fetchOfflinePlayer(uuid)!!.name
    }
}