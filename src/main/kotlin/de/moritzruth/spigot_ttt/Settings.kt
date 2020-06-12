package de.moritzruth.spigot_ttt

enum class JackalMode {
    ALWAYS,
    HALF_TIME,
    NEVER
}

object Settings {
    val jackalMode get() = JackalMode.valueOf(plugin.config.getString("roles.jackal.mode", JackalMode.HALF_TIME.toString())!!)
    val minPlayers get() = plugin.config.getInt("min-players", 4)
    val detectiveEnabled get() = plugin.config.getBoolean("roles.detective.enabled", true)
    val preparingPhaseDuration get() = plugin.config.getInt("duration.preparing", 20)
    val combatPhaseDuration get() = plugin.config.getInt("duration.combat", 480) // 8 minutes
    val overPhaseDuration get() = plugin.config.getInt("duration.over", 10)
    val initialCredits get() = plugin.config.getInt("initial-credits", 2)
    val creditsPerKill get() = plugin.config.getInt("credits-per-kill", 1)
}
