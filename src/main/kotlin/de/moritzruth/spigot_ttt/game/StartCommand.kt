package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.COMMAND_RESPONSE_PREFIX
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.EmptyTabCompleter
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class StartCommand: CommandExecutor {
    init {
        plugin.getCommand("start")?.let {
            it.setExecutor(this)
            it.tabCompleter = EmptyTabCompleter
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (GameManager.phase === null) {
            try {
                GameManager.startPreparingPhase()
            } catch (e: GameManager.NotEnoughPlayersException) {
                sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Es sind nicht genügend Spieler online.")
            }
        } else {
            sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Das Spiel läuft bereits.")
        }

        return true
    }
}
