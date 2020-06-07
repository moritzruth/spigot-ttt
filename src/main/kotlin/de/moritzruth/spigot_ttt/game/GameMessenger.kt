package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.Role
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

    fun win(winnerRole: Role?) {
        val winner = when(winnerRole) {
            null -> {
                plugin.broadcast("Niemand hat gewonnen")
                PlayerManager.tttPlayers.forEach {
                    it.player.sendTitle("Unentschieden", null, secondsToTicks(0.5), secondsToTicks(5), secondsToTicks(1))
                }

                return
            }
            Role.JACKAL, Role.SIDEKICK -> "Der ${Role.JACKAL.chatColor}Jackal"
            Role.TRAITOR -> "Die ${Role.TRAITOR.chatColor}Traitor"
            Role.INNOCENT, Role.DETECTIVE -> "Die ${Role.INNOCENT.chatColor}Innocents"
        }

        val winnerMessage = when(winnerRole) {
            Role.JACKAL, Role.SIDEKICK -> "hat gewonnen"
            Role.TRAITOR, Role.INNOCENT, Role.DETECTIVE -> "haben gewonnen"
        }

        plugin.broadcast("", false)
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

    fun corpseIdentified(by: String, who: String, role: Role) {
        plugin.broadcast("$by ${ChatColor.GOLD}hat die Leiche von ${ChatColor.WHITE}$who ${ChatColor.GOLD}identifiziert. Er/Sie war ${role.coloredDisplayName}")
    }

    fun roles() {
        val playersByRole = PlayerManager.getPlayersByRole()
        val roles = playersByRole.keys.sortedBy(Role::position)

        plugin.broadcast("", false)

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

        plugin.broadcast("", false)
    }
}
