package wtf.jaren.aero.velocity.enums

enum class PartyRemoveReason(val message: String) {
    LEFT(" left the party."),
    KICKED(" was kicked from the party."),
    PUNISHED(" was struck by the banhammer."),
    DISCONNECTED(" disconnected.");
}