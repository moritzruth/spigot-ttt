package de.moritzruth.spigot_ttt.discord

import java.util.*

data class DiscordConnection(val playerUUID: UUID, val userID: String) {
    fun collidesWith(other: DiscordConnection): Boolean {
        return other.userID == userID || other.playerUUID == playerUUID
    }
}
