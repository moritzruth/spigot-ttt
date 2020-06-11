package de.moritzruth.spigot_ttt.game.items

import de.moritzruth.spigot_ttt.COMMAND_RESPONSE_PREFIX
import de.moritzruth.spigot_ttt.TTTPlugin
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.EmptyTabCompleter
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AddItemSpawnCommand: CommandExecutor {
    init {
        plugin.getCommand("additemspawn")?.let {
            it.setExecutor(this)
            it.tabCompleter = EmptyTabCompleter
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("$COMMAND_RESPONSE_PREFIX${ChatColor.RED}Dieser Befehl kann nur von Spielern verwendet werden.")
            return true
        }

        ItemSpawner.addItemSpawnLocation(sender.location)
        sender.sendMessage("$COMMAND_RESPONSE_PREFIX${TTTPlugin.prefix}${ChatColor.GREEN}Ein Waffenspawn wurde an deiner Position hinzugefügt.")

        return true
    }
}