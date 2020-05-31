package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import org.bukkit.ChatColor

object GameMessenger {
    fun preparingPhaseStarted() = plugin.broadcast("${ChatColor.GREEN}Die Vorbereitungsphase hat begonnen.")

    fun remainingPreparingPhaseTime(remainingSeconds: Int) {
        if (remainingSeconds > 0) {
            if (remainingSeconds == 1) {
                plugin.broadcast("${ChatColor.GREEN}Die Vorbereitungsphase endet in ${ChatColor.WHITE}1 ${ChatColor.GREEN}Sekunde")
            } else {
                plugin.broadcast("${ChatColor.GREEN}Die Vorbereitungsphase endet in ${ChatColor.WHITE}$remainingSeconds ${ChatColor.GREEN}Sekunden")
            }
        } else {
            throw IllegalArgumentException("remainingSeconds must be positive and not 0")
        }
    }

    fun combatPhaseStarted() = plugin.broadcast("${ChatColor.GREEN}Die Kampfphase hat begonnen.")

    fun remainingRoundTime(remainingMinutes: Int) {
        if (remainingMinutes > 0) {
            if (remainingMinutes == 1) {
                plugin.broadcast("${ChatColor.GREEN}Das Spiel endet in ${ChatColor.WHITE}1 ${ChatColor.GREEN}Minute")
            } else {
                plugin.broadcast("${ChatColor.GREEN}Das Spiel endet in ${ChatColor.WHITE}$remainingMinutes ${ChatColor.GREEN}Minuten")
            }
        } else {
            throw IllegalArgumentException("remainingMinutes must be positive and not 0")
        }
    }

    fun win(winnerRole: TTTPlayer.Role?) {
        val winner = when(winnerRole) {
            null -> {
                plugin.broadcast("Niemand hat gewonnen")
                PlayerManager.tttPlayers.forEach {
                    it.player.sendTitle("Unentschieden", null, secondsToTicks(0.5), secondsToTicks(5), secondsToTicks(1))
                }

                return
            }
            TTTPlayer.Role.JACKAL, TTTPlayer.Role.SIDEKICK -> "Der ${TTTPlayer.Role.JACKAL.chatColor}Jackal"
            TTTPlayer.Role.TRAITOR -> "Die ${TTTPlayer.Role.TRAITOR.chatColor}Traitor"
            TTTPlayer.Role.INNOCENT, TTTPlayer.Role.DETECTIVE -> "Die ${TTTPlayer.Role.INNOCENT.chatColor}Innocents"
        }

        val winnerMessage = when(winnerRole) {
            TTTPlayer.Role.JACKAL, TTTPlayer.Role.SIDEKICK -> "hat gewonnen"
            TTTPlayer.Role.TRAITOR, TTTPlayer.Role.INNOCENT, TTTPlayer.Role.DETECTIVE -> "haben gewonnen"
        }

        plugin.broadcast("${ChatColor.GOLD}$winner ${ChatColor.GOLD}${winnerMessage}")
        PlayerManager.tttPlayers.forEach {
            it.player.sendTitle("${ChatColor.GOLD}$winner", "${ChatColor.GOLD}$winnerMessage", secondsToTicks(0.5), secondsToTicks(5), secondsToTicks(1))
        }
    }

    fun abortedPlayerLeft() {
        plugin.broadcast("${ChatColor.RED}Das Spiel wurde abgebrochen, da ein Spieler den Server verlassen hat.")
    }

    fun aborted() {
        plugin.broadcast("${ChatColor.RED}Das Spiel wurde abgebrochen.")
    }

    fun corpseIdentified(by: String, who: String, role: TTTPlayer.Role) {
        plugin.broadcast("$by ${ChatColor.GOLD}hat die Leiche von ${ChatColor.WHITE}$who ${ChatColor.GOLD}identifiziert. Er/Sie war ${role.coloredDisplayName}")
    }

    fun roles() {
        val playersByRole = PlayerManager.getPlayersByRole()
        val roles = playersByRole.keys.sortedBy(TTTPlayer.Role::position)

        for (role in roles) {
            val entries = playersByRole.getValue(role).map { tttPlayer ->
                tttPlayer.player.displayName.run {
                    if (tttPlayer.roleHistory.count() != 0)
                        this + " (${tttPlayer.roleHistory.joinToString(", ") { it.coloredDisplayName }})"
                    else this
                }
            }

            plugin.broadcast("  ${role.coloredDisplayName}: ${entries.joinToString(", ")}", false)
        }
    }
}
