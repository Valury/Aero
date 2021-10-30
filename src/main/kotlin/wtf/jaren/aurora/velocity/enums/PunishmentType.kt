package wtf.jaren.aurora.velocity.enums

enum class PunishmentType(val hasDuration: Boolean, val pastTense: String) {
    WARN(false, "warned"),
    MUTE(true, "muted"),
    KICK(false, "kicked"),
    BAN(true, "banned");
}