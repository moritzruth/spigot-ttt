package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.COMMAND_RESPONSE_PREFIX
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.worlds.MapVoting
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.EmptyTabCompleter
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class StartCommand: CommandExecutor {
    init {
        val command = plugin.getCommand("start")!!
        command.tabCompleter = EmptyTabCompleter
        command.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (GameManager.phase == null) {
            val voting = MapVoting.current

            if (voting == null) {
                try {
                    PlayerManager.checkEnoughPlayers()
                    MapVoting.start()
                } catch (e: PlayerManager.NotEnoughPlayersException) {
                    sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Es sind nicht genügend Spieler " +
                            "online.\nBenötigt: ${ChatColor.WHITE}${e.required}${ChatColor.RED}" +
                            "\nTatsächlich: ${ChatColor.WHITE}${e.actual}")
                }
            } else sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Das Map-Voting läuft bereits.")
        } else {
            sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Das Spiel läuft bereits.")
        }

        return true
    }
}
