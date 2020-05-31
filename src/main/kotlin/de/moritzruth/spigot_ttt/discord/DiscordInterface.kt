package de.moritzruth.spigot_ttt.discord

import de.moritzruth.spigot_ttt.discord.DiscordBot.jda
import de.moritzruth.spigot_ttt.plugin
import net.dv8tion.jda.api.entities.User
import org.bukkit.entity.Player
import java.util.*

object DiscordInterface {
    fun getPlayerByUserID(userID: String): Player? {
        val connection = DiscordConnections.getByUserID(userID) ?: return null
        return plugin.server.getPlayer(connection.playerUUID)
    }

    fun getUserByPlayerUUID(playerUUID: UUID): User? {
        val connection = DiscordConnections.getByPlayerUUID(playerUUID) ?: return null
        return jda.getUserById(connection.userID)
    }
}
