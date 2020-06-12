package de.moritzruth.spigot_ttt

import de.moritzruth.spigot_ttt.utils.EmptyTabCompleter
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadTTTConfigCommand: CommandExecutor {
    init {
        val command = plugin.getCommand("reloadtttconfig")!!
        command.tabCompleter = EmptyTabCompleter
        command.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        plugin.reloadConfig()
        sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.GREEN}Die Konfiguration wurde neu geladen.")
        return true
    }
}
