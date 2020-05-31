package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTScoreboard

object ScoreboardHelper {
    fun forEveryScoreboard(action: (tttScoreboard: TTTScoreboard) -> Unit) {
        PlayerManager.tttPlayers.forEach {
            action(it.scoreboard)
        }
    }
}
