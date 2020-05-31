package de.moritzruth.spigot_ttt.discord

import de.moritzruth.spigot_ttt.utils.ConfigurationFile
import java.util.*

object DiscordConnections {
    private val config = ConfigurationFile("discord-connections")

    private val connections = config.getStringList("connections").map {
        val (playerUUIDString, userID) = it.split("|")
        DiscordConnection(UUID.fromString(playerUUIDString)!!, userID)
    }.toMutableList()

    private fun saveConnections() {
        config.set("connections", connections.map { "${it.playerUUID}|${it.userID}" })
        config.save()
    }

    fun add(connection: DiscordConnection) {
        val colliding = connections.find { it.collidesWith(connection) }
        if (colliding != null) {
            throw CollisionException(connection, colliding)
        }

        connections.add(connection)
        saveConnections()
    }

    fun getByUserID(userID: String) = connections.find { it.userID == userID }
    fun getByPlayerUUID(playerUUID: UUID) = connections.find { it.playerUUID == playerUUID }

    class CollisionException(
        val connection: DiscordConnection,
        val collidingConnection: DiscordConnection
    ): RuntimeException("There is already a connection for this player or this Discord user")
}
