package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.TTTPlugin
import de.moritzruth.spigot_ttt.utils.NoOpTabCompleter
import de.moritzruth.spigot_ttt.plugin
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class StartCommand: CommandExecutor {
    init {
        plugin.getCommand("start")?.let {
            it.setExecutor(this)
            it.tabCompleter = NoOpTabCompleter()
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (GameManager.phase === null) {
            try {
                GameManager.startPreparingPhase()
            } catch (e: GameManager.NotEnoughPlayersException) {
                sender.sendMessage("${ChatColor.RED}Es sind nicht genügend Spieler online.")
            }
        } else {
            sender.sendMessage("${TTTPlugin.prefix}${ChatColor.RED}Das Spiel läuft bereits.")
        }

        return true
    }
}
