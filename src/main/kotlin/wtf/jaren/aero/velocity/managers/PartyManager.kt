package wtf.jaren.aero.velocity.managers

import com.velocitypowered.api.proxy.Player
import wtf.jaren.aero.velocity.Aero
import wtf.jaren.aero.velocity.objects.Party

class PartyManager(val plugin: Aero) {
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
}