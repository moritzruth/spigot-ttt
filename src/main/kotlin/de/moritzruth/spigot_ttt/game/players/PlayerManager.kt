package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.TTTPlugin
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.GameMessenger
import de.moritzruth.spigot_ttt.game.GamePhase
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.noop
import de.moritzruth.spigot_ttt.utils.teleportPlayerToWorldSpawn
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.entity.Player

object PlayerManager {
    val tttPlayers= mutableListOf<TTTPlayer>()

    val availablePlayers get() = plugin.server.onlinePlayers.filter { it.gameMode === GameMode.SURVIVAL }
    val stillLivingRoles get() = tttPlayers.filter { it.alive }.map { it.role }.distinct()
    val playersJoinedDuringRound = mutableSetOf<Player>()

    fun getTTTPlayer(player: Player) = tttPlayers.find { it.player === player }

    fun getPlayersByRole() = mutableMapOf<TTTPlayer.Role, MutableSet<TTTPlayer>>()
            .apply { tttPlayers.forEach { getOrPut(it.role) { mutableSetOf() }.add(it) } }
            .toMap()

    fun resetAfterGame() {
        playersJoinedDuringRound.forEach {
            teleportPlayerToWorldSpawn(it)

            plugin.server.scheduler.runTask(plugin, fun() {
                it.gameMode = GameMode.SURVIVAL
            })
        }

        tttPlayers.forEach(TTTPlayer::resetAfterGameEnd)
        tttPlayers.clear()
    }

    fun letRemainingRoleWin() {
        GameManager.ensurePhase(GamePhase.COMBAT)

        if (stillLivingRoles.count() == 1) {
            GameManager.letRoleWin(stillLivingRoles[0])
        }
    }

    fun onPlayerJoin(player: Player) {
        val tttPlayer = tttPlayers.find { it.player.uniqueId == player.uniqueId }

        if (tttPlayer == null) {
            if (GameManager.phase == null) {
                teleportPlayerToWorldSpawn(player)
                player.gameMode = GameMode.SURVIVAL
            } else {
                player.gameMode = GameMode.SPECTATOR
                playersJoinedDuringRound.add(player)
                player.sendMessage("${TTTPlugin.prefix}${ChatColor.GREEN}Du schaust jetzt zu.")
            }
        } else {
            tttPlayer.player = player

            player.sendMessage("${TTTPlugin.prefix}${ChatColor.RED}Du bist gestorben, da du das Spiel verlassen hast.")
            when(GameManager.phase) {
                GamePhase.PREPARING -> noop() // Never happens
                GamePhase.COMBAT -> {
                    player.gameMode = GameMode.SPECTATOR
                }
                GamePhase.OVER -> {
                    tttPlayer.teleportToSpawn()
                    player.gameMode = GameMode.SURVIVAL
                }
            }
        }
    }

    fun onPlayerQuit(player: Player) {
        playersJoinedDuringRound.remove(player)
        val tttPlayer = getTTTPlayer(player) ?: return

        when(GameManager.phase) {
            GamePhase.PREPARING -> {
                GameManager.abortGame()
                GameMessenger.abortedPlayerLeft()
            }
            GamePhase.COMBAT -> {
                tttPlayer.kill(DeathReason.DISCONNECTED)
            }
            GamePhase.OVER -> noop()
        }
    }

    fun createTTTPlayers() {
        val playersWithoutRole = availablePlayers.toMutableSet()

        val playerCount = playersWithoutRole.count()
        val traitorCount: Int = if (playerCount <= 4) 1 else playerCount / 4

        if (playerCount >= plugin.config.getInt("min-players-for.detective", 5)) {
            val player = playersWithoutRole.random()
            tttPlayers.add(TTTPlayer(player, TTTPlayer.Role.DETECTIVE))
            playersWithoutRole.remove(player)
        }

        if (playerCount >= plugin.config.getInt("min-players-for.jackal", 6)) {
            val player = playersWithoutRole.random()
            tttPlayers.add(TTTPlayer(player, TTTPlayer.Role.JACKAL))
            playersWithoutRole.remove(player)
        }

        for (index in 1..traitorCount) {
            val player = playersWithoutRole.random()
            tttPlayers.add(TTTPlayer(player, TTTPlayer.Role.TRAITOR))
            playersWithoutRole.remove(player)
        }

        playersWithoutRole.forEach { tttPlayers.add(TTTPlayer(it, TTTPlayer.Role.INNOCENT)) }
    }
}
