package wtf.jaren.aurora.velocity.managers

import com.velocitypowered.api.proxy.Player
import wtf.jaren.aurora.velocity.Aurora
import wtf.jaren.aurora.velocity.objects.Party

class PartyManager(plugin: Aurora) {
    val plugin: Aurora
    private val parties = HashSet<Party>()
    fun createParty(leader: Player): Party {
        val party = Party(this, leader)
        parties.add(party)
        return party
    }

    fun getPlayerParty(player: Player): Party? {
        for (party in parties) {
            if (party.members.contains(player)) {
                return party
            }
        }
        return null
    }

    fun disbandParty(party: Party) {
        parties.remove(party)
    }

    init {
        this.plugin = plugin
    }
}