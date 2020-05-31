package de.moritzruth.spigot_ttt.game

import de.moritzruth.spigot_ttt.TTTPlugin
import de.moritzruth.spigot_ttt.utils.NoOpTabCompleter
import de.moritzruth.spigot_ttt.plugin
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class AbortCommand: CommandExecutor {
    init {
        plugin.getCommand("abort")?.let {
            it.setExecutor(this)
            it.tabCompleter = NoOpTabCompleter()
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (GameManager.phase === null) {
            sender.sendMessage("${TTTPlugin.prefix}${ChatColor.RED}Zurzeit läuft kein Spiel.")
        } else {
            GameManager.abortGame(true)
        }

        return true
    }
}
